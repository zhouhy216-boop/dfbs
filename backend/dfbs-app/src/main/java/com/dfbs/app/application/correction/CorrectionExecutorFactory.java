package com.dfbs.app.application.correction;

import com.dfbs.app.modules.correction.CorrectionTargetType;
import org.springframework.stereotype.Component;

@Component
public class CorrectionExecutorFactory {

    private final QuoteCorrectionExecutor quoteExecutor;
    private final PaymentCorrectionExecutor paymentExecutor;
    private final ExpenseCorrectionExecutor expenseExecutor;
    private final FreightBillCorrectionExecutor freightBillExecutor;

    public CorrectionExecutorFactory(QuoteCorrectionExecutor quoteExecutor,
                                      PaymentCorrectionExecutor paymentExecutor,
                                      ExpenseCorrectionExecutor expenseExecutor,
                                      FreightBillCorrectionExecutor freightBillExecutor) {
        this.quoteExecutor = quoteExecutor;
        this.paymentExecutor = paymentExecutor;
        this.expenseExecutor = expenseExecutor;
        this.freightBillExecutor = freightBillExecutor;
    }

    public CorrectionExecutor get(CorrectionTargetType targetType) {
        return switch (targetType) {
            case QUOTE -> quoteExecutor;
            case PAYMENT -> paymentExecutor;
            case EXPENSE -> expenseExecutor;
            case FREIGHT_BILL -> freightBillExecutor;
        };
    }
}
