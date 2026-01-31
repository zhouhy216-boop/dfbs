package com.dfbs.app.application.damage;

import com.dfbs.app.modules.damage.RepairStage;

import java.math.BigDecimal;

/**
 * Request for updating repair stage. When stage is SETTLED, settlementDetails and costs are required.
 */
public record RepairStageUpdateRequest(
        RepairStage stage,
        String settlementDetails,
        BigDecimal repairFee,
        BigDecimal penaltyAmount
) {}
