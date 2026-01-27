package com.dfbs.app.application.masterdata;

import com.dfbs.app.modules.masterdata.PartEntity;
import com.dfbs.app.modules.masterdata.PartRepo;
import com.dfbs.app.modules.masterdata.ProductBomEntity;
import com.dfbs.app.modules.masterdata.ProductBomRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PartBomService {

    private final PartRepo partRepo;
    private final ProductBomRepo bomRepo;

    public PartBomService(PartRepo partRepo, ProductBomRepo bomRepo) {
        this.partRepo = partRepo;
        this.bomRepo = bomRepo;
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
