package com.dfbs.app.modules.inventory;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_log")
@Data
public class InventoryLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(nullable = false, length = 128)
    private String sku;

    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;

    @Column(name = "after_quantity", nullable = false)
    private Integer afterQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransactionType type;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public InventoryLogEntity() {}
}
