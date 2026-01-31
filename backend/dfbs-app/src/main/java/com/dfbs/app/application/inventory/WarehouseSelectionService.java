package com.dfbs.app.application.inventory;

import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.modules.inventory.InventoryRepo;
import com.dfbs.app.modules.inventory.WarehouseEntity;
import com.dfbs.app.modules.inventory.WarehouseRepo;
import com.dfbs.app.modules.inventory.WarehouseType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Validates warehouse selection (e.g. branch user selecting HQ).
 * If branch has enough stock but selected HQ -> REQUIRES_REASON and notify warehouse admin.
 */
@Service
public class WarehouseSelectionService {

    private static final long WAREHOUSE_ADMIN_USER_ID = 1L;

    private final WarehouseRepo warehouseRepo;
    private final InventoryRepo inventoryRepo;
    private final NotificationService notificationService;

    public WarehouseSelectionService(WarehouseRepo warehouseRepo,
                                      InventoryRepo inventoryRepo,
                                      NotificationService notificationService) {
        this.warehouseRepo = warehouseRepo;
        this.inventoryRepo = inventoryRepo;
        this.notificationService = notificationService;
    }

    /**
     * If selectedWarehouseId == HQ and officeWarehouseId != HQ (branch):
     * - If branch has inventory >= qty for sku: return REQUIRES_REASON and notify admin.
     * - Else: return OK.
     * Otherwise return OK.
     */
    @Transactional(readOnly = true)
    public WarehouseSelectionResult validateWarehouseSelection(Long officeWarehouseId,
                                                                 Long selectedWarehouseId,
                                                                 String sku,
                                                                 int qty) {
        if (officeWarehouseId == null || selectedWarehouseId == null) return WarehouseSelectionResult.OK;

        WarehouseEntity office = warehouseRepo.findById(officeWarehouseId).orElse(null);
        WarehouseEntity selected = warehouseRepo.findById(selectedWarehouseId).orElse(null);
        if (office == null || selected == null) return WarehouseSelectionResult.OK;
        if (selected.getType() != WarehouseType.HQ) return WarehouseSelectionResult.OK;
        if (office.getType() == WarehouseType.HQ) return WarehouseSelectionResult.OK;

        int branchQty = inventoryRepo.findByWarehouseIdAndSku(officeWarehouseId, sku)
                .map(inv -> inv.getQuantity())
                .orElse(0);
        if (branchQty >= qty) {
            notifyWarehouseAdmin(office.getName(), selected.getName(), sku, qty);
            return WarehouseSelectionResult.REQUIRES_REASON;
        }
        return WarehouseSelectionResult.OK;
    }

    private void notifyWarehouseAdmin(String officeName, String selectedName, String sku, int qty) {
        String title = "办事处选择总部出库";
        String content = String.format("办事处 %s 选择从 %s 出库 SKU=%s 数量=%d，请知悉。", officeName, selectedName, sku, qty);
        notificationService.send(WAREHOUSE_ADMIN_USER_ID, title, content, "/inventory");
    }
}
