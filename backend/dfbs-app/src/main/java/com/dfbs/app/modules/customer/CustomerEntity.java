package com.dfbs.app.modules.customer;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "md_customer")
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Temp record (pending confirmation); excluded from standard search. */
    @Column(name = "is_temp", nullable = false)
    private Boolean isTemp = false;

    /** Last used at (for MRU sorting in Smart Select). */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "customer_code", nullable = false, unique = true)
    private String customerCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "merged_to_id")
    private Long mergedToId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerAliasEntity> aliases = new ArrayList<>();

    protected CustomerEntity() {
    }

    public static CustomerEntity create(String customerCode, String name) {
        CustomerEntity e = new CustomerEntity();
        e.customerCode = customerCode;
        e.name = name != null ? name.trim() : "";
        e.status = "ACTIVE";
        e.createdAt = OffsetDateTime.now();
        e.updatedAt = e.createdAt;
        return e;
    }

    public void updateName(String name) {
        this.name = name != null ? name.trim() : "";
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Boolean getIsTemp() { return isTemp; }
    public void setIsTemp(Boolean isTemp) { this.isTemp = isTemp != null ? isTemp : false; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public String getCustomerCode() { return customerCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getMergedToId() { return mergedToId; }
    public void setMergedToId(Long mergedToId) { this.mergedToId = mergedToId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public List<CustomerAliasEntity> getAliases() { return aliases; }
    public void setAliases(List<CustomerAliasEntity> aliases) { this.aliases = aliases != null ? aliases : new ArrayList<>(); }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
}
