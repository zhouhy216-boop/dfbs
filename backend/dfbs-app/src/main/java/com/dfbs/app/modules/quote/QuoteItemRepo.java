package com.dfbs.app.modules.quote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteItemRepo extends JpaRepository<QuoteItemEntity, Long> {

    List<QuoteItemEntity> findByQuoteIdOrderByLineOrderAsc(Long quoteId);

    void deleteByQuoteId(Long quoteId);
}
