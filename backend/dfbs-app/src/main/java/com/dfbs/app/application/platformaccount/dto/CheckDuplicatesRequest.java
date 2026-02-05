package com.dfbs.app.application.platformaccount.dto;

import com.dfbs.app.modules.platformorg.PlatformOrgPlatform;
import jakarta.validation.constraints.NotNull;

public record CheckDuplicatesRequest(
        @NotNull PlatformOrgPlatform platform,
        String customerName,
        String email,
        String contactPhone
) {
}
