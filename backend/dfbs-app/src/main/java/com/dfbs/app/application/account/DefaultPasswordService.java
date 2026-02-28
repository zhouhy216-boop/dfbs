package com.dfbs.app.application.account;

import com.dfbs.app.config.AuthProperties;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.setting.AppSettingEntity;
import com.dfbs.app.modules.setting.AppSettingRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Single source of truth for effective default password: DB (app_setting key=auth.defaultPassword) if present, else config (dfbs.auth.defaultPassword).
 * Used by account creation, password reset (empty newPassword), and legacy login (password_hash null).
 */
@Service
public class DefaultPasswordService {

    public static final String KEY_AUTH_DEFAULT_PASSWORD = "auth.defaultPassword";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AppSettingRepo appSettingRepo;
    private final AuthProperties authProperties;
    private final CurrentUserIdResolver userIdResolver;

    public DefaultPasswordService(AppSettingRepo appSettingRepo,
                                 AuthProperties authProperties,
                                 CurrentUserIdResolver userIdResolver) {
        this.appSettingRepo = appSettingRepo;
        this.authProperties = authProperties;
        this.userIdResolver = userIdResolver;
    }

    /** Effective default password: DB value if row exists and non-blank, else config. */
    public String getEffectiveDefaultPassword() {
        return appSettingRepo.findById(KEY_AUTH_DEFAULT_PASSWORD)
                .map(AppSettingEntity::getValue)
                .filter(v -> v != null && !v.isBlank())
                .orElseGet(() -> authProperties.getDefaultPassword());
    }

    /**
     * Set admin-managed default password. Validates min length; writes/updates DB; updates timestamps.
     * Caller must ensure admin guard (e.g. AdminOrSuperAdminGuard) and write audit log.
     */
    @Transactional
    public void setDefaultPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("默认密码至少 " + MIN_PASSWORD_LENGTH + " 位");
        }
        Long actorId = userIdResolver.getCurrentUserId();
        Instant now = Instant.now();
        AppSettingEntity e = appSettingRepo.findById(KEY_AUTH_DEFAULT_PASSWORD)
                .orElseGet(() -> {
                    AppSettingEntity entity = new AppSettingEntity();
                    entity.setKey(KEY_AUTH_DEFAULT_PASSWORD);
                    return entity;
                });
        e.setValue(rawPassword);
        e.setUpdatedByUserId(actorId);
        e.setUpdatedAt(now);
        appSettingRepo.save(e);
    }

    /** Status for API: configured = DB has non-blank value; updatedAt/updatedBy from that row. */
    public DefaultPasswordStatus getStatus() {
        return appSettingRepo.findById(KEY_AUTH_DEFAULT_PASSWORD)
                .filter(e -> e.getValue() != null && !e.getValue().isBlank())
                .map(e -> new DefaultPasswordStatus(true, e.getUpdatedAt(), e.getUpdatedByUserId()))
                .orElse(new DefaultPasswordStatus(false, null, null));
    }

    public record DefaultPasswordStatus(boolean configured, Instant updatedAt, Long updatedByUserId) {}
}
