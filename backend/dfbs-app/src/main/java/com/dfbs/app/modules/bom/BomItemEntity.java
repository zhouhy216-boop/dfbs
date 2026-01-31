package com.dfbs.app.modules.bom;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "bom_item")
@Data
public class BomItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version_id", nullable = false)
    private Long versionId;

    @Column(name = "part_id", nullable = false)
    private Long partId;

    @Column(name = "index_no", nullable = false, length = 32)
    private String indexNo;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "is_optional", nullable = false)
    private Boolean isOptional = false;

    @Column(name = "remark", length = 500)
    private String remark;

    public BomItemEntity() {}
}
