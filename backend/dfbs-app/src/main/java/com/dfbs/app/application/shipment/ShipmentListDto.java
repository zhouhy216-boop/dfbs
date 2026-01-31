package com.dfbs.app.application.shipment;

import com.dfbs.app.modules.shipment.ShipmentStatus;

import java.time.LocalDateTime;

/** List row for GET /api/v1/shipments with resolved customerName. */
public record ShipmentListDto(
        Long id,
        String shipmentNo,
        String customerName,
        ShipmentStatus status,
        LocalDateTime createdAt
) {}
