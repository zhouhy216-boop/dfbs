package com.dfbs.app.application.quote.payment;

import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.dto.BatchPaymentRequest;
import com.dfbs.app.application.quote.workflow.QuoteWorkflowService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
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
class QuoteBatchPaymentTest {

    @Autowired
    private QuoteService quoteService;
    @Autowired
    private QuoteItemService itemService;
    @Autowired
    private QuoteWorkflowService workflowService;
    @Autowired
    private QuotePaymentService paymentService;
    @Autowired
    private QuoteRepo quoteRepo;
    @Autowired
    private PaymentMethodRepo methodRepo;

    @Autowired
    private com.dfbs.app.modules.quote.dictionary.FeeTypeRepo feeTypeRepo;

    private Long createQuoteViaWorkflow(BigDecimal totalAmount, Long customerId, Long collectorId) {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(com.dfbs.app.modules.quote.enums.QuoteSourceType.MANUAL);
        cmd.setCustomerId(customerId);
        QuoteEntity quote = quoteService.createDraft(cmd, "u1");
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
        workflowService.submit(quote.getId(), 10L);
        workflowService.financeAudit(quote.getId(), "PASS", collectorId, 2L, "OK");
        return quote.getId();
    }

    @Test
    void test1_happyPath_twoQuotes_sameBatchNo_andAmounts() {
        Long collectorId = 20L;
        Long quoteA = createQuoteViaWorkflow(BigDecimal.valueOf(100), 1L, collectorId);
        Long quoteB = createQuoteViaWorkflow(BigDecimal.valueOf(50), 1L, collectorId);

        BatchPaymentRequest req = new BatchPaymentRequest();
        req.setQuoteIds(List.of(quoteA, quoteB));
        req.setTotalPaymentAmount(BigDecimal.valueOf(150));
        req.setPaymentTime(LocalDateTime.now());
        req.setPaymentMethodId(methodRepo.findByIsActiveTrue().get(0).getId());
        req.setNote("Batch test");

        List<QuotePaymentEntity> created = paymentService.createBatchPayment(req, collectorId);

        assertThat(created).hasSize(2);
        String batchNo = created.get(0).getPaymentBatchNo();
        assertThat(batchNo).isNotNull();
        assertThat(created.get(1).getPaymentBatchNo()).isEqualTo(batchNo);
        assertThat(created.stream().map(QuotePaymentEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(created.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(created.get(1).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50));
        created.forEach(p -> assertThat(p.getIsFinanceConfirmed()).isFalse());
    }

    @Test
    void test2_validation_customerMismatch() {
        Long collectorId = 20L;
        Long quoteA = createQuoteViaWorkflow(BigDecimal.valueOf(100), 1L, collectorId);
        Long quoteB = createQuoteViaWorkflow(BigDecimal.valueOf(50), 2L, collectorId);

        BatchPaymentRequest req = new BatchPaymentRequest();
        req.setQuoteIds(List.of(quoteA, quoteB));
        req.setTotalPaymentAmount(BigDecimal.valueOf(150));
        req.setPaymentTime(LocalDateTime.now());
        req.setPaymentMethodId(methodRepo.findByIsActiveTrue().get(0).getId());

        assertThatThrownBy(() -> paymentService.createBatchPayment(req, collectorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("客户不一致");
    }

    @Test
    void test2_validation_currencyMismatch() {
        Long collectorId = 20L;
        Long quoteA = createQuoteViaWorkflow(BigDecimal.valueOf(100), 1L, collectorId);
        Long quoteB = createQuoteViaWorkflow(BigDecimal.valueOf(50), 1L, collectorId);
        QuoteEntity qB = quoteRepo.findById(quoteB).orElseThrow();
        qB.setCurrency(Currency.USD);
        quoteRepo.save(qB);

        BatchPaymentRequest req = new BatchPaymentRequest();
        req.setQuoteIds(List.of(quoteA, quoteB));
        req.setTotalPaymentAmount(BigDecimal.valueOf(150));
        req.setPaymentTime(LocalDateTime.now());
        req.setPaymentMethodId(methodRepo.findByIsActiveTrue().get(0).getId());

        assertThatThrownBy(() -> paymentService.createBatchPayment(req, collectorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("币种不一致");
    }

    @Test
    void test2_validation_collectorMismatch() {
        Long quoteA = createQuoteViaWorkflow(BigDecimal.valueOf(100), 1L, 20L);
        Long quoteB = createQuoteViaWorkflow(BigDecimal.valueOf(50), 1L, 30L);

        BatchPaymentRequest req = new BatchPaymentRequest();
        req.setQuoteIds(List.of(quoteA, quoteB));
        req.setTotalPaymentAmount(BigDecimal.valueOf(150));
        req.setPaymentTime(LocalDateTime.now());
        req.setPaymentMethodId(methodRepo.findByIsActiveTrue().get(0).getId());

        assertThatThrownBy(() -> paymentService.createBatchPayment(req, 20L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("收款执行人");
    }

    @Test
    void test2_validation_alreadyPaid() {
        Long collectorId = 20L;
        Long quoteA = createQuoteViaWorkflow(BigDecimal.valueOf(100), 1L, collectorId);
        Long quoteB = createQuoteViaWorkflow(BigDecimal.valueOf(50), 1L, collectorId);
        PaymentMethodEntity method = methodRepo.findByIsActiveTrue().get(0);
        paymentService.submit(quoteA, BigDecimal.valueOf(100), method.getId(), LocalDateTime.now(), collectorId, true, null);

        BatchPaymentRequest req = new BatchPaymentRequest();
        req.setQuoteIds(List.of(quoteA, quoteB));
        req.setTotalPaymentAmount(BigDecimal.valueOf(50));
        req.setPaymentTime(LocalDateTime.now());
        req.setPaymentMethodId(method.getId());

        assertThatThrownBy(() -> paymentService.createBatchPayment(req, collectorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未收金额为 0");
    }

    @Test
    void test3_strictAmountCheck_wrongTotal() {
        Long collectorId = 20L;
        Long quoteA = createQuoteViaWorkflow(BigDecimal.valueOf(100), 1L, collectorId);
        Long quoteB = createQuoteViaWorkflow(BigDecimal.valueOf(50), 1L, collectorId);

        BatchPaymentRequest req = new BatchPaymentRequest();
        req.setQuoteIds(List.of(quoteA, quoteB));
        req.setTotalPaymentAmount(BigDecimal.valueOf(149.99));
        req.setPaymentTime(LocalDateTime.now());
        req.setPaymentMethodId(methodRepo.findByIsActiveTrue().get(0).getId());

        assertThatThrownBy(() -> paymentService.createBatchPayment(req, collectorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("150");

        req.setTotalPaymentAmount(BigDecimal.valueOf(150.01));
        assertThatThrownBy(() -> paymentService.createBatchPayment(req, collectorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("150");
    }

    @Test
    void test4_financeConfirmIntegration() {
        Long collectorId = 20L;
        Long quoteA = createQuoteViaWorkflow(BigDecimal.valueOf(100), 1L, collectorId);
        Long quoteB = createQuoteViaWorkflow(BigDecimal.valueOf(50), 1L, collectorId);

        BatchPaymentRequest req = new BatchPaymentRequest();
        req.setQuoteIds(List.of(quoteA, quoteB));
        req.setTotalPaymentAmount(BigDecimal.valueOf(150));
        req.setPaymentTime(LocalDateTime.now());
        req.setPaymentMethodId(methodRepo.findByIsActiveTrue().get(0).getId());

        List<QuotePaymentEntity> created = paymentService.createBatchPayment(req, collectorId);
        QuotePaymentEntity paymentForA = created.stream().filter(p -> p.getQuoteId().equals(quoteA)).findFirst().orElseThrow();

        paymentService.financeConfirm(paymentForA.getId(), "CONFIRM", 2L, "OK", null);

        QuoteEntity qA = quoteRepo.findById(quoteA).orElseThrow();
        QuoteEntity qB = quoteRepo.findById(quoteB).orElseThrow();
        assertThat(qA.getPaymentStatus()).isEqualTo(QuotePaymentStatus.PAID);
        assertThat(qB.getPaymentStatus()).isEqualTo(QuotePaymentStatus.UNPAID);
    }
}
