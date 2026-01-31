package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bom_conflicts")
@Getter
@Setter
public class BomConflictEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bom_id", nullable = false)
    private Long bomId;

    /** Part number from BOM row (drawing no). */
    @Column(name = "row_part_no", length = 64)
    private String rowPartNo;

    /** Name from BOM row. */
    @Column(name = "row_name", length = 200)
    private String rowName;

    /** Zero-based index of the item in BOM JSON (for FIX_NO resolution). */
    @Column(name = "row_index")
    private Integer rowIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private BomConflictType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private BomConflictStatus status = BomConflictStatus.PENDING;
}
