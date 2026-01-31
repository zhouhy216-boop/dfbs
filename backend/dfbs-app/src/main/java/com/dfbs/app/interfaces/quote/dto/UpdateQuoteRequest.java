package com.dfbs.app.interfaces.quote.dto;

import com.dfbs.app.modules.quote.enums.Currency;

public record UpdateQuoteRequest(
        Currency currency,
        String recipient,
        String phone,
        String address,
        Long businessLineId,  // Optional
        Long machineId,       // Optional: for filtering (e.g. BOM parts)
        Long customerId,     // Optional: standardize customer
        String customerName  // Optional: snapshot when setting customerId
) {}
