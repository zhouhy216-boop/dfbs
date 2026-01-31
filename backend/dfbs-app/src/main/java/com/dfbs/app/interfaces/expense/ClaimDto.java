package com.dfbs.app.interfaces.expense;

import com.dfbs.app.modules.expense.ClaimEntity;
import com.dfbs.app.modules.expense.ClaimStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ClaimDto(
        Long id,
        String claimNo,
        String title,
        BigDecimal totalAmount,
        String currency,
        ClaimStatus status,
        Long submitterId,
        OffsetDateTime submitTime,
        Long approverId,
        OffsetDateTime approveTime,
        OffsetDateTime createdAt,
        Long createdBy
) {
    public static ClaimDto from(ClaimEntity c) {
        return new ClaimDto(
                c.getId(),
                c.getClaimNo(),
                c.getTitle(),
                c.getTotalAmount(),
                c.getCurrency(),
                c.getStatus(),
                c.getSubmitterId(),
                c.getSubmitTime(),
                c.getApproverId(),
                c.getApproveTime(),
                c.getCreatedAt(),
                c.getCreatedBy()
        );
    }
}
