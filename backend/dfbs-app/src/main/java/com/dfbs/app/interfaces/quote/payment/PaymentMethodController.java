package com.dfbs.app.interfaces.quote.payment;

import com.dfbs.app.application.quote.payment.PaymentMethodService;
import com.dfbs.app.modules.quote.payment.PaymentMethodEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService service;

    public PaymentMethodController(PaymentMethodService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaymentMethodEntity> listMethods() {
        return service.listAllMethods();
    }

    @GetMapping("/active")
    public List<PaymentMethodEntity> listActiveMethods() {
        return service.listActiveMethods();
    }
}
