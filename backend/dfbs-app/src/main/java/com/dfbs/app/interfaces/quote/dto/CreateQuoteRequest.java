package com.dfbs.app.interfaces.quote.dto;

import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to create a new draft quote")
public record CreateQuoteRequest(
        @Schema(description = "Source of the quote", example = "MANUAL", requiredMode = Schema.RequiredMode.REQUIRED)
        QuoteSourceType sourceType,
        @Schema(description = "External reference ID when source is not MANUAL", example = "WO-001")
        String sourceRefId,
        @Schema(description = "Customer ID", example = "1")
        Long customerId,
        @Schema(description = "Temporary customer name when customerId is null", example = "临时客户")
        String customerName,
        @Schema(description = "Business line ID", example = "1")
        Long businessLineId
) {}
