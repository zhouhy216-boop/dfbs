package com.dfbs.app.interfaces.perm;

import com.dfbs.app.application.account.AdminAccountService;
import com.dfbs.app.application.account.DefaultPasswordService;
import com.dfbs.app.application.account.PersonAlreadyBoundException;
import com.dfbs.app.application.orgstructure.OrgPersonService;
import com.dfbs.app.application.perm.AccountListService;
import com.dfbs.app.application.orgstructure.dto.PersonOptionForBindingDto;
import com.dfbs.app.application.perm.PermAccountOverrideService;
import com.dfbs.app.application.perm.PermAccountOverrideService.RoleDisabledException;
import com.dfbs.app.application.perm.PermAccountOverrideService.UserNotFoundException;
import com.dfbs.app.application.perm.PermAuditService;
import com.dfbs.app.application.perm.PermRoleService;
import com.dfbs.app.application.perm.PermRoleService.InvalidPermissionKeyException;
import com.dfbs.app.application.perm.PermRoleService.RoleKeyExistsException;
import com.dfbs.app.application.perm.PermRoleService.RoleNotFoundException;
import com.dfbs.app.config.AdminOrSuperAdminGuard;
import com.dfbs.app.infra.dto.ErrorResult;
import com.dfbs.app.modules.user.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only APIs for "Account & Permissions" entry (Accounts + Role Templates tabs).
 * Base path: /api/v1/admin/account-permissions. All endpoints require ROLE_ADMIN or ROLE_SUPER_ADMIN.
 * Permission Tree and /api/v1/admin/perm/* remain allowlist-only; no admin-only equivalents for permission-tree or modules.
 */
@RestController
@RequestMapping("/api/v1/admin/account-permissions")
public class AccountPermissionsController {

    private static final String ROLE_KEY_EXISTS = "PERM_ROLE_KEY_EXISTS";
    private static final String ROLE_NOT_FOUND = "PERM_ROLE_NOT_FOUND";
    private static final String ROLE_DISABLED = "PERM_ROLE_DISABLED";
    private static final String USER_NOT_FOUND = "PERM_USER_NOT_FOUND";
    private static final String INVALID_PERMISSION_KEY = "PERM_INVALID_PERMISSION_KEY";
    private static final String ACCTPERM_PERSON_ALREADY_BOUND = "ACCTPERM_PERSON_ALREADY_BOUND";
    private static final String ACCTPERM_USERNAME_EXISTS = "ACCTPERM_USERNAME_EXISTS";
    private static final String ACCTPERM_PERSON_NOT_FOUND = "ACCTPERM_PERSON_NOT_FOUND";
    private static final String ACCTPERM_USER_NOT_FOUND = "ACCTPERM_USER_NOT_FOUND";

    private final AdminOrSuperAdminGuard adminGuard;
    private final UserRepo userRepo;
    private final PermAccountOverrideService accountOverrideService;
    private final PermRoleService roleService;
    private final PermAuditService auditService;
    private final OrgPersonService orgPersonService;
    private final AdminAccountService adminAccountService;
    private final AccountListService accountListService;
    private final DefaultPasswordService defaultPasswordService;

    public AccountPermissionsController(AdminOrSuperAdminGuard adminGuard,
                                       UserRepo userRepo,
                                       PermAccountOverrideService accountOverrideService,
                                       PermRoleService roleService,
                                       PermAuditService auditService,
                                       OrgPersonService orgPersonService,
                                       AdminAccountService adminAccountService,
                                       AccountListService accountListService,
                                       DefaultPasswordService defaultPasswordService) {
        this.adminGuard = adminGuard;
        this.userRepo = userRepo;
        this.accountOverrideService = accountOverrideService;
        this.roleService = roleService;
        this.auditService = auditService;
        this.orgPersonService = orgPersonService;
        this.adminAccountService = adminAccountService;
        this.accountListService = accountListService;
        this.defaultPasswordService = defaultPasswordService;
    }

    // ---------- Default password (admin-managed) ----------

    /** GET default password status. No plaintext. */
    @GetMapping("/auth/default-password/status")
    public ResponseEntity<DefaultPasswordService.DefaultPasswordStatus> getDefaultPasswordStatus() {
        adminGuard.requireAdminOrSuperAdmin();
        return ResponseEntity.ok(defaultPasswordService.getStatus());
    }

