package com.dfbs.app.modules.invoice;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_record")
@Data
public class InvoiceRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false, length = 32)
    private InvoiceType invoiceType = InvoiceType.NORMAL;

    @Column(name = "tax_rate", precision = 5, scale = 4)
    private BigDecimal taxRate;

    @Column(name = "content", length = 500)
    private String content;

    public InvoiceRecordEntity() {}
}
