package com.dfbs.app.application.bom;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.dfbs.app.modules.bom.BomItemEntity;
import com.dfbs.app.modules.bom.BomItemRepo;
import com.dfbs.app.modules.bom.BomVersionEntity;
import com.dfbs.app.modules.bom.BomVersionRepo;
import com.dfbs.app.modules.masterdata.PartEntity;
import com.dfbs.app.modules.masterdata.PartRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BomService {

    private final BomVersionRepo bomVersionRepo;
    private final BomItemRepo bomItemRepo;
    private final PartRepo partRepo;

    public BomService(BomVersionRepo bomVersionRepo, BomItemRepo bomItemRepo, PartRepo partRepo) {
        this.bomVersionRepo = bomVersionRepo;
        this.bomItemRepo = bomItemRepo;
        this.partRepo = partRepo;
    }

    /**
     * Import BOM from Excel for a machine. Creates new version (max+1), deactivates previous active.
     * Rows: IndexNo, PartName, Spec (optional), DrawingNo (optional), Quantity, IsOptional, Remark.
     * Part must exist (by Name+Spec or DrawingNo). IndexNo must be unique within this version.
     */
    @Transactional
    public BomVersionEntity importBom(InputStream file, Long machineId) {
        List<BomImportExcelRow> rows = new ArrayList<>();
        EasyExcel.read(file, BomImportExcelRow.class, new ReadListener<BomImportExcelRow>() {
            @Override
            public void invoke(BomImportExcelRow data, AnalysisContext context) {
                if (data == null || data.isBlank()) return;
                rows.add(data);
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).sheet().doRead();

        int nextVersion = bomVersionRepo.findFirstByMachineIdOrderByVersionDesc(machineId)
                .map(v -> v.getVersion() + 1)
                .orElse(1);

        bomVersionRepo.findByMachineIdAndIsActiveTrue(machineId).ifPresent(active -> {
            active.setIsActive(false);
            bomVersionRepo.save(active);
        });

        BomVersionEntity versionEntity = new BomVersionEntity();
        versionEntity.setMachineId(machineId);
        versionEntity.setVersion(nextVersion);
        versionEntity.setIsActive(true);
        versionEntity.setDescription("Imported at " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        versionEntity.setCreatedAt(LocalDateTime.now());
        versionEntity.setCreatedBy(null);
        versionEntity = bomVersionRepo.save(versionEntity);

        Long versionId = versionEntity.getId();
        Set<String> seenIndexNos = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            BomImportExcelRow row = rows.get(i);
            int excelRow = i + 2;
            String indexNo = blankToNull(row.getIndexNo());
            String partName = blankToNull(row.getPartName());
            String spec = blankToNull(row.getSpec());
            String drawingNo = blankToNull(row.getDrawingNo());
            String qtyStr = blankToNull(row.getQuantity());
            String isOptStr = blankToNull(row.getIsOptional());
            String remark = blankToNull(row.getRemark());

            if (indexNo == null) throw new IllegalStateException("Row " + excelRow + ": IndexNo missing");
            if (partName == null) throw new IllegalStateException("Row " + excelRow + ": PartName missing");
            if (seenIndexNos.contains(indexNo.trim()))
                throw new IllegalStateException("Row " + excelRow + ": Duplicate IndexNo: " + indexNo);
            seenIndexNos.add(indexNo.trim());

            Optional<PartEntity> partOpt = drawingNo != null && !drawingNo.isBlank()
                    ? partRepo.findByDrawingNo(drawingNo.trim())
                    : partRepo.findByNameAndSpec(partName.trim(), spec != null ? spec : "");
            PartEntity part = partOpt.orElseThrow(() ->
                    new IllegalStateException("Part not found: [" + partName + "]"));

            BigDecimal quantity = BigDecimal.ONE;
            if (qtyStr != null && !qtyStr.isBlank()) {
                try {
                    quantity = new BigDecimal(qtyStr.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Row " + excelRow + ": Quantity invalid");
                }
            }
            boolean isOptional = parseBoolean(isOptStr, false);

            BomItemEntity item = new BomItemEntity();
            item.setVersionId(versionId);
            item.setPartId(part.getId());
            item.setIndexNo(indexNo.trim());
            item.setQuantity(quantity);
            item.setIsOptional(isOptional);
            item.setRemark(remark);
            bomItemRepo.save(item);
        }

        return versionEntity;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private static boolean parseBoolean(String s, boolean defaultValue) {
        if (s == null || s.isBlank()) return defaultValue;
        String t = s.trim().toLowerCase();
        if ("true".equals(t) || "1".equals(t) || "是".equals(t) || "yes".equals(t)) return true;
        if ("false".equals(t) || "0".equals(t) || "否".equals(t) || "no".equals(t)) return false;
        return defaultValue;
    }

    @Transactional(readOnly = true)
    public Optional<BomVersionEntity> getActiveBom(Long machineId) {
        return bomVersionRepo.findByMachineIdAndIsActiveTrue(machineId);
    }

    @Transactional(readOnly = true)
    public List<BomVersionEntity> getBomHistory(Long machineId) {
        return bomVersionRepo.findByMachineIdOrderByVersionDesc(machineId);
    }

    @Transactional(readOnly = true)
    public List<BomItemEntity> getBomItems(Long versionId) {
        return bomItemRepo.findByVersionIdOrderByIndexNoAsc(versionId);
    }
}
