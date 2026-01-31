package com.dfbs.app.modules.damage.config;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DamageTreatmentRepo extends JpaRepository<DamageTreatmentEntity, Long> {
    List<DamageTreatmentEntity> findByIsEnabledTrue();
}
