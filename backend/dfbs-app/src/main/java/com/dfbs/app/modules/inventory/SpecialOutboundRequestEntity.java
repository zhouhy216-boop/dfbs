package com.dfbs.app.modules.inventory;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "special_outbound_request")
@Data
public class SpecialOutboundRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(nullable = false, length = 128)
    private String sku;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SpecialOutboundType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SpecialOutboundStatus status = SpecialOutboundStatus.PENDING_APPROVAL;

    @Column(name = "apply_reason", length = 500)
    private String applyReason;

    @Column(name = "audit_reason", length = 500)
    private String auditReason;

    @Column(name = "auditor_id")
    private Long auditorId;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "operator_id")
    private Long operatorId;

    public SpecialOutboundRequestEntity() {}
}
