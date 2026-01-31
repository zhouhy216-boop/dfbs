package com.dfbs.app.modules.shipment;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipment")
@Data
public class ShipmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_id")
    private Long quoteId;

    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @Column(name = "applicant_id")
    private Long applicantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private ShipmentType type = ShipmentType.CUSTOMER_DELEGATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 32)
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ShipmentStatus status = ShipmentStatus.CREATED;

    /* --- Normal Shipping --- */
    @Column(name = "contract_no", length = 128)
    private String contractNo;

    @Column(name = "salesperson_name", length = 128)
    private String salespersonName;

    @Enumerated(EnumType.STRING)
    @Column(name = "packaging_type", length = 32)
    private PackagingType packagingType;

    @Column(name = "receiver_name", length = 128)
    private String receiverName;

    @Column(name = "unload_service")
    private Boolean unloadService;

    /* --- Attachments (receipt/ticket) --- */
    @Column(name = "receipt_url", length = 512)
    private String receiptUrl;

    @Column(name = "ticket_url", length = 512)
    private String ticketUrl;

    @Column(name = "entrust_matter", length = 500)
    private String entrustMatter;

    @Column(name = "ship_date")
    private LocalDate shipDate;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "model", length = 256)
    private String model;

    @Column(name = "need_packaging")
    private Boolean needPackaging;

    @Column(name = "pickup_contact", length = 128)
    private String pickupContact;

    @Column(name = "pickup_phone", length = 64)
    private String pickupPhone;

    @Column(name = "need_loading")
    private Boolean needLoading;

    @Column(name = "pickup_address", length = 500)
    private String pickupAddress;

    @Column(name = "receiver_contact", length = 128)
    private String receiverContact;

    @Column(name = "receiver_phone", length = 64)
    private String receiverPhone;

    @Column(name = "need_unloading")
    private Boolean needUnloading;

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "carrier", length = 256)
    private String carrier;

    @Column(name = "carrier_id")
    private Long carrierId;

    @Column(name = "is_billable_to_customer", nullable = false)
    private Boolean isBillableToCustomer = false;

    /** 回单号 (logistics / receipt number). */
    @Column(name = "logistics_no", length = 128)
    private String logisticsNo;

    /** If not null, shipment is linked to a freight bill and cannot be selected for another. */
    @Column(name = "freight_bill_id")
    private Long freightBillId;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "exception_reason", columnDefinition = "TEXT")
    private String exceptionReason;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public ShipmentEntity() {}
}
