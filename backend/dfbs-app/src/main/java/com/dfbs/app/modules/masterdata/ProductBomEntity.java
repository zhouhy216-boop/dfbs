package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "product_bom")
@Data
public class ProductBomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;  // FK to ProductEntity (UUID)

    @Column(name = "part_id", nullable = false)
    private Long partId;  // FK to PartEntity

    @Column(nullable = false)
    private Integer qty;

    public ProductBomEntity() {}
}
