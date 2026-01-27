package com.dfbs.app.modules.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartRepo extends JpaRepository<PartEntity, Long> {
    Optional<PartEntity> findByName(String name);
    List<PartEntity> findByIsActiveTrue();
}
