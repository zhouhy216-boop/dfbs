package com.dfbs.app.modules.damage.config;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DamageTypeRepo extends JpaRepository<DamageTypeEntity, Long> {
    List<DamageTypeEntity> findByIsEnabledTrue();
}
