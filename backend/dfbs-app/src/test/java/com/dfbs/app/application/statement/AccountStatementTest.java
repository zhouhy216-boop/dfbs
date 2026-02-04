package com.dfbs.app.application.statement;

import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.dto.BatchPaymentRequest;
import com.dfbs.app.application.quote.payment.QuotePaymentService;
import com.dfbs.app.application.quote.workflow.QuoteWorkflowService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.payment.PaymentMethodRepo;
import com.dfbs.app.modules.quote.payment.QuotePaymentEntity;
import com.dfbs.app.modules.statement.AccountStatementEntity;
import com.dfbs.app.modules.statement.AccountStatementItemRepo;
import com.dfbs.app.modules.statement.StatementStatus;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.junit.jupiter.api.BeforeEach;
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
class AccountStatementTest {

    @Autowired
    private AccountStatementService statementService;
    @Autowired
    private AccountStatementItemRepo statementItemRepo;
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
    private UserRepo userRepo;

    @Autowired
    private com.dfbs.app.modules.quote.dictionary.FeeTypeRepo feeTypeRepo;

    private Long creatorId;
    private Long collectorId;

    private Long createQuoteViaWorkflow(BigDecimal totalAmount, Long customerId, Long collectorId, Currency currency) {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(com.dfbs.app.modules.quote.enums.QuoteSourceType.MANUAL);
        cmd.setCustomerId(customerId);
        QuoteEntity quote = quoteService.createDraft(cmd, "u1");
        quote.setCurrency(currency);
        quoteRepo.save(quote);
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

    @BeforeEach
    void setUp() {
        UserEntity creator = new UserEntity();
        creator.setUsername("stmt-creator");
        creator.setCanRequestPermission(false);
        creator.setAllowNormalNotification(true);
        creator.setCanManageStatements(true);
        creator = userRepo.save(creator);
        creatorId = creator.getId();

        UserEntity collector = new UserEntity();
        collector.setUsername("stmt-collector");
        collector.setCanRequestPermission(false);
        collector.setAllowNormalNotification(true);
        collector.setCanManageStatements(false);
        collector = userRepo.save(collector);
        collectorId = collector.getId();

        if (methodRepo.findByIsActiveTrue().isEmpty()) {
            com.dfbs.app.modules.quote.payment.PaymentMethodEntity m =
                    new com.dfbs.app.modules.quote.payment.PaymentMethodEntity();
            m.setName("Test");
            m.setIsActive(true);
            methodRepo.save(m);
        }
    }

    /**
     * Test 1 (Generate): Select 2 quotes (USD) -> Success. Select mixed currency -> Fail.
     */
    @Test
    void test1_generate_twoQuotesUsd_success_mixedCurrency_fail() {
        Long quote1 = createQuoteViaWorkflow(BigDecimal.valueOf(100), 1L, collectorId, Currency.USD);
        Long quote2 = createQuoteViaWorkflow(BigDecimal.valueOf(50), 1L, collectorId, Currency.USD);

        AccountStatementEntity st = statementService.generate(1L, List.of(quote1, quote2), creatorId);
        assertThat(st.getStatementNo()).startsWith("ST-");
        assertThat(st.getCustomerId()).isEqualTo(1L);
        assertThat(st.getCurrency()).isEqualTo(Currency.USD);
        assertThat(st.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(st.getStatus()).isEqualTo(StatementStatus.PENDING);

        Long quoteCny = createQuoteViaWorkflow(BigDecimal.valueOf(200), 1L, collectorId, Currency.CNY);
        assertThatThrownBy(() -> statementService.generate(1L, List.of(quote1, quoteCny), creatorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("币种不一致");
    }

    /**
     * Test 2 (Remove): Remove 1 quote -> Total updates.
     */
    @Test
    void test2_remove_removeOneQuote_totalUpdates() {
        Long quote1 = createQuoteViaWorkflow(BigDecimal.valueOf(100), 1L, collectorId, Currency.USD);
        Long quote2 = createQuoteViaWorkflow(BigDecimal.valueOf(50), 1L, collectorId, Currency.USD);
        AccountStatementEntity st = statementService.generate(1L, List.of(quote1, quote2), creatorId);
        assertThat(st.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));

        AccountStatementEntity updated = statementService.removeItem(st.getId(), quote2);
        assertThat(updated.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(statementItemRepo.findByStatementIdOrderByIdAsc(st.getId())).hasSize(1);
    }

    /**
     * Test 3 (Payment Binding): Create Batch Payment with statementId -> Payment linked, Statement RECONCILED. Amount mismatch -> Exception.
     */
    @Test
    void test3_paymentBinding_withStatementId_linkedAndReconciled_amountMismatch_exception() {
        Long quote1 = createQuoteViaWorkflow(BigDecimal.valueOf(100), 1L, collectorId, Currency.USD);
        Long quote2 = createQuoteViaWorkflow(BigDecimal.valueOf(50), 1L, collectorId, Currency.USD);
        AccountStatementEntity st = statementService.generate(1L, List.of(quote1, quote2), creatorId);
        assertThat(st.getStatus()).isEqualTo(StatementStatus.PENDING);
        assertThat(st.getPaymentId()).isNull();

        BatchPaymentRequest req = new BatchPaymentRequest();
        req.setStatementId(st.getId());
        req.setCustomerId(1L);
        req.setTotalPaymentAmount(BigDecimal.valueOf(150));
        req.setPaymentTime(LocalDateTime.now());
        req.setPaymentMethodId(methodRepo.findByIsActiveTrue().get(0).getId());
        req.setCurrency("USD");

        List<QuotePaymentEntity> created = paymentService.createBatchPayment(req, collectorId);
        assertThat(created).hasSize(2);
        assertThat(created.stream().map(QuotePaymentEntity::getQuoteId).toList())
                .containsExactlyInAnyOrder(quote1, quote2);

        AccountStatementEntity reconciled = statementService.getById(st.getId());
        assertThat(reconciled.getStatus()).isEqualTo(StatementStatus.RECONCILED);
        assertThat(reconciled.getPaymentId()).isEqualTo(created.get(0).getId());

        AccountStatementEntity st2 = statementService.generate(1L, List.of(
                createQuoteViaWorkflow(BigDecimal.valueOf(80), 1L, collectorId, Currency.USD)), creatorId);
        BatchPaymentRequest badReq = new BatchPaymentRequest();
        badReq.setStatementId(st2.getId());
        badReq.setCustomerId(1L);
        badReq.setTotalPaymentAmount(BigDecimal.valueOf(50));
        badReq.setPaymentTime(LocalDateTime.now());
        badReq.setPaymentMethodId(methodRepo.findByIsActiveTrue().get(0).getId());
        badReq.setCurrency("USD");
        assertThatThrownBy(() -> paymentService.createBatchPayment(badReq, collectorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("到账金额必须等于对账单合计");
    }
}
