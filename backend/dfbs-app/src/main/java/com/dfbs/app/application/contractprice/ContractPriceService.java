package com.dfbs.app.application.contractprice;

import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.contractprice.ContractPriceHeaderEntity;
import com.dfbs.app.modules.contractprice.ContractPriceHeaderRepo;
import com.dfbs.app.modules.contractprice.ContractPriceItemEntity;
import com.dfbs.app.modules.contractprice.ContractStatus;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ContractPriceService {

    private static final String STRATEGY_LOWEST_PRICE = "LOWEST_PRICE";
    private static final String STRATEGY_PRIORITY = "PRIORITY";

    private final ContractPriceHeaderRepo headerRepo;
    private final CurrentUserIdResolver userIdResolver;

    public ContractPriceService(ContractPriceHeaderRepo headerRepo, CurrentUserIdResolver userIdResolver) {
        this.headerRepo = headerRepo;
        this.userIdResolver = userIdResolver;
    }

    /**
     * Calculate suggested price from active contract prices for customer on docDate and itemType.
     * PLATFORM: lowest price first; others (DATA_PLAN, REPAIR, SHIPPING, etc.): highest priority first.
     */
    @Transactional(readOnly = true)
    public PriceSuggestionDto calculateSuggestedPrice(Long customerId, LocalDate docDate, QuoteExpenseType itemType) {
        List<ContractPriceHeaderEntity> headers = headerRepo.findByCustomerIdAndStatusWithItems(customerId, ContractStatus.ACTIVE);
        List<ContractPriceItemEntity> candidates = new ArrayList<>();
        for (ContractPriceHeaderEntity h : headers) {
            if (!inDateRange(h, docDate)) continue;
            for (ContractPriceItemEntity item : h.getItems()) {
                if (item.getItemType() == itemType) {
                    candidates.add(item);
                }
            }
        }
        if (candidates.isEmpty()) return null;

        Comparator<ContractPriceItemEntity> comparator;
        String strategy;
        if (itemType == QuoteExpenseType.PLATFORM) {
            comparator = Comparator
                    .comparing(ContractPriceItemEntity::getUnitPrice)
                    .thenComparing(i -> i.getHeader().getPriority(), Comparator.reverseOrder())
                    .thenComparing(i -> i.getHeader().getEffectiveDate() != null ? i.getHeader().getEffectiveDate() : LocalDate.MIN, Comparator.reverseOrder());
            strategy = STRATEGY_LOWEST_PRICE;
        } else {
            comparator = Comparator
                    .comparing((ContractPriceItemEntity i) -> i.getHeader().getPriority(), Comparator.reverseOrder())
                    .thenComparing(i -> i.getHeader().getEffectiveDate() != null ? i.getHeader().getEffectiveDate() : LocalDate.MIN, Comparator.reverseOrder());
            strategy = STRATEGY_PRIORITY;
        }

        ContractPriceItemEntity top = candidates.stream().sorted(comparator).findFirst().orElse(null);
        if (top == null) return null;

        BigDecimal price = top.getUnitPrice() != null ? top.getUnitPrice() : BigDecimal.ZERO;
        Currency currency = top.getCurrency() != null ? top.getCurrency() : Currency.CNY;
        String sourceInfo = buildSourceInfo(top.getHeader().getId(), strategy);
        return PriceSuggestionDto.of(price, currency, sourceInfo);
    }

    private boolean inDateRange(ContractPriceHeaderEntity h, LocalDate docDate) {
        if (h.getEffectiveDate() != null && docDate.isBefore(h.getEffectiveDate())) return false;
        if (h.getExpirationDate() != null && docDate.isAfter(h.getExpirationDate())) return false;
        return true;
    }

    private String buildSourceInfo(Long contractId, String strategy) {
        return "{\"contractId\":" + contractId + ",\"strategy\":\"" + strategy + "\"}";
    }

    @Transactional
    public ContractPriceHeaderEntity create(CreateContractCommand cmd) {
        ContractPriceHeaderEntity header = new ContractPriceHeaderEntity();
        header.setContractName(cmd.getContractName());
        header.setCustomerId(cmd.getCustomerId());
        header.setEffectiveDate(cmd.getEffectiveDate());
        header.setExpirationDate(cmd.getExpirationDate());
        header.setPriority(cmd.getPriority() != null ? cmd.getPriority() : 0);
        header.setStatus(ContractStatus.ACTIVE);
        header.setCreatedBy(userIdResolver.getCurrentUserId());
        header.setCreatedAt(OffsetDateTime.now());
        for (CreateContractCommand.ItemEntry e : cmd.getItems()) {
            ContractPriceItemEntity item = new ContractPriceItemEntity();
            item.setHeader(header);
            item.setItemType(e.getItemType());
            item.setUnitPrice(e.getUnitPrice());
            item.setCurrency(e.getCurrency() != null ? e.getCurrency() : Currency.CNY);
            header.getItems().add(item);
        }
        return headerRepo.save(header);
    }

    @Transactional
    public ContractPriceHeaderEntity update(Long id, UpdateContractCommand cmd) {
        ContractPriceHeaderEntity header = headerRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Contract not found: id=" + id));
        if (header.getStatus() != ContractStatus.ACTIVE)
            throw new IllegalStateException("Only ACTIVE contract can be updated");
        if (cmd.getContractName() != null) header.setContractName(cmd.getContractName());
        if (cmd.getEffectiveDate() != null) header.setEffectiveDate(cmd.getEffectiveDate());
        if (cmd.getExpirationDate() != null) header.setExpirationDate(cmd.getExpirationDate());
        if (cmd.getPriority() != null) header.setPriority(cmd.getPriority());
        if (cmd.getItems() != null && !cmd.getItems().isEmpty()) {
            header.getItems().clear();
            for (UpdateContractCommand.ItemEntry e : cmd.getItems()) {
                ContractPriceItemEntity item = new ContractPriceItemEntity();
                item.setHeader(header);
                item.setItemType(e.getItemType());
                item.setUnitPrice(e.getUnitPrice());
                item.setCurrency(e.getCurrency() != null ? e.getCurrency() : Currency.CNY);
                header.getItems().add(item);
            }
        }
        return headerRepo.save(header);
    }

    @Transactional
    public ContractPriceHeaderEntity deactivate(Long id) {
        ContractPriceHeaderEntity header = headerRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Contract not found: id=" + id));
        header.setStatus(ContractStatus.INACTIVE);
        return headerRepo.save(header);
    }

    public static final class CreateContractCommand {
        private String contractName;
        private Long customerId;
        private LocalDate effectiveDate;
        private LocalDate expirationDate;
        private Integer priority;
        private List<ItemEntry> items = new ArrayList<>();

        public String getContractName() { return contractName; }
        public void setContractName(String contractName) { this.contractName = contractName; }
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public LocalDate getEffectiveDate() { return effectiveDate; }
        public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
        public LocalDate getExpirationDate() { return expirationDate; }
        public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        public List<ItemEntry> getItems() { return items; }
        public void setItems(List<ItemEntry> items) { this.items = items != null ? items : new ArrayList<>(); }

        public static final class ItemEntry {
            private QuoteExpenseType itemType;
            private BigDecimal unitPrice;
            private Currency currency;
            public QuoteExpenseType getItemType() { return itemType; }
            public void setItemType(QuoteExpenseType itemType) { this.itemType = itemType; }
            public BigDecimal getUnitPrice() { return unitPrice; }
            public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
            public Currency getCurrency() { return currency; }
            public void setCurrency(Currency currency) { this.currency = currency; }
        }
    }

    public static final class UpdateContractCommand {
        private String contractName;
        private LocalDate effectiveDate;
        private LocalDate expirationDate;
        private Integer priority;
        private List<ItemEntry> items;

        public String getContractName() { return contractName; }
        public void setContractName(String contractName) { this.contractName = contractName; }
        public LocalDate getEffectiveDate() { return effectiveDate; }
        public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
        public LocalDate getExpirationDate() { return expirationDate; }
        public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        public List<ItemEntry> getItems() { return items; }
        public void setItems(List<ItemEntry> items) { this.items = items; }

        public static final class ItemEntry {
            private QuoteExpenseType itemType;
            private BigDecimal unitPrice;
            private Currency currency;
            public QuoteExpenseType getItemType() { return itemType; }
            public void setItemType(QuoteExpenseType itemType) { this.itemType = itemType; }
            public BigDecimal getUnitPrice() { return unitPrice; }
            public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
            public Currency getCurrency() { return currency; }
            public void setCurrency(Currency currency) { this.currency = currency; }
        }
    }
}
