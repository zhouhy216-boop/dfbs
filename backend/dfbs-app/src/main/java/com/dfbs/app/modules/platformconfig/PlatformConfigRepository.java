package com.dfbs.app.modules.platformconfig;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformConfigRepository extends JpaRepository<PlatformConfigEntity, Long> {

    Optional<PlatformConfigEntity> findByPlatformCode(String platformCode);

    List<PlatformConfigEntity> findAllByIsActiveTrueOrderByPlatformCode();
}
