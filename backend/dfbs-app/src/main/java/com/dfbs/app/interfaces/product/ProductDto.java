package com.dfbs.app.interfaces.product;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductDto(
        UUID id,
        String productCode,
        String name,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProductDto from(com.dfbs.app.modules.product.ProductEntity entity) {
        return new ProductDto(
                entity.getId(),
                entity.getProductCode(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
