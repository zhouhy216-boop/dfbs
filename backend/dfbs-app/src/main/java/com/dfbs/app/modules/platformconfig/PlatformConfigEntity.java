package com.dfbs.app.modules.platformconfig;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "md_platform", uniqueConstraints = @UniqueConstraint(name = "uk_md_platform_code", columnNames = "platform_code"))
@Getter
@Setter
public class PlatformConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform_name", nullable = false, length = 64)
    private String platformName;

    @Column(name = "platform_code", nullable = false, length = 64)
    private String platformCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "rule_unique_email", nullable = false)
    private Boolean ruleUniqueEmail = Boolean.FALSE;

    @Column(name = "rule_unique_phone", nullable = false)
    private Boolean ruleUniquePhone = Boolean.FALSE;

    @Column(name = "rule_unique_org_name", nullable = false)
    private Boolean ruleUniqueOrgName = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "code_validator_type", nullable = false, length = 32)
    private CodeValidatorType codeValidatorType = CodeValidatorType.NONE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
