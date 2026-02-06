package com.dfbs.app.application.platformconfig.dto;

import com.dfbs.app.modules.platformconfig.CodeValidatorType;
import com.dfbs.app.modules.platformconfig.PlatformConfigEntity;

import java.time.LocalDateTime;

public record PlatformConfigResponse(
        Long id,
        String platformName,
        String platformCode,
        Boolean isActive,
        Boolean ruleUniqueEmail,
        Boolean ruleUniquePhone,
        Boolean ruleUniqueOrgName,
        CodeValidatorType codeValidatorType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PlatformConfigResponse fromEntity(PlatformConfigEntity e) {
        return new PlatformConfigResponse(
                e.getId(),
                e.getPlatformName(),
                e.getPlatformCode(),
                e.getIsActive(),
                e.getRuleUniqueEmail(),
                e.getRuleUniquePhone(),
                e.getRuleUniqueOrgName(),
                e.getCodeValidatorType(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
