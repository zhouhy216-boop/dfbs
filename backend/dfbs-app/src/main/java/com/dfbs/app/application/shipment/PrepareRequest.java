package com.dfbs.app.application.shipment;

/**
 * Minimal prep fields for POST /{id}/prepare (PENDING_SHIP, no status change). All optional; patch only.
 */
public record PrepareRequest(
        Integer quantity,
        String model,
        Boolean needPackaging,
        String entrustMatter,
        String pickupContact,
        String pickupPhone,
        Boolean needLoading,
        String pickupAddress,
        String deliveryAddress,
        String remark
) {}
