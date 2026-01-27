package com.dfbs.app.modules.quote.dictionary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeeTypeRepo extends JpaRepository<FeeTypeEntity, Long> {
    Optional<FeeTypeEntity> findByName(String name);
    List<FeeTypeEntity> findByCategoryId(Long categoryId);
    List<FeeTypeEntity> findByIsActiveTrue();
}
