package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "spare_parts")
@Getter
@Setter
public class SparePartEntity extends BaseMasterEntity {

    @Column(name = "part_no", nullable = false, unique = true, length = 64)
    private String partNo;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "spec", length = 500)
    private String spec;

    @Column(name = "unit", length = 32)
    private String unit = "个";

    @Column(name = "reference_price", precision = 19, scale = 4)
    private BigDecimal referencePrice;

    /** Comma-separated or JSON list of alternate names/aliases. */
    @Column(name = "aliases", columnDefinition = "TEXT")
    private String aliases;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private MasterDataStatus status = MasterDataStatus.ENABLE;
}
