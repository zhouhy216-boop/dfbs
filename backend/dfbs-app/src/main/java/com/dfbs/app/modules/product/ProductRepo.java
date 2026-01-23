package com.dfbs.app.modules.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepo extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {
    Optional<ProductEntity> findByProductCode(String productCode);
}
