package com.dfbs.app.interfaces.warehouse;

import com.dfbs.app.application.warehouse.WhCoreService;
import com.dfbs.app.application.warehouse.dto.WhInboundReq;
import com.dfbs.app.application.warehouse.dto.WhOutboundReq;
import com.dfbs.app.modules.warehouse.WhInventoryEntity;
import com.dfbs.app.modules.warehouse.WhWarehouseEntity;
import com.dfbs.app.modules.warehouse.WhWarehouseRepo;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouse")
public class WhStockController {

    private final WhCoreService whCoreService;
    private final WhWarehouseRepo whWarehouseRepo;

    public WhStockController(WhCoreService whCoreService, WhWarehouseRepo whWarehouseRepo) {
        this.whCoreService = whCoreService;
        this.whWarehouseRepo = whWarehouseRepo;
    }

    /** GET /api/v1/warehouse/warehouses — list all warehouses for dropdowns. */
    @GetMapping("/warehouses")
    public List<WhWarehouseEntity> listWarehouses() {
        return whWarehouseRepo.findAll();
    }

    /** Inbound (Central warehouse only). */
    @PostMapping("/inbound")
    @ResponseStatus(HttpStatus.CREATED)
    public WhInventoryEntity inbound(@RequestBody InboundBody body) {
        whCoreService.requireCentralWarehouse(body.getWarehouseId());
        WhInboundReq req = new WhInboundReq(body.getPartNo(), body.getQuantity(), body.getRemark());
        return whCoreService.inbound(body.getWarehouseId(), req);
    }

    @PostMapping("/outbound")
    public WhInventoryEntity outbound(@RequestBody WhOutboundReq req) {
        return whCoreService.outbound(req);
    }

    @GetMapping("/inventory")
    public List<WhInventoryEntity> getInventory(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String partNo) {
        return whCoreService.listInventory(warehouseId, partNo);
    }

    /** Request body for inbound: warehouseId (must be central) + WhInboundReq fields. */
    @lombok.Data
    public static class InboundBody {
        private Long warehouseId;
        private String partNo;
        private Integer quantity;
        private String remark;
    }
}
