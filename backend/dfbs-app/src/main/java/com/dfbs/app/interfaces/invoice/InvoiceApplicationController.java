package com.dfbs.app.interfaces.invoice;

import com.dfbs.app.application.invoice.InvoiceApplicationService;
import com.dfbs.app.application.invoice.dto.InvoiceApplicationCreateRequest;
import com.dfbs.app.modules.invoice.InvoiceApplicationEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoice-applications")
public class InvoiceApplicationController {

    private final InvoiceApplicationService invoiceApplicationService;

    public InvoiceApplicationController(InvoiceApplicationService invoiceApplicationService) {
        this.invoiceApplicationService = invoiceApplicationService;
    }

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceApplicationEntity submit(@RequestBody InvoiceApplicationCreateRequest request,
                                          @RequestParam Long collectorId) {
        return invoiceApplicationService.create(request, collectorId);
    }

    @PostMapping("/audit")
    @ResponseStatus(HttpStatus.OK)
    public InvoiceApplicationEntity audit(@RequestBody AuditRequest request) {
        return invoiceApplicationService.audit(
                request.applicationId(),
                request.result(),
                request.auditorId(),
                request.reason()
        );
    }

    @GetMapping("/my-applications")
    public List<InvoiceApplicationEntity> myApplications(@RequestParam Long collectorId) {
        return invoiceApplicationService.listMyApplications(collectorId);
    }

    @GetMapping("/{applicationId}")
    public InvoiceApplicationEntity getApplication(@PathVariable Long applicationId) {
        return invoiceApplicationService.getApplication(applicationId);
    }

    public record AuditRequest(Long applicationId, String result, Long auditorId, String reason) {}
}
