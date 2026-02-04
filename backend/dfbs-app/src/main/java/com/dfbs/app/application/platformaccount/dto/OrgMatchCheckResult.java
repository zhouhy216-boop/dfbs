package com.dfbs.app.application.platformaccount.dto;

import java.util.List;

/**
 * Result of checkOrgMatch(platform, orgCodeShort).
 * Helps frontend decide whether to show Option B/C (bind only vs create).
 */
public record OrgMatchCheckResult(
        boolean exists,
        Long orgId,
        List<Long> currentCustomerIds,
        /** Bitmask or flags for what differs; 0 = no diff. */
        int diffMask
) {
}
