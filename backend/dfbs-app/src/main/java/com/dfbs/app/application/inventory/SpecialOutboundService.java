package com.dfbs.app.application.inventory;

import com.dfbs.app.modules.inventory.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SpecialOutboundService {

    private final InventoryService inventoryService;
    private final SpecialOutboundRequestRepo requestRepo;
    private final WarehouseRepo warehouseRepo;

    public SpecialOutboundService(InventoryService inventoryService,
                                  SpecialOutboundRequestRepo requestRepo,
                                  WarehouseRepo warehouseRepo) {
        this.inventoryService = inventoryService;
        this.requestRepo = requestRepo;
        this.warehouseRepo = warehouseRepo;
    }

    /**
     * Apply special outbound: create request (PENDING_APPROVAL).
     */
    @Transactional
    public SpecialOutboundRequestEntity applySpecial(Long warehouseId, String sku, int qty,
                                                      SpecialOutboundType type, String reason, Long operatorId) {
        requireWarehouse(warehouseId);
        if (qty <= 0) throw new IllegalStateException("数量必须大于0");

        SpecialOutboundRequestEntity req = new SpecialOutboundRequestEntity();
        req.setWarehouseId(warehouseId);
        req.setSku(sku);
        req.setQuantity(qty);
        req.setType(type);
        req.setStatus(SpecialOutboundStatus.PENDING_APPROVAL);
        req.setApplyReason(reason);
        req.setCreatedAt(LocalDateTime.now());
        req.setOperatorId(operatorId);
        return requestRepo.save(req);
    }

    /**
     * Approve or reject. If approved, set APPROVED (execute is separate).
     */
    @Transactional
    public SpecialOutboundRequestEntity approveSpecial(Long requestId, boolean approved, String reason, Long auditorId) {
        SpecialOutboundRequestEntity req = requestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Special outbound request not found: id=" + requestId));
        if (req.getStatus() != SpecialOutboundStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("只有待审批的申请可审批");
        }

        req.setAuditorId(auditorId);
        req.setAuditReason(reason);
        req.setAuditTime(LocalDateTime.now());
        req.setStatus(approved ? SpecialOutboundStatus.APPROVED : SpecialOutboundStatus.REJECTED);
        return requestRepo.save(req);
    }

    /**
     * Execute: deduct stock, set COMPLETED. Only when status is APPROVED.
     */
    @Transactional
    public SpecialOutboundRequestEntity executeSpecial(Long requestId) {
        SpecialOutboundRequestEntity req = requestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Special outbound request not found: id=" + requestId));
        if (req.getStatus() != SpecialOutboundStatus.APPROVED) {
            throw new IllegalStateException("只有已批准的特殊出库申请可执行扣减");
        }

        inventoryService.deductStock(req.getWarehouseId(), req.getSku(), req.getQuantity(),
                TransactionType.OUTBOUND_SPECIAL, requestId, req.getAuditorId());

        req.setStatus(SpecialOutboundStatus.COMPLETED);
        return requestRepo.save(req);
    }

    @Transactional(readOnly = true)
    public SpecialOutboundRequestEntity getRequest(Long requestId) {
        return requestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Special outbound request not found: id=" + requestId));
    }

    private void requireWarehouse(Long id) {
        if (warehouseRepo.findById(id).isEmpty()) {
            throw new IllegalStateException("Warehouse not found: id=" + id);
        }
    }
}
