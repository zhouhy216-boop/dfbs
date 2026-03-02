package com.dfbs.app.modules.bizperm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "biz_perm_operation_point")
@Getter
@Setter
public class BizPermOperationPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_id")
    private Long nodeId;

    @Column(name = "permission_key", nullable = false, unique = true, length = 128)
    private String permissionKey;

    @Column(name = "cn_name", length = 128)
    private String cnName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "handled_only", nullable = false)
    private Boolean handledOnly = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
