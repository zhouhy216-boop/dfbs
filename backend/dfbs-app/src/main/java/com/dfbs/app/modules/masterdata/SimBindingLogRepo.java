package com.dfbs.app.modules.masterdata;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimBindingLogRepo extends JpaRepository<SimBindingLogEntity, Long> {
    List<SimBindingLogEntity> findBySimIdOrderByChangedAtDesc(Long simId, Pageable pageable);
}
