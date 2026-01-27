package com.dfbs.app.application.quote.payment;

import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.PaymentStatus;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.QuoteVoidStatus;
import com.dfbs.app.modules.quote.payment.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuotePaymentService {

    private final QuoteRepo quoteRepo;
    private final QuoteItemRepo itemRepo;
    private final QuotePaymentRepo paymentRepo;
    private final PaymentMethodRepo methodRepo;
    private final QuoteCollectorHistoryRepo collectorHistoryRepo;
    private final QuoteService quoteService;

    public QuotePaymentService(QuoteRepo quoteRepo, QuoteItemRepo itemRepo,
                               QuotePaymentRepo paymentRepo, PaymentMethodRepo methodRepo,
                               QuoteCollectorHistoryRepo collectorHistoryRepo, QuoteService quoteService) {
        this.quoteRepo = quoteRepo;
        this.itemRepo = itemRepo;
        this.paymentRepo = paymentRepo;
        this.methodRepo = methodRepo;
        this.collectorHistoryRepo = collectorHistoryRepo;
        this.quoteService = quoteService;
    }

    /**
     * Submit a payment record.
     * 
     * @param quoteId Quote ID
     * @param amount Payment amount
     * @param methodId Payment method ID
     * @param paidAt Payment date/time
     * @param submitterId User ID who submits
     * @param isFinance Whether submitter is Finance (auto-confirm if true)
     * @param attachmentUrls Attachment URLs (JSON or comma-separated)
     */
    @Transactional
    public QuotePaymentEntity submit(Long quoteId, BigDecimal amount, Long methodId,
                                     LocalDateTime paidAt, Long submitterId, boolean isFinance,
                                     String attachmentUrls) {
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));

        // Logic Freeze: If voidStatus == APPLYING, BLOCK the action
        if (quote.getVoidStatus() == QuoteVoidStatus.APPLYING) {
            throw new IllegalStateException("作废申请审批中，操作已冻结");
        }

        // Validation: Quote must be CONFIRMED
        if (quote.getStatus() != QuoteStatus.CONFIRMED) {
            throw new IllegalStateException("只能为已确认的报价单提交付款记录");
        }

        // Validation: paidAt <= Now
        if (paidAt.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("付款时间不能晚于当前时间");
        }

        // Validation: amount > 0
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("付款金额必须大于0");
        }

        PaymentMethodEntity method = methodRepo.findById(methodId)
                .orElseThrow(() -> new IllegalStateException("Payment method not found: id=" + methodId));

        QuotePaymentEntity payment = new QuotePaymentEntity();
        payment.setQuoteId(quoteId);
        payment.setAmount(amount);
        payment.setMethodId(methodId);
        payment.setPaidAt(paidAt);
        payment.setSubmitterId(submitterId);
        payment.setSubmittedAt(LocalDateTime.now());
        payment.setAttachmentUrls(attachmentUrls);

        // If submitter is Finance: Auto-set status to CONFIRMED and trigger "Post-Confirm Logic"
        if (isFinance) {
            payment.setStatus(PaymentStatus.CONFIRMED);
            payment.setConfirmerId(submitterId);
            payment.setConfirmedAt(LocalDateTime.now());
            payment = paymentRepo.save(payment);
            postConfirmLogic(quoteId, payment);
        } else {
            payment.setStatus(PaymentStatus.SUBMITTED);
            payment = paymentRepo.save(payment);
        }

        return payment;
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

        // CONFIRM logic (quote already loaded above)

        // Calculate totalPaid (existing confirmed + current)
        BigDecimal totalPaid = calculateTotalPaid(quote.getId());
        totalPaid = totalPaid.add(payment.getAmount());

        // Calculate quote total amount
        BigDecimal quoteTotal = calculateQuoteTotalAmount(quote.getId());

        // Check Overpayment
        if (totalPaid.compareTo(quoteTotal) > 0) {
            // Overpayment: Strategy REQUIRED
            if (overpaymentStrategy == null || overpaymentStrategy.isBlank()) {
                throw new IllegalStateException("超额付款，必须指定处理策略：REJECT 或 CREATE_BALANCE");
            }

            if ("REJECT".equals(overpaymentStrategy)) {
                throw new IllegalStateException("超额付款，请退回此付款记录");
            }

            if ("CREATE_BALANCE".equals(overpaymentStrategy)) {
                // Mark payment CONFIRMED
                payment.setStatus(PaymentStatus.CONFIRMED);
                payment.setConfirmerId(confirmerId);
                payment.setConfirmedAt(LocalDateTime.now());
                payment.setConfirmNote(confirmNote);
                BigDecimal balance = totalPaid.subtract(quoteTotal);
                payment.setRemark("超额付款，余额：" + balance.toPlainString());
                payment = paymentRepo.save(payment);

                // Create balance quote
                quoteService.createBalanceQuote(quote.getId(), balance);

                // Post-confirm logic
                postConfirmLogic(quote.getId(), payment);
                return payment;
            }

            throw new IllegalStateException("Invalid overpayment strategy: " + overpaymentStrategy);
        }

        // totalPaid <= quoteTotal: Mark CONFIRMED
        payment.setStatus(PaymentStatus.CONFIRMED);
        payment.setConfirmerId(confirmerId);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmNote(confirmNote);
        payment = paymentRepo.save(payment);

        // Post-confirm logic
        postConfirmLogic(quote.getId(), payment);

        return payment;
    }

    /**
     * Post-confirm logic: Update quote payment status and lock collector if fully paid.
     */
    private void postConfirmLogic(Long quoteId, QuotePaymentEntity payment) {
        QuoteEntity quote = quoteRepo.findById(quoteId).orElseThrow();
        BigDecimal totalPaid = calculateTotalPaid(quoteId);
        BigDecimal quoteTotal = calculateQuoteTotalAmount(quoteId);

        // Update quote.paymentStatus
        if (totalPaid.compareTo(quoteTotal) >= 0) {
            quote.setPaymentStatus(QuotePaymentStatus.PAID);
            // Lock Collector: paymentStatus == PAID means collector is locked
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            quote.setPaymentStatus(QuotePaymentStatus.PARTIAL);
        } else {
            quote.setPaymentStatus(QuotePaymentStatus.UNPAID);
        }

        quoteRepo.save(quote);
    }

    /**
     * Calculate total paid amount (sum of all CONFIRMED payments).
     */
    private BigDecimal calculateTotalPaid(Long quoteId) {
        List<QuotePaymentEntity> confirmedPayments = paymentRepo.findByQuoteIdAndStatus(
                quoteId, PaymentStatus.CONFIRMED);
        return confirmedPayments.stream()
                .map(QuotePaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
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
    public QuotePaymentEntity getPayment(Long paymentId) {
        return paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found: id=" + paymentId));
    }
}
