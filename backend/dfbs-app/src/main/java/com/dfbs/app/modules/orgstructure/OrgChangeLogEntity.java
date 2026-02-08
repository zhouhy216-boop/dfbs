package com.dfbs.app.modules.orgstructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "org_change_log")
@Getter
@Setter
public class OrgChangeLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object_type", nullable = false, length = 32)
    private String objectType; // LEVEL, ORG_NODE, PERSON

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Column(name = "action", nullable = false, length = 32)
    private String action; // CREATE, UPDATE, MOVE, ENABLE, DISABLE

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 128)
    private String operatorName;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp = Instant.now();

    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summaryText;

    @Column(name = "diff_json", columnDefinition = "TEXT")
    private String diffJson;
}
