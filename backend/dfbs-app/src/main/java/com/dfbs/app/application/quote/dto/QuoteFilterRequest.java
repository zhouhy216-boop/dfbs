package com.dfbs.app.application.quote.dto;

import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuoteFilterRequest {
    /** Optional: filter by customer name (e.g. recipient / display name contains). */
    private String customerName;
    /** Optional: created at from (inclusive). */
    private LocalDateTime createTimeFrom;
    /** Optional: created at to (inclusive). */
    private LocalDateTime createTimeTo;
    /** Optional: exact payment status. */
    private QuotePaymentStatus paymentStatus;
}
