package com.dfbs.app.application.platformaccount.dto;

import com.dfbs.app.modules.platformorg.PlatformOrgPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PlatformAccountApproveRequest(
        @NotNull PlatformOrgPlatform platform,
        Long customerId,
        @NotBlank @Size(max = 128) String orgCodeShort,
        @NotBlank @Size(max = 256) String orgFullName,
        @Size(max = 128) String contactPerson,
        @Size(max = 64) String phone,
        @Size(max = 256) String email,
        @NotBlank @Size(max = 128) String region,
        @Size(max = 128) String salesPerson,
        @Size(max = 128) String contractNo,
        BigDecimal price,
        Integer quantity,
        @Size(max = 2000) String reason,
        Boolean isCcPlanner,
        /** BIND_ONLY = add customer to org or create org; CANCEL = reject without binding */
        ApproveAction action
) {
    public enum ApproveAction { BIND_ONLY, CANCEL }
}
