package com.dfbs.app.modules.customer;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "md_customer")
public class CustomerEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "customer_code", nullable = false, unique = true)
    private String customerCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected CustomerEntity() {
    }

    public static CustomerEntity create(String customerCode, String name) {
        CustomerEntity e = new CustomerEntity();
        e.id = UUID.randomUUID();
        e.customerCode = customerCode;
        e.name = name;
        e.status = "ACTIVE";
        e.createdAt = OffsetDateTime.now();
        e.updatedAt = e.createdAt;
        return e;
    }

    // ===== 新增：领域内更新方法 =====
    public void updateName(String name) {
        this.name = name;
        this.updatedAt = OffsetDateTime.now();
    }

    // ===== getters =====
    public UUID getId() { return id; }
    public String getCustomerCode() { return customerCode; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }

    // ===== setters（仅内部使用）=====
    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
        this.updatedAt = OffsetDateTime.now();
    }
}
