package com.dfbs.app.application.platformorg.dto;

import com.dfbs.app.modules.platformorg.PlatformOrgPlatform;
import com.dfbs.app.modules.platformorg.PlatformOrgStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlatformOrgRequest(
        @NotNull PlatformOrgPlatform platform,
        @NotBlank @Size(max = 128) String orgCodeShort,
        @NotBlank @Size(max = 256) String orgFullName,
        @NotNull java.util.List<Long> customerIds,
        @Size(max = 128) String contactPerson,
        @Size(max = 64) String contactPhone,
        @Size(max = 256) String contactEmail,
        @Size(max = 128) String salesPerson,
        @Size(max = 128) String region,
        String remark,
        Boolean isActive,
        PlatformOrgStatus status,
        Long sourceApplicationId,
        String sourceType
) {
}
