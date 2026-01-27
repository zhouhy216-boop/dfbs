package com.dfbs.app.modules.quote;

import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.QuoteVoidStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

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

    @Column(name = "is_warehouse_cc_sent", nullable = false)
    private Boolean isWarehouseCcSent = false;  // Strict de-duplication flag for CC notification

    @Column(name = "is_warehouse_ship_sent", nullable = false)
    private Boolean isWarehouseShipSent = false;  // Strict de-duplication flag for Ship notification

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public QuoteEntity() {}
}
