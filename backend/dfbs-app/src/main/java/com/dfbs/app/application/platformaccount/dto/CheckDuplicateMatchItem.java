package com.dfbs.app.application.platformaccount.dto;

/**
 * One matching org from check-duplicates (same platform, customer/email/phone/orgFullName hit).
 */
public record CheckDuplicateMatchItem(
        String orgCodeShort,
        String customerName,
        String email,
        String phone,
        String orgFullName,
        String matchReason
) {
}
