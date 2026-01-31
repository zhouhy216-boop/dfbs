package com.dfbs.app.modules.invoice;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_item_ref")
@Data
public class InvoiceItemRefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_record_id", nullable = false)
    private Long invoiceRecordId;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Column(name = "quote_item_id", nullable = false)
    private Long quoteItemId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public InvoiceItemRefEntity() {}
}
