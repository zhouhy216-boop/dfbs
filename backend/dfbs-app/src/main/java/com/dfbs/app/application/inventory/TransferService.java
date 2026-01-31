package com.dfbs.app.application.inventory;

import com.dfbs.app.application.attachment.AttachmentPoint;
import com.dfbs.app.application.attachment.AttachmentRuleService;
import com.dfbs.app.application.attachment.AttachmentTargetType;
import com.dfbs.app.modules.inventory.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferService {

    private final InventoryService inventoryService;
    private final TransferOrderRepo transferOrderRepo;
    private final WarehouseRepo warehouseRepo;
    private final AttachmentRuleService ruleService;

    public TransferService(InventoryService inventoryService,
                           TransferOrderRepo transferOrderRepo,
                           WarehouseRepo warehouseRepo,
                           AttachmentRuleService ruleService) {
        this.inventoryService = inventoryService;
        this.transferOrderRepo = transferOrderRepo;
        this.warehouseRepo = warehouseRepo;
        this.ruleService = ruleService;
    }

    /**
     * Apply transfer: create TransferOrder (PENDING).
     */
    @Transactional
    public TransferOrderEntity applyTransfer(Long sourceId, Long targetId, String sku, int qty, Long operatorId) {
        if (sourceId.equals(targetId)) throw new IllegalStateException("调拨源与目标不能相同");
        requireWarehouse(sourceId);
        requireWarehouse(targetId);
        if (qty <= 0) throw new IllegalStateException("调拨数量必须大于0");

        TransferOrderEntity order = new TransferOrderEntity();
        order.setSourceWarehouseId(sourceId);
        order.setTargetWarehouseId(targetId);
        order.setSku(sku);
        order.setQuantity(qty);
        order.setStatus(TransferStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setOperatorId(operatorId);
        return transferOrderRepo.save(order);
    }

    /**
     * Ship transfer: deduct from source, set IN_TRANSIT. Logistics URL (Logistics Bill) is mandatory.
     */
    @Transactional
    public TransferOrderEntity shipTransfer(Long transferId, String logisticsUrl) {
        ruleService.validate(AttachmentTargetType.HQ_TRANSFER, AttachmentPoint.EXECUTE,
                logisticsUrl != null && !logisticsUrl.isBlank() ? List.of(logisticsUrl) : List.of());
        TransferOrderEntity order = transferOrderRepo.findById(transferId)
                .orElseThrow(() -> new IllegalStateException("Transfer order not found: id=" + transferId));
        if (order.getStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("只有待发货的调拨单可以发货");
        }

        inventoryService.deductStock(order.getSourceWarehouseId(), order.getSku(), order.getQuantity(),
                TransactionType.TRANSFER_OUT, transferId, order.getOperatorId());

        order.setStatus(TransferStatus.IN_TRANSIT);
        order.setLogisticsUrl(logisticsUrl);
        order.setAuditTime(LocalDateTime.now());
        return transferOrderRepo.save(order);
    }

    /**
     * Receive transfer: add to target, set COMPLETED.
     */
    @Transactional
    public TransferOrderEntity receiveTransfer(Long transferId) {
        TransferOrderEntity order = transferOrderRepo.findById(transferId)
                .orElseThrow(() -> new IllegalStateException("Transfer order not found: id=" + transferId));
        if (order.getStatus() != TransferStatus.IN_TRANSIT) {
            throw new IllegalStateException("只有在途的调拨单可以收货");
        }

        inventoryService.addStock(order.getTargetWarehouseId(), order.getSku(), order.getQuantity(),
                TransactionType.TRANSFER_IN, transferId, null);

        order.setStatus(TransferStatus.COMPLETED);
        order.setAuditTime(LocalDateTime.now());
        return transferOrderRepo.save(order);
    }

    @Transactional(readOnly = true)
    public TransferOrderEntity getTransfer(Long transferId) {
        return transferOrderRepo.findById(transferId)
                .orElseThrow(() -> new IllegalStateException("Transfer order not found: id=" + transferId));
    }

    private void requireWarehouse(Long id) {
        if (warehouseRepo.findById(id).isEmpty()) {
            throw new IllegalStateException("Warehouse not found: id=" + id);
        }
    }
}
