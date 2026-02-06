package com.dfbs.app.modules.platformorg;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "platform_org",
        uniqueConstraints = @UniqueConstraint(name = "uk_platform_org_code", columnNames = {"platform", "org_code_short"}))
@Getter
@Setter
public class PlatformOrgEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform", nullable = false, length = 64)
    private String platform;

    @Column(name = "org_code_short", nullable = false, length = 128)
    private String orgCodeShort;

    @Column(name = "org_full_name", nullable = false, length = 256)
    private String orgFullName;

    @OneToMany(mappedBy = "org", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<PlatformOrgCustomerEntity> customerLinks = new java.util.ArrayList<>();

    @Column(name = "contact_person", length = 128)
    private String contactPerson;

    @Column(name = "contact_phone", length = 64)
    private String contactPhone;

    @Column(name = "contact_email", length = 256)
    private String contactEmail;

    @Column(name = "sales_person", length = 128)
    private String salesPerson;

    @Column(name = "region", length = 128)
    private String region;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    private PlatformOrgStatus status = PlatformOrgStatus.ACTIVE;

    @Column(name = "source_application_id")
    private Long sourceApplicationId;

    @Column(name = "source_type", length = 32)
    private String sourceType;

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
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
