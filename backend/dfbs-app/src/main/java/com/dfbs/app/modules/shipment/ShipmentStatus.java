package com.dfbs.app.modules.shipment;

/**
 * Shipment lifecycle statuses.
 *
 * CREATED       - created from quote, waiting for warehouse to accept
 * PENDING_SHIP  - accepted by warehouse, waiting for actual shipment
 * SHIPPED       - shipped out
 * COMPLETED     - delivered / signed
 * EXCEPTION     - exception occurred during processing
 * CANCELLED     - cancelled (e.g., quote voided)
 */
public enum ShipmentStatus {
    CREATED,
    PENDING_SHIP,
    SHIPPED,
    COMPLETED,
    EXCEPTION,
    CANCELLED
}
