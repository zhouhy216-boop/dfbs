package com.dfbs.app.modules.bizperm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "biz_perm_user_op_scope")
@IdClass(BizPermUserOpScopeId.class)
@Getter
@Setter
public class BizPermUserOpScopeEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "permission_key", nullable = false, length = 128)
    private String permissionKey;

    @Column(name = "scope", nullable = false, length = 32)
    private String scope; // ALL | HANDLED_ONLY

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;
}
