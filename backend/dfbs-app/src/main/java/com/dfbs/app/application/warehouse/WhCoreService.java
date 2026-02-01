package com.dfbs.app.application.warehouse;

import com.dfbs.app.application.warehouse.dto.WhInboundReq;
import com.dfbs.app.application.warehouse.dto.WhOutboundReq;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.warehouse.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Stock management: inbound, outbound, transfer (for replenish).
 */
@Service
public class WhCoreService {

    private final WhInventoryRepo whInventoryRepo;
    private final WhStockRecordRepo whStockRecordRepo;
    private final WhWarehouseRepo whWarehouseRepo;
    private final CurrentUserIdResolver userIdResolver;

    public WhCoreService(WhInventoryRepo whInventoryRepo,
                         WhStockRecordRepo whStockRecordRepo,
                         WhWarehouseRepo whWarehouseRepo,
                         CurrentUserIdResolver userIdResolver) {
        this.whInventoryRepo = whInventoryRepo;
        this.whStockRecordRepo = whStockRecordRepo;
        this.whWarehouseRepo = whWarehouseRepo;
        this.userIdResolver = userIdResolver;
    }

    @Transactional
    public WhInventoryEntity inbound(Long warehouseId, WhInboundReq req) {
        requireWarehouseExists(warehouseId);
        requirePositive(req.getQuantity(), "quantity");
        requireNotBlank(req.getPartNo(), "partNo");

        WhInventoryEntity inv = getOrCreateInventory(warehouseId, req.getPartNo());
        int addQty = req.getQuantity();
        int balanceAfter = inv.getQuantity() + addQty;
        inv.setQuantity(balanceAfter);
        setAudit(inv);
        inv = whInventoryRepo.save(inv);

        appendStockRecord(warehouseId, req.getPartNo(), StockRecordType.INBOUND, addQty, balanceAfter, null, null, req.getRemark());
        return inv;
    }

    @Transactional
    public WhInventoryEntity outbound(WhOutboundReq req) {
        requireWarehouseExists(req.getWarehouseId());
        requirePositive(req.getQuantity(), "quantity");
        requireNotBlank(req.getPartNo(), "partNo");
        if (req.getRefType() == null || req.getRefNo() == null || req.getRefNo().isBlank()) {
            throw new IllegalArgumentException("Outbound requires refType and refNo (work order or quote).");
        }

        WhInventoryEntity inv = whInventoryRepo.findByWarehouseIdAndPartNo(req.getWarehouseId(), req.getPartNo())
                .orElseThrow(() -> new IllegalStateException("Inventory record not found: warehouseId=" + req.getWarehouseId() + ", partNo=" + req.getPartNo()));
        int current = inv.getQuantity();
        int deductQty = req.getQuantity();
        if (current < deductQty) {
            throw new IllegalStateException("Inventory insufficient: current=" + current + ", required=" + deductQty);
        }
        int balanceAfter = current - deductQty;
        inv.setQuantity(balanceAfter);
        setAudit(inv);
        inv = whInventoryRepo.save(inv);

        appendStockRecord(req.getWarehouseId(), req.getPartNo(), StockRecordType.OUTBOUND, -deductQty, balanceAfter,
                req.getRefType(), req.getRefNo(), req.getRemark());
        return inv;
    }

