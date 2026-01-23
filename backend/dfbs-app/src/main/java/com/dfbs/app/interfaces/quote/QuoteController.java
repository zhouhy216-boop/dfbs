package com.dfbs.app.interfaces.quote;

import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.config.CurrentUserProvider;
import com.dfbs.app.interfaces.quote.dto.CreateQuoteRequest;
import com.dfbs.app.interfaces.quote.dto.QuoteResponseDto;
import com.dfbs.app.interfaces.quote.dto.UpdateQuoteRequest;
import com.dfbs.app.modules.quote.QuoteEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private final QuoteService quoteService;
    private final CurrentUserProvider currentUserProvider;

    public QuoteController(QuoteService quoteService, CurrentUserProvider currentUserProvider) {
        this.quoteService = quoteService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponseDto create(@RequestBody CreateQuoteRequest req) {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(req.sourceType());
        cmd.setSourceRefId(req.sourceRefId());
        cmd.setCustomerId(req.customerId());
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

    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponseDto> getById(@PathVariable Long id) {
        return quoteService.findById(id)
                .map(QuoteResponseDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalState(IllegalStateException ex) {
        // 400 for "quote not found" / "Cannot update confirmed quote" etc.
    }
}
