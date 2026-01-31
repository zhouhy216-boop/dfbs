package com.dfbs.app.application.contractprice;

import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.workflow.QuoteWorkflowService;
import com.dfbs.app.config.ForceFlywayCleanConfig;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Import(ForceFlywayCleanConfig.class)
class PriceBookTest {

    private Long customerId;
    private Long financeUserId;

    @Autowired
    private ContractPriceService contractPriceService;
    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private QuoteItemService quoteItemService;
    @Autowired
    private QuoteWorkflowService quoteWorkflowService;
    @Autowired
    private QuoteRepo quoteRepo;
    @Autowired
    private QuoteItemRepo quoteItemRepo;
    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private com.dfbs.app.config.CurrentUserProvider currentUserProvider;

    @BeforeEach
    void setUp() {
        CustomerEntity customer = CustomerEntity.create("PB-TEST-" + System.currentTimeMillis(), "PriceBook Test Customer");
        customer = customerRepo.save(customer);
        customerId = customer.getId();

        UserEntity finance = new UserEntity();
        finance.setCanRequestPermission(false);
        finance.setAuthorities("[\"ROLE_USER\",\"ROLE_FINANCE\"]");
        finance.setAllowNormalNotification(true);
        finance.setCanManageStatements(true);
        finance = userRepo.save(finance);
        financeUserId = finance.getId();
        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(financeUserId));
    }

    /** Test 1: PLATFORM strategy = lowest price. Contract A (100, prio 10), B (50, prio 1) -> expect 50. */
    @Test
    void strategy_platform_lowestPriceWins() {
        createContract("A", new BigDecimal("100.00"), 10, QuoteExpenseType.PLATFORM);
        createContract("B", new BigDecimal("50.00"), 1, QuoteExpenseType.PLATFORM);

        PriceSuggestionDto dto = contractPriceService.calculateSuggestedPrice(customerId, LocalDate.now(), QuoteExpenseType.PLATFORM);
        assertThat(dto).isNotNull();
        assertThat(dto.getPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(dto.getSourceInfo()).contains("LOWEST_PRICE");
    }

    /** Test 2: Other (REPAIR) strategy = priority. Contract A (100, prio 10), B (50, prio 1) -> expect 100. */
    @Test
    void strategy_other_priorityWins() {
        createContract("A", new BigDecimal("100.00"), 10, QuoteExpenseType.REPAIR);
        createContract("B", new BigDecimal("50.00"), 1, QuoteExpenseType.REPAIR);

        PriceSuggestionDto dto = contractPriceService.calculateSuggestedPrice(customerId, LocalDate.now(), QuoteExpenseType.REPAIR);
        assertThat(dto).isNotNull();
        assertThat(dto.getPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(dto.getSourceInfo()).contains("PRIORITY");
    }

    /** Test 3: Lifecycle lock. Create quote -> add item (auto-price). Submit -> firstSubmissionTime set. Add new item -> no auto-suggest. */
    @Test
    void lifecycleLock_afterSubmit_noAutoSuggest() {
        createContract("C1", new BigDecimal("99.00"), 5, QuoteExpenseType.PLATFORM);

        var createCmd = new QuoteService.CreateQuoteCommand();
        createCmd.setSourceType(QuoteSourceType.MANUAL);
        createCmd.setCustomerId(customerId);
        QuoteEntity quote = quoteService.createDraft(createCmd, "test");
        Long quoteId = quote.getId();
        assertThat(quote.getFirstSubmissionTime()).isNull();

        var addCmd = new QuoteItemService.CreateItemCommand();
        addCmd.setExpenseType(QuoteExpenseType.PLATFORM);
        addCmd.setQuantity(1);
        addCmd.setDescription("Platform fee");
        QuoteItemEntity item1 = quoteItemService.addItem(quoteId, addCmd);
        assertThat(item1.getUnitPrice()).isEqualByComparingTo(new BigDecimal("99.00"));
        assertThat(item1.getPriceSourceInfo()).isNotBlank();

        quoteWorkflowService.submit(quoteId, financeUserId);
        QuoteEntity afterSubmit = quoteRepo.findById(quoteId).orElseThrow();
        assertThat(afterSubmit.getFirstSubmissionTime()).isNotNull();
        quoteWorkflowService.financeAudit(quoteId, "REJECT", null, financeUserId, "Back to edit");

        var addCmd2 = new QuoteItemService.CreateItemCommand();
        addCmd2.setExpenseType(QuoteExpenseType.REPAIR);
        addCmd2.setQuantity(1);
        addCmd2.setUnitPrice(BigDecimal.ZERO);
        addCmd2.setDescription("Repair after submit");
        QuoteItemEntity item2 = quoteItemService.addItem(quoteId, addCmd2);
        assertThat(item2.getUnitPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(item2.getPriceSourceInfo()).isNull();
    }

    private void createContract(String nameSuffix, BigDecimal unitPrice, int priority, QuoteExpenseType itemType) {
        var cmd = new ContractPriceService.CreateContractCommand();
        cmd.setContractName("Contract-" + nameSuffix);
        cmd.setCustomerId(customerId);
        cmd.setEffectiveDate(LocalDate.now());
        cmd.setExpirationDate(null);
        cmd.setPriority(priority);
        cmd.setItems(List.of(entry(itemType, unitPrice)));
        contractPriceService.create(cmd);
    }

    private static ContractPriceService.CreateContractCommand.ItemEntry entry(QuoteExpenseType type, BigDecimal price) {
        var e = new ContractPriceService.CreateContractCommand.ItemEntry();
        e.setItemType(type);
        e.setUnitPrice(price);
        e.setCurrency(Currency.CNY);
        return e;
    }
}
