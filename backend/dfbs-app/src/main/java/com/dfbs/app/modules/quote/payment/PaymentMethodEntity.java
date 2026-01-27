package com.dfbs.app.modules.quote.payment;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payment_method")
@Data
public class PaymentMethodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public PaymentMethodEntity() {}
}
