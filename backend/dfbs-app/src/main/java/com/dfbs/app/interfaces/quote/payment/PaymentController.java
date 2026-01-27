package com.dfbs.app.interfaces.quote.payment;

import com.dfbs.app.application.quote.payment.QuotePaymentService;
import com.dfbs.app.modules.quote.payment.QuotePaymentEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final QuotePaymentService paymentService;

    public PaymentController(QuotePaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public QuotePaymentEntity submit(@RequestBody SubmitPaymentRequest req) {
        return paymentService.submit(
                req.quoteId(),
                req.amount(),
                req.methodId(),
                req.paidAt(),
                req.submitterId(),
                req.isFinance() != null && req.isFinance(),
                req.attachmentUrls()
        );
    }

    @PostMapping("/{paymentId}/confirm")
    public QuotePaymentEntity financeConfirm(
            @PathVariable Long paymentId,
            @RequestBody FinanceConfirmRequest req) {
        return paymentService.financeConfirm(
                paymentId,
                req.action(),
                req.confirmerId(),
                req.confirmNote(),
                req.overpaymentStrategy()
        );
    }

    @GetMapping("/quote/{quoteId}")
    public List<QuotePaymentEntity> getPaymentsByQuote(@PathVariable Long quoteId) {
        return paymentService.getPaymentsByQuote(quoteId);
    }

    @GetMapping("/{paymentId}")
    public QuotePaymentEntity getPayment(@PathVariable Long paymentId) {
        return paymentService.getPayment(paymentId);
    }

    public record SubmitPaymentRequest(
            Long quoteId,
            BigDecimal amount,
            Long methodId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime paidAt,
            Long submitterId,
            Boolean isFinance,
            String attachmentUrls
    ) {}

    public record FinanceConfirmRequest(
            String action,  // CONFIRM or RETURN
            Long confirmerId,
            String confirmNote,
            String overpaymentStrategy  // REJECT or CREATE_BALANCE
    ) {}
}
