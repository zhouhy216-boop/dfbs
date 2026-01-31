package com.dfbs.app.application.masterdata;

import com.dfbs.app.application.masterdata.dto.BomItemDto;
import com.dfbs.app.application.masterdata.dto.CreateDraftRequest;
import com.dfbs.app.application.masterdata.dto.CreateDraftResponse;
import com.dfbs.app.modules.masterdata.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModelPartListBomService {

    private static final TypeReference<List<Map<String, Object>>> ITEMS_TYPE = new TypeReference<>() {};

    private final ModelPartListRepo modelPartListRepo;
    private final SparePartRepo sparePartRepo;
    private final BomConflictRepo bomConflictRepo;
    private final ObjectMapper objectMapper;

    public ModelPartListBomService(ModelPartListRepo modelPartListRepo, SparePartRepo sparePartRepo,
                                   BomConflictRepo bomConflictRepo, ObjectMapper objectMapper) {
        this.modelPartListRepo = modelPartListRepo;
        this.sparePartRepo = sparePartRepo;
        this.bomConflictRepo = bomConflictRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CreateDraftResponse createDraft(CreateDraftRequest request) {
        if (request.modelId() == null) throw new IllegalArgumentException("modelId is required");
        if (request.version() == null || request.version().isBlank()) throw new IllegalArgumentException("version is required");
        if (request.items() == null || request.items().isEmpty()) throw new IllegalArgumentException("items is required");

        List<Map<String, Object>> itemsJson = new ArrayList<>();
        List<BomConflictEntity> conflicts = new ArrayList<>();
        int createdPartsCount = 0;

        for (int i = 0; i < request.items().size(); i++) {
            BomItemDto item = request.items().get(i);
            String partNo = item.partNo() == null ? "" : item.partNo().trim().toUpperCase();
            String name = item.name() != null ? item.name().trim() : "";
            int qty = item.quantity() != null ? item.quantity() : 1;
            String remark = item.remark() != null ? item.remark() : "";

            if (partNo.isEmpty()) {
                BomConflictEntity conflict = new BomConflictEntity();
                conflict.setBomId(null);
                conflict.setRowPartNo(null);
                conflict.setRowName(name);
                conflict.setRowIndex(i);
                conflict.setType(BomConflictType.MISSING_NO);
                conflict.setStatus(BomConflictStatus.PENDING);
                conflicts.add(conflict);
                itemsJson.add(Map.of("partId", (Object) null, "partNo", "", "name", name, "quantity", qty, "remark", remark));
                continue;
            }

            var existing = sparePartRepo.findByPartNo(partNo);
            Long partId;
            if (existing.isEmpty()) {
                SparePartEntity newPart = new SparePartEntity();
                newPart.setPartNo(partNo);
                newPart.setName(name.isEmpty() ? partNo : name);
                newPart.setStatus(MasterDataStatus.ENABLE);
                LocalDateTime now = LocalDateTime.now();
                newPart.setCreatedAt(now);
                newPart.setUpdatedAt(now);
                newPart.setCreatedBy(request.createdBy());
                newPart = sparePartRepo.save(newPart);
                partId = newPart.getId();
                createdPartsCount++;
            } else {
                SparePartEntity part = existing.get();
                partId = part.getId();
                if (!part.getName().equals(name) && !name.isEmpty()) {
                    BomConflictEntity conflict = new BomConflictEntity();
                    conflict.setRowPartNo(partNo);
                    conflict.setRowName(name);
                    conflict.setRowIndex(i);
                    conflict.setType(BomConflictType.NAME_MISMATCH);
                    conflict.setStatus(BomConflictStatus.PENDING);
                    conflicts.add(conflict);
                }
            }
            itemsJson.add(Map.of("partId", partId, "partNo", partNo, "name", name, "quantity", qty, "remark", remark));
        }

        ModelPartListEntity bom = new ModelPartListEntity();
        bom.setModelId(request.modelId());
        bom.setVersion(request.version().trim());
        bom.setEffectiveDate(request.effectiveDate());
        try {
            bom.setItems(objectMapper.writeValueAsString(itemsJson));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize items", e);
        }
        bom.setStatus(BomStatus.DRAFT);
        LocalDateTime now = LocalDateTime.now();
        bom.setCreatedAt(now);
        bom.setUpdatedAt(now);
        bom.setCreatedBy(request.createdBy());
        bom.setUpdatedBy(request.createdBy());
        bom = modelPartListRepo.save(bom);

        for (BomConflictEntity c : conflicts) {
            c.setBomId(bom.getId());
            bomConflictRepo.save(c);
        }

        return new CreateDraftResponse(bom.getId(), conflicts.size(), createdPartsCount);
    }

    @Transactional
    public ModelPartListEntity publish(Long bomId) {
        ModelPartListEntity bom = modelPartListRepo.findById(bomId)
                .orElseThrow(() -> new IllegalArgumentException("BOM not found: id=" + bomId));
        if (bom.getStatus() != BomStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT BOM can be published");
        }
        if (bomConflictRepo.existsByBomIdAndStatus(bomId, BomConflictStatus.PENDING)) {
            throw new IllegalStateException("Must resolve conflicts first");
        }
        bom.setStatus(BomStatus.PUBLISHED);
        bom.setUpdatedAt(LocalDateTime.now());
        return modelPartListRepo.save(bom);
    }

    @Transactional(readOnly = true)
    public List<BomConflictEntity> getConflicts(Long bomId) {
        return bomConflictRepo.findByBomIdOrderByIdAsc(bomId);
    }

    @Transactional
    public BomConflictEntity resolveConflict(Long conflictId, ResolutionType resolutionType, String customValue) {
        BomConflictEntity conflict = bomConflictRepo.findById(conflictId)
                .orElseThrow(() -> new IllegalArgumentException("Conflict not found: id=" + conflictId));
        if (conflict.getStatus() == BomConflictStatus.RESOLVED) {
            throw new IllegalStateException("Conflict already resolved");
        }

        Long bomId = conflict.getBomId();
        ModelPartListEntity bom = modelPartListRepo.findById(bomId)
                .orElseThrow(() -> new IllegalArgumentException("BOM not found: id=" + bomId));

        switch (resolutionType) {
            case KEEP_MASTER -> { /* no change to SparePart */ }
            case OVERWRITE_MASTER -> {
                if (conflict.getRowPartNo() != null && !conflict.getRowPartNo().isBlank()) {
                    sparePartRepo.findByPartNo(conflict.getRowPartNo()).ifPresent(part -> {
                        part.setName(conflict.getRowName() != null ? conflict.getRowName() : part.getName());
                        part.setUpdatedAt(LocalDateTime.now());
                        sparePartRepo.save(part);
                    });
                }
            }
            case ADD_ALIAS -> {
                if (conflict.getRowPartNo() != null && !conflict.getRowPartNo().isBlank() && conflict.getRowName() != null && !conflict.getRowName().isBlank()) {
                    sparePartRepo.findByPartNo(conflict.getRowPartNo()).ifPresent(part -> {
                        String existing = part.getAliases() != null ? part.getAliases().trim() : "";
                        String added = existing.isEmpty() ? conflict.getRowName() : existing + "," + conflict.getRowName();
                        part.setAliases(added);
                        part.setUpdatedAt(LocalDateTime.now());
                        sparePartRepo.save(part);
                    });
                }
            }
            case FIX_NO -> {
                if (conflict.getType() != BomConflictType.MISSING_NO || customValue == null || customValue.isBlank()) {
                    throw new IllegalArgumentException("FIX_NO requires customValue (new PartNo) for MISSING_NO conflict");
                }
                String newPartNo = customValue.trim().toUpperCase();
                List<Map<String, Object>> items;
                try {
                    items = objectMapper.readValue(bom.getItems(), ITEMS_TYPE);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to parse BOM items", e);
                }
                int idx = conflict.getRowIndex() != null ? conflict.getRowIndex() : -1;
                if (idx < 0 || idx >= items.size()) {
                    conflict.setStatus(BomConflictStatus.RESOLVED);
                    return bomConflictRepo.save(conflict);
                }
                Map<String, Object> item = new LinkedHashMap<>(items.get(idx));
                String name = item.get("name") != null ? item.get("name").toString() : "";
                SparePartEntity part = sparePartRepo.findByPartNo(newPartNo).orElse(null);
                if (part == null) {
                    part = new SparePartEntity();
                    part.setPartNo(newPartNo);
                    part.setName(name.isEmpty() ? newPartNo : name);
                    part.setStatus(MasterDataStatus.ENABLE);
                    LocalDateTime now = LocalDateTime.now();
                    part.setCreatedAt(now);
                    part.setUpdatedAt(now);
                    part = sparePartRepo.save(part);
                }
                item.put("partId", part.getId());
                item.put("partNo", newPartNo);
                item.put("name", part.getName());
                items.set(idx, item);
                try {
                    bom.setItems(objectMapper.writeValueAsString(items));
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to serialize items", e);
                }
                bom.setUpdatedAt(LocalDateTime.now());
                modelPartListRepo.save(bom);
            }
        }

        conflict.setStatus(BomConflictStatus.RESOLVED);
        return bomConflictRepo.save(conflict);
    }
}