    /** PUT default password. Body: { defaultPassword: string }. Audit note: length only, no plaintext. */
    @PutMapping("/auth/default-password")
    public ResponseEntity<?> setDefaultPassword(@RequestBody(required = false) Map<String, String> body) {
        adminGuard.requireAdminOrSuperAdmin();
        String raw = (body != null && body.containsKey("defaultPassword")) ? body.get("defaultPassword") : null;
        if (raw == null || raw.isBlank()) {
            return ResponseEntity.badRequest().body(ErrorResult.of("defaultPassword 必填", "VALIDATION_ERROR"));
        }
        try {
            defaultPasswordService.setDefaultPassword(raw);
            auditService.log(PermAuditService.ACTION_DEFAULT_PASSWORD_CHANGED, PermAuditService.TARGET_SYSTEM, null, "",
                    "length=" + raw.length());
            return ResponseEntity.ok(defaultPasswordService.getStatus());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResult.of(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    // ---------- Accounts tab ----------

    /** Admin-only account list: username, position, department, status, role template. query= empty -> first N; limit 1–200, default 50. */
    @GetMapping("/account-list")
    public ResponseEntity<List<PermAccountOverrideDto.AccountListItemResponse>> getAccountList(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        adminGuard.requireAdminOrSuperAdmin();
        int capped = Math.min(200, Math.max(1, limit));
        List<PermAccountOverrideDto.AccountListItemResponse> list = accountListService.getAccountList(
                query != null && query.trim().isEmpty() ? "" : (query != null ? query.trim() : ""),
                capped);
        return ResponseEntity.ok(list);
    }

    /** Admin-only person options for account binding (does not relax org-structure SuperAdmin gate). */
    @GetMapping("/people")
    public ResponseEntity<List<PersonOptionForBindingDto>> getPeopleOptions(
            @RequestParam(required = false, defaultValue = "") String query) {
        adminGuard.requireAdminOrSuperAdmin();
        String q = (query != null ? query.trim() : "");
        List<PersonOptionForBindingDto> list = orgPersonService.listOptionsForAccountBinding(q.isEmpty() ? null : q);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody PermAccountOverrideDto.CreateAccountRequest request) {
        adminGuard.requireAdminOrSuperAdmin();
        try {
            var summary = adminAccountService.createAccount(
                    request.orgPersonId(),
                    request.username(),
                    request.nickname(),
                    request.roleTemplateId());
            return ResponseEntity.ok(new PermAccountOverrideDto.AccountSummaryResponse(
                    summary.id(), summary.username(), summary.nickname(), summary.enabled(), summary.orgPersonId()));
        } catch (PersonAlreadyBoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ACCTPERM_PERSON_ALREADY_BOUND));
        } catch (AdminAccountService.UsernameExistsException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ACCTPERM_USERNAME_EXISTS));
        } catch (AdminAccountService.UserNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ACCTPERM_USER_NOT_FOUND));
        } catch (AdminAccountService.PersonNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ACCTPERM_PERSON_NOT_FOUND));
        } catch (AdminAccountService.PersonNotActiveException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ACCTPERM_PERSON_NOT_FOUND));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), "VALIDATION_ERROR"));
        }
    }

    @PutMapping("/accounts/{userId}/enabled")
    public ResponseEntity<?> setAccountEnabled(@PathVariable Long userId,
                                               @RequestBody PermAccountOverrideDto.SetEnabledRequest request) {
        adminGuard.requireAdminOrSuperAdmin();
        try {
            adminAccountService.setEnabled(userId, request.enabled());
            String targetKey = userRepo.findById(userId).map(u -> u.getUsername()).orElse(null);
            auditService.log(PermAuditService.ACTION_ACCOUNT_ENABLE_SET, PermAuditService.TARGET_USER, userId, targetKey,
                    "enabled=" + request.enabled());
            return ResponseEntity.ok().build();
        } catch (AdminAccountService.UserNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ACCTPERM_USER_NOT_FOUND));
        }
    }

    @PostMapping("/accounts/{userId}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long userId,
                                           @RequestBody(required = false) PermAccountOverrideDto.ResetPasswordRequest request) {
        adminGuard.requireAdminOrSuperAdmin();
        try {
            String newPassword = (request != null && request.newPassword() != null) ? request.newPassword() : null;
            adminAccountService.resetPassword(userId, newPassword);
            String targetKey = userRepo.findById(userId).map(u -> u.getUsername()).orElse(null);
            auditService.log(PermAuditService.ACTION_ACCOUNT_PASSWORD_RESET, PermAuditService.TARGET_USER, userId, targetKey,
                    newPassword != null && !newPassword.isBlank() ? "password_reset" : "password_reset_to_default");
            return ResponseEntity.ok().build();
        } catch (AdminAccountService.UserNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ACCTPERM_USER_NOT_FOUND));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<PermAccountOverrideDto.UserSummary>> searchUsers(
            @RequestParam(required = false, defaultValue = "") String query) {
        adminGuard.requireAdminOrSuperAdmin();
        String q = (query != null ? query.trim() : "");
        if (q.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<PermAccountOverrideDto.UserSummary> list = userRepo
                .findTop20ByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCaseOrderByUsername(q, q)
                .stream()
                .map(u -> new PermAccountOverrideDto.UserSummary(u.getId(), u.getUsername(), u.getNickname(), u.getEnabled()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        adminGuard.requireAdminOrSuperAdmin();
        var opt = userRepo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResult.of("用户不存在: id=" + id, USER_NOT_FOUND));
        }
        var u = opt.get();
        return ResponseEntity.ok(new PermAccountOverrideDto.UserSummary(u.getId(), u.getUsername(), u.getNickname(), u.getEnabled()));
    }

    @GetMapping("/accounts/{userId}/override")
    public ResponseEntity<?> getAccountOverride(@PathVariable Long userId) {
        adminGuard.requireAdminOrSuperAdmin();
        try {
            return ResponseEntity.ok(accountOverrideService.getOverride(userId));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), USER_NOT_FOUND));
        }
    }

    @PutMapping("/accounts/{userId}/override")
    public ResponseEntity<?> saveAccountOverride(@PathVariable Long userId,
                                                 @RequestBody PermAccountOverrideDto.SaveAccountOverrideRequest request) {
        adminGuard.requireAdminOrSuperAdmin();
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

    // ---------- Role Templates tab ----------

    @GetMapping("/roles")
    public ResponseEntity<List<PermRoleDto.RoleResponse>> listRoles(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        adminGuard.requireAdminOrSuperAdmin();
        List<PermRoleDto.RoleResponse> list = roleService.list(enabledOnly).stream().map(PermRoleDto.RoleResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody PermRoleDto.CreateRoleRequest request) {
        adminGuard.requireAdminOrSuperAdmin();
        try {
            var e = roleService.create(request.roleKey(), request.label(), request.enabled(), request.description());
            return ResponseEntity.ok(PermRoleDto.RoleResponse.from(e));
        } catch (RoleKeyExistsException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_KEY_EXISTS));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), "VALIDATION_ERROR"));
        }
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody PermRoleDto.UpdateRoleRequest request) {
        adminGuard.requireAdminOrSuperAdmin();
        try {
            var e = roleService.update(id, request.label(), request.enabled());
            return ResponseEntity.ok(PermRoleDto.RoleResponse.from(e));
        } catch (RoleNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_NOT_FOUND));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), "VALIDATION_ERROR"));
        }
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        adminGuard.requireAdminOrSuperAdmin();
        try {
            roleService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RoleNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_NOT_FOUND));
        }
    }

    @PostMapping("/roles/{id}/clone")
    public ResponseEntity<?> cloneRole(@PathVariable Long id) {
        adminGuard.requireAdminOrSuperAdmin();
        try {
            var e = roleService.clone(id);
            auditService.log(PermAuditService.ACTION_ROLE_TEMPLATE_CLONE, PermAuditService.TARGET_ROLE, e.getId(), e.getRoleKey(),
                    "clonedFrom=" + id);
            return ResponseEntity.ok(PermRoleDto.RoleResponse.from(e));
        } catch (RoleNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_NOT_FOUND));
        }
    }

    @GetMapping("/roles/{id}/permissions")
    public ResponseEntity<?> getRolePermissions(@PathVariable Long id) {
        adminGuard.requireAdminOrSuperAdmin();
        try {
            List<String> keys = roleService.getPermissions(id);
            return ResponseEntity.ok(new PermRoleDto.RolePermissionsResponse(keys));
        } catch (RoleNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ROLE_NOT_FOUND));
        }
    }

    @PutMapping("/roles/{id}/template")
    public ResponseEntity<?> saveRoleTemplate(@PathVariable Long id, @RequestBody PermRoleDto.TemplateUpdateRequest request) {
        adminGuard.requireAdminOrSuperAdmin();
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
}
