package com.dfbs.app.interfaces.quote.dto;

import com.dfbs.app.modules.quote.enums.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * List row for GET /api/v1/quotes (general search).
 * Includes id for rowKey and optional total/paid for display.
 */
public record QuoteListDto(
        Long id,
        String quoteNo,
        QuoteStatus status,
        String customerName,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        LocalDateTime createdAt
) {}
