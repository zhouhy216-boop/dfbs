package com.dfbs.app.config;

import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.springframework.stereotype.Component;

/**
 * Resolves current user (String from CurrentUserProvider) to Long userId.
 * MVP: if string is numeric, use as id; else use first user from repo or 1L.
 */
@Component
public class CurrentUserIdResolver {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepo userRepo;

    public CurrentUserIdResolver(CurrentUserProvider currentUserProvider, UserRepo userRepo) {
        this.currentUserProvider = currentUserProvider;
        this.userRepo = userRepo;
    }

    public Long getCurrentUserId() {
        String username = currentUserProvider.getCurrentUser();
        if (username == null || username.isBlank()) {
            return userRepo.findAll().stream().findFirst().map(UserEntity::getId).orElse(1L);
        }
        try {
            return Long.parseLong(username.trim());
        } catch (NumberFormatException e) {
            return userRepo.findAll().stream().findFirst().map(UserEntity::getId).orElse(1L);
        }
    }

    public UserEntity getCurrentUserEntity() {
        Long id = getCurrentUserId();
        return userRepo.findById(id).orElseThrow(() -> new IllegalStateException("Current user not found: id=" + id));
    }

    /** True if user has canManageStatements or authorities contains ROLE_ADMIN. */
    public boolean isFinanceOrAdmin() {
        UserEntity user = getCurrentUserEntity();
        if (Boolean.TRUE.equals(user.getCanManageStatements())) return true;
        String auth = user.getAuthorities();
        return auth != null && (auth.contains("ROLE_ADMIN") || auth.contains("ROLE_FINANCE"));
    }

    /** True if user has ROLE_SUPER_ADMIN (org structure, level config, people directory, change log). */
    public boolean isSuperAdmin() {
        UserEntity user = getCurrentUserEntity();
        String auth = user.getAuthorities();
        return auth != null && auth.contains("ROLE_SUPER_ADMIN");
    }

    /** True if user has ROLE_ADMIN or ROLE_SUPER_ADMIN (admin-only entry: Account & Permissions). Does not use perm allowlist. */
    public boolean isAdminOrSuperAdmin() {
        UserEntity user = getCurrentUserEntity();
        String auth = user.getAuthorities();
        return auth != null && (auth.contains("ROLE_ADMIN") || auth.contains("ROLE_SUPER_ADMIN"));
    }
}
