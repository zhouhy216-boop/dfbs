package com.dfbs.app.interfaces.quote.workflow;

import com.dfbs.app.application.quote.workflow.QuoteWorkflowService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.workflow.QuoteWorkflowHistoryEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quotes/workflow")
public class QuoteWorkflowController {

    private final QuoteWorkflowService workflowService;

    public QuoteWorkflowController(QuoteWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/{quoteId}/submit")
    @ResponseStatus(HttpStatus.OK)
    public QuoteEntity submit(@PathVariable Long quoteId, @RequestBody SubmitRequest req) {
        return workflowService.submit(quoteId, req.customerConfirmerId());
    }

    @PostMapping("/{quoteId}/audit")
    @ResponseStatus(HttpStatus.OK)
    public QuoteEntity financeAudit(@PathVariable Long quoteId, @RequestBody AuditRequest req) {
        return workflowService.financeAudit(
                quoteId,
                req.result(),
                req.newCollectorId(),
                req.auditorId(),
                req.reason()
        );
    }

    @PostMapping("/{quoteId}/assign-collector")
    @ResponseStatus(HttpStatus.OK)
    public QuoteEntity assignCollector(@PathVariable Long quoteId, @RequestBody AssignCollectorRequest req) {
        return workflowService.assignCollector(quoteId, req.newCollectorId(), req.operatorId());
    }

    @GetMapping("/{quoteId}/history")
    public List<QuoteWorkflowHistoryEntity> getHistory(@PathVariable Long quoteId) {
        return workflowService.getHistory(quoteId);
    }

    @PostMapping("/fallback")
    @ResponseStatus(HttpStatus.OK)
    public QuoteEntity fallback(@RequestBody FallbackRequest req) {
        return workflowService.fallback(req.quoteId(), req.operatorId(), req.reason());
    }

    public record SubmitRequest(Long customerConfirmerId) {}

    public record FallbackRequest(Long quoteId, Long operatorId, String reason) {}

    public record AuditRequest(String result, Long newCollectorId, Long auditorId, String reason) {}

    public record AssignCollectorRequest(Long newCollectorId, Long operatorId) {}
}
