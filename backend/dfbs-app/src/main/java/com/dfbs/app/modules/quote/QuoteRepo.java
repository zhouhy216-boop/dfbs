package com.dfbs.app.modules.quote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuoteRepo extends JpaRepository<QuoteEntity, Long>, JpaSpecificationExecutor<QuoteEntity> {
}
