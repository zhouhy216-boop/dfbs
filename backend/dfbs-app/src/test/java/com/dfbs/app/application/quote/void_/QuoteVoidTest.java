package com.dfbs.app.application.quote.void_;

import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.payment.QuotePaymentService;
import com.dfbs.app.application.quote.void_.QuoteVoidService;
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
}
