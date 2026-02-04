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
public class MachineMasterDataService {

    private final MachineRepo machineRepo;
    private final MachineOwnershipLogRepo ownershipLogRepo;

    public MachineMasterDataService(MachineRepo machineRepo, MachineOwnershipLogRepo ownershipLogRepo) {
        this.machineRepo = machineRepo;
        this.ownershipLogRepo = ownershipLogRepo;
    }

    private static void requireNotBlank(String s, String field) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(field + " is required");
    }

    @Transactional
    public MachineEntity save(String machineNo, String serialNo, Long customerId, Long contractId, Long modelId, String createdBy) {
        requireNotBlank(machineNo, "machineNo");
        requireNotBlank(serialNo, "serialNo");
        if (machineRepo.existsByMachineNo(machineNo.trim())) {
            throw new IllegalArgumentException("machineNo already exists: " + machineNo);
        }
        if (machineRepo.existsBySerialNo(serialNo.trim())) {
            throw new IllegalArgumentException("serialNo already exists: " + serialNo);
        }
        MachineEntity e = new MachineEntity();
        e.setMachineNo(machineNo.trim());
        e.setSerialNo(serialNo.trim());
        e.setCustomerId(customerId);
        e.setContractId(contractId);
        e.setModelId(modelId);
        e.setStatus(MasterDataStatus.ENABLE);
        LocalDateTime now = LocalDateTime.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setCreatedBy(createdBy);
        e.setUpdatedBy(createdBy);
        return machineRepo.save(e);
    }

    @Transactional
    public MachineEntity update(Long id, String machineNo, String serialNo, Long customerId, Long contractId, Long modelId, String updatedBy) {
        MachineEntity e = machineRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Machine not found: id=" + id));
        if (e.getStatus() == MasterDataStatus.DISABLE) {
            throw new IllegalStateException("Cannot update disabled machine");
        }
        Long oldCustomerId = e.getCustomerId();
        if (customerId != null && !customerId.equals(oldCustomerId)) {
            MachineOwnershipLogEntity log = new MachineOwnershipLogEntity();
            log.setMachineId(id);
            log.setOldCustomerId(oldCustomerId);
            log.setNewCustomerId(customerId);
            log.setChangedAt(LocalDateTime.now());
            log.setChangedBy(updatedBy);
            ownershipLogRepo.save(log);
            e.setCustomerId(customerId);
        }
        if (machineNo != null && !machineNo.isBlank()) {
            if (!machineNo.trim().equals(e.getMachineNo()) && machineRepo.existsByMachineNo(machineNo.trim())) {
                throw new IllegalArgumentException("machineNo already exists: " + machineNo);
            }
            e.setMachineNo(machineNo.trim());
        }
        if (serialNo != null && !serialNo.isBlank()) {
            if (!serialNo.trim().equals(e.getSerialNo()) && machineRepo.existsBySerialNo(serialNo.trim())) {
                throw new IllegalArgumentException("serialNo already exists: " + serialNo);
            }
            e.setSerialNo(serialNo.trim());
        }
        if (contractId != null) e.setContractId(contractId);
        if (modelId != null) e.setModelId(modelId);
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return machineRepo.save(e);
    }

    @Transactional(readOnly = true)
    public Page<MachineEntity> page(String keyword, MasterDataStatus status, Pageable pageable) {
        Specification<MachineEntity> spec = (root, query, cb) -> {
            var p = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.toLowerCase() + "%";
                p = cb.and(p, cb.or(
                    cb.like(cb.lower(root.get("machineNo")), k),
                    cb.like(cb.lower(root.get("serialNo")), k)
                ));
            }
            if (status != null) p = cb.and(p, cb.equal(root.get("status"), status));
            p = cb.and(p, cb.equal(root.get("isTemp"), false));
            return p;
        };
        return machineRepo.findAll(spec, pageable);
    }

    @Transactional
    public MachineEntity disable(Long id, String updatedBy) {
        MachineEntity e = machineRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Machine not found: id=" + id));
        e.setStatus(MasterDataStatus.DISABLE);
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return machineRepo.save(e);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<MachineEntity> findById(Long id) {
        return machineRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public List<MachineOwnershipLogEntity> getOwnershipHistory(Long machineId, Pageable pageable) {
        return ownershipLogRepo.findByMachineIdOrderByChangedAtDesc(machineId, pageable);
    }
}
