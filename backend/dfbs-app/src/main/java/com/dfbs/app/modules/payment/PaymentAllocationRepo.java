package com.dfbs.app.modules.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentAllocationRepo extends JpaRepository<PaymentAllocationEntity, Long> {

    List<PaymentAllocationEntity> findByPayment_IdOrderByIdAsc(Long paymentId);
}
