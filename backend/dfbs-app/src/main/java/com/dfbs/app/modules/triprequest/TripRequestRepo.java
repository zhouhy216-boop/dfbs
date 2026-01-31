package com.dfbs.app.modules.triprequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TripRequestRepo extends JpaRepository<TripRequestEntity, Long>, JpaSpecificationExecutor<TripRequestEntity> {
}
