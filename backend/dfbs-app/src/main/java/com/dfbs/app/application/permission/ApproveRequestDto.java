package com.dfbs.app.application.permission;

import java.util.List;

/**
 * Request body for admin approve: comment and new authorities for target user.
 */
public record ApproveRequestDto(
        String adminComment,
        List<String> newAuthorities
) {}
