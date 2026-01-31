package com.dfbs.app.application.payment;

import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.payment.PaymentAllocationEntity;
import com.dfbs.app.modules.payment.PaymentEntity;
import com.dfbs.app.modules.payment.PaymentRepo;
import com.dfbs.app.modules.payment.PaymentStatus;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final QuoteRepo quoteRepo;
    private final QuoteItemRepo quoteItemRepo;
    private final CurrentUserIdResolver userIdResolver;

    public PaymentService(PaymentRepo paymentRepo, QuoteRepo quoteRepo, QuoteItemRepo quoteItemRepo,
                         CurrentUserIdResolver userIdResolver) {
        this.paymentRepo = paymentRepo;
        this.quoteRepo = quoteRepo;
        this.quoteItemRepo = quoteItemRepo;
        this.userIdResolver = userIdResolver;
    }

    @Transactional
    public PaymentEntity create(CreatePaymentCommand cmd) {
        if (cmd.getAmount() == null || cmd.getAmount().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalStateException("Payment amount is required and must be >= 0");
        if (cmd.getCustomerId() == null) throw new IllegalStateException("customer_id is required");
        if (cmd.getCurrency() == null) throw new IllegalStateException("currency is required");
        if (cmd.getReceivedAt() == null) throw new IllegalStateException("received_at is required");
        if (cmd.getAllocations() == null || cmd.getAllocations().isEmpty())
            throw new IllegalStateException("At least one allocation is required");

        BigDecimal sumAllocations = cmd.getAllocations().stream()
                .map(CreatePaymentCommand.AllocationItem::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b != null ? b : BigDecimal.ZERO))
                .setScale(2, RoundingMode.HALF_UP);
        if (sumAllocations.compareTo(cmd.getAmount()) != 0)
            throw new IllegalStateException("Payment amount must equal sum of allocation amounts; got payment=" + cmd.getAmount() + ", sum=" + sumAllocations);

        for (CreatePaymentCommand.AllocationItem item : cmd.getAllocations()) {
            QuoteEntity quote = quoteRepo.findById(item.getQuoteId())
                    .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + item.getQuoteId()));
            if (!quote.getCustomerId().equals(cmd.getCustomerId()))
                throw new IllegalStateException("Quote " + quote.getQuoteNo() + " does not belong to customer " + cmd.getCustomerId());
            if (quote.getCurrency() != cmd.getCurrency())
                throw new IllegalStateException("Quote " + quote.getQuoteNo() + " currency does not match payment");
            BigDecimal quoteTotal = calculateQuoteTotal(quote.getId());
            BigDecimal currentPaid = quote.getPaidAmount() != null ? quote.getPaidAmount() : BigDecimal.ZERO;
            if (currentPaid.compareTo(quoteTotal) >= 0)
                throw new IllegalStateException("Quote " + quote.getQuoteNo() + " is already fully paid");
            BigDecimal afterPaid = currentPaid.add(item.getAllocatedAmount() != null ? item.getAllocatedAmount() : BigDecimal.ZERO);
            if (afterPaid.compareTo(quoteTotal) > 0)
                throw new IllegalStateException("Quote " + quote.getQuoteNo() + " would be overpaid");
        }

        String paymentNo = "PAY-" + System.currentTimeMillis();
        while (paymentRepo.existsByPaymentNo(paymentNo)) paymentNo = "PAY-" + System.currentTimeMillis();

        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentNo(paymentNo);
        payment.setCustomerId(cmd.getCustomerId());
        payment.setAmount(cmd.getAmount());
        payment.setCurrency(cmd.getCurrency());
        payment.setReceivedAt(cmd.getReceivedAt());
        payment.setStatus(PaymentStatus.DRAFT);
        payment.setCreatedBy(userIdResolver.getCurrentUserId());
        payment.setCreatedAt(OffsetDateTime.now());

        for (CreatePaymentCommand.AllocationItem item : cmd.getAllocations()) {
            PaymentAllocationEntity alloc = new PaymentAllocationEntity();
            alloc.setPayment(payment);
            alloc.setQuoteId(item.getQuoteId());
            alloc.setAllocatedAmount(item.getAllocatedAmount());
            alloc.setPeriod(item.getPeriod());
            payment.getAllocations().add(alloc);
        }
        return paymentRepo.save(payment);
    }

    @Transactional
    public PaymentEntity confirm(Long paymentId) {
        PaymentEntity payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found: id=" + paymentId));
        if (payment.getStatus() != PaymentStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT payment can be confirmed");

        for (PaymentAllocationEntity alloc : payment.getAllocations()) {
            QuoteEntity quote = quoteRepo.findById(alloc.getQuoteId()).orElseThrow();
            BigDecimal current = quote.getPaidAmount() != null ? quote.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal add = alloc.getAllocatedAmount() != null ? alloc.getAllocatedAmount() : BigDecimal.ZERO;
            quote.setPaidAmount(current.add(add).setScale(2, RoundingMode.HALF_UP));
            BigDecimal total = calculateQuoteTotal(quote.getId());
            if (quote.getPaidAmount().compareTo(total) >= 0)
                quote.setPaymentStatus(QuotePaymentStatus.PAID);
            else
                quote.setPaymentStatus(QuotePaymentStatus.PARTIAL);
            quoteRepo.save(quote);
        }
        payment.setStatus(PaymentStatus.CONFIRMED);
        return paymentRepo.save(payment);
    }

    @Transactional
    public PaymentEntity cancel(Long paymentId) {
        PaymentEntity payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found: id=" + paymentId));
        if (payment.getStatementId() != null)
            throw new IllegalStateException("Cannot cancel payment already bound to a statement");
        if (payment.getStatus() != PaymentStatus.CONFIRMED && payment.getStatus() != PaymentStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT or CONFIRMED payment can be cancelled");

        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            for (PaymentAllocationEntity alloc : payment.getAllocations()) {
                QuoteEntity quote = quoteRepo.findById(alloc.getQuoteId()).orElseThrow();
                BigDecimal current = quote.getPaidAmount() != null ? quote.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal subtract = alloc.getAllocatedAmount() != null ? alloc.getAllocatedAmount() : BigDecimal.ZERO;
                quote.setPaidAmount(current.subtract(subtract).setScale(2, RoundingMode.HALF_UP));
                if (quote.getPaidAmount().compareTo(BigDecimal.ZERO) < 0) quote.setPaidAmount(BigDecimal.ZERO);
                BigDecimal total = calculateQuoteTotal(quote.getId());
                if (quote.getPaidAmount().compareTo(BigDecimal.ZERO) == 0)
                    quote.setPaymentStatus(QuotePaymentStatus.UNPAID);
                else if (quote.getPaidAmount().compareTo(total) >= 0)
                    quote.setPaymentStatus(QuotePaymentStatus.PAID);
                else
                    quote.setPaymentStatus(QuotePaymentStatus.PARTIAL);
                quoteRepo.save(quote);
            }
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        return paymentRepo.save(payment);
    }

    private BigDecimal calculateQuoteTotal(Long quoteId) {
        return quoteItemRepo.findByQuoteIdOrderByLineOrderAsc(quoteId).stream()
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static class CreatePaymentCommand {
        private Long customerId;
        private BigDecimal amount;
        private Currency currency;
        private LocalDate receivedAt;
        private List<AllocationItem> allocations = new ArrayList<>();

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Currency getCurrency() { return currency; }
        public void setCurrency(Currency currency) { this.currency = currency; }
        public LocalDate getReceivedAt() { return receivedAt; }
        public void setReceivedAt(LocalDate receivedAt) { this.receivedAt = receivedAt; }
        public List<AllocationItem> getAllocations() { return allocations; }
        public void setAllocations(List<AllocationItem> allocations) { this.allocations = allocations != null ? allocations : new ArrayList<>(); }

        public static class AllocationItem {
            private Long quoteId;
            private BigDecimal allocatedAmount;
            private String period;

            public Long getQuoteId() { return quoteId; }
            public void setQuoteId(Long quoteId) { this.quoteId = quoteId; }
            public BigDecimal getAllocatedAmount() { return allocatedAmount; }
            public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
            public String getPeriod() { return period; }
            public void setPeriod(String period) { this.period = period; }
        }
    }
}
