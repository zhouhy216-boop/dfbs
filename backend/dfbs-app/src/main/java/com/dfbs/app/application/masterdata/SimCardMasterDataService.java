package com.dfbs.app.application.masterdata;

import com.dfbs.app.modules.masterdata.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SimCardMasterDataService {

    private final SimCardRepo simCardRepo;
    private final SimBindingLogRepo bindingLogRepo;

    public SimCardMasterDataService(SimCardRepo simCardRepo, SimBindingLogRepo bindingLogRepo) {
        this.simCardRepo = simCardRepo;
        this.bindingLogRepo = bindingLogRepo;
    }

    private static void requireNotBlank(String s, String field) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(field + " is required");
    }

    @Transactional
    public SimCardEntity save(String cardNo, String operator, String planInfo, Long machineId, String createdBy) {
        requireNotBlank(cardNo, "cardNo");
        if (simCardRepo.existsByCardNo(cardNo.trim())) {
            throw new IllegalArgumentException("cardNo already exists: " + cardNo);
        }
        SimCardEntity e = new SimCardEntity();
        e.setCardNo(cardNo.trim());
        e.setOperator(operator);
        e.setPlanInfo(planInfo);
        e.setMachineId(machineId);
        e.setStatus(MasterDataStatus.ENABLE);
        LocalDateTime now = LocalDateTime.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setCreatedBy(createdBy);
        e.setUpdatedBy(createdBy);
        return simCardRepo.save(e);
    }

    @Transactional
    public SimCardEntity update(Long id, String cardNo, String operator, String planInfo, Long machineId, String updatedBy) {
        SimCardEntity e = simCardRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("SimCard not found: id=" + id));
        if (e.getStatus() == MasterDataStatus.DISABLE) {
            throw new IllegalStateException("Cannot update disabled sim card");
        }
        Long oldMachineId = e.getMachineId();
        if (machineId != null && (oldMachineId == null ? machineId != null : !oldMachineId.equals(machineId))) {
            SimBindingLogEntity log = new SimBindingLogEntity();
            log.setSimId(id);
            log.setMachineId(machineId);
            log.setChangedAt(LocalDateTime.now());
            log.setChangedBy(updatedBy);
            log.setAction(machineId != null ? SimBindingLogEntity.SimBindingAction.BIND : SimBindingLogEntity.SimBindingAction.UNBIND);
            bindingLogRepo.save(log);
            e.setMachineId(machineId);
        }
        if (cardNo != null && !cardNo.isBlank()) {
            if (!cardNo.trim().equals(e.getCardNo()) && simCardRepo.existsByCardNo(cardNo.trim())) {
                throw new IllegalArgumentException("cardNo already exists: " + cardNo);
            }
            e.setCardNo(cardNo.trim());
        }
        if (operator != null) e.setOperator(operator);
        if (planInfo != null) e.setPlanInfo(planInfo);
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return simCardRepo.save(e);
    }

    @Transactional(readOnly = true)
    public Page<SimCardEntity> page(String keyword, MasterDataStatus status, Pageable pageable) {
        Specification<SimCardEntity> spec = (root, query, cb) -> {
            var p = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.get("cardNo")), k));
            }
            if (status != null) p = cb.and(p, cb.equal(root.get("status"), status));
            return p;
        };
        return simCardRepo.findAll(spec, pageable);
    }

    @Transactional
    public SimCardEntity disable(Long id, String updatedBy) {
        SimCardEntity e = simCardRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("SimCard not found: id=" + id));
        e.setStatus(MasterDataStatus.DISABLE);
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return simCardRepo.save(e);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<SimCardEntity> findById(Long id) {
        return simCardRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public List<SimBindingLogEntity> getBindingHistory(Long simId, Pageable pageable) {
        return bindingLogRepo.findBySimIdOrderByChangedAtDesc(simId, pageable);
    }
}
