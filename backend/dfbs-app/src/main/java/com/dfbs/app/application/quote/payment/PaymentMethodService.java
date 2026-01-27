package com.dfbs.app.application.quote.payment;

import com.dfbs.app.modules.quote.payment.PaymentMethodEntity;
import com.dfbs.app.modules.quote.payment.PaymentMethodRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentMethodService {

    private final PaymentMethodRepo repo;

    public PaymentMethodService(PaymentMethodRepo repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodEntity> listActiveMethods() {
        return repo.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodEntity> listAllMethods() {
        return repo.findAll();
    }
}
