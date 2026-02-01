package com.dfbs.app.modules.warehouse;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 库存快照. Configurable safetyThreshold per warehouse+part.
 */
@Entity
@Table(name = "wh_inventory", uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "part_no"}))
@Getter
@Setter
public class WhInventoryEntity extends BaseAuditEntity {

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "part_no", nullable = false, length = 128)
    private String partNo;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "safety_threshold", nullable = false)
    private Integer safetyThreshold = 0;
}
