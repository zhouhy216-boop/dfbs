package com.dfbs.app.modules.dicttype;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "dict_label_snapshot_demo")
@Getter
@Setter
public class DictLabelSnapshotDemoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_code", nullable = false, length = 64)
    private String typeCode;

    @Column(name = "item_value", nullable = false, length = 64)
    private String itemValue;

    @Column(name = "item_label_snapshot", nullable = false, length = 128)
    private String itemLabelSnapshot;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
