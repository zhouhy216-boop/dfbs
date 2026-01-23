package com.dfbs.app.interfaces.customer;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerDto(
        UUID id,
        String customerCode,
        String name,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static CustomerDto from(com.dfbs.app.modules.customer.CustomerEntity entity) {
        return new CustomerDto(
                entity.getId(),
                entity.getCustomerCode(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
