package com.dfbs.app.application.shipment;

import java.time.LocalDate;

/**
 * Request body for the ship action. Missing fields will fall back to existing values on the ShipmentEntity.
 */
public record ShipActionRequest(
        String entrustMatter,
        LocalDate shipDate,
        Integer quantity,
        String model,
        Boolean needPackaging,
        String pickupContact,
        String pickupPhone,
        Boolean needLoading,
        String pickupAddress,
        String receiverContact,
        String receiverPhone,
        Boolean needUnloading,
        String deliveryAddress,
        String carrier,
        String ticketUrl
) {
}

