package com.dfbs.app.application.statement;

import com.dfbs.app.modules.payment.PaymentEntity;
import com.dfbs.app.modules.payment.PaymentRepo;
import com.dfbs.app.modules.payment.PaymentStatus;
import com.dfbs.app.modules.statement.AccountStatementEntity;
import com.dfbs.app.modules.statement.AccountStatementRepo;
import com.dfbs.app.modules.statement.StatementStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StatementReconcileService {

    private final AccountStatementRepo statementRepo;
    private final PaymentRepo paymentRepo;

    public StatementReconcileService(AccountStatementRepo statementRepo, PaymentRepo paymentRepo) {
        this.statementRepo = statementRepo;
        this.paymentRepo = paymentRepo;
    }

    /**
     * Bind confirmed payments to a statement. Sum(payments.amount) must exactly equal statement.totalAmount.
     */
    @Transactional
    public AccountStatementEntity bindPayments(Long statementId, List<Long> paymentIds) {
        AccountStatementEntity statement = statementRepo.findById(statementId)
                .orElseThrow(() -> new IllegalStateException("Statement not found: id=" + statementId));
        if (paymentIds == null || paymentIds.isEmpty())
            throw new IllegalStateException("At least one payment is required");

        BigDecimal sum = BigDecimal.ZERO;
        List<PaymentEntity> payments = new java.util.ArrayList<>();
        for (Long pid : paymentIds) {
            PaymentEntity p = paymentRepo.findById(pid)
                    .orElseThrow(() -> new IllegalStateException("Payment not found: id=" + pid));
            if (p.getStatus() != PaymentStatus.CONFIRMED)
                throw new IllegalStateException("Payment " + p.getPaymentNo() + " is not CONFIRMED");
            if (p.getStatementId() != null)
                throw new IllegalStateException("Payment " + p.getPaymentNo() + " is already bound to a statement");
            if (!p.getCustomerId().equals(statement.getCustomerId()))
                throw new IllegalStateException("Payment " + p.getPaymentNo() + " does not belong to statement's customer");
            if (p.getCurrency() != statement.getCurrency())
                throw new IllegalStateException("Payment " + p.getPaymentNo() + " currency does not match statement");
            payments.add(p);
            sum = sum.add(p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO);
        }

        BigDecimal statementTotal = statement.getTotalAmount() != null ? statement.getTotalAmount() : BigDecimal.ZERO;
        if (sum.compareTo(statementTotal) != 0)
            throw new IllegalStateException("Sum of payment amounts (" + sum + ") must equal statement total (" + statementTotal + ")");

        for (PaymentEntity p : payments) {
            p.setStatementId(statementId);
            paymentRepo.save(p);
        }
        statement.setStatus(StatementStatus.RECONCILED);
        return statementRepo.save(statement);
    }
}
