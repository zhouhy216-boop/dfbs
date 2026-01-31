package com.dfbs.app.application.shipment;

import com.dfbs.app.modules.shipment.ShipmentStatus;

/**
 * Filter parameters for querying shipments.
 */
public record ShipmentFilterRequest(
        ShipmentStatus status,
        Long quoteId,
        Long initiatorId,
        Integer page,
        Integer size
) {
}

