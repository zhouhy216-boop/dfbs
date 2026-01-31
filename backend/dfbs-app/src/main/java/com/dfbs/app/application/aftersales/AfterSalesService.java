package com.dfbs.app.application.aftersales;

import com.dfbs.app.modules.aftersales.AfterSalesEntity;
import com.dfbs.app.modules.aftersales.AfterSalesRepo;
import com.dfbs.app.modules.aftersales.AfterSalesStatus;
import com.dfbs.app.modules.aftersales.AfterSalesType;
import com.dfbs.app.modules.shipment.ShipmentMachineRepo;
import com.dfbs.app.modules.shipment.ShipmentRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AfterSalesService {

    private final AfterSalesRepo afterSalesRepo;
    private final ShipmentRepo shipmentRepo;
    private final ShipmentMachineRepo shipmentMachineRepo;

    public AfterSalesService(AfterSalesRepo afterSalesRepo, ShipmentRepo shipmentRepo,
                             ShipmentMachineRepo shipmentMachineRepo) {
        this.afterSalesRepo = afterSalesRepo;
        this.shipmentRepo = shipmentRepo;
        this.shipmentMachineRepo = shipmentMachineRepo;
    }

    @Transactional
    public AfterSalesEntity createDraft(Long sourceShipmentId, AfterSalesType type, String machineNo, String reason) {
        if (!shipmentRepo.existsById(sourceShipmentId)) {
            throw new IllegalStateException("Shipment not found: id=" + sourceShipmentId);
        }
        if (machineNo == null || machineNo.isBlank()) {
            throw new IllegalStateException("machineNo is required");
        }
        AfterSalesEntity e = new AfterSalesEntity();
        e.setType(type != null ? type : AfterSalesType.REPAIR);
        e.setStatus(AfterSalesStatus.DRAFT);
        e.setSourceShipmentId(sourceShipmentId);
        e.setMachineNo(machineNo.trim());
        e.setReason(reason);
        e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        return afterSalesRepo.save(e);
    }

    @Transactional
    public AfterSalesEntity updateDraft(Long id, AfterSalesType type, String machineNo, String reason, String attachments) {
        AfterSalesEntity e = afterSalesRepo.findById(id).orElseThrow(() -> new IllegalStateException("After-sales not found: id=" + id));
        if (e.getStatus() != AfterSalesStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT can be updated");
        }
        if (type != null) e.setType(type);
        if (machineNo != null && !machineNo.isBlank()) e.setMachineNo(machineNo.trim());
        if (reason != null) e.setReason(reason);
        if (attachments != null) e.setAttachments(attachments);
        e.setUpdatedAt(LocalDateTime.now());
        return afterSalesRepo.save(e);
    }

    private static boolean attachmentsNotEmpty(String attachments) {
        if (attachments == null || attachments.isBlank()) return false;
        String s = attachments.trim();
        if ("[]".equals(s) || "null".equals(s)) return false;
        return s.length() > 2; // "[...]"
    }

    @Transactional
    public AfterSalesEntity submit(Long id) {
        AfterSalesEntity e = afterSalesRepo.findById(id).orElseThrow(() -> new IllegalStateException("After-sales not found: id=" + id));
        if (e.getStatus() != AfterSalesStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT can be submitted");
        }
        if (!attachmentsNotEmpty(e.getAttachments())) {
            throw new IllegalStateException("At least one attachment is required to submit");
        }
        e.setStatus(AfterSalesStatus.SUBMITTED);
        e.setUpdatedAt(LocalDateTime.now());
        return afterSalesRepo.save(e);
    }

    @Transactional
    public AfterSalesEntity receive(Long id) {
        return transition(id, AfterSalesStatus.SUBMITTED, AfterSalesStatus.RECEIVED);
    }

    @Transactional
    public AfterSalesEntity process(Long id) {
        return transition(id, AfterSalesStatus.RECEIVED, AfterSalesStatus.PROCESSING);
    }

    @Transactional
    public AfterSalesEntity sendBack(Long id, Long relatedNewShipmentId) {
        AfterSalesEntity e = transition(id, AfterSalesStatus.PROCESSING, AfterSalesStatus.SENT_BACK);
        if (relatedNewShipmentId != null) {
            e.setRelatedNewShipmentId(relatedNewShipmentId);
            afterSalesRepo.save(e);
        }
        return e;
    }

    @Transactional
    public AfterSalesEntity complete(Long id) {
        return transition(id, AfterSalesStatus.SENT_BACK, AfterSalesStatus.COMPLETED);
    }

    private AfterSalesEntity transition(Long id, AfterSalesStatus from, AfterSalesStatus to) {
        AfterSalesEntity e = afterSalesRepo.findById(id).orElseThrow(() -> new IllegalStateException("After-sales not found: id=" + id));
        if (e.getStatus() != from) {
            throw new IllegalStateException("Invalid status for this action: expected " + from + ", got " + e.getStatus());
        }
        e.setStatus(to);
        e.setUpdatedAt(LocalDateTime.now());
        return afterSalesRepo.save(e);
    }

    @Transactional(readOnly = true)
    public Page<AfterSalesEntity> list(String machineNo, AfterSalesStatus status, Pageable pageable) {
        Specification<AfterSalesEntity> spec = (root, query, cb) -> {
            var p = cb.conjunction();
            if (machineNo != null && !machineNo.isBlank()) {
                p = cb.and(p, cb.like(cb.lower(root.get("machineNo")), "%" + machineNo.toLowerCase() + "%"));
            }
            if (status != null) {
                p = cb.and(p, cb.equal(root.get("status"), status));
            }
            return p;
        };
        return afterSalesRepo.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<AfterSalesEntity> findById(Long id) {
        return afterSalesRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public List<com.dfbs.app.modules.shipment.ShipmentMachineEntity> getMachinesByShipmentId(Long shipmentId) {
        return shipmentMachineRepo.findByShipmentIdOrderByIdAsc(shipmentId);
    }
}
