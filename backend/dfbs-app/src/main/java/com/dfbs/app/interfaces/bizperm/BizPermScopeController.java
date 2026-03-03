package com.dfbs.app.interfaces.bizperm;

import com.dfbs.app.application.bizperm.BizPermCatalogException;
import com.dfbs.app.application.bizperm.BizPermUserOpScopeService;
import com.dfbs.app.config.AdminOrSuperAdminGuard;
import com.dfbs.app.infra.dto.ErrorResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Per-account op scope (ALL vs HANDLED_ONLY). Admin-only, not allowlist.
 */
@RestController
@RequestMapping("/api/v1/admin/bizperm/scope")
public class BizPermScopeController {

    private final AdminOrSuperAdminGuard adminGuard;
    private final BizPermUserOpScopeService scopeService;

    public BizPermScopeController(AdminOrSuperAdminGuard adminGuard,
                                  BizPermUserOpScopeService scopeService) {
        this.adminGuard = adminGuard;
        this.scopeService = scopeService;
    }

    @ExceptionHandler(BizPermCatalogException.class)
    public ResponseEntity<ErrorResult> handleBizPermCatalogException(BizPermCatalogException ex) {
        return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ex.getMachineCode()));
    }

    @GetMapping("/accounts/{userId}/scopes")
    public ResponseEntity<BizPermScopeDto.ScopesResponse> getScopes(@PathVariable Long userId) {
        adminGuard.requireAdminOrSuperAdmin();
        Map<String, String> scopes = scopeService.getScopes(userId);
        return ResponseEntity.ok(new BizPermScopeDto.ScopesResponse(userId, scopes));
    }

    @PutMapping("/accounts/{userId}/scopes")
    public ResponseEntity<BizPermScopeDto.ScopesResponse> setScopes(
            @PathVariable Long userId,
            @RequestBody BizPermScopeDto.SetScopesRequest request) {
        adminGuard.requireAdminOrSuperAdmin();
        List<BizPermScopeDto.ScopeUpdateRequest> reqList = (request != null && request.updates() != null) ? request.updates() : List.of();
        List<BizPermUserOpScopeService.ScopeUpdate> updates = reqList
                .stream()
                .map(u -> new BizPermUserOpScopeService.ScopeUpdate(u.permissionKey(), u.scope()))
                .toList();
        Map<String, String> scopes = scopeService.setScopes(userId, updates);
        return ResponseEntity.ok(new BizPermScopeDto.ScopesResponse(userId, scopes));
    }
}
