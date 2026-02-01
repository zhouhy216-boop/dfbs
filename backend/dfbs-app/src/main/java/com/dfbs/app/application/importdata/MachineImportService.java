package com.dfbs.app.application.importdata;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.dfbs.app.application.importdata.dto.*;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import com.dfbs.app.modules.masterdata.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MachineImportService implements ImportServiceDelegate {

    private final MachineRepo machineRepo;
    private final CustomerRepo customerRepo;
    private final MachineModelRepo machineModelRepo;
    private final ObjectMapper objectMapper;

    public MachineImportService(MachineRepo machineRepo, CustomerRepo customerRepo,
                                MachineModelRepo machineModelRepo, ObjectMapper objectMapper) {
        this.machineRepo = machineRepo;
        this.customerRepo = customerRepo;
        this.machineModelRepo = machineModelRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportResultDto importFromExcel(InputStream file) {
        List<MachineImportRow> rows = new ArrayList<>();
        EasyExcel.read(file, MachineImportRow.class, new ReadListener<MachineImportRow>() {
            @Override
            public void invoke(MachineImportRow data, AnalysisContext context) {
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
            MachineImportRow row = rows.get(i);
            int rowNum = i + 2;

            String machineNo = blankToNull(row.getMachineNo());
            String serialNo = blankToNull(row.getSerialNo());
            String modelName = blankToNull(row.getModelName());
            String customerName = blankToNull(row.getCustomerName());

            if (machineNo == null || machineNo.isBlank()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(serialNo != null ? serialNo : "").reason("机器编号必填").build());
                continue;
            }
            if (serialNo == null || serialNo.isBlank()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(machineNo).reason("序列号必填").build());
                continue;
            }
            machineNo = machineNo.trim();
            serialNo = serialNo.trim();

            if (machineRepo.existsByMachineNo(machineNo) || machineRepo.existsBySerialNo(serialNo)) {
                Optional<MachineEntity> existingOpt = machineRepo.findByMachineNo(machineNo);
                if (existingOpt.isEmpty()) {
                    existingOpt = machineRepo.findBySerialNo(serialNo);
                }
                try {
                    String originalJson = objectMapper.writeValueAsString(toMap(existingOpt.get()));
                    String importJson = objectMapper.writeValueAsString(Map.of("machineNo", machineNo, "serialNo", serialNo, "modelName", modelName != null ? modelName : "", "customerName", customerName != null ? customerName : ""));
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(machineNo).originalData(originalJson).importData(importJson).build());
                } catch (Exception e) {
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(machineNo).originalData("{}").importData("{\"machineNo\":\"" + machineNo + "\"}").build());
                }
                continue;
            }

            Long customerId = null;
            if (customerName != null && !customerName.isBlank()) {
                List<CustomerEntity> customers = customerRepo.findByNameAndDeletedAtIsNull(customerName.trim());
                if (!customers.isEmpty()) customerId = customers.get(0).getId();
            }
            Long modelId = null;
            if (modelName != null && !modelName.isBlank()) {
                List<MachineModelEntity> models = machineModelRepo.findByModelName(modelName.trim());
                if (!models.isEmpty()) modelId = models.get(0).getId();
            }

            MachineEntity e = new MachineEntity();
            e.setMachineNo(machineNo);
            e.setSerialNo(serialNo);
            e.setCustomerId(customerId);
            e.setModelId(modelId);
            e.setStatus(MasterDataStatus.ENABLE);
            LocalDateTime now = LocalDateTime.now();
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            e.setCreatedBy("import");
            e.setUpdatedBy("import");
            machineRepo.save(e);
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

    private static Map<String, Object> toMap(MachineEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("machineNo", e.getMachineNo());
        m.put("serialNo", e.getSerialNo());
        m.put("customerId", e.getCustomerId());
        m.put("modelId", e.getModelId());
        m.put("status", e.getStatus() != null ? e.getStatus().name() : null);
        return m;
    }
}
