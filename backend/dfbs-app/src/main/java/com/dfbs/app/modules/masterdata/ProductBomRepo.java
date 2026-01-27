package com.dfbs.app.modules.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductBomRepo extends JpaRepository<ProductBomEntity, Long> {
    List<ProductBomEntity> findByProductId(UUID productId);
    List<ProductBomEntity> findByPartId(Long partId);
}
