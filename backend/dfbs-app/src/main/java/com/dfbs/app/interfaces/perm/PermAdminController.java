package com.dfbs.app.interfaces.perm;

import com.dfbs.app.application.perm.PermModuleManagementService;
import com.dfbs.app.application.perm.PermModuleManagementService.ActionKeyNotFoundException;
import com.dfbs.app.application.perm.PermModuleManagementService.ModuleHasChildrenException;
import com.dfbs.app.application.perm.PermModuleManagementService.ModuleKeyExistsException;
import com.dfbs.app.application.perm.PermPermissionTreeService;
import com.dfbs.app.config.PermSuperAdminGuard;
import com.dfbs.app.infra.dto.ErrorResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PERM admin: 角色与权限. All endpoints require PERM super-admin allowlist.
 * GET /me: let frontend check visibility (auth-required); never returns full allowlist.
 * GET /permission-tree: v1 read-only permission model (default actions + module tree).
 * Module management: create/update/delete modules, set module actions.
 */
@RestController
@RequestMapping("/api/v1/admin/perm")
public class PermAdminController {

    private static final String MODULE_KEY_EXISTS = "PERM_MODULE_KEY_EXISTS";
    private static final String MODULE_HAS_CHILDREN = "PERM_MODULE_HAS_CHILDREN";
    private static final String ACTION_KEY_NOT_FOUND = "PERM_ACTION_KEY_NOT_FOUND";

    private final PermSuperAdminGuard permSuperAdminGuard;
    private final PermPermissionTreeService permissionTreeService;
    private final PermModuleManagementService moduleManagementService;

    public PermAdminController(PermSuperAdminGuard permSuperAdminGuard,
                               PermPermissionTreeService permissionTreeService,
                               PermModuleManagementService moduleManagementService) {
        this.permSuperAdminGuard = permSuperAdminGuard;
        this.permissionTreeService = permissionTreeService;
        this.moduleManagementService = moduleManagementService;
    }

    /**
     * GET /api/v1/admin/perm/super-admin/me
     * Response: { "allowed": true/false }. Key omitted or masked; never return full allowlist.
     */
    @GetMapping("/super-admin/me")
    public ResponseEntity<Map<String, Object>> me() {
        boolean allowed = permSuperAdminGuard.isPermSuperAdmin();
        return ResponseEntity.ok(Map.of("allowed", allowed));
    }

    /**
     * GET /api/v1/admin/perm/permission-tree
     * Requires PERM allowlist. Returns 403 "无权限" if not allowlisted.
     * Response: keyFormat, actions (v1 default set), modules (from DB).
     */
    @GetMapping("/permission-tree")
    public ResponseEntity<PermissionTreeDto.PermissionTreeResponse> permissionTree() {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(permissionTreeService.getPermissionTree());
    }

    /** POST /api/v1/admin/perm/modules — create module node (optional parentId for child). */
    @PostMapping("/modules")
    public ResponseEntity<?> createModule(@RequestBody PermModuleDto.CreateModuleRequest request) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            var e = moduleManagementService.create(request.moduleKey(), request.label(), request.parentId());
            return ResponseEntity.ok(PermModuleDto.ModuleResponse.from(e));
        } catch (ModuleKeyExistsException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), MODULE_KEY_EXISTS));
        }
    }

    /** PUT /api/v1/admin/perm/modules/{id} — update module label and/or parent. */
    @PutMapping("/modules/{id}")
    public ResponseEntity<?> updateModule(@PathVariable Long id, @RequestBody PermModuleDto.UpdateModuleRequest request) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            var e = moduleManagementService.update(id, request.label(), request.parentId());
            return ResponseEntity.ok(PermModuleDto.ModuleResponse.from(e));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /** DELETE /api/v1/admin/perm/modules/{id} — delete module (forbidden if has children). */
    @DeleteMapping("/modules/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Long id) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            moduleManagementService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (ModuleHasChildrenException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), MODULE_HAS_CHILDREN));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), "NOT_FOUND"));
        }
    }

    /** PUT /api/v1/admin/perm/modules/{id}/actions — replace module actions (action keys must exist in perm_action). */
    @PutMapping("/modules/{id}/actions")
    public ResponseEntity<?> setModuleActions(@PathVariable Long id, @RequestBody PermModuleDto.SetModuleActionsRequest request) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            moduleManagementService.setModuleActions(id, request.actionKeys());
            return ResponseEntity.noContent().build();
        } catch (ActionKeyNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ACTION_KEY_NOT_FOUND));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), "VALIDATION_ERROR"));
        }
    }
}
