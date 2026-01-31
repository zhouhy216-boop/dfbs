package com.dfbs.app.modules.quote.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteWorkflowHistoryRepo extends JpaRepository<QuoteWorkflowHistoryEntity, Long> {
    List<QuoteWorkflowHistoryEntity> findByQuoteIdOrderByCreatedAtDesc(Long quoteId);
}
