package com.dfbs.app.application.platformaccount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlatformAccountRejectRequest(
        @NotBlank @Size(max = 512) String reason
) {
}
