package com.dfbs.app.interfaces.bizperm;

import com.dfbs.app.application.bizperm.BizPermCatalogException;
import com.dfbs.app.application.bizperm.BizPermCatalogService;
import com.dfbs.app.config.PermSuperAdminGuard;
import com.dfbs.app.infra.dto.ErrorResult;
import com.dfbs.app.modules.bizperm.BizPermCatalogNodeEntity;
import com.dfbs.app.modules.bizperm.BizPermOperationPointEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Business Module Catalog API. All endpoints require allowlist super-admin.
 * Read: GET /catalog. Write: nodes (create/update/delete/reorder), op-points (upsert/update/claim/handled-only).
 */
@RestController
@RequestMapping("/api/v1/admin/bizperm/catalog")
public class BizPermCatalogController {

    private final PermSuperAdminGuard permSuperAdminGuard;
    private final BizPermCatalogService catalogService;

    public BizPermCatalogController(PermSuperAdminGuard permSuperAdminGuard,
                                    BizPermCatalogService catalogService) {
        this.permSuperAdminGuard = permSuperAdminGuard;
        this.catalogService = catalogService;
    }

    @ExceptionHandler(BizPermCatalogException.class)
    public ResponseEntity<ErrorResult> handleBizPermCatalogException(BizPermCatalogException ex) {
        return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ex.getMachineCode()));
    }

    @GetMapping
    public ResponseEntity<BizPermCatalogDto.CatalogResponse> getCatalog() {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(catalogService.getCatalog());
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
