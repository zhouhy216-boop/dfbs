package com.dfbs.app.modules.quote.dictionary;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "fee_category")
@Data
public class FeeCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public FeeCategoryEntity() {}
}
