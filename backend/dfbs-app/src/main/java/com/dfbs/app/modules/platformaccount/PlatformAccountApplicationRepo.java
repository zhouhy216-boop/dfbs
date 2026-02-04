package com.dfbs.app.modules.platformaccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PlatformAccountApplicationRepo extends JpaRepository<PlatformAccountApplicationEntity, Long>,
        JpaSpecificationExecutor<PlatformAccountApplicationEntity> {

    Optional<PlatformAccountApplicationEntity> findByApplicationNo(String applicationNo);

    Optional<PlatformAccountApplicationEntity> findTopByApplicationNoStartingWithOrderByApplicationNoDesc(String prefix);
}
