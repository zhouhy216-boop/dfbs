package com.dfbs.app.modules.perm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "perm_user_permission_override")
@Getter
@Setter
public class PermUserPermissionOverrideEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "permission_key", nullable = false, length = 128)
    private String permissionKey;

    @Column(name = "op", nullable = false, length = 16)
    private String op; // ADD | REMOVE
}
