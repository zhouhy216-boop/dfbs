package com.dfbs.app.modules.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_user")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(length = 128)
    private String nickname;

    /** Only users with this = true can initiate permission requests. */
    @Column(name = "can_request_permission", nullable = false)
    private Boolean canRequestPermission = false;

    /** JSON array of role/authority strings, e.g. ["ROLE_USER","ROLE_ADMIN"]. */
    @Column(name = "authorities", columnDefinition = "TEXT")
    private String authorities;

    /** User can turn off Normal notifications; Urgent are always delivered. */
    @Column(name = "allow_normal_notification", nullable = false)
    private Boolean allowNormalNotification = true;

    /** User can create/manage customer account statements. */
    @Column(name = "can_manage_statements", nullable = false)
    private Boolean canManageStatements = false;

    public UserEntity() {}
}
