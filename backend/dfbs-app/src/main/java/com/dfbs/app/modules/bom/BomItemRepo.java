package com.dfbs.app.modules.bom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BomItemRepo extends JpaRepository<BomItemEntity, Long> {

    List<BomItemEntity> findByVersionIdOrderByIndexNoAsc(Long versionId);

    boolean existsByVersionIdAndIndexNo(Long versionId, String indexNo);
}
