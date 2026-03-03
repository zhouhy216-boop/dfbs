package com.dfbs.app.interfaces.bizperm;

import com.dfbs.app.application.bizperm.BizPermCatalogImportService;
import com.dfbs.app.application.bizperm.BizPermCatalogException;
import com.dfbs.app.application.bizperm.BizPermCatalogService;
import com.dfbs.app.config.AdminOrSuperAdminGuard;
import com.dfbs.app.config.PermSuperAdminGuard;
import com.dfbs.app.infra.dto.ErrorResult;
import com.dfbs.app.modules.bizperm.BizPermCatalogNodeEntity;
import com.dfbs.app.modules.bizperm.BizPermOperationPointEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Business Module Catalog API. Maintenance endpoints require allowlist super-admin.
 * GET /read: admin-readable (AdminOrSuperAdminGuard), same response as getCatalog().
 */
@RestController
@RequestMapping("/api/v1/admin/bizperm/catalog")
public class BizPermCatalogController {

    private final PermSuperAdminGuard permSuperAdminGuard;
    private final AdminOrSuperAdminGuard adminGuard;
    private final BizPermCatalogService catalogService;
    private final BizPermCatalogImportService importService;

    public BizPermCatalogController(PermSuperAdminGuard permSuperAdminGuard,
                                    AdminOrSuperAdminGuard adminGuard,
                                    BizPermCatalogService catalogService,
                                    BizPermCatalogImportService importService) {
        this.permSuperAdminGuard = permSuperAdminGuard;
        this.adminGuard = adminGuard;
        this.catalogService = catalogService;
        this.importService = importService;
    }

    @ExceptionHandler(BizPermCatalogException.class)
    public ResponseEntity<ErrorResult> handleBizPermCatalogException(BizPermCatalogException ex) {
        return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ex.getMachineCode()));
    }

    @ExceptionHandler(BizPermCatalogImportService.BizPermCatalogImportException.class)
    public ResponseEntity<BizPermCatalogImportDto.ImportValidationErrorResponse> handleImportValidation(
            BizPermCatalogImportService.BizPermCatalogImportException ex) {
        return ResponseEntity.badRequest().body(new BizPermCatalogImportDto.ImportValidationErrorResponse(
                ex.getMessage(), "BIZPERM_IMPORT_VALIDATION", ex.getErrors()));
    }

    @GetMapping
    public ResponseEntity<BizPermCatalogDto.CatalogResponse> getCatalog() {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(catalogService.getCatalog());
    }

    /** Admin-readable catalog (same shape as getCatalog). Non-admin -> 403. */
    @GetMapping("/read")
    public ResponseEntity<BizPermCatalogDto.CatalogResponse> getCatalogRead() {
        adminGuard.requireAdminOrSuperAdmin();
        return ResponseEntity.ok(catalogService.getCatalog());
    }

    /** Import preview: validate XLSX, return valid/summary/errors. No DB write. Allowlist only. */
    @PostMapping("/import/preview")
    public ResponseEntity<BizPermCatalogImportDto.ImportPreviewResponse> importPreview(@RequestParam("file") MultipartFile file) {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(importService.preview(file));
    }

    /** Import apply: validate then apply in one transaction. 400 with CN errors if invalid. Allowlist only. */
    @PostMapping("/import/apply")
    public ResponseEntity<BizPermCatalogImportDto.ImportApplyResponse> importApply(@RequestParam("file") MultipartFile file) {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(importService.apply(file));
    }

    /** Export catalog to XLSX (nodes + op points, CN headers). Allowlist super-admin only. */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCatalog() {
        permSuperAdminGuard.requirePermSuperAdmin();
        BizPermCatalogService.ExportResult result = catalogService.exportToXlsx();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        try {
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(result.filename(), StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
        headers.setContentLength(result.bytes().length);
        return ResponseEntity.ok().headers(headers).body(result.bytes());
    }

    @PostMapping("/nodes")
    public ResponseEntity<BizPermCatalogNodeEntity> createNode(@RequestBody BizPermCatalogDto.CreateNodeRequest req) {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(catalogService.createNode(req));
    }

    @PutMapping("/nodes/{id}")
    public ResponseEntity<BizPermCatalogNodeEntity> updateNode(@PathVariable Long id, @RequestBody BizPermCatalogDto.NodeUpdateRequest req) {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(catalogService.updateNode(id, req));
    }

    @DeleteMapping("/nodes/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable Long id) {
        permSuperAdminGuard.requirePermSuperAdmin();
        catalogService.deleteNode(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/nodes/{id}/reorder-children")
    public ResponseEntity<Void> reorderChildren(@PathVariable Long id, @RequestBody BizPermCatalogDto.ReorderChildrenRequest req) {
        permSuperAdminGuard.requirePermSuperAdmin();
        catalogService.reorderChildren(id, req.orderedIds() != null ? req.orderedIds() : List.of());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/op-points")
    public ResponseEntity<BizPermOperationPointEntity> upsertOpPoint(@RequestBody BizPermCatalogDto.OpPointUpsertRequest req) {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(catalogService.upsertOpPoint(req));
    }

    @PutMapping("/op-points/claim")
    public ResponseEntity<Void> claimOpPoints(@RequestBody BizPermCatalogDto.ClaimOpPointsRequest req) {
        permSuperAdminGuard.requirePermSuperAdmin();
        catalogService.claimOpPoints(req);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/op-points/{id}")
    public ResponseEntity<BizPermOperationPointEntity> updateOpPoint(@PathVariable Long id, @RequestBody BizPermCatalogDto.OpPointUpdateRequest req) {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(catalogService.updateOpPoint(id, req));
    }

    @PutMapping("/op-points/{id}/handled-only")
    public ResponseEntity<BizPermOperationPointEntity> updateHandledOnly(@PathVariable Long id, @RequestBody BizPermCatalogDto.UpdateHandledOnlyRequest req) {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(catalogService.updateHandledOnly(id, req.handledOnly()));
    }
}
