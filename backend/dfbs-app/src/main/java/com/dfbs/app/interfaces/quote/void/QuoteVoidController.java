package com.dfbs.app.interfaces.quote.void_;

import com.dfbs.app.application.quote.void_.QuoteVoidService;
import com.dfbs.app.modules.quote.void_.QuoteVoidApplicationEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quotes/void")
public class QuoteVoidController {

    private final QuoteVoidService voidService;

    public QuoteVoidController(QuoteVoidService voidService) {
        this.voidService = voidService;
    }

    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteVoidApplicationEntity apply(@RequestBody ApplyVoidRequest req) {
        return voidService.apply(
                req.quoteId(),
                req.applicantId(),
                req.applyReason(),
                req.attachmentUrls()
        );
    }

    @PostMapping("/audit")
    public QuoteVoidApplicationEntity audit(@RequestBody AuditVoidRequest req) {
        return voidService.audit(
                req.applicationId(),
                req.auditorId(),
                req.result(),
                req.note()
        );
    }

    @PostMapping("/direct-void")
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteVoidApplicationEntity directVoid(@RequestBody DirectVoidRequest req) {
        return voidService.directVoid(
                req.quoteId(),
                req.auditorId(),
                req.reason()
        );
    }

    @GetMapping("/history/{quoteId}")
    public List<QuoteVoidApplicationEntity> getHistory(@PathVariable Long quoteId) {
        return voidService.getHistory(quoteId);
    }

    public record ApplyVoidRequest(
            Long quoteId,
            Long applicantId,
            String applyReason,
            String attachmentUrls
    ) {}

    public record AuditVoidRequest(
            Long applicationId,
            Long auditorId,
            String result,  // PASS or REJECT
            String note
    ) {}

    public record DirectVoidRequest(
            Long quoteId,
            Long auditorId,
            String reason
    ) {}
}
