package com.dfbs.app.application.shipment;

import java.time.LocalDate;

/**
 * Optional supplement fields for accept (CREATED -> PENDING_SHIP). All optional; patch only.
 */
public record AcceptSupplementRequest(
        String contractNo,
        String receiverName,
        String receiverPhone,
        String deliveryAddress,
        String remark,
        LocalDate shipDate
) {}
