package com.dfbs.app.interfaces.perm;

import com.dfbs.app.application.perm.PermModuleManagementService;
import com.dfbs.app.application.perm.PermModuleManagementService.ActionKeyNotFoundException;
import com.dfbs.app.application.perm.PermModuleManagementService.ModuleHasChildrenException;
import com.dfbs.app.application.perm.PermModuleManagementService.ModuleKeyExistsException;
import com.dfbs.app.application.perm.PermPermissionTreeService;
import com.dfbs.app.application.perm.PermAccountOverrideService;
import com.dfbs.app.application.perm.PermAccountOverrideService.RoleDisabledException;
import com.dfbs.app.application.perm.PermAccountOverrideService.UserNotFoundException;
import com.dfbs.app.application.perm.PermAuditService;
import com.dfbs.app.application.perm.PermRoleService;
import com.dfbs.app.application.perm.PermRoleService.InvalidPermissionKeyException;
import com.dfbs.app.application.perm.PermRoleService.RoleKeyExistsException;
import com.dfbs.app.application.perm.PermRoleService.RoleNotFoundException;
import com.dfbs.app.config.PermSuperAdminGuard;
import com.dfbs.app.modules.perm.PermAuditLogEntity;
import com.dfbs.app.modules.user.UserRepo;
import com.dfbs.app.infra.dto.ErrorResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PERM admin: 角色与权限. All endpoints require PERM super-admin allowlist.
 * GET /me: let frontend check visibility (auth-required); never returns full allowlist.
 * GET /permission-tree: v1 read-only permission model (default actions + module tree).
 * Module management: create/update/delete modules, set module actions.
 * Role management: CRUD roles, GET/PUT role permissions (replace-style).
 */
@RestController
@RequestMapping("/api/v1/admin/perm")
public class PermAdminController {

    private static final String MODULE_KEY_EXISTS = "PERM_MODULE_KEY_EXISTS";
    private static final String MODULE_HAS_CHILDREN = "PERM_MODULE_HAS_CHILDREN";
    private static final String ACTION_KEY_NOT_FOUND = "PERM_ACTION_KEY_NOT_FOUND";
    private static final String ROLE_KEY_EXISTS = "PERM_ROLE_KEY_EXISTS";
    private static final String ROLE_NOT_FOUND = "PERM_ROLE_NOT_FOUND";
    private static final String ROLE_DISABLED = "PERM_ROLE_DISABLED";
    private static final String USER_NOT_FOUND = "PERM_USER_NOT_FOUND";
    private static final String INVALID_PERMISSION_KEY = "PERM_INVALID_PERMISSION_KEY";

    private final PermSuperAdminGuard permSuperAdminGuard;
    private final PermPermissionTreeService permissionTreeService;
    private final PermModuleManagementService moduleManagementService;
    private final PermRoleService roleService;
    private final PermAccountOverrideService accountOverrideService;
    private final UserRepo userRepo;
    private final PermAuditService auditService;

