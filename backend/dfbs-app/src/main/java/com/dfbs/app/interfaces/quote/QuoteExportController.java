package com.dfbs.app.interfaces.quote;

import com.dfbs.app.application.quote.QuoteExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteExportController {

    private final QuoteExportService exportService;

    public QuoteExportController(QuoteExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(
            @PathVariable Long id,
            @RequestParam(defaultValue = "xlsx") String format
    ) throws Exception {
        QuoteExportService.ExportResult result = exportService.export(id, format);
        MediaType contentType = result.filename().endsWith(".pdf")
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.setContentDispositionFormData("attachment", result.filename());
        headers.setContentLength(result.bytes().length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.bytes());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalState(IllegalStateException ex) {
        // 400 for "quote not found" / "Only DRAFT or CONFIRMED can export" etc.
    }
}
