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
public class ContractImportService implements ImportServiceDelegate {

    private final ContractRepo contractRepo;
    private final CustomerRepo customerRepo;
    private final ObjectMapper objectMapper;

    public ContractImportService(ContractRepo contractRepo, CustomerRepo customerRepo, ObjectMapper objectMapper) {
        this.contractRepo = contractRepo;
        this.customerRepo = customerRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportResultDto importFromExcel(InputStream file) {
        List<ContractImportRow> rows = new ArrayList<>();
        EasyExcel.read(file, ContractImportRow.class, new ReadListener<ContractImportRow>() {
            @Override
            public void invoke(ContractImportRow data, AnalysisContext context) {
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
            ContractImportRow row = rows.get(i);
            int rowNum = i + 2;

            String contractNo = blankToNull(row.getContractNo());
            String customerName = blankToNull(row.getCustomerName());

            if (contractNo == null || contractNo.isBlank()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey("").reason("合同编号必填").build());
                continue;
            }
            if (customerName == null || customerName.isBlank()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(contractNo).reason("客户名称必填").build());
                continue;
            }
            contractNo = contractNo.trim();
            customerName = customerName.trim();

            List<CustomerEntity> customers = customerRepo.findByNameAndDeletedAtIsNull(customerName);
            if (customers.isEmpty()) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(contractNo).reason("引用对象不存在：" + customerName).build());
                continue;
            }
            if (customers.size() > 1) {
                failures.add(ImportFailureDto.builder().rowNum(rowNum).uniqueKey(contractNo).reason("存在多个同名客户：" + customerName).build());
                continue;
            }
            Long customerId = customers.get(0).getId();

            Optional<ContractEntity> existingOpt = contractRepo.findByContractNo(contractNo);
            if (existingOpt.isPresent()) {
                try {
                    String originalJson = objectMapper.writeValueAsString(toMap(existingOpt.get()));
                    String importJson = objectMapper.writeValueAsString(Map.of("contractNo", contractNo, "customerName", customerName));
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(contractNo).originalData(originalJson).importData(importJson).build());
                } catch (Exception e) {
                    conflicts.add(ImportConflictDto.builder().rowNum(rowNum).uniqueKey(contractNo).originalData("{}").importData("{\"contractNo\":\"" + contractNo + "\"}").build());
                }
                continue;
            }

            ContractEntity e = new ContractEntity();
            e.setContractNo(contractNo);
            e.setCustomerId(customerId);
            e.setAttachment("{}");
            e.setStatus(MasterDataStatus.ENABLE);
            LocalDateTime now = LocalDateTime.now();
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            e.setCreatedBy("import");
            e.setUpdatedBy("import");
            contractRepo.save(e);
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

    private static Map<String, Object> toMap(ContractEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("contractNo", e.getContractNo());
        m.put("customerId", e.getCustomerId());
        m.put("status", e.getStatus() != null ? e.getStatus().name() : null);
        return m;
    }
}
