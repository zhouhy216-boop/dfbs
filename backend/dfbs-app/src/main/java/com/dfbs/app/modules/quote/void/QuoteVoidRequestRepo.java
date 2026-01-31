package com.dfbs.app.modules.quote.void_;

import com.dfbs.app.modules.quote.enums.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteVoidRequestRepo extends JpaRepository<QuoteVoidRequestEntity, Long> {
    List<QuoteVoidRequestEntity> findByQuoteIdOrderByCreatedAtDesc(Long quoteId);
    Optional<QuoteVoidRequestEntity> findFirstByQuoteIdAndStatusOrderByCreatedAtDesc(Long quoteId, VoidRequestStatus status);
}
