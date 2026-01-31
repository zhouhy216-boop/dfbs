package com.dfbs.app.application.machine.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for machine list view (enriched with latest shipment).
 */
public record MachineListDto(
        UUID id,
        String serialNo,       // mapped from machineSn
        String model,
        String customerName,
        LocalDate shipDate,
        String city,
        String address,
        String salesperson,
        String receiptNo,      // mapped from shipment.logisticsNo
        Integer batchCount
) {}
