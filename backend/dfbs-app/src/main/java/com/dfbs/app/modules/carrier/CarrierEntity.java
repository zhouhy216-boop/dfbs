package com.dfbs.app.modules.carrier;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "md_carrier")
public class CarrierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 256)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "carrier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarrierRuleEntity> rules = new ArrayList<>();

    public CarrierEntity() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public List<CarrierRuleEntity> getRules() { return rules; }
    public void setRules(List<CarrierRuleEntity> rules) { this.rules = rules != null ? rules : new ArrayList<>(); }
}
