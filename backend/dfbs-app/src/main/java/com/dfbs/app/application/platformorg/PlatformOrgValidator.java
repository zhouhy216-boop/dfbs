package com.dfbs.app.application.platformorg;

import com.dfbs.app.modules.platformorg.PlatformOrgEntity;
import com.dfbs.app.modules.platformorg.PlatformOrgPlatform;
import com.dfbs.app.modules.platformorg.PlatformOrgRepo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class PlatformOrgValidator {

    private final PlatformOrgRepo repo;

    public PlatformOrgValidator(PlatformOrgRepo repo) {
        this.repo = repo;
    }

    public void validateForCreate(PlatformOrgEntity entity) {
        validate(entity, null);
    }

    public void validateForUpdate(PlatformOrgEntity entity, Long excludeId) {
        validate(entity, excludeId);
    }

    private void validate(PlatformOrgEntity entity, Long excludeId) {
        repo.findByPlatformAndOrgCodeShort(entity.getPlatform(), entity.getOrgCodeShort())
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("同一平台下的组织编码已存在");
                });

        PlatformOrgPlatform platform = entity.getPlatform();
        if (platform == PlatformOrgPlatform.INHAND) {
            validateEmailUnique(entity, excludeId);
        } else if (platform == PlatformOrgPlatform.HENDONG) {
            validatePhoneUnique(entity, excludeId);
        }
    }

    private void validateEmailUnique(PlatformOrgEntity entity, Long excludeId) {
        String email = normalizeEmail(entity.getContactEmail());
        if (!StringUtils.hasText(email)) {
            return;
        }
        List<PlatformOrgEntity> matches = repo.findByPlatformAndContactEmailNormalized(
                PlatformOrgPlatform.INHAND, email);
        matches.stream()
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .findFirst()
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("映翰通平台的联系人邮箱已被使用");
                });
    }

    private void validatePhoneUnique(PlatformOrgEntity entity, Long excludeId) {
        String phone = normalizePhone(entity.getContactPhone());
        if (!StringUtils.hasText(phone)) {
            return;
        }
        repo.findByPlatform(PlatformOrgPlatform.HENDONG).stream()
                .filter(existing -> StringUtils.hasText(existing.getContactPhone()))
                .filter(existing -> {
                    String existingPhone = normalizePhone(existing.getContactPhone());
                    return StringUtils.hasText(existingPhone) && existingPhone.equals(phone);
                })
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .findFirst()
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("恒动平台的联系人电话已被使用");
                });
    }

    private String normalizeEmail(String email) {
        return StringUtils.hasText(email) ? email.trim().toLowerCase() : null;
    }

    private String normalizePhone(String phone) {
        return StringUtils.hasText(phone) ? phone.replaceAll("[^0-9]", "") : null;
    }
}
