package com.dfbs.app.modules.warehouse;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WhWarehouseRepo extends JpaRepository<WhWarehouseEntity, Long> {

    List<WhWarehouseEntity> findByType(WarehouseType type);

    List<WhWarehouseEntity> findByIsActiveTrue();

    /** Find satellite warehouse managed by the given user (for work order part consumption). */
    Optional<WhWarehouseEntity> findFirstByManagerIdAndTypeAndIsActiveTrue(Long managerId, WarehouseType type);
}
