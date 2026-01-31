package com.dfbs.app.modules.statement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountStatementItemRepo extends JpaRepository<AccountStatementItemEntity, Long> {

    List<AccountStatementItemEntity> findByStatementIdOrderByIdAsc(Long statementId);

    Optional<AccountStatementItemEntity> findByStatementIdAndQuoteId(Long statementId, Long quoteId);

    void deleteByStatementIdAndQuoteId(Long statementId, Long quoteId);
}
