package com.dfbs.app.interfaces.quote;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.dfbs.app.application.quote.QuoteVersionService;

@RestController
@RequestMapping("/api/quote-versions")
public class QuoteVersionController {

    private final QuoteVersionService service;

    public QuoteVersionController(QuoteVersionService service) {
        this.service = service;
    }

    public record ActivateReq(String quoteNo, Integer versionNo) {}

    @PostMapping("/activate")
    public void activate(@RequestBody ActivateReq req) {
        if (req == null || req.quoteNo() == null || req.quoteNo().isBlank() || req.versionNo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quoteNo/versionNo required");
        }
        service.activate(req.quoteNo(), req.versionNo());
    }
}
