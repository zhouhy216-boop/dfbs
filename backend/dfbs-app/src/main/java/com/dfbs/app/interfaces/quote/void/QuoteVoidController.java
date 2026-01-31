package com.dfbs.app.interfaces.quote.void_;

import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.void_.QuoteVoidService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.void_.QuoteVoidApplicationEntity;
import com.dfbs.app.modules.quote.void_.QuoteVoidRequestEntity;
import com.dfbs.app.modules.quote.void_.VoidRequesterRole;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/quotes/void")
public class QuoteVoidController {

    private final QuoteVoidService voidService;
    private final QuoteService quoteService;

    public QuoteVoidController(QuoteVoidService voidService, QuoteService quoteService) {
        this.voidService = voidService;
        this.quoteService = quoteService;
    }

    /**
     * Apply for void. With role (new flow): direct void or create void request. Without role: legacy apply (Collector only).
     */
    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public Object apply(@RequestBody ApplyVoidRequest req) {
        if (req.role() == null || req.role().isBlank()) {
            return voidService.apply(
                    req.quoteId(),
                    req.applicantId(),
                    req.applyReason() != null ? req.applyReason() : "",
                    req.attachmentUrls()
            );
        }
        VoidRequesterRole role = VoidRequesterRole.valueOf(req.role().toUpperCase());
        Optional<QuoteVoidRequestEntity> request = voidService.applyVoid(
                req.quoteId(),
                req.applyReason() != null ? req.applyReason() : "",
                req.applicantId(),
                role
        );
        if (request.isPresent()) {
            return new ApplyVoidResponse(request.get().getId(), null);
        }
        QuoteEntity quote = quoteService.findById(req.quoteId()).orElseThrow();
        return new ApplyVoidResponse(null, quote);
    }

    /**
     * Audit void: by requestId (new flow) or by applicationId (legacy).
     */
    @PostMapping("/audit")
    public Object audit(@RequestBody AuditVoidRequest req) {
        if (req.requestId() != null) {
            return voidService.auditVoid(req.requestId(), req.result(), req.auditorId(), req.note());
        }
        return voidService.audit(
                req.applicationId(),
                req.auditorId(),
                req.result(),
                req.note()
        );
    }

    /**
     * Direct void: with role (new flow) or without (legacy Finance direct void).
     */
    @PostMapping(value = { "/direct", "/direct-void" })
    @ResponseStatus(HttpStatus.CREATED)
    public Object directVoid(@RequestBody DirectVoidRequest req) {
        if (req.role() != null && !req.role().isBlank()) {
            voidService.directVoidByRole(req.quoteId(), req.auditorId(), req.reason(), VoidRequesterRole.valueOf(req.role().toUpperCase()));
            return quoteService.findById(req.quoteId()).orElseThrow();
        }
        return voidService.directVoid(req.quoteId(), req.auditorId(), req.reason());
    }

    @GetMapping("/history/{quoteId}")
    public List<QuoteVoidApplicationEntity> getHistory(@PathVariable Long quoteId) {
        return voidService.getHistory(quoteId);
    }

    @GetMapping("/requests/{quoteId}")
    public List<QuoteVoidRequestEntity> getVoidRequests(@PathVariable Long quoteId) {
        return voidService.getVoidRequests(quoteId);
    }

    public record ApplyVoidRequest(
            Long quoteId,
            Long applicantId,
            String applyReason,
            String attachmentUrls,
            String role  // INITIATOR, CUSTOMER_CONFIRMER, FINANCE, COLLECTOR, LEADER
    ) {}

    public record ApplyVoidResponse(Long requestId, QuoteEntity quote) {}

    public record AuditVoidRequest(
            Long requestId,   // new flow: void request id
            Long applicationId,  // legacy: application id
            Long auditorId,
            String result,  // PASS or REJECT
            String note
    ) {}

    public record DirectVoidRequest(
            Long quoteId,
            Long auditorId,
            String reason,
            String role  // optional: INITIATOR, FINANCE for direct void
    ) {}
}
