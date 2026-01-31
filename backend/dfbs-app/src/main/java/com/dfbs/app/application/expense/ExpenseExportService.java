package com.dfbs.app.application.expense;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.dfbs.app.application.expense.dto.ExpenseDto;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseExportService {

    private final ExpenseStatsService expenseStatsService;

    public ExpenseExportService(ExpenseStatsService expenseStatsService) {
        this.expenseStatsService = expenseStatsService;
    }

    /**
     * Export stats and details to Excel: Sheet "Summary" (ExpenseStatsItemDto rows), Sheet "Details" (ExpenseDto rows).
     */
    public void exportStats(ExpenseStatsRequest request, HttpServletResponse response) throws IOException {
        List<ExpenseStatsItemDto> stats = expenseStatsService.getStats(request);
        List<ExpenseDto> details = expenseStatsService.getDetailExpenses(request);

        List<ExpenseStatsSummaryRow> summaryRows = stats.stream().map(ExpenseStatsSummaryRow::from).collect(Collectors.toList());
        List<ExpenseStatsDetailRow> detailRows = details.stream().map(ExpenseStatsDetailRow::from).collect(Collectors.toList());

        String filename = "expense-stats-" + System.currentTimeMillis() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"");

        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                .autoCloseStream(false)
                .build();

        WriteSheet summarySheet = EasyExcel.writerSheet(0, "Summary")
                .head(ExpenseStatsSummaryRow.class)
                .build();
        excelWriter.write(summaryRows, summarySheet);

        WriteSheet detailsSheet = EasyExcel.writerSheet(1, "Details")
                .head(ExpenseStatsDetailRow.class)
                .build();
        excelWriter.write(detailRows, detailsSheet);

        excelWriter.finish();
    }
}
