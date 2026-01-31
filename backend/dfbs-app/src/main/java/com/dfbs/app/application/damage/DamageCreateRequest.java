package com.dfbs.app.application.damage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request for creating a damage record. attachmentUrls must not be empty.
 */
public record DamageCreateRequest(
        Long shipmentId,
        Long shipmentMachineId,
        LocalDateTime occurrenceTime,
        Long damageTypeId,
        Long treatmentId,
        String description,
        List<String> attachmentUrls
) {}
