package com.dfbs.app.application.expense;

import com.dfbs.app.modules.expense.ExpenseType;
import com.dfbs.app.modules.quote.enums.Currency;

import java.time.LocalDate;

public class ExpenseStatsRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private GroupBy groupBy = GroupBy.USER;
    private Currency currency;  // optional filter
    private ExpenseType expenseType;  // optional filter

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public GroupBy getGroupBy() { return groupBy; }
    public void setGroupBy(GroupBy groupBy) { this.groupBy = groupBy; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public ExpenseType getExpenseType() { return expenseType; }
    public void setExpenseType(ExpenseType expenseType) { this.expenseType = expenseType; }
}
