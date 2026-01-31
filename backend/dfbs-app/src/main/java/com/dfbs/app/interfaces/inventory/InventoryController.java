package com.dfbs.app.interfaces.inventory;

import com.dfbs.app.application.inventory.InventoryService;
import com.dfbs.app.modules.inventory.InventoryEntity;
import com.dfbs.app.modules.inventory.InventoryLogEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/stock")
    public StockResponse getStock(@RequestParam Long warehouseId, @RequestParam String sku) {
        InventoryEntity inv = inventoryService.getStock(warehouseId, sku);
        int qty = inv != null ? inv.getQuantity() : 0;
        return new StockResponse(warehouseId, sku, qty);
    }

    @PostMapping("/inbound")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryEntity inbound(@RequestBody InboundRequest request) {
        return inventoryService.inbound(
                request.warehouseId(), request.sku(), request.quantity(),
                request.sourceDesc(), request.operatorId());
    }

    @PostMapping("/return")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryEntity returnStock(@RequestBody ReturnRequest request) {
        return inventoryService.returnStock(
                request.warehouseId(), request.sku(), request.quantity(),
                request.returnType(), request.reason(), request.operatorId());
    }

    @GetMapping("/logs")
    public List<InventoryLogEntity> getLogs(@RequestParam Long warehouseId,
                                            @RequestParam String sku,
                                            @RequestParam(defaultValue = "50") int limit) {
        return inventoryService.getLogs(warehouseId, sku, limit);
    }

    public record StockResponse(Long warehouseId, String sku, int quantity) {}
    public record InboundRequest(Long warehouseId, String sku, Integer quantity, String sourceDesc, Long operatorId) {}
    public record ReturnRequest(Long warehouseId, String sku, Integer quantity, String returnType, String reason, Long operatorId) {}
}
