package com.dfbs.app.modules.freightbill;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FreightBillRepo extends JpaRepository<FreightBillEntity, Long> {
    boolean existsByBillNo(String billNo);
}
