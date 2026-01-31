package com.dfbs.app.application.damage;

import java.math.BigDecimal;

/**
 * Request for confirming compensation payment.
 */
public record CompensationConfirmRequest(
        BigDecimal amount,
        String proofUrl
) {}
