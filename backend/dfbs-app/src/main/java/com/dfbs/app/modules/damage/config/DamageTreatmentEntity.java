package com.dfbs.app.modules.damage.config;

import com.dfbs.app.modules.damage.TreatmentBehavior;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "damage_treatment")
@Data
public class DamageTreatmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TreatmentBehavior behavior;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    public DamageTreatmentEntity() {}
}
