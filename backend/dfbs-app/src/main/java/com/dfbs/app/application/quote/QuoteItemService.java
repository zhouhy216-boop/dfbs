package com.dfbs.app.application.quote;

import com.dfbs.app.application.contractprice.ContractPriceService;
import com.dfbs.app.application.contractprice.PriceSuggestionDto;
import com.dfbs.app.application.dicttype.DictionaryReadService;
import com.dfbs.app.application.masterdata.PartBomService;
import com.dfbs.app.modules.masterdata.PartEntity;
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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuoteItemService {

    private static final String QUOTE_EXPENSE_TYPE_CODE = "quote_expense_type";

    private final QuoteRepo quoteRepo;
    private final QuoteItemRepo itemRepo;
    private final FeeTypeRepo feeTypeRepo;
    private final PartBomService partBomService;
    private final ContractPriceService contractPriceService;
    private final DictionaryReadService dictionaryReadService;
    private final ApplicationContext applicationContext;

    public QuoteItemService(QuoteRepo quoteRepo, QuoteItemRepo itemRepo, FeeTypeRepo feeTypeRepo,
                            PartBomService partBomService, ContractPriceService contractPriceService,
                            DictionaryReadService dictionaryReadService,
                            ApplicationContext applicationContext) {
        this.quoteRepo = quoteRepo;
        this.itemRepo = itemRepo;
        this.feeTypeRepo = feeTypeRepo;
        this.partBomService = partBomService;
        this.contractPriceService = contractPriceService;
        this.dictionaryReadService = dictionaryReadService;
        this.applicationContext = applicationContext;
    }

    @Transactional
    public QuoteItemEntity addItem(Long quoteId, CreateItemCommand cmd) {
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("quote not found: id=" + quoteId));
        
        if (quote.getStatus() != QuoteStatus.DRAFT && quote.getStatus() != QuoteStatus.RETURNED) {
            throw new IllegalStateException("Cannot add item: only DRAFT or RETURNED quote can be edited");
        }

        QuoteItemEntity item = new QuoteItemEntity();
        item.setQuoteId(quoteId);
        item.setExpenseType(cmd.getExpenseType());
        String expenseTypeValue = cmd.getExpenseType() != null ? cmd.getExpenseType().name() : null;
        item.setExpenseTypeLabelSnapshot(dictionaryReadService.resolveLabel(QUOTE_EXPENSE_TYPE_CODE, expenseTypeValue));
        item.setDescription(cmd.getDescription());
        item.setSpec(cmd.getSpec());
        item.setQuantity(cmd.getQuantity());
        item.setWarehouse(cmd.getWarehouse());
        item.setRemark(cmd.getRemark());

        // Contract price auto-suggest (lifecycle lock: only before first submission)
        if (quote.getFirstSubmissionTime() == null && quote.getCustomerId() != null && cmd.getExpenseType() != null) {
            PriceSuggestionDto suggestion = contractPriceService.calculateSuggestedPrice(
                    quote.getCustomerId(), LocalDate.now(), cmd.getExpenseType());
            if (cmd.getUnitPrice() != null && suggestion != null && cmd.getUnitPrice().compareTo(suggestion.getPrice()) != 0) {
                if (cmd.getManualPriceReason() == null || cmd.getManualPriceReason().isBlank())
                    throw new IllegalStateException("人工改价必须填写原因");
                item.setUnitPrice(cmd.getUnitPrice());
                item.setManualPriceReason(cmd.getManualPriceReason());
            } else if (suggestion != null) {
                item.setUnitPrice(suggestion.getPrice());
                item.setPriceSourceInfo(suggestion.getSourceInfo());
            } else {
                item.setUnitPrice(cmd.getUnitPrice() != null ? cmd.getUnitPrice() : BigDecimal.ZERO);
            }
        } else {
            item.setUnitPrice(cmd.getUnitPrice() != null ? cmd.getUnitPrice() : BigDecimal.ZERO);
        }
        
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
            fillPartInfo(item, cmd.getPartId());
            if (cmd.getUnitPrice() == null) {
                item.setUnitPrice(item.getStandardPrice() != null ? item.getStandardPrice() : BigDecimal.ZERO);
            }
        }

        // Default unit from expenseType if not provided
        if (cmd.getUnit() == null || cmd.getUnit().isBlank()) {
            item.setUnit(cmd.getExpenseType().getDefaultUnit());
        } else {
            item.setUnit(cmd.getUnit());
        }

        // Calculate amount = unitPrice * quantity (2 decimal places)
        BigDecimal amount = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(cmd.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        item.setAmount(amount);

        if (cmd.getPartId() != null) {
            checkPriceDeviation(item);
        }

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
        
        if (quote.getStatus() != QuoteStatus.DRAFT && quote.getStatus() != QuoteStatus.RETURNED) {
            throw new IllegalStateException("Cannot update item: only DRAFT or RETURNED quote can be edited");
        }

        // Update fields
        if (cmd.getExpenseType() != null) {
            item.setExpenseType(cmd.getExpenseType());
            item.setExpenseTypeLabelSnapshot(dictionaryReadService.resolveLabel(QUOTE_EXPENSE_TYPE_CODE, cmd.getExpenseType().name()));
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
            if (quote.getFirstSubmissionTime() == null && quote.getCustomerId() != null && item.getExpenseType() != null) {
                PriceSuggestionDto suggestion = contractPriceService.calculateSuggestedPrice(
                        quote.getCustomerId(), LocalDate.now(), item.getExpenseType());
                if (suggestion != null && cmd.getUnitPrice().compareTo(suggestion.getPrice()) != 0
                        && (cmd.getManualPriceReason() == null || cmd.getManualPriceReason().isBlank())) {
                    throw new IllegalStateException("人工改价必须填写原因");
                }
                if (suggestion != null && cmd.getUnitPrice().compareTo(suggestion.getPrice()) != 0) {
                    item.setPriceSourceInfo(null);
                    item.setManualPriceReason(cmd.getManualPriceReason());
                }
            }
            item.setUnitPrice(cmd.getUnitPrice());
        }
        if (cmd.getManualPriceReason() != null) {
            item.setManualPriceReason(cmd.getManualPriceReason());
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
            if (item.getPartId() == null && item.getDescription() != null && !item.getDescription().isBlank()) {
                if (item.getOriginalPartName() == null || item.getOriginalPartName().isBlank()) {
                    item.setOriginalPartName(item.getDescription());
                }
            }
            item.setPartId(cmd.getPartId());
            fillPartInfo(item, cmd.getPartId());
        }
        // Do not clear partId when cmd.getPartId() is null (omitted = don't change)

        // Recalculate amount (must be after unitPrice / quantity changes)
        BigDecimal amount = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        item.setAmount(amount);

        // Always re-run price deviation check after all field updates
        checkPriceDeviation(item);

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
        
        if (quote.getStatus() != QuoteStatus.DRAFT && quote.getStatus() != QuoteStatus.RETURNED) {
            throw new IllegalStateException("Cannot delete item: only DRAFT or RETURNED quote can be edited");
        }

        itemRepo.deleteById(itemId);
    }

    /**
     * Fill quote item from Part master: partId, description (part name), spec, standardPrice (part.salesPrice).
     */
    public void fillPartInfo(QuoteItemEntity item, Long partId) {
        PartEntity part = partBomService.getPart(partId);
        item.setPartId(part.getId());
        item.setDescription(part.getName());
        item.setSpec(part.getSpec());
        item.setStandardPrice(part.getSalesPrice());
        if (part.getUnit() != null && !part.getUnit().isBlank()) {
            item.setUnit(part.getUnit());
        }
        checkPriceDeviation(item);
    }

    /**
     * Set isPriceDeviated by comparing unitPrice to standardPrice (snapshot). MVP: direct comparison.
     */
    public void checkPriceDeviation(QuoteItemEntity item) {
        if (item.getStandardPrice() == null) {
            item.setIsPriceDeviated(false);
            return;
        }
        BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        item.setIsPriceDeviated(item.getStandardPrice().compareTo(unitPrice) != 0);
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
        private String manualPriceReason;
        private Long feeTypeId;
        private Long partId;

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
        public String getManualPriceReason() { return manualPriceReason; }
        public void setManualPriceReason(String manualPriceReason) { this.manualPriceReason = manualPriceReason; }
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
        private String manualPriceReason;
        private Long feeTypeId;
        private Long partId;

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
        public String getManualPriceReason() { return manualPriceReason; }
        public void setManualPriceReason(String manualPriceReason) { this.manualPriceReason = manualPriceReason; }
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
        private Long feeTypeId;
        private Long partId;
        private String description;
        private String originalPartName;
        private String spec;
        private String unit;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal standardPrice;
        private Boolean isPriceDeviated;
        private BigDecimal amount;
        private QuoteItemWarehouse warehouse;
        private String remark;
        private String priceSourceInfo;
        private String manualPriceReason;
        private String alertMessage;
        private String expenseTypeLabelSnapshot;

        public static QuoteItemDto from(QuoteItemEntity item, String alertMessage) {
            QuoteItemDto dto = new QuoteItemDto();
            dto.id = item.getId();
            dto.quoteId = item.getQuoteId();
            dto.lineOrder = item.getLineOrder();
            dto.expenseType = item.getExpenseType();
            dto.expenseTypeLabelSnapshot = item.getExpenseTypeLabelSnapshot();
            dto.feeTypeId = item.getFeeTypeId();
            dto.partId = item.getPartId();
            dto.description = item.getDescription();
            dto.originalPartName = item.getOriginalPartName();
            dto.spec = item.getSpec();
            dto.unit = item.getUnit();
            dto.quantity = item.getQuantity();
            dto.unitPrice = item.getUnitPrice();
            dto.standardPrice = item.getStandardPrice();
            dto.isPriceDeviated = item.getIsPriceDeviated();
            dto.amount = item.getAmount();
            dto.warehouse = item.getWarehouse();
            dto.remark = item.getRemark();
            dto.priceSourceInfo = item.getPriceSourceInfo();
            dto.manualPriceReason = item.getManualPriceReason();
            dto.alertMessage = alertMessage;
            return dto;
        }

        // Getters
        public Long getId() { return id; }
        public Long getQuoteId() { return quoteId; }
        public Integer getLineOrder() { return lineOrder; }
        public QuoteExpenseType getExpenseType() { return expenseType; }
        public Long getFeeTypeId() { return feeTypeId; }
        public Long getPartId() { return partId; }
        public String getDescription() { return description; }
        public String getOriginalPartName() { return originalPartName; }
        public String getSpec() { return spec; }
        public String getUnit() { return unit; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getStandardPrice() { return standardPrice; }
        public Boolean getIsPriceDeviated() { return isPriceDeviated; }
        public BigDecimal getAmount() { return amount; }
        public QuoteItemWarehouse getWarehouse() { return warehouse; }
        public String getRemark() { return remark; }
        public String getPriceSourceInfo() { return priceSourceInfo; }
        public String getManualPriceReason() { return manualPriceReason; }
        public String getAlertMessage() { return alertMessage; }
        public String getExpenseTypeLabelSnapshot() { return expenseTypeLabelSnapshot; }
    }
}
