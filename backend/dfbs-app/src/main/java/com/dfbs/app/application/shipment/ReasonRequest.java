package com.dfbs.app.application.shipment;

/**
 * Simple request wrapper for actions that only need a reason,
 * e.g. exception handling or manual cancellation.
 */
public record ReasonRequest(String reason) {
}

