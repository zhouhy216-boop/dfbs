package com.dfbs.app.modules.settings;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessLineRepo extends JpaRepository<BusinessLineEntity, Long> {
    Optional<BusinessLineEntity> findByName(String name);
    List<BusinessLineEntity> findByIsActiveTrue();
}
