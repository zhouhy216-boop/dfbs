package com.dfbs.app.interfaces.bom;

import com.dfbs.app.application.bom.BomService;
import com.dfbs.app.modules.bom.BomItemEntity;
import com.dfbs.app.modules.bom.BomVersionEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/bom")
public class BomController {

    private final BomService bomService;

    public BomController(BomService bomService) {
        this.bomService = bomService;
    }

    /**
     * Import BOM from Excel for a machine. Creates new version; machineId as request param.
     */
    @PostMapping("/import")
    public BomVersionEntity importBom(
            @RequestParam("file") MultipartFile file,
            @RequestParam("machineId") Long machineId) {
        try (InputStream is = file.getInputStream()) {
            return bomService.importBom(is, machineId);
        } catch (Exception e) {
            throw new RuntimeException("BOM import failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get active BOM version for a machine.
     */
    @GetMapping("/machine/{machineId}/active")
    public Optional<BomVersionEntity> getActiveBom(@PathVariable Long machineId) {
        return bomService.getActiveBom(machineId);
    }

    /**
     * Get BOM version history for a machine (ordered by version desc).
     */
    @GetMapping("/machine/{machineId}/history")
    public List<BomVersionEntity> getBomHistory(@PathVariable Long machineId) {
        return bomService.getBomHistory(machineId);
    }

    /**
     * Get BOM items for a version.
     */
    @GetMapping("/version/{versionId}/items")
    public List<BomItemEntity> getBomItems(@PathVariable Long versionId) {
        return bomService.getBomItems(versionId);
    }
}
