package com.dfbs.app.interfaces.quote.dto;

import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.DownstreamType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;

import java.time.LocalDateTime;

public record QuoteResponseDto(
        Long id,
        String quoteNo,
        QuoteStatus status,
        QuoteSourceType sourceType,
        String sourceRefId,
        String sourceId,
        String machineInfo,
        Long assigneeId,
        Long customerId,
        String customerName,
        String originalCustomerName,
        String recipient,
        String phone,
        String address,
        Currency currency,
        String createdBy,
        LocalDateTime createdAt,
        DownstreamType downstreamType,
        Long downstreamId
) {
    public static QuoteResponseDto from(QuoteEntity e) {
        return fromWithCustomerName(e, e.getCustomerName());
    }

    /** Build DTO with resolved customer name (e.g. from CustomerRepo). */
    public static QuoteResponseDto fromWithCustomerName(QuoteEntity e, String customerName) {
        return new QuoteResponseDto(
                e.getId(),
                e.getQuoteNo(),
                e.getStatus(),
                e.getSourceType(),
                e.getSourceRefId(),
                e.getSourceId(),
                e.getMachineInfo(),
                e.getAssigneeId(),
                e.getCustomerId(),
                customerName != null ? customerName : ("客户 #" + e.getCustomerId()),
                e.getOriginalCustomerName(),
                e.getRecipient(),
                e.getPhone(),
                e.getAddress(),
                e.getCurrency(),
                e.getCreatedBy(),
                e.getCreatedAt(),
                e.getDownstreamType(),
                e.getDownstreamId()
        );
    }
}