    /**
     * Transfer stock from one warehouse to another (e.g. replenish: central -> satellite).
     * Decreases source (REPLENISH_OUT), increases target (REPLENISH_IN).
     */
    @Transactional
    public void transfer(Long fromWhId, Long toWhId, String partNo, Integer qty) {
        requireWarehouseExists(fromWhId);
        requireWarehouseExists(toWhId);
        requireNotBlank(partNo, "partNo");
        requirePositive(qty, "quantity");

        WhInventoryEntity fromInv = whInventoryRepo.findByWarehouseIdAndPartNo(fromWhId, partNo)
                .orElseThrow(() -> new IllegalStateException("Source inventory not found: warehouseId=" + fromWhId + ", partNo=" + partNo));
        int current = fromInv.getQuantity();
        if (current < qty) {
            throw new IllegalStateException("Source inventory insufficient: current=" + current + ", required=" + qty);
        }
        int fromBalanceAfter = current - qty;
        fromInv.setQuantity(fromBalanceAfter);
        setAudit(fromInv);
        whInventoryRepo.save(fromInv);
        appendStockRecord(fromWhId, partNo, StockRecordType.REPLENISH_OUT, -qty, fromBalanceAfter, null, null, "Transfer out");

        WhInventoryEntity toInv = getOrCreateInventory(toWhId, partNo);
        int toBalanceAfter = toInv.getQuantity() + qty;
        toInv.setQuantity(toBalanceAfter);
        setAudit(toInv);
        whInventoryRepo.save(toInv);
        appendStockRecord(toWhId, partNo, StockRecordType.REPLENISH_IN, qty, toBalanceAfter, null, null, "Transfer in");
    }

    public Optional<WhWarehouseEntity> findCentralWarehouse() {
        return whWarehouseRepo.findByType(WarehouseType.CENTRAL).stream().findFirst();
    }

    /** List inventory with optional filters. If both warehouseId and partNo set, returns at most one row. */
    public List<WhInventoryEntity> listInventory(Long warehouseId, String partNo) {
        if (warehouseId != null && partNo != null && !partNo.isBlank()) {
            return whInventoryRepo.findByWarehouseIdAndPartNo(warehouseId, partNo)
                    .map(List::of)
                    .orElse(List.of());
        }
        if (warehouseId != null) {
            return whInventoryRepo.findByWarehouseId(warehouseId);
        }
        return whInventoryRepo.findAll();
    }

    public void requireCentralWarehouse(Long warehouseId) {
        Optional<WhWarehouseEntity> central = findCentralWarehouse();
        if (central.isEmpty() || !central.get().getId().equals(warehouseId)) {
            throw new IllegalArgumentException("Inbound is allowed only for the central warehouse");
        }
    }

    private WhInventoryEntity getOrCreateInventory(Long warehouseId, String partNo) {
        return whInventoryRepo.findByWarehouseIdAndPartNo(warehouseId, partNo)
                .orElseGet(() -> {
                    WhInventoryEntity e = new WhInventoryEntity();
                    e.setWarehouseId(warehouseId);
                    e.setPartNo(partNo);
                    e.setQuantity(0);
                    e.setSafetyThreshold(0);
                    return e;
                });
    }

    private void appendStockRecord(Long warehouseId, String partNo, StockRecordType type, int quantity, int balanceAfter,
                                   OutboundRefType refType, String refNo, String remark) {
        WhStockRecordEntity rec = new WhStockRecordEntity();
        rec.setWarehouseId(warehouseId);
        rec.setPartNo(partNo);
        rec.setType(type);
        rec.setQuantity(quantity);
        rec.setBalanceAfter(balanceAfter);
        rec.setRefType(refType);
        rec.setRefNo(refNo);
        rec.setRemark(remark);
        setAudit(rec);
        whStockRecordRepo.save(rec);
    }

    private void setAudit(WhInventoryEntity e) {
        String uid = userIdResolver.getCurrentUserId().toString();
        e.setUpdatedBy(uid);
        if (e.getCreatedBy() == null) e.setCreatedBy(uid);
    }

    private void setAudit(WhStockRecordEntity e) {
        String uid = userIdResolver.getCurrentUserId().toString();
        e.setCreatedBy(uid);
        e.setUpdatedBy(uid);
    }

    private void requireWarehouseExists(Long warehouseId) {
        if (warehouseId == null) throw new IllegalArgumentException("warehouseId is required");
        if (!whWarehouseRepo.existsById(warehouseId)) {
            throw new IllegalArgumentException("Warehouse not found: id=" + warehouseId);
        }
    }

    private static void requirePositive(Integer value, String name) {
        if (value == null || value <= 0) throw new IllegalArgumentException(name + " must be positive");
    }

    private static void requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
    }
}
