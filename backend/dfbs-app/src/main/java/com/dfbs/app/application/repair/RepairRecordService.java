package com.dfbs.app.application.repair;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.dfbs.app.modules.repair.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RepairRecordService {

    private final RepairRecordRepo repairRecordRepo;

    public RepairRecordService(RepairRecordRepo repairRecordRepo) {
        this.repairRecordRepo = repairRecordRepo;
    }

    /**
     * Import repair records from Excel. Validation, deduplication on oldWorkOrderNo, batch insert.
     */
    @Transactional
    public ImportResult importFromExcel(InputStream file, Long operatorId) {
        List<String> errors = new ArrayList<>();
        List<IndexedRow> rows = new ArrayList<>();

        EasyExcel.read(file, RepairRecordExcelRow.class, new ReadListener<RepairRecordExcelRow>() {
            @Override
            public void invoke(RepairRecordExcelRow data, AnalysisContext context) {
                if (data == null || data.isBlank()) return;
                int rowIndex = context.readRowHolder().getRowIndex();
                rows.add(new IndexedRow(rowIndex, data));
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).sheet().doRead();

        if (rows.isEmpty()) {
            return ImportResult.of(0, 0, List.of("无有效数据行"));
        }

        LocalDateTime now = LocalDateTime.now();
        Set<String> existingOldNos = repairRecordRepo.findByOldWorkOrderNoIn(
                rows.stream().map(r -> r.row().getOldWorkOrderNo().trim()).collect(Collectors.toSet())
        ).stream().map(RepairRecordEntity::getOldWorkOrderNo).collect(Collectors.toSet());

        List<RepairRecordEntity> toSave = new ArrayList<>();
        for (IndexedRow ir : rows) {
            int excelRow = ir.index() + 2;  // 1-based, +1 for header
            RepairRecordExcelRow row = ir.row();

            String customerName = blankToNull(row.getCustomerName());
            String machineNo = blankToNull(row.getMachineNo());
            String machineModel = blankToNull(row.getMachineModel());
            String repairDateStr = blankToNull(row.getRepairDateStr());
            String issueDescription = blankToNull(row.getIssueDescription());
            String resolution = blankToNull(row.getResolution());
            String personInCharge = blankToNull(row.getPersonInCharge());
            String warrantyStatusStr = blankToNull(row.getWarrantyStatus());
            String oldWorkOrderNo = blankToNull(row.getOldWorkOrderNo());

            if (customerName == null) errors.add("Row " + excelRow + ": Customer missing");
            if (machineNo == null) errors.add("Row " + excelRow + ": SN missing");
            if (machineModel == null) errors.add("Row " + excelRow + ": Model missing");
            if (repairDateStr == null) errors.add("Row " + excelRow + ": Date missing");
            if (issueDescription == null) errors.add("Row " + excelRow + ": Issue missing");
            if (resolution == null) errors.add("Row " + excelRow + ": Resolution missing");
            if (personInCharge == null) errors.add("Row " + excelRow + ": Person missing");
            if (warrantyStatusStr == null) errors.add("Row " + excelRow + ": Status missing");
            if (oldWorkOrderNo == null) errors.add("Row " + excelRow + ": OldNo missing");

            if (customerName == null || machineNo == null || machineModel == null || repairDateStr == null
                    || issueDescription == null || resolution == null || personInCharge == null
                    || warrantyStatusStr == null || oldWorkOrderNo == null) {
                continue;
            }

            LocalDateTime repairDate = parseRepairDate(repairDateStr);
            if (repairDate == null) {
                errors.add("Row " + excelRow + ": Date format invalid");
                continue;
            }

            WarrantyStatus warrantyStatus = parseWarrantyStatus(warrantyStatusStr);
            if (warrantyStatus == null) {
                errors.add("Row " + excelRow + ": Status must be IN_WARRANTY/在保 or OUT_WARRANTY/过保");
                continue;
            }

            if (existingOldNos.contains(oldWorkOrderNo.trim())) {
                errors.add("Row " + excelRow + ": Duplicate Old Work Order No");
                continue;
            }

            RepairRecordEntity entity = new RepairRecordEntity();
            entity.setCustomerName(customerName.trim());
            entity.setMachineNo(machineNo.trim());
            entity.setMachineModel(machineModel.trim());
            entity.setRepairDate(repairDate);
            entity.setIssueDescription(issueDescription.trim());
            entity.setResolution(resolution.trim());
            entity.setPersonInCharge(personInCharge.trim());
            entity.setWarrantyStatus(warrantyStatus);
            entity.setOldWorkOrderNo(oldWorkOrderNo.trim());
            entity.setSourceType(RepairSource.HISTORY_IMPORT);
            entity.setWorkOrderId(null);
            entity.setCreatedAt(now);
            entity.setOperatorId(operatorId);
            toSave.add(entity);
            existingOldNos.add(oldWorkOrderNo.trim());
        }

        if (!toSave.isEmpty()) {
            repairRecordRepo.saveAll(toSave);
        }

        int successCount = toSave.size();
        int failureCount = rows.size() - successCount;
        return ImportResult.of(successCount, failureCount, errors);
    }

    @Transactional(readOnly = true)
    public Page<RepairRecordEntity> search(RepairRecordFilterRequest filter) {
        int page = filter.page() != null ? Math.max(0, filter.page()) : 0;
        int size = filter.size() != null ? (filter.size() > 0 ? Math.min(100, filter.size()) : 20) : 20;
        Specification<RepairRecordEntity> spec = RepairRecordSpecification.filter(
                filter.customerName(), filter.machineNo(),
                filter.repairDateFrom(), filter.repairDateTo(),
                filter.warrantyStatus(), filter.personInCharge(), filter.oldWorkOrderNo());
        return repairRecordRepo.findAll(spec, PageRequest.of(page, size));
    }

    /**
     * Placeholder for future Work Order integration.
     */
    public RepairRecordEntity createFromWorkOrder(Long workOrderId, Object params) {
        throw new UnsupportedOperationException("createFromWorkOrder not implemented yet");
    }

    /**
     * Generate sample Excel template bytes for download.
     */
    public byte[] generateTemplate() {
        List<RepairRecordExcelRow> sample = List.of(
                sampleRow("客户A", "SN001", "Model-X", "2024-01-15", "故障描述", "处理结果", "张三", "在保", "OLD-WO-001")
        );
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        EasyExcel.write(out, RepairRecordExcelRow.class).sheet("维修记录").doWrite(sample);
        return out.toByteArray();
    }

    private static RepairRecordExcelRow sampleRow(String customer, String sn, String model, String date,
                                                   String issue, String resolution, String person, String status, String oldNo) {
        RepairRecordExcelRow row = new RepairRecordExcelRow();
        row.setCustomerName(customer);
        row.setMachineNo(sn);
        row.setMachineModel(model);
        row.setRepairDateStr(date);
        row.setIssueDescription(issue);
        row.setResolution(resolution);
        row.setPersonInCharge(person);
        row.setWarrantyStatus(status);
        row.setOldWorkOrderNo(oldNo);
        return row;
    }

    private static String blankToNull(String s) {
        return s != null && !s.isBlank() ? s : null;
    }

    private static LocalDateTime parseRepairDate(String s) {
        if (s == null || s.isBlank()) return null;
        s = s.trim();
        try {
            return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (Exception e2) {
                try {
                    return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    private static WarrantyStatus parseWarrantyStatus(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        if ("IN_WARRANTY".equalsIgnoreCase(t) || "在保".equals(t)) return WarrantyStatus.IN_WARRANTY;
        if ("OUT_WARRANTY".equalsIgnoreCase(t) || "过保".equals(t)) return WarrantyStatus.OUT_WARRANTY;
        return null;
    }

    private record IndexedRow(int index, RepairRecordExcelRow row) {}
}
