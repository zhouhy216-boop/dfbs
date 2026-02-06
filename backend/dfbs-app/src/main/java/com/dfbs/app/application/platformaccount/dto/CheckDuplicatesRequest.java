package com.dfbs.app.application.platformaccount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckDuplicatesRequest(
        @NotBlank @Size(max = 64) String platform,
        String customerName,
        String email,
        String contactPhone,
        @Size(max = 512) String orgFullName
) {
}
