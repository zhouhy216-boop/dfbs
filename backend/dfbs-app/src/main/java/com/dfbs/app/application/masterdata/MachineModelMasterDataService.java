package com.dfbs.app.application.masterdata;

import com.dfbs.app.modules.masterdata.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MachineModelMasterDataService {

    private final MachineModelRepo machineModelRepo;

    public MachineModelMasterDataService(MachineModelRepo machineModelRepo) {
        this.machineModelRepo = machineModelRepo;
    }

    private static void requireNotBlank(String s, String field) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(field + " is required");
    }

    @Transactional
    public MachineModelEntity save(String modelName, String modelNo, String freightInfo, String warrantyInfo, String createdBy) {
        requireNotBlank(modelNo, "modelNo");
        if (machineModelRepo.existsByModelNo(modelNo.trim())) {
            throw new IllegalArgumentException("modelNo already exists: " + modelNo);
        }
        MachineModelEntity e = new MachineModelEntity();
        e.setModelName(modelName);
        e.setModelNo(modelNo.trim());
        e.setFreightInfo(freightInfo);
        e.setWarrantyInfo(warrantyInfo);
        e.setStatus(MasterDataStatus.ENABLE);
        LocalDateTime now = LocalDateTime.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setCreatedBy(createdBy);
        e.setUpdatedBy(createdBy);
        return machineModelRepo.save(e);
    }

    @Transactional
    public MachineModelEntity update(Long id, String modelName, String modelNo, String freightInfo, String warrantyInfo, String updatedBy) {
        MachineModelEntity e = machineModelRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("MachineModel not found: id=" + id));
        if (e.getStatus() == MasterDataStatus.DISABLE) {
            throw new IllegalStateException("Cannot update disabled machine model");
        }
        if (modelNo != null && !modelNo.isBlank()) {
            if (!modelNo.trim().equals(e.getModelNo()) && machineModelRepo.existsByModelNo(modelNo.trim())) {
                throw new IllegalArgumentException("modelNo already exists: " + modelNo);
            }
            e.setModelNo(modelNo.trim());
        }
        if (modelName != null) e.setModelName(modelName);
        if (freightInfo != null) e.setFreightInfo(freightInfo);
        if (warrantyInfo != null) e.setWarrantyInfo(warrantyInfo);
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return machineModelRepo.save(e);
    }

    @Transactional(readOnly = true)
    public Page<MachineModelEntity> page(String keyword, MasterDataStatus status, Pageable pageable) {
        Specification<MachineModelEntity> spec = (root, query, cb) -> {
            var p = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.toLowerCase() + "%";
                p = cb.and(p, cb.or(
                    cb.like(cb.lower(root.get("modelNo")), k),
                    cb.like(cb.lower(root.get("modelName")), k)
                ));
            }
            if (status != null) p = cb.and(p, cb.equal(root.get("status"), status));
            p = cb.and(p, cb.equal(root.get("isTemp"), false));
            return p;
        };
        return machineModelRepo.findAll(spec, pageable);
    }

    @Transactional
    public MachineModelEntity disable(Long id, String updatedBy) {
        MachineModelEntity e = machineModelRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("MachineModel not found: id=" + id));
        e.setStatus(MasterDataStatus.DISABLE);
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return machineModelRepo.save(e);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<MachineModelEntity> findById(Long id) {
        return machineModelRepo.findById(id);
    }
}
