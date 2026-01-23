package com.dfbs.app.application.product;

import com.dfbs.app.modules.product.ProductEntity;
import com.dfbs.app.modules.product.ProductRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public Page<ProductEntity> search(String keyword, Pageable pageable) {
        Specification<ProductEntity> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));

        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = "%" + keyword.toLowerCase() + "%";
            Specification<ProductEntity> keywordSpec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), lowerKeyword),
                            cb.like(cb.lower(root.get("productCode")), lowerKeyword)
                    );
            spec = spec.and(keywordSpec);
        }

        return productRepo.findAll(spec, pageable);
    }
}
