package com.dfbs.app.application.shipment;

/**
 * One available next action for shipment workflow (points to existing POST endpoint).
 */
public record WorkflowActionDto(
        String actionCode,
        String labelCn,
        String method,
        String path,
        String confirmTextCn
) {}
