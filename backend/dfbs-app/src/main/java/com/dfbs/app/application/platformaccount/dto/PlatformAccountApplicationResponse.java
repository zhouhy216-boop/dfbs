package com.dfbs.app.application.platformaccount.dto;

import com.dfbs.app.modules.platformaccount.ApplicationSourceType;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationEntity;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationStatus;
import com.dfbs.app.modules.platformorg.PlatformOrgPlatform;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlatformAccountApplicationResponse(
        Long id,
        String applicationNo,
        PlatformAccountApplicationStatus status,
        PlatformOrgPlatform platform,
        ApplicationSourceType sourceType,
        Long customerId,
        String customerName,
        String orgCodeShort,
        String orgFullName,
        String contactPerson,
        String phone,
        String email,
        String region,
        String salesPerson,
        String contractNo,
        BigDecimal price,
        Integer quantity,
        String reason,
        Boolean isCcPlanner,
        Long applicantId,
        Long plannerId,
        Long adminId,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PlatformAccountApplicationResponse fromEntity(PlatformAccountApplicationEntity entity) {
        return new PlatformAccountApplicationResponse(
                entity.getId(),
                entity.getApplicationNo(),
                entity.getStatus(),
                entity.getPlatform(),
                entity.getSourceType() != null ? entity.getSourceType() : ApplicationSourceType.FACTORY,
                entity.getCustomerId(),
                entity.getCustomerName(),
                entity.getOrgCodeShort(),
                entity.getOrgFullName(),
                entity.getContactPerson(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getRegion(),
                entity.getSalesPerson(),
                entity.getContractNo(),
                entity.getPrice(),
                entity.getQuantity(),
                entity.getReason(),
                entity.getIsCcPlanner(),
                entity.getApplicantId(),
                entity.getPlannerId(),
                entity.getAdminId(),
                entity.getRejectReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
