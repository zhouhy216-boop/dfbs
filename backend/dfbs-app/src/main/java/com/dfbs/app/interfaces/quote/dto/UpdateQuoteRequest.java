package com.dfbs.app.interfaces.quote.dto;

import com.dfbs.app.modules.quote.enums.Currency;

public record UpdateQuoteRequest(
        Currency currency,
        String recipient,
        String phone,
        String address,
        Long businessLineId  // Optional
) {}
