package com.dfbs.app.interfaces.quote;

import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.dto.WorkOrderImportRequest;
import com.dfbs.app.interfaces.quote.dto.QuoteResponseDto;
import com.dfbs.app.modules.quote.QuoteEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quotes")
public class WorkOrderQuoteController {

    private final QuoteService quoteService;

    public WorkOrderQuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping("/from-workorder")
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponseDto createFromWorkOrder(@RequestBody WorkOrderImportRequest req) {
        QuoteEntity created = quoteService.createFromWorkOrder(req);
        return QuoteResponseDto.from(created);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalState(IllegalStateException ex) {
        // 400 for validation errors
    }
}
