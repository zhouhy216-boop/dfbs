package com.dfbs.app.modules.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceApplicationRepo extends JpaRepository<InvoiceApplicationEntity, Long> {
    Optional<InvoiceApplicationEntity> findByApplicationNo(String applicationNo);
    List<InvoiceApplicationEntity> findByCollectorIdOrderByCreatedAtDesc(Long collectorId);
}
