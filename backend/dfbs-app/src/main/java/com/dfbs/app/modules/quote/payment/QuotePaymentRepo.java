package com.dfbs.app.modules.quote.payment;

import com.dfbs.app.modules.quote.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuotePaymentRepo extends JpaRepository<QuotePaymentEntity, Long> {
    List<QuotePaymentEntity> findByQuoteId(Long quoteId);
    List<QuotePaymentEntity> findByQuoteIdAndStatus(Long quoteId, PaymentStatus status);
    List<QuotePaymentEntity> findByQuoteIdAndIsFinanceConfirmedTrue(Long quoteId);
    Page<QuotePaymentEntity> findByQuoteId(Long quoteId, Pageable pageable);
}
