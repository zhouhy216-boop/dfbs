package com.dfbs.app.application.iccid.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for ICCID list view (enriched with shipment/customer when bound).
 */
public record IccidListDto(
        UUID id,
        String iccid,          // mapped from iccidNo
        String boundMachineNo,  // mapped from machineSn
        String customerName,
        String contractNo,
        String orgCode,
        String plan,
        String platform,
        LocalDate expiryDate,
        Boolean isBound
) {}
