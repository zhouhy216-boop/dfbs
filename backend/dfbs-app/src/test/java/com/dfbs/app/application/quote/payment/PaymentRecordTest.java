package com.dfbs.app.application.quote.payment;

import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.payment.QuotePaymentService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.PaymentStatus;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.payment.PaymentMethodEntity;
import com.dfbs.app.modules.quote.payment.PaymentMethodRepo;
import com.dfbs.app.modules.quote.payment.QuoteCollectorHistoryEntity;
import com.dfbs.app.modules.quote.payment.QuoteCollectorHistoryRepo;
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
class PaymentRecordTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private QuoteItemRepo itemRepo;

    @Autowired
    private QuotePaymentService paymentService;

    @Autowired
    private PaymentMethodRepo methodRepo;

    @Autowired
    private QuoteCollectorHistoryRepo collectorHistoryRepo;

    @Autowired
    private QuoteRepo quoteRepo;

    @Autowired
    private com.dfbs.app.modules.quote.dictionary.FeeTypeRepo feeTypeRepo;

    private Long createConfirmedQuoteWithAmount(BigDecimal totalAmount) {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        // Add item to reach totalAmount
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(com.dfbs.app.modules.quote.enums.QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(totalAmount);
        itemCmd.setDescription("Test Item");
        itemCmd.setUnit("?");
        // Get a valid FeeType for confirmation
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);

        quoteService.confirm(quote.getId());
        return quote.getId();
    }

    @Test
    void scenario1_submitBlocked_ifQuoteNotConfirmed() {
        // Create DRAFT quote
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);

        // Try to submit payment for DRAFT quote - should fail
        assertThatThrownBy(() -> paymentService.submit(
                quote.getId(),
                BigDecimal.valueOf(100.00),
                method.getId(),
                LocalDateTime.now(),
                1L,
                false,
                null
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("只能为已确认的报价单提交付款记录");
    }

    @Test
    void scenario2_submitValidPayment_statusSubmitted() {
        Long quoteId = createConfirmedQuoteWithAmount(BigDecimal.valueOf(1000.00));
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);

        QuotePaymentEntity payment = paymentService.submit(
                quoteId,
                BigDecimal.valueOf(500.00),
                method.getId(),
                LocalDateTime.now(),
                1L,
                false,  // Not finance
                null
        );

        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUBMITTED);
        assertThat(payment.getQuoteId()).isEqualTo(quoteId);
        assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
    }

    @Test
    void scenario3_financeConfirm_statusConfirmed_quoteStatusUpdated() {
        Long quoteId = createConfirmedQuoteWithAmount(BigDecimal.valueOf(1000.00));
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);

        // Submit payment
        QuotePaymentEntity payment = paymentService.submit(
                quoteId,
                BigDecimal.valueOf(500.00),
                method.getId(),
                LocalDateTime.now(),
                1L,
                false,
                null
        );

        // Finance confirm
        QuotePaymentEntity confirmed = paymentService.financeConfirm(
                payment.getId(),
                "CONFIRM",
                2L,  // Finance user
                "确认付款",
                null
        );

        assertThat(confirmed.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(confirmed.getConfirmerId()).isEqualTo(2L);

        // Verify quote payment status updated to PARTIAL
        QuoteEntity quote = quoteService.findById(quoteId).orElseThrow();
        assertThat(quote.getPaymentStatus()).isEqualTo(QuotePaymentStatus.PARTIAL);
    }

    @Test
    void scenario4_overpayment_createBalanceQuote() {
        Long quoteId = createConfirmedQuoteWithAmount(BigDecimal.valueOf(1000.00));
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);

        // Finance submits overpayment (collector cannot submit more than unpaid amount)
        QuotePaymentEntity payment = paymentService.submit(
                quoteId,
                BigDecimal.valueOf(1200.00),  // Overpayment
                method.getId(),
                LocalDateTime.now(),
                2L,   // Finance user
                true, // isFinance -> auto-confirm and create balance
                null
        );

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(payment.getRemark()).contains("余额");

        // Verify quote payment status is PAID (fully paid)
        QuoteEntity quote = quoteService.findById(quoteId).orElseThrow();
        assertThat(quote.getPaymentStatus()).isEqualTo(QuotePaymentStatus.PAID);
    }

    @Test
    void scenario5_fullPayment_changeCollectorFails() {
        Long quoteId = createConfirmedQuoteWithAmount(BigDecimal.valueOf(1000.00));
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);

        // Submit and confirm full payment
        QuotePaymentEntity payment = paymentService.submit(
                quoteId,
                BigDecimal.valueOf(1000.00),
                method.getId(),
                LocalDateTime.now(),
                1L,
                true,  // Finance auto-confirms
                null
        );

        // Verify quote is PAID
        QuoteEntity quote = quoteService.findById(quoteId).orElseThrow();
        assertThat(quote.getPaymentStatus()).isEqualTo(QuotePaymentStatus.PAID);

        // Try to change collector - should fail
        assertThatThrownBy(() -> quoteService.changeCollector(quoteId, 999L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("报价单已付清，不能更改收款人");
    }

    @Test
    void scenario6_changeCollector_verifyHistoryLog() {
        Long quoteId = createConfirmedQuoteWithAmount(BigDecimal.valueOf(1000.00));

        // Set initial collector
        QuoteEntity quote = quoteService.findById(quoteId).orElseThrow();
        quote.setCollectorId(100L);
        quoteRepo.save(quote);

        // Change collector
        QuoteEntity updated = quoteService.changeCollector(quoteId, 200L, 1L);

        assertThat(updated.getCollectorId()).isEqualTo(200L);

        // Verify history log
        List<QuoteCollectorHistoryEntity> history = collectorHistoryRepo.findByQuoteIdOrderByChangedAtDesc(quoteId);
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getFromUserId()).isEqualTo(100L);
        assertThat(history.get(0).getToUserId()).isEqualTo(200L);
        assertThat(history.get(0).getChangedBy()).isEqualTo(1L);
    }
}

