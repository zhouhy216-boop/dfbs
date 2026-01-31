package com.dfbs.app.interfaces.quote;

import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.dto.QuoteFilterRequest;
import com.dfbs.app.application.quote.dto.QuotePendingPaymentDTO;
import com.dfbs.app.config.CurrentUserProvider;
import com.dfbs.app.interfaces.quote.dto.CreateQuoteRequest;
import com.dfbs.app.interfaces.quote.dto.QuoteListDto;
import com.dfbs.app.interfaces.quote.dto.QuoteResponseDto;
import com.dfbs.app.interfaces.quote.dto.UpdateQuoteRequest;
import com.dfbs.app.modules.quote.QuoteEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Quote", description = "Quote (报价单) CRUD and lifecycle")
@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private final QuoteService quoteService;
    private final CurrentUserProvider currentUserProvider;

    public QuoteController(QuoteService quoteService, CurrentUserProvider currentUserProvider) {
        this.quoteService = quoteService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Create draft quote", description = "Creates a new draft quote with source and customer")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponseDto create(@RequestBody CreateQuoteRequest req) {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(req.sourceType());
        cmd.setSourceRefId(req.sourceRefId());
        cmd.setCustomerId(req.customerId());
        cmd.setCustomerName(req.customerName());
        if (req.businessLineId() != null) {
            cmd.setBusinessLineId(req.businessLineId());
        }
        QuoteEntity created = quoteService.createDraft(cmd, currentUserProvider.getCurrentUser());
        return QuoteResponseDto.from(created);
    }

    @PutMapping("/{id}")
    public QuoteResponseDto update(@PathVariable Long id, @RequestBody UpdateQuoteRequest req) {
        var cmd = new QuoteService.UpdateQuoteCommand();
        cmd.setCurrency(req.currency());
        cmd.setRecipient(req.recipient());
        cmd.setPhone(req.phone());
        cmd.setAddress(req.address());
        if (req.businessLineId() != null) {
            cmd.setBusinessLineId(req.businessLineId());
        }
        if (req.machineId() != null) {
            cmd.setMachineId(req.machineId());
        }
        if (req.customerId() != null) {
            cmd.setCustomerId(req.customerId());
        }
        if (req.customerName() != null) {
            cmd.setCustomerName(req.customerName());
        }
        QuoteEntity updated = quoteService.updateHeader(id, cmd);
        return QuoteResponseDto.from(updated);
    }

    @PostMapping("/{id}/confirm")
    public QuoteResponseDto confirm(@PathVariable Long id) {
        QuoteEntity confirmed = quoteService.confirm(id);
        return QuoteResponseDto.from(confirmed);
    }

    @PostMapping("/{id}/cancel")
    public QuoteResponseDto cancel(@PathVariable Long id) {
        QuoteEntity cancelled = quoteService.cancel(id);
        return QuoteResponseDto.from(cancelled);
    }

    @Operation(summary = "Submit quote", description = "DRAFT -> PENDING; requires at least one item. Returns 400 if no items.")
    @PostMapping("/{id}/submit")
    public QuoteResponseDto submit(@PathVariable Long id) {
        QuoteEntity submitted = quoteService.submit(id);
        return quoteService.findDetailById(submitted.getId()).orElse(QuoteResponseDto.from(submitted));
    }

    @Operation(summary = "List quotes (general search)", description = "Paginated list of all quotes; supports page, size, sort (e.g. createdAt,desc)")
    @GetMapping
    public Page<QuoteListDto> list(Pageable pageable) {
        return quoteService.listQuotes(pageable);
    }

    @Operation(summary = "Get quote by ID", description = "Returns quote with resolved customer name from master data")
    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponseDto> getById(@PathVariable Long id) {
        return quoteService.findDetailById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-pending")
    public List<QuotePendingPaymentDTO> myPending(
            @RequestParam Long collectorId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createTimeFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createTimeTo,
            @RequestParam(required = false) com.dfbs.app.modules.quote.enums.QuotePaymentStatus paymentStatus) {
        QuoteFilterRequest filter = new QuoteFilterRequest();
        filter.setCustomerName(customerName);
        filter.setCreateTimeFrom(createTimeFrom);
        filter.setCreateTimeTo(createTimeTo);
        filter.setPaymentStatus(paymentStatus);
        return quoteService.listMyPendingQuotes(collectorId, filter);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalState(IllegalStateException ex) {
        // 400 for "quote not found" / "Cannot update confirmed quote" etc.
    }
}
