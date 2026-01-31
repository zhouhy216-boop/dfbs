package com.dfbs.app.application.quote.void_;

import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.payment.QuotePaymentService;
import com.dfbs.app.application.quote.void_.QuoteVoidService;
import com.dfbs.app.application.quote.workflow.QuoteWorkflowService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.QuoteVoidStatus;
import com.dfbs.app.modules.quote.payment.PaymentMethodEntity;
import com.dfbs.app.modules.quote.payment.PaymentMethodRepo;
import com.dfbs.app.modules.quote.payment.QuotePaymentEntity;
import com.dfbs.app.modules.quote.void_.QuoteVoidApplicationEntity;
import com.dfbs.app.modules.quote.void_.QuoteVoidRequestEntity;
import com.dfbs.app.modules.quote.void_.VoidRequesterRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class QuoteVoidTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private QuoteItemRepo itemRepo;

    @Autowired
    private QuoteVoidService voidService;

    @Autowired
    private QuotePaymentService paymentService;

    @Autowired
    private PaymentMethodRepo methodRepo;

    @Autowired
    private com.dfbs.app.modules.quote.dictionary.FeeTypeRepo feeTypeRepo;

    @Autowired
    private QuoteRepo quoteRepo;

    @Autowired
    private QuoteWorkflowService workflowService;

    private Long createConfirmedQuoteWithCollector(Long collectorId) {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");
        
        // Set collector via repo (for test setup)
        quote.setCollectorId(collectorId);
        quoteRepo.save(quote);
        quote = quoteService.findById(quote.getId()).orElseThrow();

        // Add item with valid FeeType
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(1000.00));
        itemCmd.setDescription("Test Item");
        itemCmd.setUnit("次");
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);

        quoteService.confirm(quote.getId());
        return quote.getId();
    }

    @Test
    void scenario1_nonCollector_cannotApply() {
        Long quoteId = createConfirmedQuoteWithCollector(100L);

        // Try to apply with different user (not collector)
        assertThatThrownBy(() -> voidService.apply(quoteId, 999L, "Test reason", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("只有收款人可以申请作废报价单");
    }

    @Test
    void scenario2_applySuccess_voidStatusApplying_blockPaymentAndEdit() {
        Long collectorId = 100L;
        Long quoteId = createConfirmedQuoteWithCollector(collectorId);

        // Apply for void
        QuoteVoidApplicationEntity application = voidService.apply(quoteId, collectorId, "申请作废", null);

        assertThat(application).isNotNull();
        assertThat(application.getApplicantId()).isEqualTo(collectorId);

        // Verify quote.voidStatus is APPLYING
        QuoteEntity quote = quoteService.findById(quoteId).orElseThrow();
        assertThat(quote.getVoidStatus()).isEqualTo(QuoteVoidStatus.APPLYING);

        // Try to submit payment - should be blocked
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);
        assertThatThrownBy(() -> paymentService.submit(
                quoteId,
                BigDecimal.valueOf(500.00),
                method.getId(),
                LocalDateTime.now(),
                1L,
                false,
                null
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("作废申请审批中，操作已冻结");

        // Try to update quote - should be blocked
        var updateCmd = new QuoteService.UpdateQuoteCommand();
        updateCmd.setCurrency(com.dfbs.app.modules.quote.enums.Currency.USD);
        assertThatThrownBy(() -> quoteService.updateHeader(quoteId, updateCmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("作废申请审批中，操作已冻结");
    }

    @Test
    void scenario3_financeAuditPass_statusCancelled_blockAllActions() {
        Long collectorId = 100L;
        Long quoteId = createConfirmedQuoteWithCollector(collectorId);

        // Apply for void
        QuoteVoidApplicationEntity application = voidService.apply(quoteId, collectorId, "申请作废", null);

        // Finance audit PASS
        QuoteVoidApplicationEntity audited = voidService.audit(application.getId(), 2L, "PASS", "同意作废");

        assertThat(audited.getAuditResult()).isEqualTo("PASS");
        assertThat(audited.getAuditorId()).isEqualTo(2L);

        // Verify quote.status becomes CANCELLED
        QuoteEntity quote = quoteService.findById(quoteId).orElseThrow();
        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.CANCELLED);
        assertThat(quote.getVoidStatus()).isEqualTo(QuoteVoidStatus.VOIDED);

        // Try to submit payment - should be blocked (already CANCELLED)
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);
        assertThatThrownBy(() -> paymentService.submit(
                quoteId,
                BigDecimal.valueOf(500.00),
                method.getId(),
                LocalDateTime.now(),
                1L,
                false,
                null
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("只能为已确认的报价单提交付款记录");
    }

    @Test
    void scenario4_financeAuditReject_voidStatusRejected_actionsAllowedAgain() {
        Long collectorId = 100L;
        Long quoteId = createConfirmedQuoteWithCollector(collectorId);

        // Apply for void
        QuoteVoidApplicationEntity application = voidService.apply(quoteId, collectorId, "申请作废", null);

        // Finance audit REJECT
        QuoteVoidApplicationEntity audited = voidService.audit(application.getId(), 2L, "REJECT", "不同意作废");

        assertThat(audited.getAuditResult()).isEqualTo("REJECT");

        // Verify quote.voidStatus becomes REJECTED
        QuoteEntity quote = quoteService.findById(quoteId).orElseThrow();
        assertThat(quote.getVoidStatus()).isEqualTo(QuoteVoidStatus.REJECTED);
        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.CONFIRMED);  // Status unchanged

        // Actions should be allowed again (no freeze)
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);
        QuotePaymentEntity payment = paymentService.submit(
                quoteId,
                BigDecimal.valueOf(500.00),
                method.getId(),
                LocalDateTime.now(),
                1L,
                false,
                null
        );
        assertThat(payment).isNotNull();
    }

    @Test
    void scenario5_financeDirectVoid_statusCancelled() {
        Long quoteId = createConfirmedQuoteWithCollector(100L);

        // Finance direct void
        QuoteVoidApplicationEntity application = voidService.directVoid(quoteId, 2L, "财务直接作废");

        assertThat(application).isNotNull();
        assertThat(application.getAuditResult()).isEqualTo("PASS");
        assertThat(application.getApplicantId()).isEqualTo(2L);
        assertThat(application.getAuditorId()).isEqualTo(2L);

        // Verify quote.status becomes CANCELLED
        QuoteEntity quote = quoteService.findById(quoteId).orElseThrow();
        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.CANCELLED);
        assertThat(quote.getVoidStatus()).isEqualTo(QuoteVoidStatus.VOIDED);
    }

    @Test
    void scenario6_workOrderStatusLogic_quotedVsUnquoted() {
        // Create a work order quote
        var req = new com.dfbs.app.application.quote.dto.WorkOrderImportRequest(
                "WO-TEST-001",
                1L,
                "Test Customer",
                "Recipient",
                "13800000000",
                "Test Address",
                "Machine Model TEST",
                false,
                null,
                100L,
                null
        );

        QuoteEntity quote1 = quoteService.createFromWorkOrder(req);
        quoteService.confirm(quote1.getId());

        // Check: 1 active quote (status != CANCELLED) = Quoted
        // This would be checked in WorkOrder service logic
        // For this test, we verify the quote exists and is CONFIRMED
        assertThat(quote1.getStatus()).isEqualTo(QuoteStatus.CONFIRMED);
        assertThat(quote1.getSourceId()).isEqualTo("WO-TEST-001");

        // Void the quote
        voidService.directVoid(quote1.getId(), 2L, "Test void");

        // Verify quote is CANCELLED
        QuoteEntity cancelled = quoteService.findById(quote1.getId()).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(QuoteStatus.CANCELLED);

        // Now: 1 Cancelled quote = Unquoted (for work order status logic)
        // The work order service would check: count(quotes where sourceId=woId AND status!=CANCELLED) == 0
    }

    // --- New void flow (applyVoid / auditVoid / directVoidByRole) ---

    @Test
    void newFlow_test1_preFinance_initiatorDirectVoid_and_confirmerApply_then_initiatorApprove() {
        Long assigneeId = 10L;
        Long confirmerId = 20L;
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");
        quote.setAssigneeId(assigneeId);
        quoteRepo.save(quote);
        addItem(quote.getId());
        Long quoteId = quote.getId();

        // Initiator direct void -> CANCELLED
        Optional<QuoteVoidRequestEntity> req = voidService.applyVoid(quoteId, "Initiator direct void", assigneeId, VoidRequesterRole.INITIATOR);
        assertThat(req).isEmpty();
        QuoteEntity after = quoteService.findById(quoteId).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(QuoteStatus.CANCELLED);

        // New quote: CustomerConfirmer apply -> VOID_AUDIT_INITIATOR
        QuoteEntity quote2 = quoteService.createDraft(cmd, "test-user");
        quote2.setAssigneeId(assigneeId);
        quote2.setCustomerConfirmerId(confirmerId);
        quoteRepo.save(quote2);
        addItem(quote2.getId());
        workflowService.submit(quote2.getId(), confirmerId);
        Long quoteId2 = quote2.getId();

        Optional<QuoteVoidRequestEntity> req2 = voidService.applyVoid(quoteId2, "Confirmer apply", confirmerId, VoidRequesterRole.CUSTOMER_CONFIRMER);
        assertThat(req2).isPresent();
        QuoteEntity q2 = quoteService.findById(quoteId2).orElseThrow();
        assertThat(q2.getStatus()).isEqualTo(QuoteStatus.VOID_AUDIT_INITIATOR);

        // Initiator approve -> CANCELLED
        voidService.auditVoid(req2.get().getId(), "PASS", assigneeId, "Approve");
        QuoteEntity q2After = quoteService.findById(quoteId2).orElseThrow();
        assertThat(q2After.getStatus()).isEqualTo(QuoteStatus.CANCELLED);
    }

    @Test
    void newFlow_test2_postFinanceNormal_collectorApply_financeApprove_and_financeDirectVoid() {
        Long collectorId = 100L;
        Long financeId = 2L;
        Long quoteId = createConfirmedQuoteWithCollector(collectorId);

        // Collector apply -> VOID_AUDIT_FINANCE
        Optional<QuoteVoidRequestEntity> req = voidService.applyVoid(quoteId, "Collector apply", collectorId, VoidRequesterRole.COLLECTOR);
        assertThat(req).isPresent();
        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.VOID_AUDIT_FINANCE);

        // Finance approve -> CANCELLED
        voidService.auditVoid(req.get().getId(), "PASS", financeId, "OK");
        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.CANCELLED);

        // New quote: Finance direct void
        Long quoteId2 = createConfirmedQuoteWithCollector(collectorId);
        voidService.directVoidByRole(quoteId2, financeId, "Finance direct void", VoidRequesterRole.FINANCE);
        assertThat(quoteService.findById(quoteId2).orElseThrow().getStatus()).isEqualTo(QuoteStatus.CANCELLED);
    }

    @Test
    void newFlow_test3_strictControl_paid_collectorApply_financeApprove_leaderApprove_cascade() {
        Long collectorId = 100L;
        Long financeId = 2L;
        Long quoteId = createConfirmedQuoteWithCollector(collectorId);

        // Pay fully: submit then finance confirm
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);
        List<QuoteItemEntity> items = itemRepo.findByQuoteIdOrderByLineOrderAsc(quoteId);
        BigDecimal total = items.stream().map(QuoteItemEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        QuotePaymentEntity pay = paymentService.submit(quoteId, total, method.getId(), LocalDateTime.now(), collectorId, false, null);
        paymentService.financeConfirm(pay.getId(), "CONFIRM", financeId, null, null);
        assertThat(quoteService.findById(quoteId).orElseThrow().getPaymentStatus()).isEqualTo(QuotePaymentStatus.PAID);

        // Collector apply -> VOID_AUDIT_FINANCE
        Optional<QuoteVoidRequestEntity> req = voidService.applyVoid(quoteId, "Strict paid void", collectorId, VoidRequesterRole.COLLECTOR);
        assertThat(req).isPresent();
        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.VOID_AUDIT_FINANCE);

        // Finance approve -> VOID_AUDIT_LEADER
        voidService.auditVoid(req.get().getId(), "PASS", financeId, "OK");
        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.VOID_AUDIT_LEADER);

        // Leader approve -> CANCELLED (executeVoid: cancelUnconfirmed, cancelPending)
        voidService.auditVoid(req.get().getId(), "PASS", 3L, "Leader OK");
        QuoteEntity after = quoteService.findById(quoteId).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(QuoteStatus.CANCELLED);
        List<QuotePaymentEntity> payments = paymentService.getPaymentsByQuote(quoteId);
        assertThat(payments).isNotEmpty();
    }

    @Test
    void newFlow_test4_strictControl_invoice_sameFlow() {
        Long collectorId = 100L;
        Long financeId = 2L;
        Long quoteId = createConfirmedQuoteWithCollector(collectorId);

        // Trigger strict by invoice status (has invoice application)
        QuoteEntity quote = quoteRepo.findById(quoteId).orElseThrow();
        quote.setInvoiceStatus(com.dfbs.app.modules.quote.enums.QuoteInvoiceStatus.IN_PROCESS);
        quoteRepo.save(quote);
        assertThat(voidService.isStrictControlScenario(quote)).isTrue();

        Optional<QuoteVoidRequestEntity> req = voidService.applyVoid(quoteId, "Strict invoice void", collectorId, VoidRequesterRole.COLLECTOR);
        assertThat(req).isPresent();
        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.VOID_AUDIT_FINANCE);

        voidService.auditVoid(req.get().getId(), "PASS", financeId, null);
        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.VOID_AUDIT_LEADER);

        voidService.auditVoid(req.get().getId(), "PASS", 3L, null);
        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.CANCELLED);
    }

    @Test
    void newFlow_test5_fallback_submit_fallbackToDraft_resubmit() {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");
        addItem(quote.getId());
        Long quoteId = quote.getId();
        Long confirmerId = 20L;

        workflowService.submit(quoteId, confirmerId);
        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.APPROVAL_PENDING);

        workflowService.fallback(quoteId, 2L, "Need to fix details");
        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.DRAFT);

        workflowService.submit(quoteId, confirmerId);
        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.APPROVAL_PENDING);
    }

    private void addItem(Long quoteId) {
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(1000.00));
        itemCmd.setDescription("Test Item");
        itemCmd.setUnit("次");
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quoteId, itemCmd);
    }
}
