package com.dfbs.app.application.orgstructure.dto;

/** Minimal person fields for admin account-binding selection (GET /admin/account-permissions/people). */
public record PersonOptionForBindingDto(
        Long personId,
        String name,
        String orgUnitLabel,
        String title,
        String phone,
        String email
) {}
