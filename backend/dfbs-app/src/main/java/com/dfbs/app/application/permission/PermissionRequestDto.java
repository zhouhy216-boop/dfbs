package com.dfbs.app.application.permission;

/**
 * Request body for creating a permission request.
 */
public record PermissionRequestDto(
        Long targetUserId,
        String description,
        String reason,
        String expectedTime
) {}
