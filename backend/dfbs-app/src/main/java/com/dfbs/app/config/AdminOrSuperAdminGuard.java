package com.dfbs.app.config;

import com.dfbs.app.application.perm.PermForbiddenException;
import org.springframework.stereotype.Component;

/**
 * Admin-only gate: only users with ROLE_ADMIN or ROLE_SUPER_ADMIN may access.
 * Does not use dfbs.perm.superAdminAllowlist (that remains for Permission Tree / perm super-admin).
 */
@Component
public class AdminOrSuperAdminGuard {

    private static final String MESSAGE_NO_ACCESS = "无权限";

    private final CurrentUserIdResolver userIdResolver;

    public AdminOrSuperAdminGuard(CurrentUserIdResolver userIdResolver) {
        this.userIdResolver = userIdResolver;
    }

    /** Throws PermForbiddenException (403 PERM_FORBIDDEN) if current user is not admin or super-admin. */
    public void requireAdminOrSuperAdmin() {
        if (!userIdResolver.isAdminOrSuperAdmin()) {
            throw new PermForbiddenException(MESSAGE_NO_ACCESS);
        }
    }
}
