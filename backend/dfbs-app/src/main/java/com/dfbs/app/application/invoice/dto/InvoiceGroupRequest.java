package com.dfbs.app.application.invoice.dto;

import com.dfbs.app.modules.invoice.InvoiceType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceGroupRequest {
    private List<QuoteItemSelection> items;
    private InvoiceType invoiceType;
    private BigDecimal taxRate;
    private String content;
}
