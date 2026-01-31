package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * BOM: model part list. items stored as JSON (array of partId, partNo, name, quantity, remark).
 */
@Entity
@Table(name = "model_part_lists")
@Getter
@Setter
public class ModelPartListEntity extends BaseMasterEntity {

    @Column(name = "model_id", nullable = false)
    private Long modelId;

    @Column(name = "version", nullable = false, length = 32)
    private String version;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    /** JSON array: [{ "partId": 1, "partNo": "P1", "name": "...", "quantity": 2, "remark": "..." }, ...] */
    @Column(name = "items", nullable = false, columnDefinition = "TEXT")
    private String items;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private BomStatus status = BomStatus.DRAFT;
}
