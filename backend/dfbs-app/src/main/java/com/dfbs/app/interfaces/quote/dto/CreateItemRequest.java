package com.dfbs.app.interfaces.quote.dto;

import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteItemWarehouse;

import java.math.BigDecimal;

public record CreateItemRequest(
        QuoteExpenseType expenseType,
        String description,
        String spec,
        String unit,
        Integer quantity,
        BigDecimal unitPrice,
        QuoteItemWarehouse warehouse,
        String remark
) {}
