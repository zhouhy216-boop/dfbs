package com.dfbs.app.application.shipment;

import java.time.LocalDateTime;

/**
 * Read DTO for GET /api/v1/shipments/{id}/exceptions.
 */
public record ExceptionRecordDto(
        Long id,
        Long machineId,
        String exceptionType,
        String description,
        String responsibility,
        String evidenceUrl,
        Long operatorId,
        LocalDateTime createdAt
) {}
