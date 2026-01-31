package com.dfbs.app.application.repair;

import com.dfbs.app.modules.repair.WarrantyStatus;

import java.time.LocalDateTime;

/**
 * Filter for repair record search.
 */
public record RepairRecordFilterRequest(
        String customerName,
        String machineNo,
        LocalDateTime repairDateFrom,
        LocalDateTime repairDateTo,
        WarrantyStatus warrantyStatus,
        String personInCharge,
        String oldWorkOrderNo,
        Integer page,
        Integer size
) {}
