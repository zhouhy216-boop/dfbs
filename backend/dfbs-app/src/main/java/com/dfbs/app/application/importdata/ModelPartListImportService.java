package com.dfbs.app.application.importdata;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.dfbs.app.application.importdata.dto.*;
import com.dfbs.app.application.masterdata.ModelPartListBomService;
import com.dfbs.app.application.masterdata.dto.BomItemDto;
import com.dfbs.app.application.masterdata.dto.CreateDraftRequest;
import com.dfbs.app.modules.masterdata.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BOM import: flat rows (Model Name, Version, Part No, Qty). Group by Model+Version.
 * Conflict: BOM with same model+version already exists and is NOT Draft.
 * Action: Create DRAFT BOM via ModelPartListBomService.createDraft.
 */
@Service
public class ModelPartListImportService implements ImportServiceDelegate {

    private final ModelPartListRepo modelPartListRepo;
    private final ModelPartListBomService bomService;
    private final MachineModelRepo machineModelRepo;
    private final SparePartRepo sparePartRepo;
    private final ObjectMapper objectMapper;

    public ModelPartListImportService(ModelPartListRepo modelPartListRepo, ModelPartListBomService bomService,
                                      MachineModelRepo machineModelRepo, SparePartRepo sparePartRepo,
                                      ObjectMapper objectMapper) {
        this.modelPartListRepo = modelPartListRepo;
        this.bomService = bomService;
        this.machineModelRepo = machineModelRepo;
        this.sparePartRepo = sparePartRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportResultDto importFromExcel(InputStream file) {
        List<ModelPartListImportRow> rows = new ArrayList<>();
        EasyExcel.read(file, ModelPartListImportRow.class, new ReadListener<ModelPartListImportRow>() {
            @Override
            public void invoke(ModelPartListImportRow data, AnalysisContext context) {
                if (data == null || data.isBlank()) return;
                rows.add(data);
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).sheet().doRead();

        int successCount = 0;
        List<ImportFailureDto> failures = new ArrayList<>();
        List<ImportConflictDto> conflicts = new ArrayList<>();

        List<Map.Entry<Integer, ModelPartListImportRow>> indexed = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) indexed.add(Map.entry(i + 2, rows.get(i)));

        Map<String, List<Map.Entry<Integer, ModelPartListImportRow>>> groups = indexed.stream()
                .collect(Collectors.groupingBy(e -> key(e.getValue().getModelName(), e.getValue().getVersion())));

        for (Map.Entry<String, List<Map.Entry<Integer, ModelPartListImportRow>>> entry : groups.entrySet()) {
            List<Map.Entry<Integer, ModelPartListImportRow>> groupEntries = entry.getValue();
            if (groupEntries.isEmpty()) continue;

            int rowNum = groupEntries.stream().mapToInt(Map.Entry::getKey).min().orElse(2);
            List<ModelPartListImportRow> groupRows = groupEntries.stream().map(Map.Entry::getValue).toList();
            ModelPartListImportRow first = groupRows.get(0);
            String modelName = blankToNull(first.getModelName());
            String version = blankToNull(first.getVersion());

            if (modelName == null || modelName.isBlank()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(version != null ? version : "").reason("型号名称必填").build());
                continue;
            }
            if (version == null || version.isBlank()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(modelName).reason("版本号必填").build());
                continue;
            }
            modelName = modelName.trim();
            version = version.trim();

            List<MachineModelEntity> models = machineModelRepo.findByModelName(modelName);
            if (models.isEmpty()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(modelName + "/" + version).reason("引用对象不存在：型号 " + modelName).build());
                continue;
            }
            Long modelId = models.get(0).getId();

            List<ModelPartListEntity> existingBoms = modelPartListRepo.findByModelId(modelId);
            final String versionKey = version;
            Optional<ModelPartListEntity> sameVersion = existingBoms.stream()
                    .filter(b -> versionKey.equals(b.getVersion()) && b.getStatus() != BomStatus.DRAFT)
                    .findFirst();
            if (sameVersion.isPresent()) {
                try {
                    String originalJson = objectMapper.writeValueAsString(toMap(sameVersion.get()));
                    String importJson = objectMapper.writeValueAsString(Map.of("modelName", modelName, "version", version, "rowCount", groupRows.size()));
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(modelName + "/" + version).originalData(originalJson).importData(importJson).build());
                } catch (Exception e) {
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(modelName + "/" + version).originalData("{}").importData("{\"modelName\":\"" + modelName + "\",\"version\":\"" + version + "\"}").build());
                }
                continue;
            }

            List<BomItemDto> items = new ArrayList<>();
            for (ModelPartListImportRow r : groupRows) {
                String partNo = blankToNull(r.getPartNo());
                if (partNo == null || partNo.isBlank()) continue;
                partNo = partNo.trim();
                int qty = r.getQuantity() != null && r.getQuantity() > 0 ? r.getQuantity() : 1;
                String name = sparePartRepo.findByPartNo(partNo).map(SparePartEntity::getName).orElse(partNo);
                items.add(new BomItemDto(partNo, name, qty, ""));
            }
            if (items.isEmpty()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(modelName + "/" + version).reason("该组无有效清单行").build());
                continue;
            }

            try {
                CreateDraftRequest req = new CreateDraftRequest(modelId, version, null, items, "import");
                bomService.createDraft(req);
                successCount++;
            } catch (Exception e) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(modelName + "/" + version).reason(e.getMessage() != null ? e.getMessage() : "创建草稿失败").build());
            }
        }

        return ImportResultDto.builder().successCount(successCount).failureCount(failures.size()).conflictCount(conflicts.size()).failures(failures).conflicts(conflicts).build();
    }

    @Transactional
    public ImportResultDto resolve(List<ImportActionReq> actions) {
        return ImportResultDto.builder().successCount(0).failureCount(0).conflictCount(0).failures(List.of()).conflicts(List.of()).build();
    }

    private static String key(String modelName, String version) {
        return (modelName != null ? modelName : "") + "|" + (version != null ? version : "");
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private static Map<String, Object> toMap(ModelPartListEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("modelId", e.getModelId());
        m.put("version", e.getVersion());
        m.put("status", e.getStatus() != null ? e.getStatus().name() : null);
        return m;
    }
}
