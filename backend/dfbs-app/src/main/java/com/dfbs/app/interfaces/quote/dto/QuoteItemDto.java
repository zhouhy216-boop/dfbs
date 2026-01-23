package com.dfbs.app.interfaces.quote.dto;

import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteItemWarehouse;

import java.math.BigDecimal;

public record QuoteItemDto(
        Long id,
        Long quoteId,
        Integer lineOrder,
        QuoteExpenseType expenseType,
        String description,
        String spec,
        String unit,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal amount,
        QuoteItemWarehouse warehouse,
        String remark,
        String alertMessage
) {
    public static QuoteItemDto from(QuoteItemService.QuoteItemDto serviceDto) {
        return new QuoteItemDto(
                serviceDto.getId(),
                serviceDto.getQuoteId(),
                serviceDto.getLineOrder(),
                serviceDto.getExpenseType(),
                serviceDto.getDescription(),
                serviceDto.getSpec(),
                serviceDto.getUnit(),
                serviceDto.getQuantity(),
                serviceDto.getUnitPrice(),
                serviceDto.getAmount(),
                serviceDto.getWarehouse(),
                serviceDto.getRemark(),
                serviceDto.getAlertMessage()
        );
    }
}
