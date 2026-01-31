package com.dfbs.app.modules.invoice;

import com.dfbs.app.modules.quote.enums.Currency;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_application")
@Data
public class InvoiceApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_no", nullable = false, unique = true, length = 64)
    private String applicationNo;

    @Column(name = "collector_id", nullable = false)
    private Long collectorId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InvoiceApplicationStatus status = InvoiceApplicationStatus.PENDING;

    @Column(name = "invoice_title", length = 200)
    private String invoiceTitle;

    @Column(name = "tax_id", length = 100)
    private String taxId;

    @Column(length = 500)
    private String address;

    @Column(length = 64)
    private String phone;

    @Column(name = "bank_name", length = 200)
    private String bankName;

    @Column(name = "bank_account", length = 100)
    private String bankAccount;

    @Column(length = 128)
    private String email;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "auditor_id")
    private Long auditorId;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public InvoiceApplicationEntity() {}
}
