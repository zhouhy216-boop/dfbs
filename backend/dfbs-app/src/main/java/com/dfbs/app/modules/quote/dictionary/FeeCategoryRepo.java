package com.dfbs.app.modules.quote.dictionary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeeCategoryRepo extends JpaRepository<FeeCategoryEntity, Long> {
    Optional<FeeCategoryEntity> findByName(String name);
}
