package com.dfbs.app.application.quote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BatchPaymentRequest {
    /** Optional: when set, use statement's quote IDs and validate amount/currency/customerId against statement. */
    private Long statementId;
    /** Customer ID (required when statementId is set; must equal statement.customerId). */
    private Long customerId;
    /** Quote IDs to pay (mandatory when statementId is null). Ignored when statementId is set. */
    private List<Long> quoteIds;
    /** Total payment amount (must equal sum of quotes' unpaid amounts, or statement.totalAmount when statementId set). */
    private BigDecimal totalPaymentAmount;
    private LocalDateTime paymentTime;
    private Long paymentMethodId;
    private String currency;
    private String note;
    private List<String> attachmentUrls;
}
