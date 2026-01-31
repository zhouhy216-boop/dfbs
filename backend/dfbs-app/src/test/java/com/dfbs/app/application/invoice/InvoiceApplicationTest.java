package com.dfbs.app.application.invoice;

import com.dfbs.app.application.invoice.dto.InvoiceApplicationCreateRequest;
import com.dfbs.app.application.invoice.dto.InvoiceGroupRequest;
import com.dfbs.app.application.invoice.dto.QuoteItemSelection;
import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.workflow.QuoteWorkflowService;
import com.dfbs.app.modules.invoice.InvoiceApplicationEntity;
import com.dfbs.app.modules.invoice.InvoiceApplicationRepo;
import com.dfbs.app.modules.invoice.InvoiceApplicationStatus;
import com.dfbs.app.modules.invoice.InvoiceItemRefEntity;
import com.dfbs.app.modules.invoice.InvoiceItemRefRepo;
import com.dfbs.app.modules.invoice.InvoiceRecordEntity;
import com.dfbs.app.modules.invoice.InvoiceRecordRepo;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.QuoteInvoiceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class InvoiceApplicationTest {

    @Autowired
    private QuoteService quoteService;
    @Autowired
    private QuoteItemService itemService;
    @Autowired
    private QuoteWorkflowService workflowService;
    @Autowired
    private InvoiceApplicationService invoiceApplicationService;
    @Autowired
    private QuoteRepo quoteRepo;
    @Autowired
    private QuoteItemRepo quoteItemRepo;
    @Autowired
    private InvoiceApplicationRepo applicationRepo;
    @Autowired
    private InvoiceRecordRepo recordRepo;
    @Autowired
    private InvoiceItemRefRepo itemRefRepo;

    @Autowired
    private com.dfbs.app.modules.quote.dictionary.FeeTypeRepo feeTypeRepo;

    private Long createConfirmedQuote(BigDecimal amount, Long customerId, Long collectorId) {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(com.dfbs.app.modules.quote.enums.QuoteSourceType.MANUAL);
        cmd.setCustomerId(customerId);
        QuoteEntity quote = quoteService.createDraft(cmd, "u1");
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(com.dfbs.app.modules.quote.enums.QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(amount);
        itemCmd.setDescription("Test");
        itemCmd.setUnit("?");
        if (!feeTypeRepo.findByIsActiveTrue().isEmpty()) {
            itemCmd.setFeeTypeId(feeTypeRepo.findByIsActiveTrue().get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);
        workflowService.submit(quote.getId(), 10L);
        workflowService.financeAudit(quote.getId(), "PASS", collectorId, 2L, "OK");
        return quote.getId();
    }

    private Long getFirstQuoteItemId(Long quoteId) {
        return quoteItemRepo.findByQuoteIdOrderByLineOrderAsc(quoteId).get(0).getId();
    }

    @Test
    void test1_happyPath_singleQuote_fullAmount_approve() {
        Long collectorId = 20L;
        Long quoteId = createConfirmedQuote(BigDecimal.valueOf(500), 1L, collectorId);
        Long quoteItemId = getFirstQuoteItemId(quoteId);

        InvoiceApplicationCreateRequest req = new InvoiceApplicationCreateRequest();
        QuoteItemSelection sel = new QuoteItemSelection();
        sel.setQuoteItemId(quoteItemId);
        sel.setAmount(BigDecimal.valueOf(500));
        InvoiceGroupRequest group = new InvoiceGroupRequest();
        group.setItems(List.of(sel));
        group.setContent("Software Service");
        req.setGroups(List.of(group));

        InvoiceApplicationEntity app = invoiceApplicationService.create(req, collectorId);
        assertThat(app.getStatus()).isEqualTo(InvoiceApplicationStatus.PENDING);
        assertThat(app.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));

        invoiceApplicationService.audit(app.getId(), "APPROVE", 2L, null);

        QuoteEntity quote = quoteRepo.findById(quoteId).orElseThrow();
        assertThat(quote.getInvoiceStatus()).isEqualTo(QuoteInvoiceStatus.FULLY_INVOICED);
        assertThat(quote.getInvoicedAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void test2_mergeAndSplit_twoQuotes_twoGroups() {
        Long collectorId = 20L;
        Long quoteA = createConfirmedQuote(BigDecimal.valueOf(100), 1L, collectorId);
        Long quoteB = createConfirmedQuote(BigDecimal.valueOf(50), 1L, collectorId);
        Long itemA = getFirstQuoteItemId(quoteA);
        Long itemB = getFirstQuoteItemId(quoteB);

        InvoiceApplicationCreateRequest req = new InvoiceApplicationCreateRequest();
        QuoteItemSelection selA = new QuoteItemSelection();
        selA.setQuoteItemId(itemA);
        selA.setAmount(BigDecimal.valueOf(100));
        InvoiceGroupRequest groupService = new InvoiceGroupRequest();
        groupService.setItems(List.of(selA));
        groupService.setContent("Service Items");

        QuoteItemSelection selB = new QuoteItemSelection();
        selB.setQuoteItemId(itemB);
        selB.setAmount(BigDecimal.valueOf(50));
        InvoiceGroupRequest groupHardware = new InvoiceGroupRequest();
        groupHardware.setItems(List.of(selB));
        groupHardware.setContent("Hardware Items");

        req.setGroups(List.of(groupService, groupHardware));

        InvoiceApplicationEntity app = invoiceApplicationService.create(req, collectorId);
        assertThat(app.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(recordRepo.findByApplicationId(app.getId())).hasSize(2);
        List<InvoiceRecordEntity> records = recordRepo.findByApplicationId(app.getId());
        assertThat(records.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(records.get(1).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50));
        List<InvoiceItemRefEntity> refs = itemRefRepo.findByQuoteId(quoteA);
        assertThat(refs).hasSize(1);
        assertThat(refs.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void test3_validation_differentCustomers() {
        Long collectorId = 20L;
        Long quoteA = createConfirmedQuote(BigDecimal.valueOf(100), 1L, collectorId);
        Long quoteB = createConfirmedQuote(BigDecimal.valueOf(50), 2L, collectorId);
        Long itemA = getFirstQuoteItemId(quoteA);
        Long itemB = getFirstQuoteItemId(quoteB);

        InvoiceApplicationCreateRequest req = new InvoiceApplicationCreateRequest();
        InvoiceGroupRequest group = new InvoiceGroupRequest();
        QuoteItemSelection selA = new QuoteItemSelection();
        selA.setQuoteItemId(itemA);
        selA.setAmount(BigDecimal.valueOf(100));
        QuoteItemSelection selB = new QuoteItemSelection();
        selB.setQuoteItemId(itemB);
        selB.setAmount(BigDecimal.valueOf(50));
        group.setItems(List.of(selA, selB));
        req.setGroups(List.of(group));

        assertThatThrownBy(() -> invoiceApplicationService.create(req, collectorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("客户不一致");
    }

    @Test
    void test3_validation_amountExceedsUninvoiced() {
        Long collectorId = 20L;
        Long quoteId = createConfirmedQuote(BigDecimal.valueOf(100), 1L, collectorId);
        Long quoteItemId = getFirstQuoteItemId(quoteId);

        InvoiceApplicationCreateRequest req = new InvoiceApplicationCreateRequest();
        QuoteItemSelection sel = new QuoteItemSelection();
        sel.setQuoteItemId(quoteItemId);
        sel.setAmount(BigDecimal.valueOf(150)); // exceeds quote total 100
        InvoiceGroupRequest group = new InvoiceGroupRequest();
        group.setItems(List.of(sel));
        req.setGroups(List.of(group));

        assertThatThrownBy(() -> invoiceApplicationService.create(req, collectorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能超过");
    }

    @Test
    void test4_reject_quoteStatusReverts() {
        Long collectorId = 20L;
        Long quoteId = createConfirmedQuote(BigDecimal.valueOf(200), 1L, collectorId);
        Long quoteItemId = getFirstQuoteItemId(quoteId);

        InvoiceApplicationCreateRequest req = new InvoiceApplicationCreateRequest();
        QuoteItemSelection sel = new QuoteItemSelection();
        sel.setQuoteItemId(quoteItemId);
        sel.setAmount(BigDecimal.valueOf(200));
        InvoiceGroupRequest group = new InvoiceGroupRequest();
        group.setItems(List.of(sel));
        req.setGroups(List.of(group));

        InvoiceApplicationEntity app = invoiceApplicationService.create(req, collectorId);
        QuoteEntity quoteAfterCreate = quoteRepo.findById(quoteId).orElseThrow();
        assertThat(quoteAfterCreate.getInvoiceStatus()).isEqualTo(QuoteInvoiceStatus.IN_PROCESS);

        invoiceApplicationService.audit(app.getId(), "REJECT", 2L, "Need more info");

        QuoteEntity quoteAfterReject = quoteRepo.findById(quoteId).orElseThrow();
        assertThat(quoteAfterReject.getInvoiceStatus()).isEqualTo(QuoteInvoiceStatus.UNINVOICED);
        assertThat(quoteAfterReject.getInvoicedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        InvoiceApplicationEntity appReloaded = applicationRepo.findById(app.getId()).orElseThrow();
        assertThat(appReloaded.getStatus()).isEqualTo(InvoiceApplicationStatus.REJECTED);
    }
}
