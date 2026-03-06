package com.dfbs.app.application.shipment;

/**
 * Request for marking a shipment (or device) exception. Backward-compatible: reason is required and used as description.
 */
public record ExceptionMarkRequest(
        String reason,
        Long machineId,
        String exceptionType,
        String responsibility,
        String evidenceUrl
) {
    /** Description for the exception record; use reason when present. */
    public String description() {
        return reason != null && !reason.isBlank() ? reason.trim() : null;
    }
}
