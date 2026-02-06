package com.dfbs.app.modules.platformaccount;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "platform_account_applications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_platform_account_app_no", columnNames = "application_no")
})
@Getter
@Setter
public class PlatformAccountApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_no", nullable = false, length = 64)
    private String applicationNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PlatformAccountApplicationStatus status = PlatformAccountApplicationStatus.DRAFT;

    // Org snapshot
    @Column(name = "platform", nullable = false, length = 64)
    private String platform;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private ApplicationSourceType sourceType = ApplicationSourceType.FACTORY;

    /** Set when user selects existing customer; null when free-text customer name is used. */
    @Column(name = "customer_id")
    private Long customerId;

    /** Snapshot when customerId is null (free-text new customer name). */
    @Column(name = "customer_name", length = 256)
    private String customerName;

    /** Set by Admin at approval; null at creation. */
    @Column(name = "org_code_short", length = 128)
    private String orgCodeShort;

    @Column(name = "org_full_name", nullable = false, length = 256)
    private String orgFullName;

    @Column(name = "contact_person", length = 128)
    private String contactPerson;

    @Column(name = "phone", length = 64)
    private String phone;

    @Column(name = "email", length = 256)
    private String email;

    /** Set by Admin at approval; null at creation. */
    @Column(name = "region", length = 128)
    private String region;

    @Column(name = "sales_person", length = 128)
    private String salesPerson;

    @Column(name = "contract_no", length = 128)
    private String contractNo;

    @Column(name = "price", precision = 19, scale = 4)
    private java.math.BigDecimal price;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "is_cc_planner", nullable = false)
    private Boolean isCcPlanner = Boolean.FALSE;

    // Audit
    @Column(name = "applicant_id")
    private Long applicantId;

    @Column(name = "planner_id")
    private Long plannerId;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "reject_reason", length = 512)
    private String rejectReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
