package com.dfbs.app.modules.quote;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepo extends JpaRepository<QuoteEntity, Long> {
}
