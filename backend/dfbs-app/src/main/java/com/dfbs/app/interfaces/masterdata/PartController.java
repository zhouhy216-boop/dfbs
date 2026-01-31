package com.dfbs.app.interfaces.masterdata;

import com.dfbs.app.application.masterdata.PartBomService;
import com.dfbs.app.application.masterdata.PartImportResult;
import com.dfbs.app.modules.masterdata.PartEntity;
import com.dfbs.app.modules.masterdata.ProductBomEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parts")
public class PartController {

    private final PartBomService partBomService;

    public PartController(PartBomService partBomService) {
        this.partBomService = partBomService;
    }

    /**
     * List all parts.
     */
    @GetMapping
    public List<PartEntity> listParts() {
        return partBomService.listParts();
    }

    /**
     * List only active parts.
     */
    @GetMapping("/active")
    public List<PartEntity> listActiveParts() {
        return partBomService.listActiveParts();
    }

    /**
     * Search parts by name, spec, or drawingNo (optional). If machineId provided, restrict to BOM parts for that machine (keyword = name).
     */
    @GetMapping("/search")
    public List<PartEntity> searchParts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String spec,
            @RequestParam(required = false) String drawingNo,
            @RequestParam(required = false) Long machineId) {
        if (machineId != null) {
            return partBomService.searchWithMachine(keyword != null ? keyword : name, machineId);
        }
        return partBomService.searchParts(name != null ? name : keyword, spec, drawingNo);
    }

    /**
     * Import parts from Excel. Columns: Name, Spec, Price, DrawingNo (optional).
     */
    @PostMapping("/import")
    public PartImportResult importParts(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return partBomService.importParts(is);
        } catch (Exception e) {
            throw new RuntimeException("Import failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get BOM (Bill of Materials) for a product.
     */
    @GetMapping("/bom/{productId}")
    public List<ProductBomEntity> getBomByProduct(@PathVariable UUID productId) {
        return partBomService.getBomByProduct(productId);
    }
}
