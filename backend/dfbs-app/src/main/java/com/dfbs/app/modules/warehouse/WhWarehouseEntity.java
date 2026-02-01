package com.dfbs.app.modules.warehouse;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 仓库定义 (大库/小库). Central warehouse is a singleton logically, but stored here.
 */
@Entity
@Table(name = "wh_warehouse", uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
@Getter
@Setter
public class WhWarehouseEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WarehouseType type;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
