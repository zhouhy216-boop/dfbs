package com.dfbs.app.application.platformorg.dto;

import com.dfbs.app.modules.platformorg.PlatformOrgCustomerEntity;
import com.dfbs.app.modules.platformorg.PlatformOrgEntity;
import com.dfbs.app.modules.platformorg.PlatformOrgPlatform;

import java.time.LocalDateTime;

public record PlatformOrgResponse(
        Long id,
        PlatformOrgPlatform platform,
        String orgCodeShort,
        String orgFullName,
        java.util.List<Long> customerIds,
        java.util.List<SimpleCustomerDto> linkedCustomers,
        String contactPerson,
        String contactPhone,
        String contactEmail,
        String salesPerson,
        String region,
        String remark,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PlatformOrgResponse fromEntity(PlatformOrgEntity entity) {
        return fromEntity(entity, java.util.List.of());
    }

    public static PlatformOrgResponse fromEntity(PlatformOrgEntity entity,
                                                 java.util.List<SimpleCustomerDto> linkedCustomers) {
        java.util.List<Long> customerIds = entity.getCustomerLinks() == null
                ? java.util.List.of()
                : entity.getCustomerLinks().stream().map(PlatformOrgCustomerEntity::getCustomerId).toList();
        return new PlatformOrgResponse(
                entity.getId(),
                entity.getPlatform(),
                entity.getOrgCodeShort(),
                entity.getOrgFullName(),
                customerIds,
                linkedCustomers != null ? linkedCustomers : java.util.List.of(),
                entity.getContactPerson(),
                entity.getContactPhone(),
                entity.getContactEmail(),
                entity.getSalesPerson(),
                entity.getRegion(),
                entity.getRemark(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
