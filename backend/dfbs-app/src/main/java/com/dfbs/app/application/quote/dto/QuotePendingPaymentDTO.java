package com.dfbs.app.application.quote.dto;

import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class QuotePendingPaymentDTO {
    private Long id;
    private String quoteNo;
    private String customerName;
    private Currency currency;
    private BigDecimal totalAmount;
    private BigDecimal unpaidAmount;
    private QuotePaymentStatus paymentStatus;
    private LocalDateTime financeAssignedAt;
}
