package com.dfbs.app.interfaces.quote.dto;

import com.dfbs.app.modules.quote.enums.QuoteSourceType;

public record CreateQuoteRequest(
        QuoteSourceType sourceType,
        String sourceRefId,
        Long customerId,
        Long businessLineId  // Optional
) {}
