package com.dfbs.app.application.masterdata;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.dfbs.app.application.bom.BomService;
import com.dfbs.app.modules.bom.BomItemEntity;
import com.dfbs.app.modules.masterdata.PartEntity;
import com.dfbs.app.modules.masterdata.PartRepo;
import com.dfbs.app.modules.masterdata.ProductBomEntity;
import com.dfbs.app.modules.masterdata.ProductBomRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PartBomService {

    private final PartRepo partRepo;
    private final ProductBomRepo bomRepo;
    private final BomService bomService;

    public PartBomService(PartRepo partRepo, ProductBomRepo bomRepo, BomService bomService) {
        this.partRepo = partRepo;
        this.bomRepo = bomRepo;
        this.bomService = bomService;
    }

    // ========== Part CRUD ==========

    @Transactional
    public PartEntity createPart(String name, String spec, String unit) {
        PartEntity entity = new PartEntity();
        entity.setName(name);
        entity.setSpec(spec);
        entity.setUnit(unit != null ? unit : "个");
        entity.setIsActive(true);
        return partRepo.save(entity);
    }

    @Transactional
    public PartEntity updatePart(Long id, String name, String spec, String unit) {
        PartEntity entity = partRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Part not found: id=" + id));
        
        entity.setName(name);
        entity.setSpec(spec);
        if (unit != null) {
            entity.setUnit(unit);
        }
        return partRepo.save(entity);
    }

    @Transactional(readOnly = true)
    public List<PartEntity> listParts() {
        return partRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<PartEntity> listActiveParts() {
        return partRepo.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public PartEntity getPart(Long id) {
        return partRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Part not found: id=" + id));
    }

    /** Generate unique system number: PT-YYYYMMDD-NNN (sequence per day). */
    @Transactional(readOnly = true)
    public String generateSystemNo() {
        String prefix = "PT-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        long count = partRepo.countBySystemNoStartingWith(prefix);
        return prefix + String.format("%03d", count + 1);
    }

    /**
     * Import parts from Excel. Columns: Name, Spec, Price, DrawingNo (optional).
     * Create if not exists (by Name+Spec or DrawingNo). Generate SystemNo for new parts.
     */
    @Transactional
    public PartImportResult importParts(InputStream file) {
        List<PartImportExcelRow> rows = new ArrayList<>();
        EasyExcel.read(file, PartImportExcelRow.class, new ReadListener<PartImportExcelRow>() {
            @Override
            public void invoke(PartImportExcelRow data, AnalysisContext context) {
                if (data == null || data.isBlank()) return;
                rows.add(data);
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).sheet().doRead();

        List<String> errors = new ArrayList<>();
        int created = 0;
        int updated = 0;
        String prefix = "PT-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        long baseSeq = partRepo.countBySystemNoStartingWith(prefix) + 1;
        int newPartIndex = 0;
        for (int i = 0; i < rows.size(); i++) {
            PartImportExcelRow row = rows.get(i);
            int excelRow = i + 2;
            String name = blankToNull(row.getName());
            String spec = blankToNull(row.getSpec());
            String priceStr = blankToNull(row.getPrice());
            String drawingNo = blankToNull(row.getDrawingNo());

            if (name == null) {
                errors.add("Row " + excelRow + ": Name missing");
                continue;
            }
            if (spec == null) spec = "";
            if (priceStr == null) {
                errors.add("Row " + excelRow + ": Price missing");
                continue;
            }
            BigDecimal price;
            try {
                price = new BigDecimal(priceStr.trim());
            } catch (NumberFormatException e) {
                errors.add("Row " + excelRow + ": Price invalid");
                continue;
            }

            Optional<PartEntity> existing = drawingNo != null && !drawingNo.isBlank()
                    ? partRepo.findByDrawingNo(drawingNo.trim())
                    : partRepo.findByNameAndSpec(name.trim(), spec);

            if (existing.isPresent()) {
                PartEntity p = existing.get();
                p.setSalesPrice(price);
                if (drawingNo != null && !drawingNo.isBlank()) p.setDrawingNo(drawingNo.trim());
                partRepo.save(p);
                updated++;
            } else {
                PartEntity entity = new PartEntity();
                entity.setSystemNo(prefix + String.format("%03d", baseSeq + newPartIndex));
                newPartIndex++;
                entity.setName(name.trim());
                entity.setSpec(spec);
                entity.setDrawingNo(drawingNo != null ? drawingNo.trim() : null);
                entity.setSalesPrice(price);
                entity.setUnit("个");
                entity.setIsActive(true);
                partRepo.save(entity);
                created++;
            }
        }
        return new PartImportResult(created, updated, errors);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    /**
     * Price deviation: compare standard (part.salesPrice RMB) with actualPrice * exchangeRate (RMB).
     * Returns true if they differ.
     */
    @Transactional(readOnly = true)
    public boolean isPriceDeviated(Long partId, BigDecimal actualPrice, BigDecimal exchangeRate) {
        PartEntity part = partRepo.findById(partId)
                .orElseThrow(() -> new IllegalStateException("Part not found: id=" + partId));
        BigDecimal standardPriceRmb = part.getSalesPrice() != null ? part.getSalesPrice() : BigDecimal.ZERO;
        BigDecimal actualPriceRmb = actualPrice != null && exchangeRate != null
                ? actualPrice.multiply(exchangeRate)
                : BigDecimal.ZERO;
        return standardPriceRmb.compareTo(actualPriceRmb) != 0;
    }

    @Transactional(readOnly = true)
    public List<PartEntity> searchParts(String name, String spec, String drawingNo) {
        String n = (name != null && !name.isBlank()) ? name.trim() : null;
        String s = (spec != null && !spec.isBlank()) ? spec.trim() : null;
        String d = (drawingNo != null && !drawingNo.isBlank()) ? drawingNo.trim() : null;
        return partRepo.search(n, s, d);
    }

    /**
     * Search parts: if machineId present, restrict to BOM parts for that machine and filter by keyword (name);
     * otherwise global search by keyword (name).
     */
    @Transactional(readOnly = true)
    public List<PartEntity> searchWithMachine(String keyword, Long machineId) {
        if (machineId != null) {
            List<Long> partIds = bomService.getActiveBom(machineId)
                    .map(v -> bomService.getBomItems(v.getId()).stream()
                            .map(BomItemEntity::getPartId)
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
            if (partIds.isEmpty()) {
                return Collections.emptyList();
            }
            if (keyword == null || keyword.isBlank()) {
                return partRepo.findAllById(partIds);
            }
            return partRepo.findByIdInAndNameContainingIgnoreCase(partIds, keyword.trim());
        }
        if (keyword == null || keyword.isBlank()) {
            return partRepo.findByIsActiveTrue();
        }
        return partRepo.search(keyword.trim(), null, null);
    }

    @Transactional
    public void disablePart(Long id, Long replacementPartId) {
        if (replacementPartId == null) {
            throw new IllegalStateException("replacementPartId is required when disabling a part");
        }
        
        PartEntity part = partRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Part not found: id=" + id));
        
        PartEntity replacement = partRepo.findById(replacementPartId)
                .orElseThrow(() -> new IllegalStateException("Replacement part not found: id=" + replacementPartId));
        
        if (!replacement.getIsActive()) {
            throw new IllegalStateException("Replacement part must be active");
        }
        
        if (id.equals(replacementPartId)) {
            throw new IllegalStateException("Cannot replace part with itself");
        }

        part.setIsActive(false);
        part.setReplacementPartId(replacementPartId);
        partRepo.save(part);
    }

    // ========== BOM Management ==========

    @Transactional(readOnly = true)
    public List<ProductBomEntity> getBomByProduct(UUID productId) {
        return bomRepo.findByProductId(productId);
    }

    @Transactional
    public void manageBom(UUID productId, List<Long> partIds) {
        // Remove existing BOM entries for this product
        List<ProductBomEntity> existing = bomRepo.findByProductId(productId);
        bomRepo.deleteAll(existing);

        // Add new BOM entries
        for (Long partId : partIds) {
            PartEntity part = partRepo.findById(partId)
                    .orElseThrow(() -> new IllegalStateException("Part not found: id=" + partId));
            
            if (!part.getIsActive()) {
                throw new IllegalStateException("Part is not active: id=" + partId);
            }

            ProductBomEntity bom = new ProductBomEntity();
            bom.setProductId(productId);
            bom.setPartId(partId);
            bom.setQty(1);  // Default qty, can be updated later if needed
            bomRepo.save(bom);
        }
    }

    @Transactional
    public void addBomEntry(UUID productId, Long partId, Integer qty) {
        PartEntity part = partRepo.findById(partId)
                .orElseThrow(() -> new IllegalStateException("Part not found: id=" + partId));
        
        if (!part.getIsActive()) {
            throw new IllegalStateException("Part is not active: id=" + partId);
        }

        // Check if entry already exists
        List<ProductBomEntity> existing = bomRepo.findByProductId(productId);
        boolean exists = existing.stream().anyMatch(bom -> bom.getPartId().equals(partId));
        if (exists) {
            throw new IllegalStateException("BOM entry already exists for product and part");
        }

        ProductBomEntity bom = new ProductBomEntity();
        bom.setProductId(productId);
        bom.setPartId(partId);
        bom.setQty(qty != null ? qty : 1);
        bomRepo.save(bom);
    }

    @Transactional
    public void removeBomEntry(UUID productId, Long partId) {
        List<ProductBomEntity> entries = bomRepo.findByProductId(productId);
        ProductBomEntity toRemove = entries.stream()
                .filter(bom -> bom.getPartId().equals(partId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("BOM entry not found"));
        
        bomRepo.delete(toRemove);
    }
}
