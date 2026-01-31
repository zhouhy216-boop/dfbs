package com.dfbs.app.application.correction;

import com.dfbs.app.application.payment.PaymentService;
import com.dfbs.app.modules.payment.PaymentAllocationEntity;
import com.dfbs.app.modules.payment.PaymentEntity;
import com.dfbs.app.modules.payment.PaymentRepo;
import com.dfbs.app.modules.payment.PaymentStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@Component
public class PaymentCorrectionExecutor implements CorrectionExecutor {

    private final PaymentService paymentService;
    private final PaymentRepo paymentRepo;

    public PaymentCorrectionExecutor(PaymentService paymentService, PaymentRepo paymentRepo) {
        this.paymentService = paymentService;
        this.paymentRepo = paymentRepo;
    }

    @Override
    @Transactional
    public void voidOld(Long id) {
        paymentService.cancel(id);
    }

    @Override
    @Transactional
    public Long createNew(Long oldId, String changesJson, Long createdBy) {
        PaymentEntity old = paymentRepo.findById(oldId)
                .orElseThrow(() -> new IllegalStateException("Payment not found: id=" + oldId));
        if (old.getStatementId() != null) {
            throw new IllegalStateException("Cannot clone payment already bound to a statement");
        }

        String newPaymentNo = "PAY-" + System.currentTimeMillis();
        while (paymentRepo.existsByPaymentNo(newPaymentNo)) newPaymentNo = "PAY-" + System.currentTimeMillis();

        PaymentEntity neu = new PaymentEntity();
        neu.setPaymentNo(newPaymentNo);
        neu.setCustomerId(old.getCustomerId());
        neu.setAmount(old.getAmount());
        neu.setCurrency(old.getCurrency());
        neu.setReceivedAt(old.getReceivedAt());
        neu.setStatus(PaymentStatus.DRAFT);
        neu.setStatementId(null);
        neu.setCreatedBy(createdBy);
        neu.setCreatedAt(OffsetDateTime.now());
        neu.setAllocations(new ArrayList<>());
        neu = paymentRepo.save(neu);

        for (PaymentAllocationEntity oa : old.getAllocations()) {
            PaymentAllocationEntity na = new PaymentAllocationEntity();
            na.setPayment(neu);
            na.setQuoteId(oa.getQuoteId());
            na.setAllocatedAmount(oa.getAllocatedAmount());
            na.setPeriod(oa.getPeriod());
            neu.getAllocations().add(na);
        }
        paymentRepo.save(neu);
        return neu.getId();
    }
}
