package com.dfbs.app.modules.damage.config;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "damage_type")
@Data
public class DamageTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    public DamageTypeEntity() {}
}
