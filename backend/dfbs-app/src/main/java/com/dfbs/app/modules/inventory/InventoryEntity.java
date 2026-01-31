package com.dfbs.app.modules.inventory;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "inventory", uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "sku"}))
@Data
public class InventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(nullable = false, length = 128)
    private String sku;

    @Column(nullable = false)
    private Integer quantity = 0;

    public InventoryEntity() {}
}
