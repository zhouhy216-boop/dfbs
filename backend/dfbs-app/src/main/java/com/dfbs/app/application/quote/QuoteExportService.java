package com.dfbs.app.application.quote;

import com.dfbs.app.config.CompanyInfoProperties;
import com.dfbs.app.config.CurrentUserProvider;
import com.dfbs.app.config.UserInfoProvider;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.spire.xls.FileFormat;
import com.spire.xls.Workbook;

@Service
public class QuoteExportService {

    private static final String TEMPLATE_PATH = "templates/excel/quote_template_v3.xlsx";
    private static final int ITEM_START_ROW = 9;  // 0-based, row 10
    private static final int PRESET_ITEM_ROWS = 9;
    private static final int TOTAL_ROW_ORIGINAL = 18;  // 0-based, row 19
    private static final int[] ITEM_COLS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};  // A-J

    private final QuoteService quoteService;
    private final QuoteItemService itemService;
    private final CompanyInfoProperties companyInfo;
    private final CurrentUserProvider currentUserProvider;
    private final UserInfoProvider userInfoProvider;

    public QuoteExportService(QuoteService quoteService, QuoteItemService itemService,
                              CompanyInfoProperties companyInfo, CurrentUserProvider currentUserProvider,
                              UserInfoProvider userInfoProvider) {
        this.quoteService = quoteService;
        this.itemService = itemService;
        this.companyInfo = companyInfo;
        this.currentUserProvider = currentUserProvider;
        this.userInfoProvider = userInfoProvider;
    }

    public record ExportResult(byte[] bytes, String filename) {}

    public ExportResult export(Long quoteId, String format) throws Exception {
        QuoteEntity quote = quoteService.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("quote not found: id=" + quoteId));
        if (quote.getStatus() != QuoteStatus.DRAFT && quote.getStatus() != QuoteStatus.CONFIRMED) {
            throw new IllegalStateException("Only DRAFT or CONFIRMED quote can be exported");
        }

        List<QuoteItemService.QuoteItemDto> items = itemService.getItems(quoteId);
        byte[] excelBytes = fillTemplate(quote, items);
        String ext = "pdf".equalsIgnoreCase(format) ? "pdf" : "xlsx";
        String safeNo = quote.getQuoteNo() != null ? quote.getQuoteNo().replaceAll("[^a-zA-Z0-9_-]", "_") : ("quote_" + quoteId);
        String filename = "quote_" + safeNo + "." + ext;

        byte[] out = "pdf".equalsIgnoreCase(format) ? excelToPdf(excelBytes) : excelBytes;
        return new ExportResult(out, filename);
    }

    private byte[] fillTemplate(QuoteEntity quote, List<QuoteItemService.QuoteItemDto> items) throws Exception {
        try (var resource = new ClassPathResource(TEMPLATE_PATH).getInputStream();
             org.apache.poi.ss.usermodel.Workbook poiWorkbook = new XSSFWorkbook(resource)) {
            Sheet sheet = poiWorkbook.getSheetAt(0);

            String customerName = quote.getRecipient() != null ? quote.getRecipient() : ("客户#" + quote.getCustomerId());

            setCellValue(sheet, 1, 1, quote.getQuoteNo());
            setCellValue(sheet, 2, 1, customerName);
            setCellValue(sheet, 3, 1, nvl(quote.getRecipient()));
            setCellValue(sheet, 4, 1, nvl(quote.getPhone()));
            setCellValue(sheet, 5, 1, nvl(quote.getAddress()));

            Currency currency = quote.getCurrency() != null ? quote.getCurrency() : Currency.CNY;
            String unitLabel = currencyUnitLabel(currency);
            Cell g9 = CellUtil.getCell(CellUtil.getRow(8, sheet), 6);
            Cell h9 = CellUtil.getCell(CellUtil.getRow(8, sheet), 7);
            g9.setCellValue("单价（" + unitLabel + "）");
            h9.setCellValue("金额（" + unitLabel + "）");

            int size = items.size();
            Row templateRow = sheet.getRow(ITEM_START_ROW);
            CellStyle[] styles = templateRow != null ? copyRowStyles(templateRow, poiWorkbook) : null;

            if (size <= PRESET_ITEM_ROWS) {
                for (int i = 0; i < PRESET_ITEM_ROWS; i++) {
                    Row row = sheet.getRow(ITEM_START_ROW + i);
                    if (row == null) row = sheet.createRow(ITEM_START_ROW + i);
                    if (i < size) {
                        fillItemRow(row, items.get(i), i + 1, styles, poiWorkbook);
                    } else {
                        clearRow(row, 10);
                    }
                }
            } else {
                int insertCount = size - PRESET_ITEM_ROWS;
                int lastRow = sheet.getLastRowNum();
                sheet.shiftRows(TOTAL_ROW_ORIGINAL, Math.max(lastRow, TOTAL_ROW_ORIGINAL), insertCount, true, false);
                for (int i = 0; i < size; i++) {
                    Row row = sheet.getRow(ITEM_START_ROW + i);
                    if (row == null) row = sheet.createRow(ITEM_START_ROW + i);
                    fillItemRow(row, items.get(i), i + 1, styles, poiWorkbook);
                }
            }

            int totalRowIdx = ITEM_START_ROW + size;
            Row totalRow = sheet.getRow(totalRowIdx);
            if (totalRow == null) totalRow = sheet.createRow(totalRowIdx);

            BigDecimal totalAmount = items.stream()
                    .map(QuoteItemService.QuoteItemDto::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            String totalC = "";
            String totalSuffix = "元整";
            String totalSymbol = "￥";
            switch (currency) {
                case CNY -> {
                    totalC = toChineseUppercase(totalAmount);
                    totalSuffix = "元整";
                    totalSymbol = "￥";
                }
                case USD -> {
                    totalSuffix = "美元";
                    totalSymbol = "$";
                }
                case JPY -> {
                    totalSuffix = "日元";
                    totalSymbol = "JP¥";
                }
            }

            Cell totalCellC = CellUtil.getCell(totalRow, 2);
            totalCellC.setCellValue(totalC);
            Cell totalCellF = CellUtil.getCell(totalRow, 5);
            totalCellF.setCellValue(totalSuffix);
            Cell totalCellI = CellUtil.getCell(totalRow, 8);
            totalCellI.setCellValue(totalSuffix);
            Cell totalCellH = CellUtil.getCell(totalRow, 7);
            totalCellH.setCellValue(totalSymbol + totalAmount.toPlainString());

            int bankRowIdx = totalRowIdx + 1;
            ensureRow(sheet, bankRowIdx);
            setCellValue(sheet, bankRowIdx, 1, companyInfo.getName());
            setCellValue(sheet, bankRowIdx + 1, 1, "开户行：" + companyInfo.getBankName() + " " + companyInfo.getBankNo());
            setCellValue(sheet, bankRowIdx + 2, 1, "账号：" + companyInfo.getAccountNo());
            setCellValue(sheet, bankRowIdx + 3, 1, "税号：" + companyInfo.getTaxNo());
            setCellValue(sheet, bankRowIdx + 4, 1, "电话：" + companyInfo.getPhone());
            setCellValue(sheet, bankRowIdx + 5, 1, "地址：" + companyInfo.getAddress());

            // Use assigneeId if available, otherwise fall back to createdBy
            UserInfoProvider.UserInfo userInfo = null;
            if (quote.getAssigneeId() != null) {
                userInfo = userInfoProvider.getUserInfo(quote.getAssigneeId());
            }
            String submitterName = userInfo != null ? userInfo.name() : 
                    (quote.getCreatedBy() != null ? quote.getCreatedBy() : currentUserProvider.getCurrentUser());
            String submitterPhone = userInfo != null ? userInfo.phone() : "";
            String submitterOffice = userInfo != null ? userInfo.office() : "";

            ensureRow(sheet, bankRowIdx + 6);
            setCellValue(sheet, bankRowIdx + 6, 1, "提交人：" + (submitterName != null ? submitterName : ""));
            setCellValue(sheet, bankRowIdx + 7, 1, "电话：" + submitterPhone);
            setCellValue(sheet, bankRowIdx + 8, 1, "办事处：" + submitterOffice);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            poiWorkbook.write(out);
            return out.toByteArray();
        }
    }

    private static void setCellValue(Sheet sheet, int rowIdx, int colIdx, String value) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);
        Cell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);
        cell.setCellValue(value != null ? value : "");
    }

    private static void ensureRow(Sheet sheet, int rowIdx) {
        if (sheet.getRow(rowIdx) == null) sheet.createRow(rowIdx);
    }

    private static void clearRow(Row row, int colCount) {
        for (int c = 0; c < colCount; c++) {
            Cell cell = row.getCell(c);
            if (cell != null) cell.setCellValue("");
        }
    }

    private static void fillItemRow(Row row, QuoteItemService.QuoteItemDto item, int seq,
                                    CellStyle[] styles, org.apache.poi.ss.usermodel.Workbook workbook) {
        String expenseDesc = item.getExpenseType() != null ? item.getExpenseType().getDescription() : "";
        String warehouseLabel = item.getWarehouse() != null ? item.getWarehouse().getLabel() : "";
        for (int i = 0; i < ITEM_COLS.length; i++) {
            Cell cell = row.getCell(ITEM_COLS[i]);
            if (cell == null) cell = row.createCell(ITEM_COLS[i]);
            switch (i) {
                case 0 -> cell.setCellValue(seq);
                case 1 -> cell.setCellValue(expenseDesc);
                case 2 -> cell.setCellValue(nvl(item.getDescription()));
                case 3 -> cell.setCellValue(nvl(item.getSpec()));
                case 4 -> cell.setCellValue(nvl(item.getUnit()));
                case 5 -> cell.setCellValue(item.getQuantity() != null ? item.getQuantity() : 0);
                case 6 -> cell.setCellValue(item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0);
                case 7 -> cell.setCellValue(item.getAmount() != null ? item.getAmount().doubleValue() : 0);
                case 8 -> cell.setCellValue(warehouseLabel);
                case 9 -> cell.setCellValue(nvl(item.getRemark()));
                default -> {}
            }
            if (styles != null && i < styles.length && styles[i] != null) {
                cell.setCellStyle(styles[i]);
            }
        }
    }

    private static CellStyle[] copyRowStyles(Row row, org.apache.poi.ss.usermodel.Workbook workbook) {
        CellStyle[] styles = new CellStyle[10];
        for (int i = 0; i < 10; i++) {
            Cell c = row.getCell(i);
            if (c != null && c.getCellStyle() != null) {
                CellStyle copy = workbook.createCellStyle();
                copy.cloneStyleFrom(c.getCellStyle());
                styles[i] = copy;
            }
        }
        return styles;
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String currencyUnitLabel(Currency c) {
        return switch (c) {
            case CNY -> "人民币";
            case USD -> "美元";
            case JPY -> "日元";
        };
    }

    private static String toChineseUppercase(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return "零元整";
        String[] digits = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[] units = {"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿"};
        long yuan = amount.longValue();
        int fen = amount.multiply(BigDecimal.valueOf(100)).remainder(BigDecimal.valueOf(100)).intValue();
        if (yuan == 0 && fen == 0) return "零元整";
        StringBuilder sb = new StringBuilder();
        String y = String.valueOf(yuan);
        int n = y.length();
        for (int i = 0; i < n; i++) {
            int d = y.charAt(i) - '0';
            if (d != 0) sb.append(digits[d]).append(units[n - 1 - i]);
            else if (sb.length() > 0 && !"零".equals(sb.substring(sb.length() - 1))) sb.append("零");
        }
        if (sb.length() == 0) sb.append("零");
        sb.append("元");
        if (fen == 0) sb.append("整");
        else {
            sb.append(digits[fen / 10]).append("角");
            if (fen % 10 != 0) sb.append(digits[fen % 10]).append("分");
        }
        return sb.toString();
    }

    private byte[] excelToPdf(byte[] excelBytes) throws Exception {
        Workbook workbook = new Workbook();
        workbook.loadFromStream(new ByteArrayInputStream(excelBytes));
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        workbook.saveToStream(pdfOut, FileFormat.PDF);
        return pdfOut.toByteArray();
    }
}
