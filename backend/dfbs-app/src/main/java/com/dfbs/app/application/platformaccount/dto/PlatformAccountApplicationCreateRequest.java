package com.dfbs.app.application.platformaccount.dto;

import com.dfbs.app.modules.platformaccount.ApplicationSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PlatformAccountApplicationCreateRequest(
        @NotBlank @Size(max = 64) String platform,
        @NotNull ApplicationSourceType sourceType,
        /** Existing customer ID; optional when customerName is provided (free-text new customer). */
        Long customerId,
        /** Free-text customer name when customerId is null. */
        @Size(max = 256) String customerName,
        /** Admin-only; null at creation. */
        @Size(max = 128) String orgCodeShort,
        @NotBlank @Size(max = 256) String orgFullName,
        @NotBlank @Size(max = 128) String contactPerson,
        @NotBlank @Size(max = 64) String phone,
        @NotBlank @Size(max = 256) String email,
        /** Admin-only; null at creation. */
        @Size(max = 128) String region,
        @Size(max = 128) String salesPerson,
        @Size(max = 128) String contractNo,
        BigDecimal price,
        Integer quantity,
        @Size(max = 2000) String reason,
        Boolean isCcPlanner,
        Boolean skipPlanner
) {
}
