package com.dfbs.app.application.platformorg;

import com.dfbs.app.application.platformconfig.PlatformConfigService;
import com.dfbs.app.application.platformconfig.dto.PlatformRulesDto;
import com.dfbs.app.modules.platformorg.PlatformOrgEntity;
import com.dfbs.app.modules.platformorg.PlatformOrgRepo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class PlatformOrgValidator {

    private final PlatformOrgRepo repo;
    private final PlatformConfigService platformConfigService;

    public PlatformOrgValidator(PlatformOrgRepo repo, PlatformConfigService platformConfigService) {
        this.repo = repo;
        this.platformConfigService = platformConfigService;
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

        PlatformRulesDto config = platformConfigService.getRulesByCode(entity.getPlatform());
        if (config.ruleUniqueEmail()) {
            validateEmailUnique(entity, excludeId);
        }
        if (config.ruleUniquePhone()) {
            validatePhoneUnique(entity, excludeId);
        }
        if (config.ruleUniqueOrgName()) {
            validateOrgFullNameUnique(entity, excludeId);
        }
    }

    private void validateOrgFullNameUnique(PlatformOrgEntity entity, Long excludeId) {
        if (!StringUtils.hasText(entity.getOrgFullName())) {
            return;
        }
        repo.findByPlatformAndOrgFullName(entity.getPlatform(), entity.getOrgFullName().trim())
                .stream()
                .findFirst()
                .ifPresent(existing -> {
                    if (excludeId == null || !existing.getId().equals(excludeId)) {
                        if (!java.util.Objects.equals(
                                existing.getOrgCodeShort() != null ? existing.getOrgCodeShort().trim() : null,
                                entity.getOrgCodeShort() != null ? entity.getOrgCodeShort().trim() : null)) {
                            throw new IllegalArgumentException(
                                    entity.getPlatform() + "平台下机构全称已存在: " + entity.getOrgFullName());
                        }
                    }
                });
    }

    private void validateEmailUnique(PlatformOrgEntity entity, Long excludeId) {
        String email = normalizeEmail(entity.getContactEmail());
        if (!StringUtils.hasText(email)) {
            return;
        }
        List<PlatformOrgEntity> matches = repo.findByPlatformAndContactEmailNormalized(
                entity.getPlatform(), email);
        matches.stream()
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .findFirst()
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("该平台下联系人邮箱已被使用");
                });
    }

    private void validatePhoneUnique(PlatformOrgEntity entity, Long excludeId) {
        String phone = normalizePhone(entity.getContactPhone());
        if (!StringUtils.hasText(phone)) {
            return;
        }
        repo.findByPlatform(entity.getPlatform()).stream()
                .filter(existing -> StringUtils.hasText(existing.getContactPhone()))
                .filter(existing -> {
                    String existingPhone = normalizePhone(existing.getContactPhone());
                    return StringUtils.hasText(existingPhone) && existingPhone.equals(phone);
                })
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .findFirst()
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("该平台下联系人电话已被使用");
                });
    }

    private String normalizeEmail(String email) {
        return StringUtils.hasText(email) ? email.trim().toLowerCase() : null;
    }

    private String normalizePhone(String phone) {
        return StringUtils.hasText(phone) ? phone.replaceAll("[^0-9]", "") : null;
    }
}
