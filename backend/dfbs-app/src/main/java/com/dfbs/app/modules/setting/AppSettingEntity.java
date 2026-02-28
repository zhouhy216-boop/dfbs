package com.dfbs.app.modules.setting;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "app_setting")
@Getter
@Setter
public class AppSettingEntity {

    @Id
    @Column(name = "key", nullable = false, length = 128)
    private String key;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
