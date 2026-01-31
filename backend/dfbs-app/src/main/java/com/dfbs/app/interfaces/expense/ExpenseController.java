package com.dfbs.app.interfaces.expense;

import com.dfbs.app.application.expense.ExpenseService;
import com.dfbs.app.application.expense.dto.ExpenseDto;
import com.dfbs.app.modules.expense.ExpenseEntity;
import com.dfbs.app.modules.expense.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseEntity create(@RequestBody CreateExpenseRequest req) {
        var cmd = new ExpenseService.CreateExpenseCommand();
        cmd.setExpenseDate(req.expenseDate());
        cmd.setAmount(req.amount());
        cmd.setCurrency(req.currency());
        cmd.setExpenseType(req.expenseType());
        cmd.setDescription(req.description());
        cmd.setQuoteId(req.quoteId());
        cmd.setWorkOrderId(req.workOrderId());
        cmd.setInventoryOutboundId(req.inventoryOutboundId());
        cmd.setTripRequestId(req.tripRequestId());
        return expenseService.create(cmd);
    }

    @PutMapping("/{id}")
    public ExpenseEntity update(@PathVariable Long id, @RequestBody UpdateExpenseRequest req) {
        var cmd = new ExpenseService.UpdateExpenseCommand();
        cmd.setExpenseDate(req.expenseDate());
        cmd.setAmount(req.amount());
        cmd.setCurrency(req.currency());
        cmd.setExpenseType(req.expenseType());
        cmd.setDescription(req.description());
        cmd.setQuoteId(req.quoteId());
        cmd.setWorkOrderId(req.workOrderId());
        cmd.setInventoryOutboundId(req.inventoryOutboundId());
        cmd.setTripRequestId(req.tripRequestId());
        return expenseService.update(id, cmd);
    }

    @PostMapping("/{id}/void")
    public ExpenseEntity voidExpense(@PathVariable Long id) {
        return expenseService.voidExpense(id);
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseDto>> search(
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) java.time.LocalDate fromDate,
            @RequestParam(required = false) java.time.LocalDate toDate,
            Pageable pageable) {
        var request = new ExpenseService.ExpenseSearchRequest();
        request.setStatus(status);
        request.setFromDate(fromDate);
        request.setToDate(toDate);
        Page<ExpenseEntity> page = expenseService.search(request, pageable);
        return ResponseEntity.ok(page.map(ExpenseDto::from));
    }
}
