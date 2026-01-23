package com.dfbs.app.interfaces.contract;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ContractDto(
        UUID id,
        String contractNo,
        String customerCode,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ContractDto from(com.dfbs.app.modules.contract.ContractEntity entity) {
        return new ContractDto(
                entity.getId(),
                entity.getContractNo(),
                entity.getCustomerCode(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
