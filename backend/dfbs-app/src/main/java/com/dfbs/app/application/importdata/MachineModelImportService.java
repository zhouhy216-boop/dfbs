package com.dfbs.app.application.importdata;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.dfbs.app.application.importdata.dto.*;
import com.dfbs.app.modules.masterdata.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MachineModelImportService implements ImportServiceDelegate {

    private final MachineModelRepo machineModelRepo;
    private final ObjectMapper objectMapper;

    public MachineModelImportService(MachineModelRepo machineModelRepo, ObjectMapper objectMapper) {
        this.machineModelRepo = machineModelRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportResultDto importFromExcel(InputStream file) {
        List<MachineModelImportRow> rows = new ArrayList<>();
        EasyExcel.read(file, MachineModelImportRow.class, new ReadListener<MachineModelImportRow>() {
            @Override
            public void invoke(MachineModelImportRow data, AnalysisContext context) {
                if (data == null || data.isBlank()) return;
                rows.add(data);
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).sheet().doRead();

        int successCount = 0;
        List<ImportFailureDto> failures = new ArrayList<>();
        List<ImportConflictDto> conflicts = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            MachineModelImportRow row = rows.get(i);
            int rowNum = i + 2;

            String modelName = blankToNull(row.getModelName());
            String modelNo = blankToNull(row.getModelNo());

            if (modelName == null || modelName.isBlank()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(modelNo != null ? modelNo : "").reason("型号名称必填").build());
                continue;
            }
            if (modelNo == null || modelNo.isBlank()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(modelName).reason("型号编号必填").build());
                continue;
            }
            modelName = modelName.trim();
            modelNo = modelNo.trim();

            if (machineModelRepo.existsByModelNo(modelNo)) {
                Optional<MachineModelEntity> existingOpt = machineModelRepo.findByModelNo(modelNo);
                try {
                    String originalJson = objectMapper.writeValueAsString(toMap(existingOpt.get()));
                    String importJson = objectMapper.writeValueAsString(Map.of("modelName", modelName, "modelNo", modelNo));
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(modelNo).originalData(originalJson).importData(importJson).build());
                } catch (Exception e) {
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(modelNo).originalData("{}").importData("{\"modelNo\":\"" + modelNo + "\"}").build());
                }
                continue;
            }

            MachineModelEntity e = new MachineModelEntity();
            e.setModelName(modelName);
            e.setModelNo(modelNo);
            e.setFreightInfo(blankToNull(row.getFreightInfo()));
            e.setWarrantyInfo(blankToNull(row.getWarrantyInfo()));
            e.setStatus(MasterDataStatus.ENABLE);
            LocalDateTime now = LocalDateTime.now();
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            e.setCreatedBy("import");
            e.setUpdatedBy("import");
            machineModelRepo.save(e);
            successCount++;
        }

        return ImportResultDto.builder().successCount(successCount).failureCount(failures.size()).conflictCount(conflicts.size()).failures(failures).conflicts(conflicts).build();
    }

    @Transactional
    public ImportResultDto resolve(List<ImportActionReq> actions) {
        return ImportResultDto.builder().successCount(0).failureCount(0).conflictCount(0).failures(List.of()).conflicts(List.of()).build();
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private static Map<String, Object> toMap(MachineModelEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("modelName", e.getModelName());
        m.put("modelNo", e.getModelNo());
        m.put("status", e.getStatus() != null ? e.getStatus().name() : null);
        return m;
    }
}
