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
public class SparePartImportService implements ImportServiceDelegate {

    private final SparePartRepo sparePartRepo;
    private final ObjectMapper objectMapper;

    public SparePartImportService(SparePartRepo sparePartRepo, ObjectMapper objectMapper) {
        this.sparePartRepo = sparePartRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportResultDto importFromExcel(InputStream file) {
        List<SparePartImportRow> rows = new ArrayList<>();
        EasyExcel.read(file, SparePartImportRow.class, new ReadListener<SparePartImportRow>() {
            @Override
            public void invoke(SparePartImportRow data, AnalysisContext context) {
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
            SparePartImportRow row = rows.get(i);
            int rowNum = i + 2;

            String partNo = blankToNull(row.getPartNo());
            String name = blankToNull(row.getName());

            if (partNo == null || partNo.isBlank()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(name != null ? name : "").reason("图号必填").build());
                continue;
            }
            if (name == null || name.isBlank()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(partNo).reason("名称必填").build());
                continue;
            }
            partNo = partNo.trim();
            name = name.trim();

            if (sparePartRepo.existsByPartNo(partNo)) {
                Optional<SparePartEntity> existingOpt = sparePartRepo.findByPartNo(partNo);
                try {
                    String originalJson = objectMapper.writeValueAsString(toMap(existingOpt.get()));
                    String importJson = objectMapper.writeValueAsString(Map.of("partNo", partNo, "name", name));
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(partNo).originalData(originalJson).importData(importJson).build());
                } catch (Exception ex) {
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(partNo).originalData("{}").importData("{\"partNo\":\"" + partNo + "\"}").build());
                }
                continue;
            }

            SparePartEntity e = new SparePartEntity();
            e.setPartNo(partNo);
            e.setName(name);
            e.setSpec(blankToNull(row.getSpec()));
            e.setUnit(blankToNull(row.getUnit()) != null ? blankToNull(row.getUnit()) : "个");
            e.setStatus(MasterDataStatus.ENABLE);
            LocalDateTime now = LocalDateTime.now();
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            e.setCreatedBy("import");
            e.setUpdatedBy("import");
            sparePartRepo.save(e);
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

    private static Map<String, Object> toMap(SparePartEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("partNo", e.getPartNo());
        m.put("name", e.getName());
        m.put("status", e.getStatus() != null ? e.getStatus().name() : null);
        return m;
    }
}
