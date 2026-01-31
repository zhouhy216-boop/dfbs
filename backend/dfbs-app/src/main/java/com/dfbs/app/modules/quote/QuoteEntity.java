package com.dfbs.app.modules.quote;

import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.DownstreamType;
import com.dfbs.app.modules.quote.enums.QuoteInvoiceStatus;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.QuoteVoidStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quote")
@EntityListeners(AuditingEntityListener.class)
@Data
public class QuoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_no", nullable = false, unique = true)
    private String quoteNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, updatable = false)
    private QuoteSourceType sourceType;

    @Column(name = "source_ref_id")
    private String sourceRefId;

    @Column(name = "source_id")
    private String sourceId;

    @Column(name = "machine_info", length = 1000)
    private String machineInfo;

    @Column(name = "machine_id")
    private Long machineId;  // For filtering (e.g. BOM parts); nullable

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "customer_confirmer_id")
    private Long customerConfirmerId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 256)
    private String customerName;

    @Column(name = "original_customer_name", length = 256)
    private String originalCustomerName;

    private String recipient;
    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 32)
    private QuotePaymentStatus paymentStatus = QuotePaymentStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    @Column(name = "void_status", nullable = false, length = 32)
    private QuoteVoidStatus voidStatus = QuoteVoidStatus.NONE;

    @Column(name = "collector_id")
    private Long collectorId;  // The person responsible for collecting money

    @Column(name = "parent_quote_id")
    private Long parentQuoteId;  // To link "Balance Adjustment Quote" to original

    @Column(name = "business_line_id")
    private Long businessLineId;  // FK to BusinessLineEntity

    @Enumerated(EnumType.STRING)
    @Column(name = "downstream_type", length = 32)
    private DownstreamType downstreamType;

    @Column(name = "downstream_id")
    private Long downstreamId;  // One-time link: shipment.id or work_order.id

    @Column(name = "is_warehouse_cc_sent", nullable = false)
    private Boolean isWarehouseCcSent = false;  // Strict de-duplication flag for CC notification

    @Column(name = "is_warehouse_ship_sent", nullable = false)
    private Boolean isWarehouseShipSent = false;  // Strict de-duplication flag for Ship notification

    @Column(name = "invoiced_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal invoicedAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false, length = 32)
    private QuoteInvoiceStatus invoiceStatus = QuoteInvoiceStatus.UNINVOICED;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /** Set on first submit to Finance (lifecycle lock for contract price auto-suggest). */
    @Column(name = "first_submission_time")
    private LocalDateTime firstSubmissionTime;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public LocalDateTime getFirstSubmissionTime() { return firstSubmissionTime; }
    public void setFirstSubmissionTime(LocalDateTime firstSubmissionTime) { this.firstSubmissionTime = firstSubmissionTime; }

    public QuoteEntity() {}
}
