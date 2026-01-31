package com.dfbs.app.application.statement;

import com.dfbs.app.application.quote.payment.QuotePaymentService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.statement.*;
import com.dfbs.app.modules.user.UserRepo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountStatementService {

    private final AccountStatementRepo statementRepo;
    private final AccountStatementItemRepo itemRepo;
    private final QuoteRepo quoteRepo;
    private final QuoteItemRepo quoteItemRepo;
    private final QuotePaymentService paymentService;
    private final UserRepo userRepo;

    public AccountStatementService(AccountStatementRepo statementRepo,
                                  AccountStatementItemRepo itemRepo,
                                  QuoteRepo quoteRepo,
                                  QuoteItemRepo quoteItemRepo,
                                  QuotePaymentService paymentService,
                                  UserRepo userRepo) {
        this.statementRepo = statementRepo;
        this.itemRepo = itemRepo;
        this.quoteRepo = quoteRepo;
        this.quoteItemRepo = quoteItemRepo;
        this.paymentService = paymentService;
        this.userRepo = userRepo;
    }

    /**
     * Whether the user has permission to manage statements (create, edit, export, list).
     */
    @Transactional(readOnly = true)
    public boolean hasManagementPermission(Long userId) {
        return userRepo.findById(userId)
                .map(u -> Boolean.TRUE.equals(u.getCanManageStatements()))
                .orElse(false);
    }

    /**
     * Generate a statement from selected quotes for a customer.
     * Validates: all quotes belong to customerId, same currency, unpaid > 0.
     */
    @Transactional
    public AccountStatementEntity generate(Long customerId, List<Long> quoteIds, Long creatorId) {
        if (quoteIds == null || quoteIds.isEmpty()) {
            throw new IllegalArgumentException("quoteIds 不能为空");
        }

        List<QuoteEntity> quotes = new ArrayList<>();
        for (Long id : quoteIds) {
            QuoteEntity q = quoteRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Quote not found: id=" + id));
            if (!q.getCustomerId().equals(customerId)) {
                throw new IllegalArgumentException("报价单 " + q.getQuoteNo() + " 不属于该客户");
            }
            quotes.add(q);
        }

        Currency firstCurrency = quotes.get(0).getCurrency();
        for (QuoteEntity q : quotes) {
            if (q.getCurrency() != firstCurrency) {
                throw new IllegalArgumentException("所选报价单币种不一致，无法生成对账单");
            }
        }

        BigDecimal totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (QuoteEntity q : quotes) {
            BigDecimal unpaid = paymentService.getUnpaidAmount(q.getId());
            if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("报价单 " + q.getQuoteNo() + " 无未付金额，无法加入对账单");
            }
            totalAmount = totalAmount.add(unpaid);
        }

        String statementNo = generateStatementNo();
        String customerName = quotes.get(0).getRecipient() != null
                ? quotes.get(0).getRecipient()
                : ("客户#" + customerId);

        AccountStatementEntity header = new AccountStatementEntity();
        header.setStatementNo(statementNo);
        header.setCustomerId(customerId);
        header.setCustomerName(customerName);
        header.setCurrency(firstCurrency);
        header.setTotalAmount(totalAmount);
        header.setStatus(StatementStatus.PENDING);
        header.setCreatorId(creatorId);
        header.setCreatedAt(LocalDateTime.now());
        header = statementRepo.save(header);

        for (QuoteEntity q : quotes) {
            BigDecimal quoteTotal = calculateQuoteTotal(q.getId());
            BigDecimal unpaid = paymentService.getUnpaidAmount(q.getId());
            BigDecimal quotePaid = quoteTotal.subtract(unpaid).setScale(2, RoundingMode.HALF_UP);

            AccountStatementItemEntity item = new AccountStatementItemEntity();
            item.setStatementId(header.getId());
            item.setQuoteId(q.getId());
            item.setQuoteNo(q.getQuoteNo());
            item.setQuoteTotal(quoteTotal);
            item.setQuotePaid(quotePaid);
            item.setQuoteUnpaid(unpaid);
            itemRepo.save(item);
        }

        return statementRepo.findById(header.getId()).orElseThrow();
    }

    /**
     * Remove one quote from statement. Recalculates total. Only when status is PENDING.
     */
    @Transactional
    public AccountStatementEntity removeItem(Long statementId, Long quoteId) {
        AccountStatementEntity st = statementRepo.findById(statementId)
                .orElseThrow(() -> new IllegalStateException("Statement not found: id=" + statementId));
        if (st.getStatus() != StatementStatus.PENDING) {
            throw new IllegalStateException("只有待确认的对账单可以修改");
        }

        AccountStatementItemEntity item = itemRepo.findByStatementIdAndQuoteId(statementId, quoteId)
                .orElseThrow(() -> new IllegalStateException("对账单中未包含该报价单: quoteId=" + quoteId));
        BigDecimal subtract = item.getQuoteUnpaid();
        itemRepo.delete(item);
        st.setTotalAmount(st.getTotalAmount().subtract(subtract).setScale(2, RoundingMode.HALF_UP));
        return statementRepo.save(st);
    }

    /**
     * Export statement as Excel (.xlsx). Header info + item list.
     */
    @Transactional(readOnly = true)
    public byte[] exportExcel(Long statementId) {
        AccountStatementEntity st = statementRepo.findById(statementId)
                .orElseThrow(() -> new IllegalStateException("Statement not found: id=" + statementId));
        List<AccountStatementItemEntity> items = itemRepo.findByStatementIdOrderByIdAsc(statementId);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("对账单");
            int rowNum = 0;

            Row h1 = sheet.createRow(rowNum++);
            h1.createCell(0).setCellValue("对账单号");
            h1.createCell(1).setCellValue(st.getStatementNo());
            rowNum++;
            Row h2 = sheet.createRow(rowNum++);
            h2.createCell(0).setCellValue("客户");
            h2.createCell(1).setCellValue(st.getCustomerName() != null ? st.getCustomerName() : "");
            Row h3 = sheet.createRow(rowNum++);
            h3.createCell(0).setCellValue("币种");
            h3.createCell(1).setCellValue(st.getCurrency() != null ? st.getCurrency().name() : "");
            Row h4 = sheet.createRow(rowNum++);
            h4.createCell(0).setCellValue("合计未付");
            h4.createCell(1).setCellValue(st.getTotalAmount() != null ? st.getTotalAmount().toPlainString() : "0");
            Row h5 = sheet.createRow(rowNum++);
            h5.createCell(0).setCellValue("状态");
            h5.createCell(1).setCellValue(st.getStatus() != null ? st.getStatus().name() : "");
            rowNum++;

            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("报价单号");
            headerRow.createCell(1).setCellValue("报价总额");
            headerRow.createCell(2).setCellValue("已付");
            headerRow.createCell(3).setCellValue("未付");
            for (AccountStatementItemEntity it : items) {
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(it.getQuoteNo());
                r.createCell(1).setCellValue(it.getQuoteTotal() != null ? it.getQuoteTotal().toPlainString() : "0");
                r.createCell(2).setCellValue(it.getQuotePaid() != null ? it.getQuotePaid().toPlainString() : "0");
                r.createCell(3).setCellValue(it.getQuoteUnpaid() != null ? it.getQuoteUnpaid().toPlainString() : "0");
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Export failed", e);
        }
    }

    @Transactional(readOnly = true)
    public AccountStatementEntity getById(Long id) {
        return statementRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Statement not found: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<AccountStatementEntity> list(Long customerId, StatementStatus status) {
        if (customerId != null && status != null) {
            return statementRepo.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status);
        }
        if (customerId != null) {
            return statementRepo.findByCustomerIdOrderByCreatedAtDesc(customerId);
        }
        if (status != null) {
            return statementRepo.findByStatusOrderByCreatedAtDesc(status);
        }
        return statementRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private BigDecimal calculateQuoteTotal(Long quoteId) {
        List<QuoteItemEntity> items = quoteItemRepo.findByQuoteIdOrderByLineOrderAsc(quoteId);
        return items.stream()
                .map(QuoteItemEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String generateStatementNo() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "ST-" + datePrefix + "-";
        long count = statementRepo.countByStatementNoStartingWith(prefix);
        return prefix + String.format("%03d", count + 1);
    }
}
