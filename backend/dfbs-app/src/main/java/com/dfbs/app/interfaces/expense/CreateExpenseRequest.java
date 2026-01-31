package com.dfbs.app.interfaces.expense;

import com.dfbs.app.modules.expense.ExpenseType;
import com.dfbs.app.modules.quote.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
        LocalDate expenseDate,
        BigDecimal amount,
        Currency currency,
        ExpenseType expenseType,
        String description,
        Long quoteId,
        Long workOrderId,
        Long inventoryOutboundId,
        Long tripRequestId
) {}
