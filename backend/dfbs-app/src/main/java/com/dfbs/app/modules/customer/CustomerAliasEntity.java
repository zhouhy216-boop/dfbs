package com.dfbs.app.modules.customer;

import jakarta.persistence.*;

@Entity
@Table(name = "md_customer_alias")
public class CustomerAliasEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(name = "alias_name", nullable = false, length = 200)
    private String aliasName;

    protected CustomerAliasEntity() {
    }

    public static CustomerAliasEntity of(CustomerEntity customer, String aliasName) {
        CustomerAliasEntity e = new CustomerAliasEntity();
        e.customer = customer;
        e.aliasName = aliasName != null ? aliasName.trim() : "";
        return e;
    }

    public Long getId() { return id; }
    public CustomerEntity getCustomer() { return customer; }
    public void setCustomer(CustomerEntity customer) { this.customer = customer; }
    public String getAliasName() { return aliasName; }
    public void setAliasName(String aliasName) { this.aliasName = aliasName; }
}
