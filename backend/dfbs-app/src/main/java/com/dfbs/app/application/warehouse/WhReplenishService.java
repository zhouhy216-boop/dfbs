package com.dfbs.app.application.warehouse;

import com.dfbs.app.application.warehouse.dto.WhApproveReq;
import com.dfbs.app.application.warehouse.dto.WhReplenishReq;
import com.dfbs.app.modules.warehouse.ReplenishStatus;
import com.dfbs.app.modules.warehouse.WhReplenishRequestEntity;
import com.dfbs.app.modules.warehouse.WhReplenishRequestRepo;
import com.dfbs.app.modules.warehouse.WhWarehouseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Replenish request workflow: create (PENDING_L1), approveL1 (-> PENDING_L2 or REJECTED), approveL2 (-> COMPLETED via transfer or REJECTED).
 */
@Service
public class WhReplenishService {

    private final WhReplenishRequestRepo replenishRequestRepo;
    private final WhCoreService whCoreService;

    public WhReplenishService(WhReplenishRequestRepo replenishRequestRepo, WhCoreService whCoreService) {
        this.replenishRequestRepo = replenishRequestRepo;
        this.whCoreService = whCoreService;
    }

    @Transactional
    public WhReplenishRequestEntity create(WhReplenishReq req, Long applicantId) {
        validateReplenishReq(req);
        String requestNo = "REP-" + System.currentTimeMillis();
        WhReplenishRequestEntity entity = new WhReplenishRequestEntity();
        entity.setRequestNo(requestNo);
        entity.setTargetWarehouseId(req.getTargetWarehouseId());
        entity.setApplicantId(applicantId);
        entity.setPartNo(req.getPartNo());
        entity.setQuantity(req.getQuantity());
        entity.setReason(req.getReason());
        entity.setStatus(ReplenishStatus.PENDING_L1);
        return replenishRequestRepo.save(entity);
    }

    @Transactional
    public WhReplenishRequestEntity approveL1(Long userId, WhApproveReq req) {
        WhReplenishRequestEntity entity = replenishRequestRepo.findById(req.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Replenish request not found: id=" + req.getRequestId()));
        if (entity.getStatus() != ReplenishStatus.PENDING_L1) {
            throw new IllegalStateException("Request is not in PENDING_L1: current=" + entity.getStatus());
        }
        entity.setL1ApproverId(userId);
        entity.setL1Comment(req.getComment());
        entity.setL1Time(LocalDateTime.now());
        if (Boolean.TRUE.equals(req.getApproved())) {
            entity.setStatus(ReplenishStatus.PENDING_L2);
        } else {
            entity.setStatus(ReplenishStatus.REJECTED);
        }
        return replenishRequestRepo.save(entity);
    }

    @Transactional
    public WhReplenishRequestEntity approveL2(Long userId, WhApproveReq req) {
        WhReplenishRequestEntity entity = replenishRequestRepo.findById(req.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Replenish request not found: id=" + req.getRequestId()));
        if (entity.getStatus() != ReplenishStatus.PENDING_L2) {
            throw new IllegalStateException("Request is not in PENDING_L2: current=" + entity.getStatus());
        }
        entity.setL2ApproverId(userId);
        entity.setL2Comment(req.getComment());
        entity.setL2Time(LocalDateTime.now());
        if (Boolean.TRUE.equals(req.getApproved())) {
            WhWarehouseEntity central = whCoreService.findCentralWarehouse()
                    .orElseThrow(() -> new IllegalStateException("Central warehouse not configured"));
            whCoreService.transfer(central.getId(), entity.getTargetWarehouseId(), entity.getPartNo(), entity.getQuantity());
            entity.setStatus(ReplenishStatus.COMPLETED);
        } else {
            entity.setStatus(ReplenishStatus.REJECTED);
        }
        return replenishRequestRepo.save(entity);
    }

    /** Pending tasks: requests in PENDING_L1 or PENDING_L2 (for listing / my-pending). */
    public List<WhReplenishRequestEntity> listPending() {
        List<WhReplenishRequestEntity> l1 = replenishRequestRepo.findByStatus(ReplenishStatus.PENDING_L1);
        List<WhReplenishRequestEntity> l2 = replenishRequestRepo.findByStatus(ReplenishStatus.PENDING_L2);
        return java.util.stream.Stream.concat(l1.stream(), l2.stream())
                .toList();
    }

    /** My submitted requests (for "我的申请" tab). */
    public List<WhReplenishRequestEntity> listMyRequests(Long applicantId) {
        return replenishRequestRepo.findByApplicantIdOrderByCreatedAtDesc(applicantId);
    }

    private static void validateReplenishReq(WhReplenishReq req) {
        if (req.getTargetWarehouseId() == null) throw new IllegalArgumentException("targetWarehouseId is required");
        if (req.getPartNo() == null || req.getPartNo().isBlank()) throw new IllegalArgumentException("partNo is required");
        if (req.getQuantity() == null || req.getQuantity() <= 0) throw new IllegalArgumentException("quantity must be positive");
    }
}
