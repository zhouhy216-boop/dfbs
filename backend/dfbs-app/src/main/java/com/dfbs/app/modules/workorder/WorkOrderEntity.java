package com.dfbs.app.modules.workorder;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 工单主表. Links customer need to service execution.
 */
@Entity
@Table(name = "work_order", uniqueConstraints = @UniqueConstraint(columnNames = {"order_no"}))
@Getter
@Setter
public class WorkOrderEntity extends BaseAuditEntity {

    @Column(name = "order_no", nullable = false, length = 64)
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WorkOrderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WorkOrderStatus status;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", nullable = false, length = 256)
    private String customerName;

    @Column(name = "contact_person", nullable = false, length = 128)
    private String contactPerson;

    @Column(name = "contact_phone", nullable = false, length = 64)
    private String contactPhone;

    @Column(name = "service_address", nullable = false, length = 500)
    private String serviceAddress;

    @Column(name = "device_model_id")
    private Long deviceModelId;

    @Column(name = "machine_no", length = 128)
    private String machineNo;

    @Column(name = "issue_description", columnDefinition = "TEXT")
    private String issueDescription;

    @Column(name = "appointment_time")
    private LocalDateTime appointmentTime;

    @Column(name = "dispatcher_id")
    private Long dispatcherId;

    @Column(name = "service_manager_id")
    private Long serviceManagerId;

    @Column(name = "customer_signature_url", length = 512)
    private String customerSignatureUrl;

    /** Optional: link to quote when created from quote (downstream). */
    @Column(name = "quote_id")
    private Long quoteId;

    /** Optional: user who initiated (e.g. from quote or internal create). */
    @Column(name = "initiator_id")
    private Long initiatorId;

    /** Reason when order is rejected/cancelled (e.g. invalid request). */
    @Column(name = "cancellation_reason", length = 512)
    private String cancellationReason;
}
