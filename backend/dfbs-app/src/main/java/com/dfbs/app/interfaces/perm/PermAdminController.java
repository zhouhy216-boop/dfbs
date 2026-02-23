package com.dfbs.app.interfaces.perm;

import com.dfbs.app.application.perm.PermPermissionTreeService;
import com.dfbs.app.config.PermSuperAdminGuard;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * PERM admin: 角色与权限. All endpoints require PERM super-admin allowlist.
 * GET /me: let frontend check visibility (auth-required); never returns full allowlist.
 * GET /permission-tree: v1 read-only permission model (default actions + module tree).
 */
@RestController
@RequestMapping("/api/v1/admin/perm")
public class PermAdminController {

    private final PermSuperAdminGuard permSuperAdminGuard;
    private final PermPermissionTreeService permissionTreeService;

    public PermAdminController(PermSuperAdminGuard permSuperAdminGuard,
                               PermPermissionTreeService permissionTreeService) {
        this.permSuperAdminGuard = permSuperAdminGuard;
        this.permissionTreeService = permissionTreeService;
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
     * Response: keyFormat, actions (v1 default set), modules (minimal tree).
     */
    @GetMapping("/permission-tree")
    public ResponseEntity<PermissionTreeDto.PermissionTreeResponse> permissionTree() {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(permissionTreeService.getPermissionTree());
    }
}