    public PermAdminController(PermSuperAdminGuard permSuperAdminGuard,
                               PermPermissionTreeService permissionTreeService,
                               PermModuleManagementService moduleManagementService,
                               PermRoleService roleService,
                               PermAccountOverrideService accountOverrideService,
                               UserRepo userRepo,
                               PermAuditService auditService) {
        this.permSuperAdminGuard = permSuperAdminGuard;
        this.permissionTreeService = permissionTreeService;
        this.moduleManagementService = moduleManagementService;
        this.roleService = roleService;
        this.accountOverrideService = accountOverrideService;
        this.userRepo = userRepo;
        this.auditService = auditService;
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

    /** GET /api/v1/admin/perm/audit — recent audit log (newest first). Optional: actionType, targetType, targetId, limit (default 50). */
    @GetMapping("/audit")
    public ResponseEntity<List<PermAuditLogEntity>> getAudit(
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        permSuperAdminGuard.requirePermSuperAdmin();
        return ResponseEntity.ok(auditService.getRecent(limit, actionType, targetType, targetId));
    }

    /** POST /api/v1/admin/perm/modules — create module node (optional parentId for child, optional enabled default true). */
    @PostMapping("/modules")
    public ResponseEntity<?> createModule(@RequestBody PermModuleDto.CreateModuleRequest request) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            Boolean enabled = request.enabled() != null ? request.enabled() : true;
            var e = moduleManagementService.create(request.moduleKey(), request.label(), request.parentId(), enabled);
            auditService.log(PermAuditService.ACTION_MODULE_CREATE, PermAuditService.TARGET_MODULE, e.getId(), e.getModuleKey(), null);
            return ResponseEntity.ok(PermModuleDto.ModuleResponse.from(e));
        } catch (ModuleKeyExistsException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), MODULE_KEY_EXISTS));
        }
    }

    /** PUT /api/v1/admin/perm/modules/{id} — update module label, parent, and/or enabled. */
    @PutMapping("/modules/{id}")
    public ResponseEntity<?> updateModule(@PathVariable Long id, @RequestBody PermModuleDto.UpdateModuleRequest request) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            var e = moduleManagementService.update(id, request.label(), request.parentId(), request.enabled());
            String note = request.enabled() != null ? "enabled=" + request.enabled() : null;
            auditService.log(PermAuditService.ACTION_MODULE_UPDATE, PermAuditService.TARGET_MODULE, e.getId(), e.getModuleKey(), note);
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
            auditService.log(PermAuditService.ACTION_MODULE_DELETE, PermAuditService.TARGET_MODULE, id, null, null);
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
            auditService.log(PermAuditService.ACTION_MODULE_ACTIONS_SET, PermAuditService.TARGET_MODULE, id, null,
                    "actionKeys=" + (request.actionKeys() != null ? request.actionKeys().size() : 0));
            return ResponseEntity.noContent().build();
        } catch (ActionKeyNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ACTION_KEY_NOT_FOUND));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /** GET /api/v1/admin/perm/roles — optional ?enabledOnly=true to filter to enabled roles only. */
    @GetMapping("/roles")
    public ResponseEntity<List<PermRoleDto.RoleResponse>> listRoles(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        permSuperAdminGuard.requirePermSuperAdmin();
        List<PermRoleDto.RoleResponse> list = roleService.list(enabledOnly).stream().map(PermRoleDto.RoleResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    /** POST /api/v1/admin/perm/roles */
    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody PermRoleDto.CreateRoleRequest request) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            var e = roleService.create(request.roleKey(), request.label(), request.enabled(), request.description());
            return ResponseEntity.ok(PermRoleDto.RoleResponse.from(e));
        } catch (RoleKeyExistsException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_KEY_EXISTS));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /** PUT /api/v1/admin/perm/roles/{id} */
    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody PermRoleDto.UpdateRoleRequest request) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            var e = roleService.update(id, request.label(), request.enabled());
            return ResponseEntity.ok(PermRoleDto.RoleResponse.from(e));
        } catch (RoleNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_NOT_FOUND));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /** PUT /api/v1/admin/perm/roles/{id}/template — atomic save label + enabled + permissions (replace-style). */
    @PutMapping("/roles/{id}/template")
    public ResponseEntity<?> saveRoleTemplate(@PathVariable Long id, @RequestBody PermRoleDto.TemplateUpdateRequest request) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            var e = roleService.saveTemplate(id, request.label(), request.enabled(), request.permissionKeys(), request.description());
            auditService.log(PermAuditService.ACTION_ROLE_TEMPLATE_SAVE, PermAuditService.TARGET_ROLE, e.getId(), e.getRoleKey(),
                    "enabled=" + e.getEnabled() + ", permissionKeysCount=" + (request.permissionKeys() != null ? request.permissionKeys().size() : 0));
            return ResponseEntity.ok(PermRoleDto.RoleResponse.from(e));
        } catch (RoleNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_NOT_FOUND));
        } catch (InvalidPermissionKeyException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), INVALID_PERMISSION_KEY));
        }
    }

    /** DELETE /api/v1/admin/perm/roles/{id} */
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            roleService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RoleNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_NOT_FOUND));
        }
    }

    /** POST /api/v1/admin/perm/roles/{id}/clone — clone role (label + "-副本", enabled=false, permissions copied). */
    @PostMapping("/roles/{id}/clone")
    public ResponseEntity<?> cloneRole(@PathVariable Long id) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            var e = roleService.clone(id);
            auditService.log(PermAuditService.ACTION_ROLE_TEMPLATE_CLONE, PermAuditService.TARGET_ROLE, e.getId(), e.getRoleKey(),
                    "clonedFrom=" + id);
            return ResponseEntity.ok(PermRoleDto.RoleResponse.from(e));
        } catch (RoleNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_NOT_FOUND));
        }
    }

    /** GET /api/v1/admin/perm/roles/{id}/permissions */
    @GetMapping("/roles/{id}/permissions")
    public ResponseEntity<?> getRolePermissions(@PathVariable Long id) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            List<String> keys = roleService.getPermissions(id);
            return ResponseEntity.ok(new PermRoleDto.RolePermissionsResponse(keys));
        } catch (RoleNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_NOT_FOUND));
        }
    }

    /** PUT /api/v1/admin/perm/roles/{id}/permissions — replace role permissions (keys must be moduleKey:actionKey, both exist). */
    @PutMapping("/roles/{id}/permissions")
    public ResponseEntity<?> setRolePermissions(@PathVariable Long id, @RequestBody PermRoleDto.SetRolePermissionsRequest request) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            roleService.setPermissions(id, request.permissionKeys());
            return ResponseEntity.noContent().build();
        } catch (RoleNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_NOT_FOUND));
        } catch (InvalidPermissionKeyException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), INVALID_PERMISSION_KEY));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /** GET /api/v1/admin/perm/users?query=... — minimal search for account selection (id, username, nickname). */
    @GetMapping("/users")
    public ResponseEntity<List<PermAccountOverrideDto.UserSummary>> searchUsers(
            @RequestParam(required = false, defaultValue = "") String query) {
        permSuperAdminGuard.requirePermSuperAdmin();
        String q = (query != null ? query.trim() : "");
        if (q.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<PermAccountOverrideDto.UserSummary> list = userRepo
                .findTop20ByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCaseOrderByUsername(q, q)
                .stream()
                .map(u -> new PermAccountOverrideDto.UserSummary(u.getId(), u.getUsername(), u.getNickname()))
                .toList();
        return ResponseEntity.ok(list);
    }

    /** GET /api/v1/admin/perm/users/{id} — basic user info for account selection. */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        permSuperAdminGuard.requirePermSuperAdmin();
        var opt = userRepo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResult.of("用户不存在: id=" + id, USER_NOT_FOUND));
        }
        var u = opt.get();
        return ResponseEntity.ok(new PermAccountOverrideDto.UserSummary(u.getId(), u.getUsername(), u.getNickname()));
    }

    /** GET /api/v1/admin/perm/accounts/{userId}/override */
    @GetMapping("/accounts/{userId}/override")
    public ResponseEntity<?> getAccountOverride(@PathVariable Long userId) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            return ResponseEntity.ok(accountOverrideService.getOverride(userId));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), USER_NOT_FOUND));
        }
    }

    /** PUT /api/v1/admin/perm/accounts/{userId}/override — atomic save (template + addKeys + removeKeys). */
    @PutMapping("/accounts/{userId}/override")
    public ResponseEntity<?> saveAccountOverride(@PathVariable Long userId,
                                                 @RequestBody PermAccountOverrideDto.SaveAccountOverrideRequest request) {
        permSuperAdminGuard.requirePermSuperAdmin();
        try {
            var resp = accountOverrideService.saveOverride(userId, request.roleTemplateId(),
                    request.addKeys(), request.removeKeys());
            String targetKey = userRepo.findById(userId).map(u -> u.getUsername()).orElse(null);
            auditService.log(PermAuditService.ACTION_ACCOUNT_OVERRIDE_SAVE, PermAuditService.TARGET_USER, userId, targetKey,
                    "addKeys=" + (request.addKeys() != null ? request.addKeys().size() : 0) + ", removeKeys=" + (request.removeKeys() != null ? request.removeKeys().size() : 0));
            return ResponseEntity.ok(resp);
        } catch (UserNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), USER_NOT_FOUND));
        } catch (RoleNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_NOT_FOUND));
        } catch (RoleDisabledException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_DISABLED));
        } catch (InvalidPermissionKeyException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), INVALID_PERMISSION_KEY));
        }
    }
}
