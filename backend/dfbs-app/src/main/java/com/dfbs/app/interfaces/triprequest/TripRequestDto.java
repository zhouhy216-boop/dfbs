package com.dfbs.app.interfaces.triprequest;

import com.dfbs.app.modules.triprequest.TripRequestEntity;
import com.dfbs.app.modules.triprequest.TripRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TripRequestDto(
        Long id,
        OffsetDateTime createdAt,
        Long createdBy,
        TripRequestStatus status,
        String city,
        LocalDate startDate,
        LocalDate endDate,
        String purpose,
        BigDecimal estTransportCost,
        BigDecimal estAccommodationCost,
        String currency,
        Long workOrderId,
        String independentReason,
        String cancellationReason,
        Long approverLeaderId,
        OffsetDateTime approveLeaderTime,
        Long approverFinanceId,
        OffsetDateTime approveFinanceTime
) {
    public static TripRequestDto from(TripRequestEntity e) {
        return new TripRequestDto(
                e.getId(),
                e.getCreatedAt(),
                e.getCreatedBy(),
                e.getStatus(),
                e.getCity(),
                e.getStartDate(),
                e.getEndDate(),
                e.getPurpose(),
                e.getEstTransportCost(),
                e.getEstAccommodationCost(),
                e.getCurrency(),
                e.getWorkOrderId(),
                e.getIndependentReason(),
                e.getCancellationReason(),
                e.getApproverLeaderId(),
                e.getApproveLeaderTime(),
                e.getApproverFinanceId(),
                e.getApproveFinanceTime()
        );
    }
}
