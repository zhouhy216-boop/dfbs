package com.dfbs.app.modules.product;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepo extends JpaRepository<ProductEntity, UUID> {
    Optional<ProductEntity> findByProductCode(String productCode);
}
