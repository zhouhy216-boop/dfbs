package com.dfbs.app.modules.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceItemRefRepo extends JpaRepository<InvoiceItemRefEntity, Long> {
    List<InvoiceItemRefEntity> findByInvoiceRecordId(Long invoiceRecordId);
    List<InvoiceItemRefEntity> findByQuoteId(Long quoteId);
}
