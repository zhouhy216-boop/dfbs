package com.dfbs.app.interfaces.expense;

import com.dfbs.app.application.expense.ExpenseExportService;
import com.dfbs.app.application.expense.ExpenseStatsItemDto;
import com.dfbs.app.application.expense.ExpenseStatsRequest;
import com.dfbs.app.application.expense.ExpenseStatsService;
import com.dfbs.app.application.expense.GroupBy;
import com.dfbs.app.modules.expense.ExpenseType;
import com.dfbs.app.modules.quote.enums.Currency;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stats/expenses")
public class ExpenseStatsController {

    private final ExpenseStatsService expenseStatsService;
    private final ExpenseExportService expenseExportService;

    public ExpenseStatsController(ExpenseStatsService expenseStatsService, ExpenseExportService expenseExportService) {
        this.expenseStatsService = expenseStatsService;
        this.expenseExportService = expenseExportService;
    }

    @PostMapping
    public ResponseEntity<List<ExpenseStatsItemDto>> getStats(@RequestBody ExpenseStatsRequest request) {
        if (request == null) request = new ExpenseStatsRequest();
        return ResponseEntity.ok(expenseStatsService.getStats(request));
    }

    @GetMapping("/export")
    public void exportStats(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) GroupBy groupBy,
            @RequestParam(required = false) Currency currency,
            @RequestParam(required = false) ExpenseType expenseType,
            HttpServletResponse response) throws IOException {
        var request = new ExpenseStatsRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setGroupBy(groupBy != null ? groupBy : GroupBy.USER);
        request.setCurrency(currency);
        request.setExpenseType(expenseType);
        expenseExportService.exportStats(request, response);
    }
}
