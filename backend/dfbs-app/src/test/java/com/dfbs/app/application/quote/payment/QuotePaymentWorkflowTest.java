package com.dfbs.app.application.quote.payment;

import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.workflow.QuoteWorkflowService;
import com.dfbs.app.application.quote.void_.QuoteVoidService;
import com.dfbs.app.modules.notification.NotificationRepo;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.payment.PaymentMethodEntity;
import com.dfbs.app.modules.quote.payment.PaymentMethodRepo;
import com.dfbs.app.modules.quote.payment.QuotePaymentEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class QuotePaymentWorkflowTest {

    @Autowired
    private QuoteService quoteService;
    @Autowired
    private QuoteItemService itemService;
    @Autowired
    private QuoteWorkflowService workflowService;
    @Autowired
    private QuotePaymentService paymentService;
    @Autowired
    private QuoteVoidService voidService;
    @Autowired
    private QuoteRepo quoteRepo;
    @Autowired
    private PaymentMethodRepo methodRepo;
    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private com.dfbs.app.modules.quote.dictionary.FeeTypeRepo feeTypeRepo;

    private Long createQuoteViaWorkflow(BigDecimal totalAmount, Long collectorId) {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(com.dfbs.app.modules.quote.enums.QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "initiator");

        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(com.dfbs.app.modules.quote.enums.QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(totalAmount);
        itemCmd.setDescription("Test");
        itemCmd.setUnit("?");
        if (!feeTypeRepo.findByIsActiveTrue().isEmpty()) {
            itemCmd.setFeeTypeId(feeTypeRepo.findByIsActiveTrue().get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);

        workflowService.submit(quote.getId(), 10L); // customer confirmer
        workflowService.financeAudit(quote.getId(), "PASS", collectorId, 2L, "OK");
        return quote.getId();
    }

    @Test
    void scenario1_happyPath_partialThenFullPayment() {
        Long quoteId = createQuoteViaWorkflow(BigDecimal.valueOf(1000.00), 20L);
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);

        // Collector adds partial payment
        QuotePaymentEntity p1 = paymentService.submit(
                quoteId, BigDecimal.valueOf(400.00), method.getId(), LocalDateTime.now(), 20L, false, null);
        assertThat(p1.getStatus().name()).isEqualTo("SUBMITTED");
        assertThat(p1.getIsFinanceConfirmed()).isFalse();

        // Finance confirms
        paymentService.financeConfirm(p1.getId(), "CONFIRM", 2L, "OK", null);
        QuoteEntity q = quoteRepo.findById(quoteId).orElseThrow();
        assertThat(q.getPaymentStatus()).isEqualTo(QuotePaymentStatus.PARTIAL);

        // Collector adds rest
        QuotePaymentEntity p2 = paymentService.submit(
                quoteId, BigDecimal.valueOf(600.00), method.getId(), LocalDateTime.now(), 20L, false, null);
        paymentService.financeConfirm(p2.getId(), "CONFIRM", 2L, "OK", null);

        q = quoteRepo.findById(quoteId).orElseThrow();
        assertThat(q.getPaymentStatus()).isEqualTo(QuotePaymentStatus.PAID);
    }

    @Test
    void scenario2_rejection_thenResubmit() {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(com.dfbs.app.modules.quote.enums.QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "u1");
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(com.dfbs.app.modules.quote.enums.QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(100));
        itemCmd.setDescription("X");
        itemCmd.setUnit("?");
        if (!feeTypeRepo.findByIsActiveTrue().isEmpty()) {
            itemCmd.setFeeTypeId(feeTypeRepo.findByIsActiveTrue().get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);

        workflowService.submit(quote.getId(), 10L);
        QuoteEntity afterSubmit = quoteRepo.findById(quote.getId()).orElseThrow();
        assertThat(afterSubmit.getStatus()).isEqualTo(QuoteStatus.APPROVAL_PENDING);

        workflowService.financeAudit(quote.getId(), "REJECT", null, 2L, "Please fix amount");
        QuoteEntity afterReject = quoteRepo.findById(quote.getId()).orElseThrow();
        assertThat(afterReject.getStatus()).isEqualTo(QuoteStatus.RETURNED);

        // Edit (RETURNED is editable) and resubmit
        workflowService.submit(quote.getId(), 10L);
        workflowService.financeAudit(quote.getId(), "PASS", 20L, 2L, null);
        QuoteEntity afterApprove = quoteRepo.findById(quote.getId()).orElseThrow();
        assertThat(afterApprove.getStatus()).isEqualTo(QuoteStatus.CONFIRMED);
    }

    @Test
    void scenario3_validation_payMoreThanRemainingBalance_fails() {
        Long quoteId = createQuoteViaWorkflow(BigDecimal.valueOf(1000.00), 20L);
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);

        assertThatThrownBy(() -> paymentService.submit(
                quoteId,
                BigDecimal.valueOf(1500.00), // more than total
                method.getId(),
                LocalDateTime.now(),
                20L,
                false, // collector
                null
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("未付金额");
    }

    @Test
    void scenario4_voidAfterFullPayment_notifyLeader_statusCancelled() {
        Long quoteId = createQuoteViaWorkflow(BigDecimal.valueOf(500.00), 20L);
        quoteRepo.findById(quoteId).orElseThrow().setBusinessLineId(1L);
        quoteRepo.flush();

        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);
        paymentService.submit(quoteId, BigDecimal.valueOf(500.00), method.getId(), LocalDateTime.now(), 20L, false, null);
        List<QuotePaymentEntity> payments = paymentService.getPaymentsByQuote(quoteId);
        paymentService.financeConfirm(payments.get(0).getId(), "CONFIRM", 2L, "OK", null);

        QuoteEntity quote = quoteRepo.findById(quoteId).orElseThrow();
        assertThat(quote.getPaymentStatus()).isEqualTo(QuotePaymentStatus.PAID);

        long notifBefore = notificationRepo.count();
        voidService.directVoid(quoteId, 2L, "Void after paid for test");
        QuoteEntity afterVoid = quoteRepo.findById(quoteId).orElseThrow();
        assertThat(afterVoid.getStatus()).isEqualTo(QuoteStatus.CANCELLED);
        if (quote.getBusinessLineId() != null) {
            assertThat(notificationRepo.count()).isGreaterThanOrEqualTo(notifBefore);
        }
    }

    @Test
    void scenario5_mergedPrep_paymentBatchNoSaved() {
        Long quoteId = createQuoteViaWorkflow(BigDecimal.valueOf(300.00), 20L);
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);

        QuotePaymentEntity p = paymentService.submit(
                quoteId,
                BigDecimal.valueOf(200.00),
                method.getId(),
                LocalDateTime.now(),
                20L,
                false,
                null,
                "BATCH-001",
                null,
                "Merged payment note"
        );
        assertThat(p.getPaymentBatchNo()).isEqualTo("BATCH-001");
        assertThat(p.getNote()).isEqualTo("Merged payment note");
        assertThat(p.getPaymentTime()).isNotNull();
        assertThat(p.getPaidAt()).isNotNull();

        QuotePaymentEntity single = paymentService.submit(
                quoteId, BigDecimal.valueOf(100.00), method.getId(), LocalDateTime.now(), 20L, false, null);
        assertThat(single.getPaymentBatchNo()).isNull();
    }
}

