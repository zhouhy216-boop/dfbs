package com.dfbs.app.application.product;

import com.dfbs.app.modules.product.ProductEntity;
import com.dfbs.app.modules.product.ProductRepo;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ProductMasterDataService {

    private final ProductRepo productRepo;

    public ProductMasterDataService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    public ProductEntity create(String productCode, String name) {
        ProductEntity entity = new ProductEntity();
        entity.setId(UUID.randomUUID());
        entity.setProductCode(productCode);
        entity.setName(name);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        return productRepo.save(entity);
    }
}
