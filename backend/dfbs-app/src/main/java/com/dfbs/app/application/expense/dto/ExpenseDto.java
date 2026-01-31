package com.dfbs.app.application.expense.dto;

import com.dfbs.app.modules.expense.ExpenseEntity;
import com.dfbs.app.modules.expense.ExpenseStatus;
import com.dfbs.app.modules.expense.ExpenseType;
import com.dfbs.app.modules.quote.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ExpenseDto(
        Long id,
        OffsetDateTime createdAt,
        Long createdBy,
        LocalDate expenseDate,
        BigDecimal amount,
        Currency currency,
        ExpenseType expenseType,
        String description,
        ExpenseStatus status,
        Long quoteId,
        Long workOrderId,
        Long inventoryOutboundId,
        Long tripRequestId,
        Long claimId
) {
    public static ExpenseDto from(ExpenseEntity e) {
        return new ExpenseDto(
                e.getId(),
                e.getCreatedAt(),
                e.getCreatedBy(),
                e.getExpenseDate(),
                e.getAmount(),
                e.getCurrency(),
                e.getExpenseType(),
                e.getDescription(),
                e.getStatus(),
                e.getQuoteId(),
                e.getWorkOrderId(),
                e.getInventoryOutboundId(),
                e.getTripRequestId(),
                e.getClaim() != null ? e.getClaim().getId() : null
        );
    }
}
