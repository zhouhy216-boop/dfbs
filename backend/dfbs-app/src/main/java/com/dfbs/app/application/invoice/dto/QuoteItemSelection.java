package com.dfbs.app.application.invoice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuoteItemSelection {
    private Long quoteItemId;
    private BigDecimal amount;
}
