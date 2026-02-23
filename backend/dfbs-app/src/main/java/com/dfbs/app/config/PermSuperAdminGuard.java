package com.dfbs.app.config;

import com.dfbs.app.infra.dto.ErrorResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * PERM-only gate: only users whose identity key is in dfbs.perm.superAdminAllowlist
 * may access PERM admin endpoints. Does not change existing SuperAdminGuard behavior.
 * Identity key used: userId (Long), compared as string in allowlist.
 */
@Component
public class PermSuperAdminGuard {

    private static final String MESSAGE_NO_ACCESS = "无权限";

    private final CurrentUserIdResolver userIdResolver;
    private final PermAllowlistProperties allowlistProperties;

    public PermSuperAdminGuard(CurrentUserIdResolver userIdResolver,
                              PermAllowlistProperties allowlistProperties) {
        this.userIdResolver = userIdResolver;
        this.allowlistProperties = allowlistProperties;
    }

    /** Throws 403 if current user is not in the PERM super-admin allowlist. */
    public void requirePermSuperAdmin() {
        if (!isPermSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MESSAGE_NO_ACCESS);
        }
    }

    /** True if current user's userId (as string) is in dfbs.perm.superAdminAllowlist. */
    public boolean isPermSuperAdmin() {
        Long userId = userIdResolver.getCurrentUserId();
        String key = userId != null ? String.valueOf(userId) : "";
        List<String> allowlist = allowlistProperties.getSuperAdminAllowlistKeys();
        return allowlist.contains(key);
    }
}
