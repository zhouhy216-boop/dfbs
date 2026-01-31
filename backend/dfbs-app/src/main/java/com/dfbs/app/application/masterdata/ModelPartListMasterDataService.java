package com.dfbs.app.application.masterdata;

import com.dfbs.app.modules.masterdata.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ModelPartListMasterDataService {

    private final ModelPartListRepo modelPartListRepo;

    public ModelPartListMasterDataService(ModelPartListRepo modelPartListRepo) {
        this.modelPartListRepo = modelPartListRepo;
    }

    private static void requireNotBlank(String s, String field) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(field + " is required");
    }

    @Transactional
    public ModelPartListEntity save(Long modelId, String version, LocalDate effectiveDate, String items, String createdBy) {
        if (modelId == null) throw new IllegalArgumentException("modelId is required");
        requireNotBlank(version, "version");
        if (items == null || items.isBlank()) throw new IllegalArgumentException("items is required");
        ModelPartListEntity e = new ModelPartListEntity();
        e.setModelId(modelId);
        e.setVersion(version.trim());
        e.setEffectiveDate(effectiveDate);
        e.setItems(items.trim());
        e.setStatus(BomStatus.DRAFT);
        LocalDateTime now = LocalDateTime.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setCreatedBy(createdBy);
        e.setUpdatedBy(createdBy);
        return modelPartListRepo.save(e);
    }

    @Transactional
    public ModelPartListEntity update(Long id, Long modelId, String version, LocalDate effectiveDate, String items, String updatedBy) {
        ModelPartListEntity e = modelPartListRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("ModelPartList not found: id=" + id));
        if (e.getStatus() == BomStatus.DEPRECATED) {
            throw new IllegalStateException("Cannot update deprecated model part list");
        }
        if (modelId != null) e.setModelId(modelId);
        if (version != null && !version.isBlank()) e.setVersion(version.trim());
        if (effectiveDate != null) e.setEffectiveDate(effectiveDate);
        if (items != null && !items.isBlank()) e.setItems(items.trim());
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return modelPartListRepo.save(e);
    }

    @Transactional(readOnly = true)
    public Page<ModelPartListEntity> page(String keyword, BomStatus status, Long modelId, Pageable pageable) {
        Specification<ModelPartListEntity> spec = (root, query, cb) -> {
            var p = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.get("version")), k));
            }
            if (status != null) p = cb.and(p, cb.equal(root.get("status"), status));
            if (modelId != null) p = cb.and(p, cb.equal(root.get("modelId"), modelId));
            return p;
        };
        return modelPartListRepo.findAll(spec, pageable);
    }

    @Transactional
    public ModelPartListEntity disable(Long id, String updatedBy) {
        ModelPartListEntity e = modelPartListRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("ModelPartList not found: id=" + id));
        e.setStatus(BomStatus.DEPRECATED);
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return modelPartListRepo.save(e);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<ModelPartListEntity> findById(Long id) {
        return modelPartListRepo.findById(id);
    }
}
