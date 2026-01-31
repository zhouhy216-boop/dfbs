package com.dfbs.app.modules.inventory;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "warehouse")
@Data
public class WarehouseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WarehouseType type;

    @Column(name = "manager_id")
    private Long managerId;

    public WarehouseEntity() {}
}
