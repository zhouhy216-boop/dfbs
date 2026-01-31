package com.dfbs.app.application.damage;

import com.dfbs.app.modules.damage.CompensationStatus;
import com.dfbs.app.modules.damage.RepairStage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for damage record list/detail with type name, treatment name, and machine info.
 */
public record DamageRecordDto(
        Long id,
        Long shipmentId,
        Long shipmentMachineId,
        String machineModel,
        String machineNo,
        LocalDateTime occurrenceTime,
        Long damageTypeId,
        String damageTypeName,
        Long treatmentId,
        String treatmentName,
        String description,
        List<String> attachmentUrls,
        RepairStage repairStage,
        CompensationStatus compensationStatus,
        String settlementDetails,
        BigDecimal compensationAmount,
        BigDecimal repairFee,
        BigDecimal penaltyAmount,
        LocalDateTime createdAt,
        Long operatorId
) {}
