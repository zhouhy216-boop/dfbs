package com.dfbs.app.modules.quote.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepo extends JpaRepository<PaymentMethodEntity, Long> {
    Optional<PaymentMethodEntity> findByName(String name);
    List<PaymentMethodEntity> findByIsActiveTrue();
}
