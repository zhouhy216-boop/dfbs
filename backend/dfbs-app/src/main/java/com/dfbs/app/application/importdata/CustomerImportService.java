package com.dfbs.app.application.importdata;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.dfbs.app.application.importdata.dto.*;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

/**
 * Parse Excel -> Validate -> Check Duplicates (Conflict) -> Insert or Report.
 * Conflict: existing customer by code; do not overwrite on first pass.
 */
@Service
public class CustomerImportService implements ImportServiceDelegate {

    private final CustomerRepo customerRepo;
    private final ObjectMapper objectMapper;

    public CustomerImportService(CustomerRepo customerRepo, ObjectMapper objectMapper) {
        this.customerRepo = customerRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportResultDto importFromExcel(InputStream file) {
        List<CustomerImportRow> rows = new ArrayList<>();

        EasyExcel.read(file, CustomerImportRow.class, new ReadListener<CustomerImportRow>() {
            @Override
            public void invoke(CustomerImportRow data, AnalysisContext context) {
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
            CustomerImportRow row = rows.get(i);
            int rowNum = i + 2; // 1-based, +1 for header

            String name = blankToNull(row.getName());
            String code = blankToNull(row.getCode());

            // Validation: required Name
            if (name == null || name.isBlank()) {
                failures.add(ImportFailureDto.builder()
                        .rowNum(rowNum)
                        .uniqueKey(code != null ? code : "")
                        .reason("名称必填")
                        .build());
                continue;
            }
            name = name.trim();
            String effectiveCode = (code != null && !code.isBlank()) ? code.trim() : ("IMP-" + rowNum);

            // Conflict check: existing by customer code (do not overwrite)
            Optional<CustomerEntity> existingOpt = customerRepo.findByCustomerCode(effectiveCode);
            if (existingOpt.isPresent()) {
                try {
                    String originalJson = objectMapper.writeValueAsString(toMap(existingOpt.get()));
                    String importJson = objectMapper.writeValueAsString(Map.of("name", name, "code", effectiveCode));
                    conflicts.add(ImportConflictDto.builder()
                            .rowNum(rowNum)
                            .uniqueKey(effectiveCode)
                            .originalData(originalJson)
                            .importData(importJson)
                            .build());
                } catch (Exception e) {
                    conflicts.add(ImportConflictDto.builder()
                            .rowNum(rowNum)
                            .uniqueKey(effectiveCode)
                            .originalData("{}")
                            .importData("{\"name\":\"" + name + "\",\"code\":\"" + effectiveCode + "\"}")
                            .build());
                }
                continue;
            }

            // Persist: valid and unique
            CustomerEntity entity = CustomerEntity.create(effectiveCode, name);
            customerRepo.save(entity);
            successCount++;
        }

        return ImportResultDto.builder()
                .successCount(successCount)
                .failureCount(failures.size())
                .conflictCount(conflicts.size())
                .failures(failures)
                .conflicts(conflicts)
                .build();
    }

    /**
     * Resolve conflicts: accept list of actions (SKIP / UPDATE / REUSE). Stub: no-op, returns success.
     */
    @Transactional
    public ImportResultDto resolve(List<ImportActionReq> actions) {
        // Stub: apply updates/skips in a later iteration
        return ImportResultDto.builder()
                .successCount(0)
                .failureCount(0)
                .conflictCount(0)
                .failures(List.of())
                .conflicts(List.of())
                .build();
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private static Map<String, Object> toMap(CustomerEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("customerCode", e.getCustomerCode());
        m.put("name", e.getName());
        m.put("status", e.getStatus());
        return m;
    }
}
