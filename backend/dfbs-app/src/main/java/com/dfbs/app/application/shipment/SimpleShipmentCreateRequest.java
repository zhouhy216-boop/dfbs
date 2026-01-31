package com.dfbs.app.application.shipment;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request for creating a standalone shipment (customer + type). Maps to Normal shipment. */
@Schema(description = "Create standalone shipment: customerId and shipmentType (STANDARD/EXPRESS)")
public record SimpleShipmentCreateRequest(
        @Schema(description = "Customer ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long customerId,
        @Schema(description = "Shipment type: STANDARD or EXPRESS", example = "STANDARD")
        String shipmentType
) {}
