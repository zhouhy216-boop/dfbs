package com.dfbs.app.modules.iccid;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface IccidRepo extends JpaRepository<IccidEntity, UUID> {
    Optional<IccidEntity> findByIccidNo(String iccidNo);
}
