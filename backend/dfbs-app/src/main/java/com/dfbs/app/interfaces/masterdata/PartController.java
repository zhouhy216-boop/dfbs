package com.dfbs.app.interfaces.masterdata;

import com.dfbs.app.application.masterdata.PartBomService;
import com.dfbs.app.modules.masterdata.PartEntity;
import com.dfbs.app.modules.masterdata.ProductBomEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Get BOM (Bill of Materials) for a product.
     */
    @GetMapping("/bom/{productId}")
    public List<ProductBomEntity> getBomByProduct(@PathVariable UUID productId) {
        return partBomService.getBomByProduct(productId);
    }
}
