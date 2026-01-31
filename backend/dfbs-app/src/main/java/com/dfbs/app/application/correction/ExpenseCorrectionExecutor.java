package com.dfbs.app.application.correction;

import com.dfbs.app.application.expense.ExpenseService;
import com.dfbs.app.modules.expense.ExpenseEntity;
import com.dfbs.app.modules.expense.ExpenseRepo;
import com.dfbs.app.modules.expense.ExpenseStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
public class ExpenseCorrectionExecutor implements CorrectionExecutor {

    private final ExpenseService expenseService;
    private final ExpenseRepo expenseRepo;

    public ExpenseCorrectionExecutor(ExpenseService expenseService, ExpenseRepo expenseRepo) {
        this.expenseService = expenseService;
        this.expenseRepo = expenseRepo;
    }

    @Override
    @Transactional
    public void voidOld(Long id) {
        expenseService.voidExpenseForCorrection(id);
    }

    @Override
    @Transactional
    public Long createNew(Long oldId, String changesJson, Long createdBy) {
        ExpenseEntity old = expenseRepo.findById(oldId)
                .orElseThrow(() -> new IllegalStateException("Expense not found: id=" + oldId));

        ExpenseEntity neu = new ExpenseEntity();
        neu.setCreatedAt(OffsetDateTime.now());
        neu.setCreatedBy(createdBy != null ? createdBy : old.getCreatedBy());
        neu.setExpenseDate(old.getExpenseDate());
        neu.setAmount(old.getAmount());
        neu.setCurrency(old.getCurrency());
        neu.setExpenseType(old.getExpenseType());
        neu.setDescription(old.getDescription());
        neu.setStatus(ExpenseStatus.DRAFT);
        neu.setQuoteId(old.getQuoteId());
        neu.setWorkOrderId(old.getWorkOrderId());
        neu.setInventoryOutboundId(old.getInventoryOutboundId());
        neu.setTripRequestId(old.getTripRequestId());
        neu.setClaim(null);
        neu = expenseRepo.save(neu);
        return neu.getId();
    }
}
