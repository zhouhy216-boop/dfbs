package com.dfbs.app.application.shipment;

import com.dfbs.app.modules.shipment.ShipmentStatus;

import java.util.List;

/**
 * Workflow read contract: current step + available next actions (aligned with ShipmentService require() rules).
 */
public record ShipmentWorkflowDto(
        Long shipmentId,
        ShipmentStatus status,
        String stepCode,
        String stepLabelCn,
        List<WorkflowActionDto> actions
) {}
