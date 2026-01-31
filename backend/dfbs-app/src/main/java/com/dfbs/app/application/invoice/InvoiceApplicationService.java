package com.dfbs.app.application.invoice;

import com.dfbs.app.application.invoice.dto.InvoiceApplicationCreateRequest;
import com.dfbs.app.application.invoice.dto.InvoiceGroupRequest;
import com.dfbs.app.application.invoice.dto.QuoteItemSelection;
import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.modules.invoice.*;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteInvoiceStatus;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class InvoiceApplicationService {

    private final QuoteRepo quoteRepo;
    private final QuoteItemRepo quoteItemRepo;
    private final InvoiceApplicationRepo applicationRepo;
    private final InvoiceRecordRepo recordRepo;
    private final InvoiceItemRefRepo itemRefRepo;
    private final NotificationService notificationService;

    public InvoiceApplicationService(QuoteRepo quoteRepo, QuoteItemRepo quoteItemRepo,
                                     InvoiceApplicationRepo applicationRepo, InvoiceRecordRepo recordRepo,
                                     InvoiceItemRefRepo itemRefRepo, NotificationService notificationService) {
        this.quoteRepo = quoteRepo;
        this.quoteItemRepo = quoteItemRepo;
        this.applicationRepo = applicationRepo;
        this.recordRepo = recordRepo;
        this.itemRefRepo = itemRefRepo;
        this.notificationService = notificationService;
    }

    @Transactional
    public InvoiceApplicationEntity create(InvoiceApplicationCreateRequest request, Long collectorId) {
        if (request.getGroups() == null || request.getGroups().isEmpty()) {
            throw new IllegalArgumentException("groups 不能为空");
        }

        List<QuoteItemSelection> allSelections = request.getGroups().stream()
                .filter(g -> g.getItems() != null)
                .flatMap(g -> g.getItems().stream())
                .filter(s -> s.getQuoteItemId() != null && s.getAmount() != null)
                .collect(Collectors.toList());
        if (allSelections.isEmpty()) {
            throw new IllegalArgumentException("至少需要一条明细");
        }

        Set<Long> quoteIds = new HashSet<>();
        Map<Long, BigDecimal> amountPerQuote = new HashMap<>();
        Long firstCustomerId = null;
        Currency firstCurrency = null;

        for (QuoteItemSelection sel : allSelections) {
            QuoteItemEntity item = quoteItemRepo.findById(sel.getQuoteItemId())
                    .orElseThrow(() -> new IllegalArgumentException("报价明细不存在: id=" + sel.getQuoteItemId()));
            if (sel.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("金额必须大于0: quoteItemId=" + sel.getQuoteItemId());
            }
            QuoteEntity quote = quoteRepo.findById(item.getQuoteId())
                    .orElseThrow(() -> new IllegalStateException("Quote not found"));
            if (quote.getStatus() != QuoteStatus.CONFIRMED) {
                throw new IllegalArgumentException("只能对已确认的报价单申请开票");
            }
            if (firstCustomerId == null) {
                firstCustomerId = quote.getCustomerId();
                firstCurrency = quote.getCurrency();
            }
            if (!quote.getCustomerId().equals(firstCustomerId)) {
                throw new IllegalArgumentException("所选报价单客户不一致，无法合并开票");
            }
            if (quote.getCurrency() != null && firstCurrency != null && !quote.getCurrency().equals(firstCurrency)) {
                throw new IllegalArgumentException("所选报价单币种不一致，无法合并开票");
            }
            if (firstCurrency == null && quote.getCurrency() != null) {
                firstCurrency = quote.getCurrency();
            }
            if (quote.getCollectorId() == null || !quote.getCollectorId().equals(collectorId)) {
                throw new IllegalArgumentException("存在未指派给当前收款执行人的报价单，无法合并开票");
            }
            quoteIds.add(quote.getId());
            amountPerQuote.merge(quote.getId(), sel.getAmount().setScale(2, RoundingMode.HALF_UP),
                    BigDecimal::add);
        }

        for (Long quoteId : quoteIds) {
            QuoteEntity q = quoteRepo.findById(quoteId).orElseThrow();
            BigDecimal totalAmount = calculateQuoteTotalFromItems(quoteId);
            BigDecimal remainingQuota = totalAmount.subtract(q.getInvoicedAmount() != null ? q.getInvoicedAmount() : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal applied = amountPerQuote.get(quoteId);
            if (applied.compareTo(remainingQuota) > 0) {
                throw new IllegalArgumentException("开票金额不能超过报价单未开票金额，报价单 id=" + quoteId + "，剩余可开票=" + remainingQuota.toPlainString());
            }
        }

        BigDecimal totalAmount = allSelections.stream()
                .map(QuoteItemSelection::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        String applicationNo = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + ThreadLocalRandom.current().nextInt(100, 1000);

        Currency applicationCurrency = firstCurrency != null ? firstCurrency : Currency.CNY;
        InvoiceApplicationEntity app = new InvoiceApplicationEntity();
        app.setApplicationNo(applicationNo);
        app.setCollectorId(collectorId);
        app.setCustomerId(firstCustomerId);
        app.setTotalAmount(totalAmount);
        app.setCurrency(applicationCurrency);
        app.setStatus(InvoiceApplicationStatus.PENDING);
        app.setInvoiceTitle(request.getInvoiceTitle());
        app.setTaxId(request.getTaxId());
        app.setAddress(request.getAddress());
        app.setPhone(request.getPhone());
        app.setBankName(request.getBankName());
        app.setBankAccount(request.getBankAccount());
        app.setEmail(request.getEmail());
        app.setCreatedAt(LocalDateTime.now());
        app = applicationRepo.save(app);

        for (InvoiceGroupRequest group : request.getGroups()) {
            if (group.getItems() == null || group.getItems().isEmpty()) continue;
            BigDecimal recordAmount = group.getItems().stream()
                    .map(QuoteItemSelection::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            InvoiceRecordEntity record = new InvoiceRecordEntity();
            record.setApplicationId(app.getId());
            record.setAmount(recordAmount);
            record.setInvoiceType(group.getInvoiceType() != null ? group.getInvoiceType() : InvoiceType.NORMAL);
            record.setTaxRate(group.getTaxRate());
            record.setContent(group.getContent() != null ? group.getContent() : "Software Service");
            record = recordRepo.save(record);
            for (QuoteItemSelection sel : group.getItems()) {
                QuoteItemEntity item = quoteItemRepo.findById(sel.getQuoteItemId()).orElseThrow();
                InvoiceItemRefEntity ref = new InvoiceItemRefEntity();
                ref.setInvoiceRecordId(record.getId());
                ref.setQuoteId(item.getQuoteId());
                ref.setQuoteItemId(sel.getQuoteItemId());
                ref.setAmount(sel.getAmount().setScale(2, RoundingMode.HALF_UP));
                itemRefRepo.save(ref);
            }
        }

        for (Long quoteId : quoteIds) {
            QuoteEntity q = quoteRepo.findById(quoteId).orElseThrow();
            if (q.getInvoiceStatus() == QuoteInvoiceStatus.UNINVOICED) {
                q.setInvoiceStatus(QuoteInvoiceStatus.IN_PROCESS);
                quoteRepo.save(q);
            }
        }

        return app;
    }

    @Transactional
    public InvoiceApplicationEntity audit(Long applicationId, String result, Long auditorId, String reason) {
        InvoiceApplicationEntity app = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在: id=" + applicationId));
        if (app.getStatus() != InvoiceApplicationStatus.PENDING) {
            throw new IllegalStateException("只能审批待处理申请");
        }
        if ("REJECT".equals(result)) {
            app.setStatus(InvoiceApplicationStatus.REJECTED);
            app.setAuditorId(auditorId);
            app.setAuditTime(LocalDateTime.now());
            app.setRejectReason(reason);
            applicationRepo.save(app);
            recalcQuoteInvoiceStatusForApplication(applicationId);
            notificationService.send(app.getCollectorId(), "开票申请已驳回", reason != null ? reason : "开票申请已驳回", "/invoice-applications/" + applicationId);
            return app;
        }
        if (!"APPROVE".equals(result)) {
            throw new IllegalArgumentException("Invalid audit result: " + result);
        }
        app.setStatus(InvoiceApplicationStatus.APPROVED);
        app.setAuditorId(auditorId);
        app.setAuditTime(LocalDateTime.now());
        applicationRepo.save(app);

        Map<Long, BigDecimal> amountToAddPerQuote = new HashMap<>();
        List<InvoiceRecordEntity> records = recordRepo.findByApplicationId(applicationId);
        for (InvoiceRecordEntity rec : records) {
            for (InvoiceItemRefEntity ref : itemRefRepo.findByInvoiceRecordId(rec.getId())) {
                amountToAddPerQuote.merge(ref.getQuoteId(), ref.getAmount().setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
            }
        }
        for (Map.Entry<Long, BigDecimal> e : amountToAddPerQuote.entrySet()) {
            QuoteEntity quote = quoteRepo.findById(e.getKey()).orElseThrow();
            BigDecimal newInvoiced = (quote.getInvoicedAmount() != null ? quote.getInvoicedAmount() : BigDecimal.ZERO).add(e.getValue()).setScale(2, RoundingMode.HALF_UP);
            quote.setInvoicedAmount(newInvoiced);
            BigDecimal total = calculateQuoteTotalFromItems(quote.getId());
            quote.setInvoiceStatus(newInvoiced.compareTo(total) >= 0 ? QuoteInvoiceStatus.FULLY_INVOICED : QuoteInvoiceStatus.PARTIAL);
            quoteRepo.save(quote);
        }

        notificationService.send(app.getCollectorId(), "开票申请已通过", "开票申请 " + app.getApplicationNo() + " 已通过", "/invoice-applications/" + applicationId);
        return app;
    }

    private void recalcQuoteInvoiceStatusForApplication(Long applicationId) {
        List<InvoiceRecordEntity> records = recordRepo.findByApplicationId(applicationId);
        Set<Long> quoteIds = new HashSet<>();
        for (InvoiceRecordEntity rec : records) {
            itemRefRepo.findByInvoiceRecordId(rec.getId()).forEach(ref -> quoteIds.add(ref.getQuoteId()));
        }
        for (Long quoteId : quoteIds) {
            recalcQuoteInvoiceStatus(quoteId);
        }
    }

    private void recalcQuoteInvoiceStatus(Long quoteId) {
        QuoteEntity quote = quoteRepo.findById(quoteId).orElseThrow();
        BigDecimal invoiced = quote.getInvoicedAmount() != null ? quote.getInvoicedAmount() : BigDecimal.ZERO;
        BigDecimal total = calculateQuoteTotalFromItems(quoteId);
        if (invoiced.compareTo(BigDecimal.ZERO) <= 0) {
            quote.setInvoiceStatus(QuoteInvoiceStatus.UNINVOICED);
        } else if (invoiced.compareTo(total) >= 0) {
            quote.setInvoiceStatus(QuoteInvoiceStatus.FULLY_INVOICED);
        } else {
            quote.setInvoiceStatus(QuoteInvoiceStatus.PARTIAL);
        }
        quoteRepo.save(quote);
    }

    private BigDecimal calculateQuoteTotalFromItems(Long quoteId) {
        return quoteItemRepo.findByQuoteIdOrderByLineOrderAsc(quoteId).stream()
                .map(QuoteItemEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<InvoiceApplicationEntity> listMyApplications(Long collectorId) {
        return applicationRepo.findByCollectorIdOrderByCreatedAtDesc(collectorId);
    }

    @Transactional(readOnly = true)
    public InvoiceApplicationEntity getApplication(Long applicationId) {
        return applicationRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在: id=" + applicationId));
    }

    /**
     * Cancel pending invoice applications that reference this quote (e.g. when quote is voided).
     */
    @Transactional
    public void cancelPending(Long quoteId) {
        List<InvoiceItemRefEntity> refs = itemRefRepo.findByQuoteId(quoteId);
        Set<Long> applicationIds = refs.stream().map(ref -> {
            InvoiceRecordEntity rec = recordRepo.findById(ref.getInvoiceRecordId()).orElse(null);
            return rec != null ? rec.getApplicationId() : null;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        for (Long appId : applicationIds) {
            InvoiceApplicationEntity app = applicationRepo.findById(appId).orElse(null);
            if (app != null && app.getStatus() == InvoiceApplicationStatus.PENDING) {
                app.setStatus(InvoiceApplicationStatus.CANCELLED);
                applicationRepo.save(app);
            }
        }
    }
}
