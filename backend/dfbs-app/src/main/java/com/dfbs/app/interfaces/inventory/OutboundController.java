package com.dfbs.app.interfaces.inventory;

import com.dfbs.app.application.inventory.OutboundService;
import com.dfbs.app.application.inventory.SpecialOutboundService;
import com.dfbs.app.application.inventory.WarehouseSelectionResult;
import com.dfbs.app.application.inventory.WarehouseSelectionService;
import com.dfbs.app.modules.inventory.SpecialOutboundRequestEntity;
import com.dfbs.app.modules.inventory.SpecialOutboundType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/outbound")
public class OutboundController {

    private final OutboundService outboundService;
    private final SpecialOutboundService specialOutboundService;
    private final WarehouseSelectionService warehouseSelectionService;

    public OutboundController(OutboundService outboundService,
                               SpecialOutboundService specialOutboundService,
                               WarehouseSelectionService warehouseSelectionService) {
        this.outboundService = outboundService;
        this.specialOutboundService = specialOutboundService;
        this.warehouseSelectionService = warehouseSelectionService;
    }

    @PostMapping("/wo")
    @ResponseStatus(HttpStatus.OK)
    public void outboundForWorkOrder(@RequestBody OutboundWoRequest request) {
        outboundService.outboundForWorkOrder(
                request.warehouseId(), request.sku(), request.quantity(),
                request.workOrderId(), request.workOrderItemId());
    }

    @PostMapping("/quote")
    @ResponseStatus(HttpStatus.OK)
    public void outboundForQuote(@RequestBody OutboundQuoteRequest request) {
        outboundService.outboundForQuote(
                request.warehouseId(), request.sku(), request.quantity(),
                request.quoteId(), request.quoteItemId());
    }

    @PostMapping("/special/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public SpecialOutboundRequestEntity applySpecial(@RequestBody SpecialApplyRequest request) {
        return specialOutboundService.applySpecial(
                request.warehouseId(), request.sku(), request.quantity(),
                request.type(), request.reason(), request.operatorId());
    }

    @PostMapping("/special/approve")
    public SpecialOutboundRequestEntity approveSpecial(@RequestBody SpecialApproveRequest request) {
        return specialOutboundService.approveSpecial(
                request.requestId(), request.approved(), request.reason(), request.auditorId());
    }

    @PostMapping("/special/confirm")
    public SpecialOutboundRequestEntity confirmSpecial(@RequestParam Long requestId) {
        return specialOutboundService.executeSpecial(requestId);
    }

    @GetMapping("/validate-warehouse")
    public WarehouseSelectionResult validateWarehouse(@RequestParam Long officeWarehouseId,
                                                       @RequestParam Long selectedWarehouseId,
                                                       @RequestParam String sku,
                                                       @RequestParam int quantity) {
        return warehouseSelectionService.validateWarehouseSelection(
                officeWarehouseId, selectedWarehouseId, sku, quantity);
    }

    public record OutboundWoRequest(Long warehouseId, String sku, Integer quantity, Long workOrderId, Long workOrderItemId) {}
    public record OutboundQuoteRequest(Long warehouseId, String sku, Integer quantity, Long quoteId, Long quoteItemId) {}
    public record SpecialApplyRequest(Long warehouseId, String sku, Integer quantity, SpecialOutboundType type, String reason, Long operatorId) {}
    public record SpecialApproveRequest(Long requestId, Boolean approved, String reason, Long auditorId) {}
}
