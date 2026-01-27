package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "part")
@Data
public class PartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String spec;

    @Column(length = 32)
    private String unit = "个";  // default "个"

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "replacement_part_id")
    private Long replacementPartId;  // Nullable

    public PartEntity() {}
}
