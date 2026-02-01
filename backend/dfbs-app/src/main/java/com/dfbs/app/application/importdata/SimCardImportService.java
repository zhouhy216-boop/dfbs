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
public class SimCardImportService implements ImportServiceDelegate {

    private final SimCardRepo simCardRepo;
    private final ObjectMapper objectMapper;

    public SimCardImportService(SimCardRepo simCardRepo, ObjectMapper objectMapper) {
        this.simCardRepo = simCardRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportResultDto importFromExcel(InputStream file) {
        List<SimCardImportRow> rows = new ArrayList<>();
        EasyExcel.read(file, SimCardImportRow.class, new ReadListener<SimCardImportRow>() {
            @Override
            public void invoke(SimCardImportRow data, AnalysisContext context) {
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
            SimCardImportRow row = rows.get(i);
            int rowNum = i + 2;

            String cardNo = blankToNull(row.getCardNo());

            if (cardNo == null || cardNo.isBlank()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey("").reason("卡号必填").build());
                continue;
            }
            cardNo = cardNo.trim();

            if (simCardRepo.findByCardNo(cardNo).isPresent()) {
                Optional<SimCardEntity> existingOpt = simCardRepo.findByCardNo(cardNo);
                try {
                    String originalJson = objectMapper.writeValueAsString(toMap(existingOpt.get()));
                    String importJson = objectMapper.writeValueAsString(Map.of("cardNo", cardNo, "operator", blankToNull(row.getOperator()) != null ? blankToNull(row.getOperator()) : ""));
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(cardNo).originalData(originalJson).importData(importJson).build());
                } catch (Exception e) {
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(cardNo).originalData("{}").importData("{\"cardNo\":\"" + cardNo + "\"}").build());
                }
                continue;
            }

            SimCardEntity e = new SimCardEntity();
            e.setCardNo(cardNo);
            e.setOperator(blankToNull(row.getOperator()));
            e.setPlanInfo(blankToNull(row.getPlanInfo()));
            e.setStatus(MasterDataStatus.ENABLE);
            LocalDateTime now = LocalDateTime.now();
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            e.setCreatedBy("import");
            e.setUpdatedBy("import");
            simCardRepo.save(e);
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

    private static Map<String, Object> toMap(SimCardEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("cardNo", e.getCardNo());
        m.put("operator", e.getOperator());
        m.put("status", e.getStatus() != null ? e.getStatus().name() : null);
        return m;
    }
}
