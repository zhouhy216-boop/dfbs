package com.dfbs.app.application.correction;

import com.dfbs.app.application.freightbill.FreightBillService;
import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.dictionary.FeeDictionaryService;
import com.dfbs.app.modules.quote.dictionary.FeeCategoryEntity;
import com.dfbs.app.modules.quote.dictionary.FeeTypeEntity;
import com.dfbs.app.config.CurrentUserProvider;
import com.dfbs.app.modules.correction.CorrectionEntity;
import com.dfbs.app.modules.correction.CorrectionStatus;
import com.dfbs.app.modules.correction.CorrectionTargetType;
import com.dfbs.app.modules.freightbill.FreightBillEntity;
import com.dfbs.app.modules.freightbill.FreightBillRepo;
import com.dfbs.app.modules.freightbill.FreightBillStatus;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class CorrectionTest {

    @Autowired
    private CorrectionService correctionService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private QuoteItemService quoteItemService;
    @Autowired
    private QuoteRepo quoteRepo;
    @Autowired
    private FreightBillService freightBillService;
    @Autowired
    private FreightBillRepo freightBillRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    private FeeDictionaryService feeDictionaryService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    private Long userWithoutPermissionId;
    private Long userWithPermissionId;
    private Long customerId;

    @BeforeEach
    void setUp() {
        CustomerEntity customer = CustomerEntity.create("CORR-TEST-" + System.currentTimeMillis(), "Correction Test Customer");
        customer = customerRepo.save(customer);
        customerId = customer.getId();
        UserEntity noPerm = new UserEntity();
        noPerm.setCanRequestPermission(false);
        noPerm.setAuthorities("[\"ROLE_USER\"]");
        noPerm.setAllowNormalNotification(true);
        noPerm.setCanManageStatements(false);
        noPerm = userRepo.save(noPerm);
        userWithoutPermissionId = noPerm.getId();

        UserEntity withPerm = new UserEntity();
        withPerm.setCanRequestPermission(false);
        withPerm.setAuthorities("[\"ROLE_USER\",\"APPROVE_EXECUTE_CORRECTION\"]");
        withPerm.setAllowNormalNotification(true);
        withPerm.setCanManageStatements(false);
        withPerm = userRepo.save(withPerm);
        userWithPermissionId = withPerm.getId();
    }

    private QuoteEntity createConfirmedQuote() {
        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(userWithPermissionId));
        // Dictionary: expense type must be from dictionary for confirm
        FeeCategoryEntity category = feeDictionaryService.createCategory("CorrectionTest-Cat-" + System.currentTimeMillis());
        FeeTypeEntity feeType = feeDictionaryService.createFeeType("CorrectionTest-Fee", category.getId(), "次", "次", null);
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(customerId);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");
        var addCmd = new QuoteItemService.CreateItemCommand();
        addCmd.setExpenseType(QuoteExpenseType.PLATFORM);
        addCmd.setFeeTypeId(feeType.getId());
        addCmd.setQuantity(1);
        addCmd.setUnitPrice(BigDecimal.TEN);
        addCmd.setDescription("Test");
        quoteItemService.addItem(quote.getId(), addCmd);
        quoteService.confirm(quote.getId());
        return quoteRepo.findById(quote.getId()).orElseThrow();
    }

    /** Test 1 (Permissions): User without APPROVE_EXECUTE_CORRECTION -> SecurityException; with permission -> Success. */
    @Test
    void test1_permissions_withoutPermission_throwsForbidden() {
        QuoteEntity quote = createConfirmedQuote();
        CorrectionEntity c = correctionService.createDraft(
                new CorrectionService.CreateCorrectionDto(CorrectionTargetType.QUOTE, quote.getId(), "reason", null, LocalDate.now()));
        correctionService.submit(c.getId(), List.of("https://example.com/att.pdf"));

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(userWithoutPermissionId));
        assertThatThrownBy(() -> correctionService.approveAndExecute(c.getId()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("APPROVE_EXECUTE_CORRECTION");

        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(userWithPermissionId));
        CorrectionEntity executed = correctionService.approveAndExecute(c.getId());
        assertThat(executed.getStatus()).isEqualTo(CorrectionStatus.EXECUTED);
        assertThat(executed.getNewRecordId()).isNotNull();
    }

    /** Test 2 (Attachment Rule): Submit without attachment -> Fails; with attachment -> Success. */
    @Test
    void test2_attachment_submitWithoutAttachment_fails() {
        QuoteEntity quote = createConfirmedQuote();
        CorrectionEntity c = correctionService.createDraft(
                new CorrectionService.CreateCorrectionDto(CorrectionTargetType.QUOTE, quote.getId(), "reason", null, LocalDate.now()));

        assertThatThrownBy(() -> correctionService.submit(c.getId(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("attachment");

        correctionService.submit(c.getId(), List.of("https://example.com/doc.pdf"));
        assertThat(correctionService.get(c.getId()).getStatus()).isEqualTo(CorrectionStatus.SUBMITTED);
    }

    /** Test 3 (Flow - Quote): Correction submitted/executed -> Quote A CANCELLED, New Quote B exists, Correction links A -> B. */
    @Test
    void test3_flow_quote_correctionExecuted_quoteCancelled_newQuoteExists() {
        QuoteEntity quoteA = createConfirmedQuote();
        Long quoteAId = quoteA.getId();
        when(currentUserProvider.getCurrentUser()).thenReturn(String.valueOf(userWithPermissionId));

        CorrectionEntity corr = correctionService.createDraft(
                new CorrectionService.CreateCorrectionDto(CorrectionTargetType.QUOTE, quoteAId, "Data correction", null, LocalDate.now()));
        correctionService.submit(corr.getId(), List.of("https://example.com/corr.pdf"));
        CorrectionEntity executed = correctionService.approveAndExecute(corr.getId());

        assertThat(executed.getStatus()).isEqualTo(CorrectionStatus.EXECUTED);
        assertThat(executed.getNewRecordId()).isNotNull();
        QuoteEntity quoteAAfter = quoteRepo.findById(quoteAId).orElseThrow();
        assertThat(quoteAAfter.getStatus()).isEqualTo(QuoteStatus.CANCELLED);
        QuoteEntity quoteB = quoteRepo.findById(executed.getNewRecordId()).orElseThrow();
        assertThat(quoteB.getStatus()).isEqualTo(QuoteStatus.DRAFT);
        assertThat(quoteB.getParentQuoteId()).isEqualTo(quoteAId);
    }

    /** Test 4 (Flow - FreightBill): Verify FreightBill can be set to VOID. */
    @Test
    void test4_freightBill_voidStatus() {
        assertThat(FreightBillStatus.VOID).isNotNull();
        FreightBillEntity draft = new FreightBillEntity();
        draft.setBillNo("FB-VOID-" + System.currentTimeMillis());
        draft.setStatus(FreightBillStatus.DRAFT);
        draft.setCarrier("Test Carrier");
        draft.setCreatedTime(java.time.LocalDateTime.now());
        draft.setTotalAmount(java.math.BigDecimal.ZERO);
        draft = freightBillRepo.save(draft);
        FreightBillEntity voided = freightBillService.voidBill(draft.getId());
        assertThat(voided.getStatus()).isEqualTo(FreightBillStatus.VOID);
    }
}
