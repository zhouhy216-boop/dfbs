package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "part")
@Data
public class PartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** System number, unique, e.g. PT-00001 */
    @Column(name = "system_no", unique = true, length = 64)
    private String systemNo;

    @Column(nullable = false, length = 200)
    private String name;

    /** 资材及规格图号及尺寸 */
    @Column(length = 500)
    private String spec;

    @Column(name = "drawing_no", length = 128)
    private String drawingNo;

    /** Sales price, mandatory, default RMB */
    @Column(name = "sales_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal salesPrice = BigDecimal.ZERO;

    @Column(length = 32)
    private String unit = "个";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "replacement_part_id")
    private Long replacementPartId;

    public PartEntity() {}
}
