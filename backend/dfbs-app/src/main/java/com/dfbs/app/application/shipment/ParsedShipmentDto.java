package com.dfbs.app.application.shipment;

import com.dfbs.app.modules.shipment.PackagingType;
import com.dfbs.app.modules.shipment.ShipmentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/** Result of smart text parsing for shipment creation. Fields are best-guess; may be null. */
@Schema(description = "Parsed shipment fields from pasted text (response)")
public record ParsedShipmentDto(
        @Schema(description = "Inferred shipment type", example = "NORMAL")
        ShipmentType type,
        @Schema(description = "Contract number", example = "CON-001")
        String contractNo,
        @Schema(description = "Salesperson name")
        String salespersonName,
        @Schema(description = "Receiver name")
        String receiverName,
        @Schema(description = "Receiver phone")
        String receiverPhone,
        @Schema(description = "Delivery address")
        String deliveryAddress,
        @Schema(description = "Entrust matter")
        String entrustMatter,
        @Schema(description = "Pickup contact")
        String pickupContact,
        @Schema(description = "Pickup address")
        String pickupAddress,
        @Schema(description = "Remark")
        String remark,
        @Schema(description = "Extra parsed key-value pairs")
        Map<String, String> extra
) {}
