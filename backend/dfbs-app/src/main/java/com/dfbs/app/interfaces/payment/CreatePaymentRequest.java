package com.dfbs.app.interfaces.payment;

import com.dfbs.app.modules.quote.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreatePaymentRequest(
        Long customerId,
        BigDecimal amount,
        Currency currency,
        LocalDate receivedAt,
        List<AllocationItemRequest> allocations
) {
    public record AllocationItemRequest(Long quoteId, BigDecimal allocatedAmount, String period) {}
}
