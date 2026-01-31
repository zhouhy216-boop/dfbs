package com.dfbs.app.interfaces.quote.payment;

import com.dfbs.app.application.quote.dto.BatchPaymentRequest;
import com.dfbs.app.application.quote.payment.QuotePaymentService;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.payment.QuotePaymentEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Finance (Payment)", description = "Payment submit, confirm, batch")
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final QuotePaymentService paymentService;

    public PaymentController(QuotePaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Submit payment", description = "Submit a payment for a quote")
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
                req.attachmentUrls(),
                req.paymentBatchNo(),
                req.currency(),
                req.note()
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

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<QuotePaymentEntity> batch(@RequestBody BatchPaymentRequest request,
                                          @RequestParam Long operatorId) {
        return paymentService.createBatchPayment(request, operatorId);
    }

    @Operation(summary = "List payments", description = "Paginated list; optional quoteId filter")
    @GetMapping
    public Page<QuotePaymentEntity> list(
            @RequestParam(required = false) Long quoteId,
            Pageable pageable) {
        return paymentService.listPayments(quoteId, pageable);
    }

    @GetMapping("/quote/{quoteId}")
    public List<QuotePaymentEntity> getPaymentsByQuote(@PathVariable Long quoteId) {
        return paymentService.getPaymentsByQuote(quoteId);
    }

    @GetMapping("/{paymentId}")
    public QuotePaymentEntity getPayment(@PathVariable Long paymentId) {
        return paymentService.getPayment(paymentId);
    }

    @Schema(description = "Request to submit a payment")
    public record SubmitPaymentRequest(
            @Schema(description = "Quote ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long quoteId,
            @Schema(description = "Payment amount", example = "1000.00", requiredMode = Schema.RequiredMode.REQUIRED)
            BigDecimal amount,
            @Schema(description = "Payment method ID", example = "1")
            Long methodId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Schema(description = "Paid at (ISO datetime)", example = "2025-01-31T10:00:00")
            LocalDateTime paidAt,
            @Schema(description = "Submitter user ID", example = "1")
            Long submitterId,
            @Schema(description = "Is finance submitter", example = "true")
            Boolean isFinance,
            @Schema(description = "Attachment URLs (comma-separated)")
            String attachmentUrls,
            @Schema(description = "Payment batch number")
            String paymentBatchNo,
            @Schema(description = "Currency", example = "CNY")
            Currency currency,
            @Schema(description = "Note")
            String note
    ) {}

    public record FinanceConfirmRequest(
            String action,  // CONFIRM or RETURN
            Long confirmerId,
            String confirmNote,
            String overpaymentStrategy  // REJECT or CREATE_BALANCE
    ) {}
}
