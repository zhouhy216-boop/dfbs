package com.dfbs.app.application.masterdata;

import com.dfbs.app.modules.masterdata.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SparePartMasterDataService {

    private final SparePartRepo sparePartRepo;

    public SparePartMasterDataService(SparePartRepo sparePartRepo) {
        this.sparePartRepo = sparePartRepo;
    }

    private static void requireNotBlank(String s, String field) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(field + " is required");
    }

    @Transactional
    public SparePartEntity save(String partNo, String name, String spec, String unit, BigDecimal referencePrice, String createdBy) {
        requireNotBlank(partNo, "partNo");
        requireNotBlank(name, "name");
        if (sparePartRepo.existsByPartNo(partNo.trim())) {
            throw new IllegalArgumentException("partNo already exists: " + partNo);
        }
        SparePartEntity e = new SparePartEntity();
        e.setPartNo(partNo.trim());
        e.setName(name.trim());
        e.setSpec(spec);
        e.setUnit(unit != null ? unit : "个");
        e.setReferencePrice(referencePrice);
        e.setStatus(MasterDataStatus.ENABLE);
        LocalDateTime now = LocalDateTime.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setCreatedBy(createdBy);
        e.setUpdatedBy(createdBy);
        return sparePartRepo.save(e);
    }

    @Transactional
    public SparePartEntity update(Long id, String partNo, String name, String spec, String unit, BigDecimal referencePrice, String updatedBy) {
        SparePartEntity e = sparePartRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("SparePart not found: id=" + id));
        if (e.getStatus() == MasterDataStatus.DISABLE) {
            throw new IllegalStateException("Cannot update disabled spare part");
        }
        if (partNo != null && !partNo.isBlank()) {
            if (!partNo.trim().equals(e.getPartNo()) && sparePartRepo.existsByPartNo(partNo.trim())) {
                throw new IllegalArgumentException("partNo already exists: " + partNo);
            }
            e.setPartNo(partNo.trim());
        }
        if (name != null && !name.isBlank()) e.setName(name.trim());
        if (spec != null) e.setSpec(spec);
        if (unit != null) e.setUnit(unit);
        if (referencePrice != null) e.setReferencePrice(referencePrice);
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return sparePartRepo.save(e);
    }

    @Transactional(readOnly = true)
    public Page<SparePartEntity> page(String keyword, MasterDataStatus status, Pageable pageable) {
        Specification<SparePartEntity> spec = (root, query, cb) -> {
            var p = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.toLowerCase() + "%";
                p = cb.and(p, cb.or(
                    cb.like(cb.lower(root.get("partNo")), k),
                    cb.like(cb.lower(root.get("name")), k)
                ));
            }
            if (status != null) p = cb.and(p, cb.equal(root.get("status"), status));
            return p;
        };
        return sparePartRepo.findAll(spec, pageable);
    }

    @Transactional
    public SparePartEntity disable(Long id, String updatedBy) {
        SparePartEntity e = sparePartRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("SparePart not found: id=" + id));
        e.setStatus(MasterDataStatus.DISABLE);
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return sparePartRepo.save(e);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<SparePartEntity> findById(Long id) {
        return sparePartRepo.findById(id);
    }
}
