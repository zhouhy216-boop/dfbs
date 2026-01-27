package com.dfbs.app.interfaces.quote.dto;

import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.enums.Currency;
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
        String recipient,
        String phone,
        String address,
        Currency currency,
        String createdBy,
        LocalDateTime createdAt
) {
    public static QuoteResponseDto from(QuoteEntity e) {
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
                e.getRecipient(),
                e.getPhone(),
                e.getAddress(),
                e.getCurrency(),
                e.getCreatedBy(),
                e.getCreatedAt()
        );
    }
}
