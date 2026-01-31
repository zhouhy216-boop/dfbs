package com.dfbs.app.application.shipment;

import com.dfbs.app.modules.shipment.PackagingType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/** Request for creating a Normal (Sales) shipment. */
@Schema(description = "Request to create a normal (sales) shipment")
public record NormalShipmentCreateRequest(
        @Schema(description = "Contract number", example = "CON-2025-001")
        String contractNo,
        @Schema(description = "Salesperson name", example = "张三")
        String salespersonName,
        @Schema(description = "Packaging type", example = "STANDARD")
        PackagingType packagingType,
        @Schema(description = "Receiver name", example = "李四")
        String receiverName,
        @Schema(description = "Receiver phone", example = "13800138000")
        String receiverPhone,
        @Schema(description = "Need unload service", example = "true")
        Boolean unloadService,
        @Schema(description = "Delivery address", example = "北京市朝阳区xxx")
        String deliveryAddress,
        @Schema(description = "Remark")
        String remark
) {}
