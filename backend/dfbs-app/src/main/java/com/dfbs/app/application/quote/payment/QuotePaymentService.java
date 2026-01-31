package com.dfbs.app.application.quote.payment;

import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.dto.BatchPaymentRequest;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.statement.AccountStatementEntity;
import com.dfbs.app.modules.statement.AccountStatementItemRepo;
import com.dfbs.app.modules.statement.AccountStatementRepo;
import com.dfbs.app.modules.statement.StatementStatus;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.PaymentStatus;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.QuoteVoidStatus;
import com.dfbs.app.modules.quote.payment.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class QuotePaymentService {

    private static final long MOCK_FINANCE_NOTIFY_USER_ID = 1L;

    private final QuoteRepo quoteRepo;
    private final QuoteItemRepo itemRepo;
    private final QuotePaymentRepo paymentRepo;
    private final PaymentMethodRepo methodRepo;
    private final QuoteCollectorHistoryRepo collectorHistoryRepo;
    private final QuoteService quoteService;
    private final NotificationService notificationService;
    private final AccountStatementRepo statementRepo;
    private final AccountStatementItemRepo statementItemRepo;

    public QuotePaymentService(QuoteRepo quoteRepo, QuoteItemRepo itemRepo,
                               QuotePaymentRepo paymentRepo, PaymentMethodRepo methodRepo,
                               QuoteCollectorHistoryRepo collectorHistoryRepo, QuoteService quoteService,
                               NotificationService notificationService,
                               AccountStatementRepo statementRepo,
                               AccountStatementItemRepo statementItemRepo) {
        this.quoteRepo = quoteRepo;
        this.itemRepo = itemRepo;
        this.paymentRepo = paymentRepo;
        this.methodRepo = methodRepo;
        this.collectorHistoryRepo = collectorHistoryRepo;
        this.quoteService = quoteService;
        this.notificationService = notificationService;
        this.statementRepo = statementRepo;
        this.statementItemRepo = statementItemRepo;
    }

    /**
     * Submit a payment record (Collector or Finance).
     * 
     * @param quoteId Quote ID
     * @param amount Payment amount (must be <= unpaid amount)
     * @param methodId Payment method ID
     * @param paidAt Payment date/time
     * @param submitterId User ID who submits
     * @param isFinance Whether submitter is Finance (auto-confirm if true)
     * @param attachmentUrls Attachment URLs (JSON or comma-separated)
     * @param paymentBatchNo Optional batch no for merged payment (can be null)
     * @param currency Optional currency (can be null)
     * @param note Optional note (can be null)
     */
    @Transactional
    public QuotePaymentEntity submit(Long quoteId, BigDecimal amount, Long methodId,
                                     LocalDateTime paidAt, Long submitterId, boolean isFinance,
                                     String attachmentUrls, String paymentBatchNo, Currency currency, String note) {
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));

        if (quote.getVoidStatus() == QuoteVoidStatus.APPLYING) {
            throw new IllegalStateException("作废申请审批中，操作已冻结");
        }

        if (quote.getStatus() != QuoteStatus.CONFIRMED) {
            throw new IllegalStateException("只能为已确认的报价单提交付款记录");
        }

        if (paidAt.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("付款时间不能晚于当前时间");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("付款金额必须大于0");
        }

        BigDecimal unpaid = getUnpaidAmount(quoteId);
        if (!isFinance && amount.compareTo(unpaid) > 0) {
            throw new IllegalStateException("付款金额不能大于未付金额: " + unpaid.toPlainString());
        }

        PaymentMethodEntity method = methodRepo.findById(methodId)
                .orElseThrow(() -> new IllegalStateException("Payment method not found: id=" + methodId));

        QuotePaymentEntity payment = new QuotePaymentEntity();
        payment.setQuoteId(quoteId);
        payment.setAmount(amount);
        payment.setMethodId(methodId);
        payment.setPaidAt(paidAt);
        payment.setPaymentTime(paidAt);
        payment.setPaymentBatchNo(paymentBatchNo);
        payment.setCurrency(currency != null ? currency : quote.getCurrency());
        payment.setNote(note);
        payment.setSubmitterId(submitterId);
        payment.setSubmittedAt(LocalDateTime.now());
        payment.setAttachmentUrls(attachmentUrls);
        payment.setIsFinanceConfirmed(false);

        if (isFinance) {
            payment.setStatus(PaymentStatus.CONFIRMED);
            payment.setConfirmerId(submitterId);
            payment.setConfirmedAt(LocalDateTime.now());
            payment.setIsFinanceConfirmed(true);
            BigDecimal quoteTotal = calculateQuoteTotalAmount(quoteId);
            if (amount.compareTo(quoteTotal) > 0) {
                BigDecimal balance = amount.subtract(quoteTotal);
                payment.setRemark("超额付款，余额：" + balance.toPlainString());
                quoteService.createBalanceQuote(quoteId, balance);
            }
            payment = paymentRepo.save(payment);
            postConfirmLogic(quoteId);
        } else {
            payment.setStatus(PaymentStatus.SUBMITTED);
            payment = paymentRepo.save(payment);
        }

        return payment;
    }

    /** Backward-compatible submit (paymentBatchNo, currency, note = null). */
    @Transactional
    public QuotePaymentEntity submit(Long quoteId, BigDecimal amount, Long methodId,
                                     LocalDateTime paidAt, Long submitterId, boolean isFinance,
                                     String attachmentUrls) {
        return submit(quoteId, amount, methodId, paidAt, submitterId, isFinance, attachmentUrls, null, null, null);
    }

    /**
     * Unpaid amount = quote total - sum of finance-confirmed payments.
     */
    @Transactional(readOnly = true)
    public BigDecimal getUnpaidAmount(Long quoteId) {
        BigDecimal total = calculateQuoteTotalAmount(quoteId);
        BigDecimal confirmed = calculateTotalConfirmedAmount(quoteId);
        return total.subtract(confirmed).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate total confirmed amount (sum of payments where isFinanceConfirmed = true).
     */
    private BigDecimal calculateTotalConfirmedAmount(Long quoteId) {
        List<QuotePaymentEntity> confirmed = paymentRepo.findByQuoteIdAndIsFinanceConfirmedTrue(quoteId);
        return confirmed.stream()
                .map(QuotePaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Finance confirm or return a payment.
     * 
     * @param paymentId Payment ID
     * @param action CONFIRM or RETURN
     * @param confirmerId Finance user ID
     * @param confirmNote Confirmation note
     * @param overpaymentStrategy REJECT or CREATE_BALANCE (only for CONFIRM)
     */
    @Transactional
    public QuotePaymentEntity financeConfirm(Long paymentId, String action, Long confirmerId,
                                             String confirmNote, String overpaymentStrategy) {
        QuotePaymentEntity payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found: id=" + paymentId));

        QuoteEntity quote = quoteRepo.findById(payment.getQuoteId())
                .orElseThrow(() -> new IllegalStateException("Quote not found"));

        // Logic Freeze: If voidStatus == APPLYING, BLOCK the action
        if (quote.getVoidStatus() == QuoteVoidStatus.APPLYING) {
            throw new IllegalStateException("作废申请审批中，操作已冻结");
        }

        if (payment.getStatus() != PaymentStatus.SUBMITTED) {
            throw new IllegalStateException("只能确认或退回状态为 SUBMITTED 的付款记录");
        }

        if ("RETURN".equals(action)) {
            payment.setStatus(PaymentStatus.RETURNED);
            payment.setConfirmerId(confirmerId);
            payment.setConfirmedAt(LocalDateTime.now());
            payment.setConfirmNote(confirmNote);
            return paymentRepo.save(payment);
        }

        if (!"CONFIRM".equals(action)) {
            throw new IllegalStateException("Invalid action: " + action);
        }

        BigDecimal totalConfirmedAfter = calculateTotalConfirmedAmount(quote.getId()).add(payment.getAmount());
        BigDecimal quoteTotal = calculateQuoteTotalAmount(quote.getId());

        if (totalConfirmedAfter.compareTo(quoteTotal) > 0) {
            if (overpaymentStrategy == null || overpaymentStrategy.isBlank()) {
                throw new IllegalStateException("超额付款，必须指定处理策略：REJECT 或 CREATE_BALANCE");
            }
            if ("REJECT".equals(overpaymentStrategy)) {
                throw new IllegalStateException("超额付款，请退回此付款记录");
            }
            if ("CREATE_BALANCE".equals(overpaymentStrategy)) {
                payment.setStatus(PaymentStatus.CONFIRMED);
                payment.setConfirmerId(confirmerId);
                payment.setConfirmedAt(LocalDateTime.now());
                payment.setConfirmNote(confirmNote);
                payment.setIsFinanceConfirmed(true);
                BigDecimal balance = totalConfirmedAfter.subtract(quoteTotal);
                payment.setRemark("超额付款，余额：" + balance.toPlainString());
                payment = paymentRepo.save(payment);
                quoteService.createBalanceQuote(quote.getId(), balance);
                postConfirmLogic(quote.getId());
                return payment;
            }
            throw new IllegalStateException("Invalid overpayment strategy: " + overpaymentStrategy);
        }

        payment.setStatus(PaymentStatus.CONFIRMED);
        payment.setConfirmerId(confirmerId);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmNote(confirmNote);
        payment.setIsFinanceConfirmed(true);
        payment = paymentRepo.save(payment);

        postConfirmLogic(quote.getId());
        return payment;
    }

    /**
     * Post-confirm logic: Update quote payment status from sum of finance-confirmed payments.
     */
    private void postConfirmLogic(Long quoteId) {
        QuoteEntity quote = quoteRepo.findById(quoteId).orElseThrow();
        BigDecimal totalConfirmed = calculateTotalConfirmedAmount(quoteId);
        BigDecimal quoteTotal = calculateQuoteTotalAmount(quoteId);

        quote.setPaidAmount(totalConfirmed);
        if (totalConfirmed.compareTo(quoteTotal) >= 0) {
            quote.setPaymentStatus(QuotePaymentStatus.PAID);
            quote.setStatus(QuoteStatus.PAID);
        } else if (totalConfirmed.compareTo(BigDecimal.ZERO) > 0) {
            quote.setPaymentStatus(QuotePaymentStatus.PARTIAL);
            quote.setStatus(QuoteStatus.PARTIAL_PAID);
        } else {
            quote.setPaymentStatus(QuotePaymentStatus.UNPAID);
            // Leave quote status as CONFIRMED when unpaid
        }
        quoteRepo.save(quote);
    }

    /**
     * Calculate quote total amount (sum of all items).
     */
    private BigDecimal calculateQuoteTotalAmount(Long quoteId) {
        List<QuoteItemEntity> items = itemRepo.findByQuoteIdOrderByLineOrderAsc(quoteId);
        return items.stream()
                .map(QuoteItemEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<QuotePaymentEntity> getPaymentsByQuote(Long quoteId) {
        return paymentRepo.findByQuoteId(quoteId);
    }

    @Transactional(readOnly = true)
    public Page<QuotePaymentEntity> listPayments(Long quoteId, Pageable pageable) {
        if (quoteId != null) {
            return paymentRepo.findByQuoteId(quoteId, pageable);
        }
        return paymentRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public QuotePaymentEntity getPayment(Long paymentId) {
        return paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found: id=" + paymentId));
    }

    /**
     * Cancel unconfirmed payments for a quote (e.g. when quote is voided).
     * Sets status to CANCELLED for payments where isFinanceConfirmed = false.
     */
    @Transactional
    public void cancelUnconfirmed(Long quoteId) {
        List<QuotePaymentEntity> payments = paymentRepo.findByQuoteId(quoteId);
        for (QuotePaymentEntity p : payments) {
            if (Boolean.FALSE.equals(p.getIsFinanceConfirmed())) {
                p.setStatus(PaymentStatus.CANCELLED);
                paymentRepo.save(p);
            }
        }
    }

    /**
     * Create batch payment: one payment per quote (amount = quote.unpaidAmount), linked by batchNo.
     * When statementId is set: use statement's quote IDs, validate amount/currency/customerId, then set statement RECONCILED.
     */
    @Transactional
    public List<QuotePaymentEntity> createBatchPayment(BatchPaymentRequest request, Long operatorId) {
        if (request.getTotalPaymentAmount() == null) {
            throw new IllegalArgumentException("totalPaymentAmount 不能为空");
        }
        if (request.getPaymentTime() == null) {
            throw new IllegalArgumentException("paymentTime 不能为空");
        }
        if (request.getPaymentMethodId() == null) {
            throw new IllegalArgumentException("paymentMethodId 不能为空");
        }

        List<Long> quoteIdsToUse = request.getQuoteIds();
        if (request.getStatementId() != null) {
            AccountStatementEntity st = statementRepo.findById(request.getStatementId())
                    .orElseThrow(() -> new IllegalArgumentException("Statement not found: id=" + request.getStatementId()));
            if (st.getStatus() != StatementStatus.PENDING) {
                throw new IllegalArgumentException("对账单已核销，无法用于回款");
            }
            if (request.getTotalPaymentAmount().compareTo(st.getTotalAmount()) != 0) {
                throw new IllegalArgumentException(String.format(
                        "到账金额必须等于对账单合计（应为：%s），当前为：%s",
                        st.getTotalAmount().toPlainString(), request.getTotalPaymentAmount().toPlainString()));
            }
            Currency requestCurrency = parseCurrency(request.getCurrency());
            if (requestCurrency != null && requestCurrency != st.getCurrency()) {
                throw new IllegalArgumentException("请求币种与对账单币种不一致");
            }
            if (request.getCustomerId() == null || !request.getCustomerId().equals(st.getCustomerId())) {
                throw new IllegalArgumentException("客户与对账单客户不一致");
            }
            quoteIdsToUse = statementItemRepo.findByStatementIdOrderByIdAsc(st.getId()).stream()
                    .map(com.dfbs.app.modules.statement.AccountStatementItemEntity::getQuoteId)
                    .toList();
            if (quoteIdsToUse.isEmpty()) {
                throw new IllegalArgumentException("对账单无明细，无法回款");
            }
        }

        if (quoteIdsToUse == null || quoteIdsToUse.isEmpty()) {
            throw new IllegalArgumentException("quoteIds 不能为空");
        }

        List<QuoteEntity> quotes = new ArrayList<>();
        for (Long id : quoteIdsToUse) {
            quotes.add(quoteRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Quote not found: id=" + id)));
        }

        Long firstCustomerId = quotes.get(0).getCustomerId();
        for (QuoteEntity q : quotes) {
            if (!q.getCustomerId().equals(firstCustomerId)) {
                throw new IllegalArgumentException("所选报价单客户不一致，无法合并回款");
            }
        }

        Currency firstCurrency = quotes.get(0).getCurrency();
        for (QuoteEntity q : quotes) {
            if (q.getCurrency() != firstCurrency) {
                throw new IllegalArgumentException("所选报价单币种不一致，无法合并回款");
            }
        }
        Currency requestCurrency = parseCurrency(request.getCurrency());
        if (requestCurrency != null && requestCurrency != firstCurrency) {
            throw new IllegalArgumentException("请求币种与报价单币种不一致，无法合并回款");
        }

        for (QuoteEntity q : quotes) {
            if (q.getCollectorId() == null || !q.getCollectorId().equals(operatorId)) {
                throw new IllegalArgumentException("存在未指派给当前收款执行人的报价单，无法合并回款");
            }
        }

        BigDecimal sumExpected = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (QuoteEntity q : quotes) {
            if (q.getPaymentStatus() == QuotePaymentStatus.PAID) {
                throw new IllegalArgumentException("存在未收金额为 0 的报价单，无法合并回款");
            }
            BigDecimal unpaid = getUnpaidAmount(q.getId());
            if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("存在未收金额为 0 的报价单，无法合并回款");
            }
            sumExpected = sumExpected.add(unpaid);
        }
        if (request.getTotalPaymentAmount().compareTo(sumExpected) != 0) {
            throw new IllegalArgumentException(String.format(
                    "到账金额必须等于所选报价单未收金额合计（应为：%s），当前为：%s",
                    sumExpected.toPlainString(), request.getTotalPaymentAmount().toPlainString()));
        }

        PaymentMethodEntity method = methodRepo.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: id=" + request.getPaymentMethodId()));

        String batchNo = UUID.randomUUID().toString();
        String attachmentUrlsStr = request.getAttachmentUrls() != null && !request.getAttachmentUrls().isEmpty()
                ? String.join(",", request.getAttachmentUrls()) : null;
        LocalDateTime paidAt = request.getPaymentTime();
        Currency currency = requestCurrency != null ? requestCurrency : firstCurrency;

        List<QuotePaymentEntity> created = new ArrayList<>();
        for (QuoteEntity quote : quotes) {
            BigDecimal amount = getUnpaidAmount(quote.getId());
            QuotePaymentEntity payment = new QuotePaymentEntity();
            payment.setQuoteId(quote.getId());
            payment.setAmount(amount);
            payment.setMethodId(request.getPaymentMethodId());
            payment.setPaidAt(paidAt);
            payment.setPaymentTime(paidAt);
            payment.setPaymentBatchNo(batchNo);
            payment.setCurrency(currency);
            payment.setNote(request.getNote());
            payment.setSubmitterId(operatorId);
            payment.setSubmittedAt(LocalDateTime.now());
            payment.setAttachmentUrls(attachmentUrlsStr);
            payment.setIsFinanceConfirmed(false);
            payment.setStatus(PaymentStatus.SUBMITTED);
            payment = paymentRepo.save(payment);
            created.add(payment);
        }

        notificationService.send(MOCK_FINANCE_NOTIFY_USER_ID,
                "合并回款待确认",
                String.format("收款人提交了合并回款，共 %d 笔报价单，批次号：%s。请确认。", quotes.size(), batchNo),
                "/payments?batchNo=" + batchNo);

        if (request.getStatementId() != null) {
            AccountStatementEntity st = statementRepo.findById(request.getStatementId()).orElseThrow();
            st.setPaymentId(created.get(0).getId());
            st.setStatus(StatementStatus.RECONCILED);
            statementRepo.save(st);
        }

        return created;
    }

    private static Currency parseCurrency(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Currency.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
