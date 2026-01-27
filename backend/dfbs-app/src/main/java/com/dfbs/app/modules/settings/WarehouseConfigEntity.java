package com.dfbs.app.modules.settings;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "warehouse_config")
@Data
public class WarehouseConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_ids", length = 1000, nullable = false)
    private String userIds;  // JSON array or comma-separated user IDs

    public WarehouseConfigEntity() {}
}
