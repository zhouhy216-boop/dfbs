package com.dfbs.app.application.inventory;

import com.dfbs.app.modules.inventory.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryService {

    private final InventoryRepo inventoryRepo;
    private final InventoryLogRepo inventoryLogRepo;
    private final WarehouseRepo warehouseRepo;

    public InventoryService(InventoryRepo inventoryRepo,
                            InventoryLogRepo inventoryLogRepo,
                            WarehouseRepo warehouseRepo) {
        this.inventoryRepo = inventoryRepo;
        this.inventoryLogRepo = inventoryLogRepo;
        this.warehouseRepo = warehouseRepo;
    }

    /**
     * Add stock atomically: update inventory (+qty), insert log.
     */
    @Transactional
    public InventoryEntity addStock(Long warehouseId, String sku, int qty,
                                    TransactionType type, Long relatedId, Long operatorId) {
        if (qty <= 0) throw new IllegalStateException("入库数量必须大于0");
        requireWarehouseExists(warehouseId);
        requireNotBlank(sku, "sku不能为空");

        InventoryEntity inv = getOrCreateInventory(warehouseId, sku);
        int after = inv.getQuantity() + qty;
        inv.setQuantity(after);
        inv = inventoryRepo.save(inv);

        insertLog(warehouseId, sku, qty, after, type, relatedId, operatorId, null);
        return inv;
    }

    /**
     * Deduct stock atomically: check currentQty >= qty, update (-qty), insert log.
     */
    @Transactional
    public InventoryEntity deductStock(Long warehouseId, String sku, int qty,
                                       TransactionType type, Long relatedId, Long operatorId) {
        if (qty <= 0) throw new IllegalStateException("出库数量必须大于0");
        requireWarehouseExists(warehouseId);
        requireNotBlank(sku, "sku不能为空");

        InventoryEntity inv = inventoryRepo.findByWarehouseIdAndSku(warehouseId, sku)
                .orElseThrow(() -> new IllegalStateException("库存记录不存在: warehouseId=" + warehouseId + ", sku=" + sku));
        int current = inv.getQuantity();
        if (current < qty) {
            throw new IllegalStateException("库存不足: 当前=" + current + ", 需要=" + qty + " (warehouseId=" + warehouseId + ", sku=" + sku + ")");
        }
        int after = current - qty;
        inv.setQuantity(after);
        inv = inventoryRepo.save(inv);

        insertLog(warehouseId, sku, -qty, after, type, relatedId, operatorId, null);
        return inv;
    }

    /**
     * Inbound (replenishment). Calls addStock with type INBOUND.
     */
    @Transactional
    public InventoryEntity inbound(Long warehouseId, String sku, int qty, String sourceDesc, Long operatorId) {
        return addStock(warehouseId, sku, qty, TransactionType.INBOUND, null, operatorId);
    }

    /**
     * Return (restore). Calls addStock with type RETURN.
     */
    @Transactional
    public InventoryEntity returnStock(Long warehouseId, String sku, int qty, String returnType, String reason, Long operatorId) {
        return addStock(warehouseId, sku, qty, TransactionType.RETURN, null, operatorId);
    }

    @Transactional(readOnly = true)
    public InventoryEntity getStock(Long warehouseId, String sku) {
        return inventoryRepo.findByWarehouseIdAndSku(warehouseId, sku).orElse(null);
    }

    @Transactional(readOnly = true)
    public int getQuantity(Long warehouseId, String sku) {
        return inventoryRepo.findByWarehouseIdAndSku(warehouseId, sku)
                .map(InventoryEntity::getQuantity)
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public java.util.List<InventoryLogEntity> getLogs(Long warehouseId, String sku, int limit) {
        return inventoryLogRepo.findByWarehouseIdAndSkuOrderByCreatedAtDesc(
                warehouseId, sku, org.springframework.data.domain.PageRequest.of(0, limit));
    }

    /** Package-level for use by Transfer/Outbound services. */
    InventoryEntity getOrCreateInventory(Long warehouseId, String sku) {
        return inventoryRepo.findByWarehouseIdAndSku(warehouseId, sku)
                .orElseGet(() -> {
                    InventoryEntity e = new InventoryEntity();
                    e.setWarehouseId(warehouseId);
                    e.setSku(sku);
                    e.setQuantity(0);
                    return inventoryRepo.save(e);
                });
    }

    private void insertLog(Long warehouseId, String sku, int changeAmount, int afterQuantity,
                           TransactionType type, Long relatedId, Long operatorId, String remark) {
        InventoryLogEntity log = new InventoryLogEntity();
        log.setWarehouseId(warehouseId);
        log.setSku(sku);
        log.setChangeAmount(changeAmount);
        log.setAfterQuantity(afterQuantity);
        log.setType(type);
        log.setRelatedId(relatedId);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        inventoryLogRepo.save(log);
    }

    private void requireWarehouseExists(Long warehouseId) {
        if (warehouseRepo.findById(warehouseId).isEmpty()) {
            throw new IllegalStateException("Warehouse not found: id=" + warehouseId);
        }
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalStateException(message);
    }
}
