package com.dfbs.app.modules.orgstructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "org_position_catalog")
@Getter
@Setter
public class OrgPositionCatalogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_name", nullable = false, length = 64)
    private String baseName;

    @Column(name = "grade", nullable = false, length = 16)
    private String grade; // PRIMARY, DEPUTY, ACTING, NONE

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(name = "short_name", length = 64)
    private String shortName;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
