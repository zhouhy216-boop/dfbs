package com.dfbs.app.modules.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepo extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByStatementId(Long statementId);

    boolean existsByPaymentNo(String paymentNo);
}
