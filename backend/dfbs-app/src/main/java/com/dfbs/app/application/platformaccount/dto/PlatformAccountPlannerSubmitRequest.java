package com.dfbs.app.application.platformaccount.dto;

import com.dfbs.app.modules.platformorg.PlatformOrgPlatform;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Planner node: all fields editable (platform, customer, org, contacts, contract/price). */
public record PlatformAccountPlannerSubmitRequest(
        PlatformOrgPlatform platform,
        Long customerId,
        @Size(max = 256) String customerName,
        @Size(max = 128) String orgCodeShort,
        @Size(max = 256) String orgFullName,
        @Size(max = 128) String contactPerson,
        @Size(max = 64) String phone,
        @Size(max = 256) String email,
        @Size(max = 128) String region,
        @Size(max = 128) String salesPerson,
        @Size(max = 128) String contractNo,
        BigDecimal price,
        Integer quantity,
        @Size(max = 2000) String reason
) {
}
