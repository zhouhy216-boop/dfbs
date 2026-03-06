package com.dfbs.app.application.shipment;

/**
 * Optional patch fields for POST /{id}/tracking (SHIPPED, no status change).
 */
public record TrackingRequest(
        String logisticsNo,
        String ticketUrl,
        String receiptUrl,
        String remark
) {}
