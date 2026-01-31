package com.dfbs.app.modules.inventory;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_order")
@Data
public class TransferOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_warehouse_id", nullable = false)
    private Long sourceWarehouseId;

    @Column(name = "target_warehouse_id", nullable = false)
    private Long targetWarehouseId;

    @Column(nullable = false, length = 128)
    private String sku;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransferStatus status = TransferStatus.PENDING;

    @Column(name = "logistics_url", length = 512)
    private String logisticsUrl;

    @Column(name = "apply_reason", length = 500)
    private String applyReason;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "operator_id")
    private Long operatorId;

    public TransferOrderEntity() {}
}
