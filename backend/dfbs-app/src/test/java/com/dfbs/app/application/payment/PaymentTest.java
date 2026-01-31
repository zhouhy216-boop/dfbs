package com.dfbs.app.application.payment;

import com.dfbs.app.application.statement.StatementReconcileService;
import com.dfbs.app.config.ForceFlywayCleanConfig;
import com.dfbs.app.modules.payment.PaymentEntity;
import com.dfbs.app.modules.payment.PaymentRepo;
import com.dfbs.app.modules.payment.PaymentStatus;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.statement.AccountStatementEntity;
import com.dfbs.app.modules.statement.AccountStatementRepo;
import com.dfbs.app.modules.statement.StatementStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(ForceFlywayCleanConfig.class)
class PaymentTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private StatementReconcileService statementReconcileService;

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private QuoteRepo quoteRepo;

    @Autowired
    private QuoteItemRepo quoteItemRepo;

    @Autowired
    private AccountStatementRepo statementRepo;

    private Long customerId = 1L;

    private Long createQuoteWithTotal(BigDecimal total) {
        QuoteEntity q = new QuoteEntity();
        q.setQuoteNo("QT-PAY-" + System.currentTimeMillis());
        q.setStatus(QuoteStatus.DRAFT);
        q.setSourceType(QuoteSourceType.MANUAL);
        q.setCustomerId(customerId);
        q.setCurrency(Currency.CNY);
        q.setPaidAmount(BigDecimal.ZERO);
        q.setPaymentStatus(QuotePaymentStatus.UNPAID);
        q = quoteRepo.save(q);
        QuoteItemEntity item = new QuoteItemEntity();
        item.setQuoteId(q.getId());
        item.setLineOrder(1);
        item.setExpenseType(QuoteExpenseType.OTHER);
        item.setQuantity(1);
        item.setUnitPrice(total);
        item.setAmount(total);
        item.setUnit("次");
        quoteItemRepo.save(item);
        return q.getId();
    }

    private AccountStatementEntity createStatement(BigDecimal totalAmount) {
        AccountStatementEntity st = new AccountStatementEntity();
        st.setStatementNo("ST-PAY-" + System.currentTimeMillis());
        st.setCustomerId(customerId);
        st.setCurrency(Currency.CNY);
        st.setTotalAmount(totalAmount);
        st.setStatus(StatementStatus.PENDING);
        st.setCreatedAt(LocalDateTime.now());
        return statementRepo.save(st);
    }

    @Test
    void strictSum_allocation99_fails_allocation100_succeeds() {
        Long quoteId = createQuoteWithTotal(new BigDecimal("100.00"));
        var cmd = new PaymentService.CreatePaymentCommand();
        cmd.setCustomerId(customerId);
        cmd.setAmount(new BigDecimal("100.00"));
        cmd.setCurrency(Currency.CNY);
        cmd.setReceivedAt(LocalDate.now());
        cmd.setAllocations(List.of(alloc(quoteId, "99.00")));
        assertThatThrownBy(() -> paymentService.create(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must equal sum of allocation amounts");

        cmd.setAllocations(List.of(alloc(quoteId, "100.00")));
        PaymentEntity payment = paymentService.create(cmd);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DRAFT);
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void confirm_quotePaidAmountIncreases_statusPaid() {
        Long quoteId = createQuoteWithTotal(new BigDecimal("100.00"));
        var cmd = new PaymentService.CreatePaymentCommand();
        cmd.setCustomerId(customerId);
        cmd.setAmount(new BigDecimal("100.00"));
        cmd.setCurrency(Currency.CNY);
        cmd.setReceivedAt(LocalDate.now());
        cmd.setAllocations(List.of(alloc(quoteId, "100.00")));
        PaymentEntity payment = paymentService.create(cmd);
        paymentService.confirm(payment.getId());

        QuoteEntity quote = quoteRepo.findById(quoteId).orElseThrow();
        assertThat(quote.getPaidAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(quote.getPaymentStatus()).isEqualTo(QuotePaymentStatus.PAID);
    }

    @Test
    void statementBind_sumMatch_success_sumMismatch_fail() {
        Long quote1 = createQuoteWithTotal(new BigDecimal("500.00"));
        Long quote2 = createQuoteWithTotal(new BigDecimal("500.00"));
        var cmdA = new PaymentService.CreatePaymentCommand();
        cmdA.setCustomerId(customerId);
        cmdA.setAmount(new BigDecimal("500.00"));
        cmdA.setCurrency(Currency.CNY);
        cmdA.setReceivedAt(LocalDate.now());
        cmdA.setAllocations(List.of(alloc(quote1, "500.00")));
        PaymentEntity payA = paymentService.create(cmdA);
        paymentService.confirm(payA.getId());

        var cmdB = new PaymentService.CreatePaymentCommand();
        cmdB.setCustomerId(customerId);
        cmdB.setAmount(new BigDecimal("500.00"));
        cmdB.setCurrency(Currency.CNY);
        cmdB.setReceivedAt(LocalDate.now());
        cmdB.setAllocations(List.of(alloc(quote2, "500.00")));
        PaymentEntity payB = paymentService.create(cmdB);
        paymentService.confirm(payB.getId());

        AccountStatementEntity statement = createStatement(new BigDecimal("1000.00"));
        assertThatThrownBy(() -> statementReconcileService.bindPayments(statement.getId(), List.of(payA.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must equal statement total");

        AccountStatementEntity reconciled = statementReconcileService.bindPayments(statement.getId(), List.of(payA.getId(), payB.getId()));
        assertThat(reconciled.getStatus()).isEqualTo(StatementStatus.RECONCILED);
        assertThat(paymentRepo.findById(payA.getId()).orElseThrow().getStatementId()).isEqualTo(statement.getId());
    }

    @Test
    void doubleBind_alreadyBound_fails() {
        Long quoteId = createQuoteWithTotal(new BigDecimal("200.00"));
        var cmd = new PaymentService.CreatePaymentCommand();
        cmd.setCustomerId(customerId);
        cmd.setAmount(new BigDecimal("200.00"));
        cmd.setCurrency(Currency.CNY);
        cmd.setReceivedAt(LocalDate.now());
        cmd.setAllocations(List.of(alloc(quoteId, "200.00")));
        PaymentEntity payment = paymentService.create(cmd);
        paymentService.confirm(payment.getId());

        AccountStatementEntity st1 = createStatement(new BigDecimal("200.00"));
        statementReconcileService.bindPayments(st1.getId(), List.of(payment.getId()));

        AccountStatementEntity st2 = createStatement(new BigDecimal("200.00"));
        assertThatThrownBy(() -> statementReconcileService.bindPayments(st2.getId(), List.of(payment.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already bound");
    }

    private static PaymentService.CreatePaymentCommand.AllocationItem alloc(Long quoteId, String amount) {
        var a = new PaymentService.CreatePaymentCommand.AllocationItem();
        a.setQuoteId(quoteId);
        a.setAllocatedAmount(new BigDecimal(amount));
        return a;
    }
}
