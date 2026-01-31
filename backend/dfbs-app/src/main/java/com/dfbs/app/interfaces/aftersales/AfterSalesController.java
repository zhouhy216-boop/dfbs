package com.dfbs.app.interfaces.aftersales;

import com.dfbs.app.application.aftersales.AfterSalesService;
import com.dfbs.app.modules.aftersales.AfterSalesEntity;
import com.dfbs.app.modules.aftersales.AfterSalesStatus;
import com.dfbs.app.modules.aftersales.AfterSalesType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "After-Sales", description = "Exchange/Repair tickets from Shipment")
@RestController
@RequestMapping("/api/v1/after-sales")
public class AfterSalesController {

    private final AfterSalesService afterSalesService;

    public AfterSalesController(AfterSalesService afterSalesService) {
        this.afterSalesService = afterSalesService;
    }

    @Operation(summary = "Create draft", description = "Create after-sales ticket from a shipment. sourceShipmentId and machineNo required.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AfterSalesEntity create(@RequestBody CreateAfterSalesRequest req) {
        return afterSalesService.createDraft(
                req.sourceShipmentId(),
                req.type(),
                req.machineNo(),
                req.reason()
        );
    }

    @Operation(summary = "Update draft")
    @PutMapping("/{id}")
    public AfterSalesEntity update(@PathVariable Long id, @RequestBody UpdateAfterSalesRequest req) {
        return afterSalesService.updateDraft(id, req.type(), req.machineNo(), req.reason(), req.attachments());
    }

    @Operation(summary = "Submit", description = "Draft -> SUBMITTED. Requires attachments not empty.")
    @PostMapping("/{id}/submit")
    public AfterSalesEntity submit(@PathVariable Long id) {
        return afterSalesService.submit(id);
    }

    @PostMapping("/{id}/receive")
    public AfterSalesEntity receive(@PathVariable Long id) {
        return afterSalesService.receive(id);
    }

    @PostMapping("/{id}/process")
    public AfterSalesEntity process(@PathVariable Long id) {
        return afterSalesService.process(id);
    }

    @PostMapping("/{id}/send-back")
    public AfterSalesEntity sendBack(@PathVariable Long id,
                                     @RequestParam(required = false) Long relatedNewShipmentId) {
        return afterSalesService.sendBack(id, relatedNewShipmentId);
    }

    @PostMapping("/{id}/complete")
    public AfterSalesEntity complete(@PathVariable Long id) {
        return afterSalesService.complete(id);
    }

    @Operation(summary = "List", description = "Search by machineNo, status")
    @GetMapping
    public Page<AfterSalesEntity> list(
            @RequestParam(required = false) String machineNo,
            @RequestParam(required = false) AfterSalesStatus status,
            Pageable pageable) {
        return afterSalesService.list(machineNo, status, pageable);
    }

    @GetMapping("/{id}")
    public AfterSalesEntity getById(@PathVariable Long id) {
        return afterSalesService.findById(id).orElseThrow(() -> new IllegalStateException("After-sales not found: id=" + id));
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalState(IllegalStateException ex) {
        // 400 for "not found" / "Only DRAFT can be submitted" / "At least one attachment required" etc.
    }

    public record CreateAfterSalesRequest(Long sourceShipmentId, AfterSalesType type, String machineNo, String reason) {}
    public record UpdateAfterSalesRequest(AfterSalesType type, String machineNo, String reason, String attachments) {}
}
