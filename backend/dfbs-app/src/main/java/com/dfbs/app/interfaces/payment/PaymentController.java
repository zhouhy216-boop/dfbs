package com.dfbs.app.interfaces.payment;

import com.dfbs.app.application.payment.PaymentService;
import com.dfbs.app.modules.payment.PaymentEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController("paymentAllocationController")
@RequestMapping("/api/v1/general-payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentEntity create(@RequestBody CreatePaymentRequest req) {
        var cmd = new PaymentService.CreatePaymentCommand();
        cmd.setCustomerId(req.customerId());
        cmd.setAmount(req.amount());
        cmd.setCurrency(req.currency());
        cmd.setReceivedAt(req.receivedAt());
        if (req.allocations() != null) {
            List<PaymentService.CreatePaymentCommand.AllocationItem> items = req.allocations().stream()
                    .map(a -> {
                        var item = new PaymentService.CreatePaymentCommand.AllocationItem();
                        item.setQuoteId(a.quoteId());
                        item.setAllocatedAmount(a.allocatedAmount());
                        item.setPeriod(a.period());
                        return item;
                    })
                    .collect(Collectors.toList());
            cmd.setAllocations(items);
        } else {
            cmd.setAllocations(new ArrayList<>());
        }
        return paymentService.create(cmd);
    }

    @PostMapping("/{id}/confirm")
    public PaymentEntity confirm(@PathVariable Long id) {
        return paymentService.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    public PaymentEntity cancel(@PathVariable Long id) {
        return paymentService.cancel(id);
    }
}
