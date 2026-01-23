package com.dfbs.app.modules.quote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuoteSequenceRepo extends JpaRepository<QuoteSequenceEntity, Long> {

    Optional<QuoteSequenceEntity> findByUserInitialsAndYearMonth(String userInitials, String yearMonth);
}
