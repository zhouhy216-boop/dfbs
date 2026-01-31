package com.dfbs.app.modules.carrier;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarrierRepo extends JpaRepository<CarrierEntity, Long> {

    List<CarrierEntity> findByIsActiveTrue();
}
