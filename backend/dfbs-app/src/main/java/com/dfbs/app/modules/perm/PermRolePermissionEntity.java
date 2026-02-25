package com.dfbs.app.modules.perm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "perm_role_permission")
@Getter
@Setter
public class PermRolePermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "permission_key", nullable = false, length = 128)
    private String permissionKey;
}
