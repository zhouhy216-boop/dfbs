package com.dfbs.app.application.bizperm;

import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.application.perm.PermAuditService;
import com.dfbs.app.application.perm.PermPermissionTreeService;
import com.dfbs.app.modules.bizperm.BizPermOperationPointRepo;
import com.dfbs.app.modules.bizperm.BizPermUserOpScopeEntity;
import com.dfbs.app.modules.bizperm.BizPermUserOpScopeId;
import com.dfbs.app.modules.bizperm.BizPermUserOpScopeRepo;
import com.dfbs.app.modules.user.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Per-account op scope (ALL vs HANDLED_ONLY). HANDLED_ONLY only allowed when catalog op point has handled_only=true.
 */
@Service
public class BizPermUserOpScopeService {

    public static final String SCOPE_ALL = "ALL";
    public static final String SCOPE_HANDLED_ONLY = "HANDLED_ONLY";

    private final BizPermUserOpScopeRepo scopeRepo;
    private final UserRepo userRepo;
    private final PermPermissionTreeService permissionTreeService;
    private final BizPermOperationPointRepo operationPointRepo;
    private final PermAuditService auditService;
    private final CurrentUserIdResolver userIdResolver;

    public BizPermUserOpScopeService(BizPermUserOpScopeRepo scopeRepo,
                                     UserRepo userRepo,
                                     PermPermissionTreeService permissionTreeService,
                                     BizPermOperationPointRepo operationPointRepo,
                                     PermAuditService auditService,
                                     CurrentUserIdResolver userIdResolver) {
        this.scopeRepo = scopeRepo;
        this.userRepo = userRepo;
        this.permissionTreeService = permissionTreeService;
        this.operationPointRepo = operationPointRepo;
        this.auditService = auditService;
        this.userIdResolver = userIdResolver;
    }

    public Map<String, String> getScopes(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new BizPermCatalogException(BizPermCatalogException.USER_NOT_FOUND, "用户不存在");
        }
        Map<String, String> out = new HashMap<>();
        for (BizPermUserOpScopeEntity e : scopeRepo.findByUserId(userId)) {
            out.put(e.getPermissionKey(), e.getScope());
        }
        return out;
    }

    @Transactional
    public Map<String, String> setScopes(Long userId, List<ScopeUpdate> updates) {
        if (!userRepo.existsById(userId)) {
            throw new BizPermCatalogException(BizPermCatalogException.USER_NOT_FOUND, "用户不存在");
        }
        Set<String> universe = permissionTreeService.getAllPermissionKeys();
        Long actorId = userIdResolver.getCurrentUserId();
        Instant now = Instant.now();

        for (ScopeUpdate u : updates) {
            String permissionKey = u.permissionKey() != null ? u.permissionKey().trim() : "";
            String scope = u.scope();
            if (permissionKey.isBlank() || (scope == null || scope.isBlank())) continue;
            scope = scope.toUpperCase();
            if (!SCOPE_ALL.equals(scope) && !SCOPE_HANDLED_ONLY.equals(scope)) continue;

            if (!universe.contains(permissionKey)) {
                throw new BizPermCatalogException(BizPermCatalogException.PERMISSION_KEY_NOT_FOUND, "权限键不在权限树中");
            }
            if (SCOPE_HANDLED_ONLY.equals(scope)) {
                final String pk = permissionKey;
                operationPointRepo.findByPermissionKey(pk)
                        .filter(op -> Boolean.TRUE.equals(op.getHandledOnly()))
                        .orElseThrow(() -> new BizPermCatalogException(BizPermCatalogException.SCOPE_NOT_SUPPORTED, "该操作不支持仅已处理范围"));
            }

            BizPermUserOpScopeId id = new BizPermUserOpScopeId(userId, permissionKey);
            final String pkForEntity = permissionKey;
            BizPermUserOpScopeEntity entity = scopeRepo.findById(id).orElseGet(() -> {
                BizPermUserOpScopeEntity e = new BizPermUserOpScopeEntity();
                e.setUserId(userId);
                e.setPermissionKey(pkForEntity);
                return e;
            });
            entity.setScope(scope);
            entity.setUpdatedAt(now);
            entity.setUpdatedByUserId(actorId);
            scopeRepo.save(entity);
        }

        auditService.log(
                PermAuditService.ACTION_BIZPERM_SCOPE_SET,
                PermAuditService.TARGET_USER,
                userId,
                null,
                "已更新 " + updates.size() + " 项操作范围");
        return getScopes(userId);
    }

    public record ScopeUpdate(String permissionKey, String scope) {}
}
