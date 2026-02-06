package com.dfbs.app.application.platformconfig.dto;

import com.dfbs.app.modules.platformconfig.CodeValidatorType;

/**
 * Validation rules for a platform (used by frontend for org code format and duplicate checks).
 */
public record PlatformRulesDto(
        String platformCode,
        String platformName,
        boolean ruleUniqueEmail,
        boolean ruleUniquePhone,
        boolean ruleUniqueOrgName,
        CodeValidatorType codeValidatorType
) {}
