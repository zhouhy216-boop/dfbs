package com.dfbs.app.application.platformconfig.dto;

import com.dfbs.app.modules.platformconfig.CodeValidatorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Create/update request for platform config.
 */
public record PlatformConfigRequest(
        @NotBlank @Size(max = 64) String platformName,
        @NotBlank @Size(max = 64) String platformCode,
        Boolean isActive,
        Boolean ruleUniqueEmail,
        Boolean ruleUniquePhone,
        Boolean ruleUniqueOrgName,
        CodeValidatorType codeValidatorType
) {}
