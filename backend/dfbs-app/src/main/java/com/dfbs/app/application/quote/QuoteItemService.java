package com.dfbs.app.application.quote;

import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.dictionary.FeeTypeEntity;
import com.dfbs.app.modules.quote.dictionary.FeeTypeRepo;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteItemWarehouse;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuoteItemService {

    private final QuoteRepo quoteRepo;
    private final QuoteItemRepo itemRepo;
    private final FeeTypeRepo feeTypeRepo;
    private final ApplicationContext applicationContext;

    public QuoteItemService(QuoteRepo quoteRepo, QuoteItemRepo itemRepo, FeeTypeRepo feeTypeRepo,
                           ApplicationContext applicationContext) {
        this.quoteRepo = quoteRepo;
        this.itemRepo = itemRepo;
        this.feeTypeRepo = feeTypeRepo;
        this.applicationContext = applicationContext;
    }

    @Transactional
    public QuoteItemEntity addItem(Long quoteId, CreateItemCommand cmd) {
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("quote not found: id=" + quoteId));
        
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new IllegalStateException("Cannot add item to confirmed or cancelled quote");
        }

        QuoteItemEntity item = new QuoteItemEntity();
        item.setQuoteId(quoteId);
        item.setExpenseType(cmd.getExpenseType());
        item.setDescription(cmd.getDescription());
        item.setSpec(cmd.getSpec());
        item.setQuantity(cmd.getQuantity());
        item.setUnitPrice(cmd.getUnitPrice());
        item.setWarehouse(cmd.getWarehouse());
        item.setRemark(cmd.getRemark());
        
        // DRAFT mode: Allow null feeTypeId or partId (Free text mode)
        // If feeTypeId is provided, verify it exists (basic check)
        // For new items, prefer Active ones, but allow inactive if it was already set (historical)
        if (cmd.getFeeTypeId() != null) {
            FeeTypeEntity feeType = feeTypeRepo.findById(cmd.getFeeTypeId())
                    .orElseThrow(() -> new IllegalStateException("FeeType not found: id=" + cmd.getFeeTypeId()));
            item.setFeeTypeId(cmd.getFeeTypeId());
        }
        
        if (cmd.getPartId() != null) {
            item.setPartId(cmd.getPartId());
        }

        // Default unit from expenseType if not provided
        if (cmd.getUnit() == null || cmd.getUnit().isBlank()) {
            item.setUnit(cmd.getExpenseType().getDefaultUnit());
        } else {
            item.setUnit(cmd.getUnit());
        }

        // Calculate amount = unitPrice * quantity (2 decimal places)
        BigDecimal amount = cmd.getUnitPrice()
                .multiply(BigDecimal.valueOf(cmd.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        item.setAmount(amount);

        // Set lineOrder: find max + 1
        List<QuoteItemEntity> existingItems = itemRepo.findByQuoteIdOrderByLineOrderAsc(quoteId);
        int maxOrder = existingItems.stream()
                .mapToInt(QuoteItemEntity::getLineOrder)
                .max()
                .orElse(0);
        item.setLineOrder(maxOrder + 1);

        QuoteItemEntity saved = itemRepo.save(item);
        
        // Trigger warehouse CC notification check after adding item
        QuoteEntity updatedQuote = quoteRepo.findById(quoteId).orElseThrow();
        QuoteService quoteService = applicationContext.getBean(QuoteService.class);
        quoteService.checkAndSendWarehouseCcNotification(updatedQuote);
        
        return saved;
    }

    @Transactional
    public QuoteItemEntity updateItem(Long itemId, UpdateItemCommand cmd) {
        QuoteItemEntity item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalStateException("item not found: id=" + itemId));
        
        QuoteEntity quote = quoteRepo.findById(item.getQuoteId())
                .orElseThrow(() -> new IllegalStateException("quote not found: id=" + item.getQuoteId()));
        
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new IllegalStateException("Cannot update item in confirmed or cancelled quote");
        }

        // Update fields
        if (cmd.getExpenseType() != null) {
            item.setExpenseType(cmd.getExpenseType());
        }
        if (cmd.getDescription() != null) {
            item.setDescription(cmd.getDescription());
        }
        if (cmd.getSpec() != null) {
            item.setSpec(cmd.getSpec());
        }
        if (cmd.getUnit() != null) {
            item.setUnit(cmd.getUnit());
        }
        if (cmd.getQuantity() != null) {
            item.setQuantity(cmd.getQuantity());
        }
        if (cmd.getUnitPrice() != null) {
            item.setUnitPrice(cmd.getUnitPrice());
        }
        if (cmd.getWarehouse() != null) {
            item.setWarehouse(cmd.getWarehouse());
        }
        if (cmd.getRemark() != null) {
            item.setRemark(cmd.getRemark());
        }
        
        // DRAFT mode: Allow updating feeTypeId or partId (Free text mode)
        // If feeTypeId is provided, verify it exists (basic check)
        // Allow inactive if it was already set (historical), but for new assignments prefer Active
        if (cmd.getFeeTypeId() != null) {
            FeeTypeEntity feeType = feeTypeRepo.findById(cmd.getFeeTypeId())
                    .orElseThrow(() -> new IllegalStateException("FeeType not found: id=" + cmd.getFeeTypeId()));
            item.setFeeTypeId(cmd.getFeeTypeId());
        } else if (cmd.getFeeTypeId() == null && item.getFeeTypeId() != null) {
            // Allow clearing feeTypeId in DRAFT mode
            item.setFeeTypeId(null);
        }
        
        if (cmd.getPartId() != null) {
            item.setPartId(cmd.getPartId());
        } else if (cmd.getPartId() == null && item.getPartId() != null) {
            // Allow clearing partId in DRAFT mode
            item.setPartId(null);
        }

        // Recalculate amount
        BigDecimal amount = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        item.setAmount(amount);

        QuoteItemEntity saved = itemRepo.save(item);
        
        // Trigger warehouse CC notification check after updating item
        QuoteEntity updatedQuote = quoteRepo.findById(item.getQuoteId()).orElseThrow();
        QuoteService quoteService = applicationContext.getBean(QuoteService.class);
        quoteService.checkAndSendWarehouseCcNotification(updatedQuote);
        
        return saved;
    }

    @Transactional
    public void deleteItem(Long itemId) {
        QuoteItemEntity item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalStateException("item not found: id=" + itemId));
        
        QuoteEntity quote = quoteRepo.findById(item.getQuoteId())
                .orElseThrow(() -> new IllegalStateException("quote not found: id=" + item.getQuoteId()));
        
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new IllegalStateException("Cannot delete item from confirmed or cancelled quote");
        }

        itemRepo.deleteById(itemId);
    }

    @Transactional(readOnly = true)
    public List<QuoteItemDto> getItems(Long quoteId) {
        List<QuoteItemEntity> items = itemRepo.findByQuoteIdOrderByLineOrderAsc(quoteId);
        return items.stream()
                .map(item -> {
                    String alertMessage = (item.getWarehouse() == QuoteItemWarehouse.HEADQUARTERS)
                            ? "需提醒总部发货"
                            : null;
                    return QuoteItemDto.from(item, alertMessage);
                })
                .collect(Collectors.toList());
    }

    /** Command for creating an item. */
    public static final class CreateItemCommand {
        private QuoteExpenseType expenseType;
        private String description;
        private String spec;
        private String unit;
        private Integer quantity;
        private BigDecimal unitPrice;
        private QuoteItemWarehouse warehouse;
        private String remark;
        private Long feeTypeId;  // Optional, for dictionary mode
        private Long partId;     // Optional, for parts

        public QuoteExpenseType getExpenseType() { return expenseType; }
        public void setExpenseType(QuoteExpenseType expenseType) { this.expenseType = expenseType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSpec() { return spec; }
        public void setSpec(String spec) { this.spec = spec; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public QuoteItemWarehouse getWarehouse() { return warehouse; }
        public void setWarehouse(QuoteItemWarehouse warehouse) { this.warehouse = warehouse; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public Long getFeeTypeId() { return feeTypeId; }
        public void setFeeTypeId(Long feeTypeId) { this.feeTypeId = feeTypeId; }
        public Long getPartId() { return partId; }
        public void setPartId(Long partId) { this.partId = partId; }
    }

    /** Command for updating an item. */
    public static final class UpdateItemCommand {
        private QuoteExpenseType expenseType;
        private String description;
        private String spec;
        private String unit;
        private Integer quantity;
        private BigDecimal unitPrice;
        private QuoteItemWarehouse warehouse;
        private String remark;
        private Long feeTypeId;  // Optional, for dictionary mode
        private Long partId;     // Optional, for parts

        public QuoteExpenseType getExpenseType() { return expenseType; }
        public void setExpenseType(QuoteExpenseType expenseType) { this.expenseType = expenseType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSpec() { return spec; }
        public void setSpec(String spec) { this.spec = spec; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public QuoteItemWarehouse getWarehouse() { return warehouse; }
        public void setWarehouse(QuoteItemWarehouse warehouse) { this.warehouse = warehouse; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public Long getFeeTypeId() { return feeTypeId; }
        public void setFeeTypeId(Long feeTypeId) { this.feeTypeId = feeTypeId; }
        public Long getPartId() { return partId; }
        public void setPartId(Long partId) { this.partId = partId; }
    }

    /** DTO for item response. */
    public static final class QuoteItemDto {
        private Long id;
        private Long quoteId;
        private Integer lineOrder;
        private QuoteExpenseType expenseType;
        private String description;
        private String spec;
        private String unit;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private QuoteItemWarehouse warehouse;
        private String remark;
        private String alertMessage;

        public static QuoteItemDto from(QuoteItemEntity item, String alertMessage) {
            QuoteItemDto dto = new QuoteItemDto();
            dto.id = item.getId();
            dto.quoteId = item.getQuoteId();
            dto.lineOrder = item.getLineOrder();
            dto.expenseType = item.getExpenseType();
            dto.description = item.getDescription();
            dto.spec = item.getSpec();
            dto.unit = item.getUnit();
            dto.quantity = item.getQuantity();
            dto.unitPrice = item.getUnitPrice();
            dto.amount = item.getAmount();
            dto.warehouse = item.getWarehouse();
            dto.remark = item.getRemark();
            dto.alertMessage = alertMessage;
            return dto;
        }

        // Getters
        public Long getId() { return id; }
        public Long getQuoteId() { return quoteId; }
        public Integer getLineOrder() { return lineOrder; }
        public QuoteExpenseType getExpenseType() { return expenseType; }
        public String getDescription() { return description; }
        public String getSpec() { return spec; }
        public String getUnit() { return unit; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getAmount() { return amount; }
        public QuoteItemWarehouse getWarehouse() { return warehouse; }
        public String getRemark() { return remark; }
        public String getAlertMessage() { return alertMessage; }
    }
}
