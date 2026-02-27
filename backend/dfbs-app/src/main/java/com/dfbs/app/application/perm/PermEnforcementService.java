package com.dfbs.app.application.perm;

import com.dfbs.app.config.CurrentUserIdResolver;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * RBAC enforcement: resolve effective permissions for current user and require permission by key.
 * effective = (template ∪ add) \ remove (remove wins). Uses PermAccountOverrideService for resolution.
 */
@Service
public class PermEnforcementService {

    private static final String MESSAGE_NO_ACCESS = "无权限";

    private final CurrentUserIdResolver userIdResolver;
    private final PermAccountOverrideService accountOverrideService;

    public PermEnforcementService(CurrentUserIdResolver userIdResolver,
                                  PermAccountOverrideService accountOverrideService) {
        this.userIdResolver = userIdResolver;
        this.accountOverrideService = accountOverrideService;
    }

    /** Effective permission keys for current request user. */
    public Set<String> getEffectiveKeysForCurrentUser() {
        Long userId = userIdResolver.getCurrentUserId();
        return accountOverrideService.getEffectiveKeys(userId);
    }

    /** Throws PermForbiddenException (403 PERM_FORBIDDEN) if current user does not have the permission key. */
    public void requirePermission(String permissionKey) {
        if (permissionKey == null || permissionKey.isBlank()) {
            return;
        }
        Set<String> effective = getEffectiveKeysForCurrentUser();
        if (!effective.contains(permissionKey.trim())) {
            throw new PermForbiddenException(MESSAGE_NO_ACCESS);
        }
    }
}
