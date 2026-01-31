package com.dfbs.app.modules.freightbill;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreightBillItemRepo extends JpaRepository<FreightBillItemEntity, Long> {
    List<FreightBillItemEntity> findByBillIdOrderByIdAsc(Long billId);

    List<FreightBillItemEntity> findByBillIdIn(List<Long> billIds);

    void deleteByBillIdAndShipmentId(Long billId, Long shipmentId);

    List<FreightBillItemEntity> findByBillIdAndShipmentId(Long billId, Long shipmentId);
}
