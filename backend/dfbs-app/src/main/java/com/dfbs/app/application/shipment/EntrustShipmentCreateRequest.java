package com.dfbs.app.application.shipment;

import java.time.LocalDate;

/**
 * Request for creating an Entrust (Internal or Customer) shipment.
 * For Customer Entrust, quoteId must be set and quote must not already have a shipment.
 */
public record EntrustShipmentCreateRequest(
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
