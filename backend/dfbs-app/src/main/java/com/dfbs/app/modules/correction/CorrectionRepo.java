package com.dfbs.app.modules.correction;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CorrectionRepo extends JpaRepository<CorrectionEntity, Long> {
    boolean existsByCorrectionNo(String correctionNo);
}
