package com.dfbs.app.modules.orgstructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobLevelRepo extends JpaRepository<JobLevelEntity, Long> {

    List<JobLevelEntity> findAllByOrderByOrderIndexAsc();
}
