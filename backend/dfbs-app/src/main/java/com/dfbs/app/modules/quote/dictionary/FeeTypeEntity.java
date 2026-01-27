package com.dfbs.app.modules.quote.dictionary;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "fee_type")
@Data
public class FeeTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "default_unit", length = 32)
    private String defaultUnit;

    @Column(name = "allowed_units", length = 500)
    private String allowedUnits;  // comma-separated or JSON

    @Column(name = "replacement_fee_type_id")
    private Long replacementFeeTypeId;  // Nullable, for statistical merging when disabled

    @Column(name = "fixed_spec_options", length = 500)
    private String fixedSpecOptions;  // comma-separated or JSON, e.g., "月,年" for 物联卡流量费

    public FeeTypeEntity() {}
}
