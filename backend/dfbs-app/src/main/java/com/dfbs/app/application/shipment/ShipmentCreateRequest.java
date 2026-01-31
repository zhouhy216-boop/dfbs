package com.dfbs.app.application.shipment;

import java.time.LocalDate;

/**
 * Request DTO for creating a shipment from a CONFIRMED quote.
 */
public record ShipmentCreateRequest(
        Long quoteId,
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
        String remark
) {}
