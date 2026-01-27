package com.dfbs.app.modules.quote.void_;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteVoidApplicationRepo extends JpaRepository<QuoteVoidApplicationEntity, Long> {
    List<QuoteVoidApplicationEntity> findByQuoteIdOrderByApplyTimeDesc(Long quoteId);
    List<QuoteVoidApplicationEntity> findByQuoteIdAndAuditResultIsNull(Long quoteId);
}
