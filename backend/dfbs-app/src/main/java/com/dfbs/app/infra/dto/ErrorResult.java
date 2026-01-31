package com.dfbs.app.infra.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard error response for API contract. Frontend can branch on machineCode.
 */
@Schema(description = "Standard error response with human message and machine code")
public record ErrorResult(
        @Schema(description = "Human-readable error message", example = "Quote not found")
        String message,
        @Schema(description = "Machine code for frontend logic", example = "QUOTA_EXCEEDED")
        String machineCode
) {
    public static ErrorResult of(String message, String machineCode) {
        return new ErrorResult(message, machineCode != null ? machineCode : "UNKNOWN");
    }
}
