package com.dfbs.app.modules.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRecordRepo extends JpaRepository<InvoiceRecordEntity, Long> {
    List<InvoiceRecordEntity> findByApplicationId(Long applicationId);
}
