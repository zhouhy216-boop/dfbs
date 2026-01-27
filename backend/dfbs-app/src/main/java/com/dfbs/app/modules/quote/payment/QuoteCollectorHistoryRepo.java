package com.dfbs.app.modules.quote.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteCollectorHistoryRepo extends JpaRepository<QuoteCollectorHistoryEntity, Long> {
    List<QuoteCollectorHistoryEntity> findByQuoteIdOrderByChangedAtDesc(Long quoteId);
}
