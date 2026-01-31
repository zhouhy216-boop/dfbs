package com.dfbs.app.modules.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartRepo extends JpaRepository<PartEntity, Long> {
    Optional<PartEntity> findByName(String name);
    Optional<PartEntity> findBySystemNo(String systemNo);
    Optional<PartEntity> findByNameAndSpec(String name, String spec);
    Optional<PartEntity> findByDrawingNo(String drawingNo);
    long countBySystemNoStartingWith(String prefix);
    List<PartEntity> findByIsActiveTrue();

    @Query("SELECT p FROM PartEntity p WHERE (:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) "
            + "AND (:spec IS NULL OR :spec = '' OR LOWER(p.spec) LIKE LOWER(CONCAT('%', :spec, '%'))) "
            + "AND (:drawingNo IS NULL OR :drawingNo = '' OR LOWER(p.drawingNo) LIKE LOWER(CONCAT('%', :drawingNo, '%')))")
    List<PartEntity> search(@Param("name") String name, @Param("spec") String spec, @Param("drawingNo") String drawingNo);

    List<PartEntity> findByIdInAndNameContainingIgnoreCase(List<Long> ids, String name);
}
