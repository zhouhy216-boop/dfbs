package com.dfbs.app.application.quote;

import com.dfbs.app.config.CurrentUserProvider;
import com.dfbs.app.application.quote.dto.WorkOrderImportRequest;
import com.dfbs.app.modules.masterdata.PartEntity;
import com.dfbs.app.modules.masterdata.PartRepo;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.dictionary.FeeTypeEntity;
import com.dfbs.app.modules.quote.dictionary.FeeTypeRepo;
import com.dfbs.app.application.quote.dictionary.QuoteItemValidationHelper;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.QuoteVoidStatus;
import com.dfbs.app.modules.quote.payment.QuoteCollectorHistoryEntity;
import com.dfbs.app.modules.quote.payment.QuoteCollectorHistoryRepo;
import com.dfbs.app.modules.settings.BusinessLineEntity;
import com.dfbs.app.modules.settings.BusinessLineRepo;
import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.application.settings.WarehouseConfigService;
import com.dfbs.app.modules.quote.enums.QuoteItemWarehouse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuoteService {

    private final QuoteRepo quoteRepo;
    private final QuoteNumberService numberService;
    private final QuoteItemService itemService;
    private final CurrentUserProvider currentUserProvider;
    private final FeeTypeRepo feeTypeRepo;
    private final PartRepo partRepo;
    private final QuoteItemRepo itemRepo;
    private final QuoteItemValidationHelper validationHelper;
    private final QuoteCollectorHistoryRepo collectorHistoryRepo;
    private final BusinessLineRepo businessLineRepo;
    private final NotificationService notificationService;
    private final WarehouseConfigService warehouseConfigService;

    public QuoteService(QuoteRepo quoteRepo, QuoteNumberService numberService,
                        QuoteItemService itemService, CurrentUserProvider currentUserProvider,
                        FeeTypeRepo feeTypeRepo, PartRepo partRepo, QuoteItemRepo itemRepo,
                        QuoteItemValidationHelper validationHelper, QuoteCollectorHistoryRepo collectorHistoryRepo,
                        BusinessLineRepo businessLineRepo, NotificationService notificationService,
                        WarehouseConfigService warehouseConfigService) {
        this.quoteRepo = quoteRepo;
        this.numberService = numberService;
        this.itemService = itemService;
        this.currentUserProvider = currentUserProvider;
        this.feeTypeRepo = feeTypeRepo;
        this.partRepo = partRepo;
        this.itemRepo = itemRepo;
        this.validationHelper = validationHelper;
        this.collectorHistoryRepo = collectorHistoryRepo;
        this.businessLineRepo = businessLineRepo;
        this.notificationService = notificationService;
        this.warehouseConfigService = warehouseConfigService;
    }

    /**
     * Creates a new quote as DRAFT. Validates customer exists (optional mock – skipped for MVP).
     */
    @Transactional
    public QuoteEntity createDraft(CreateQuoteCommand cmd, String currentUser) {
        String quoteNo = numberService.generate(cmd.getSourceType(), currentUser);
        QuoteEntity entity = new QuoteEntity();
        entity.setQuoteNo(quoteNo);
        entity.setStatus(QuoteStatus.DRAFT);
        entity.setSourceType(cmd.getSourceType());
        entity.setSourceRefId(cmd.getSourceRefId());
        entity.setCustomerId(cmd.getCustomerId());
        if (cmd.getBusinessLineId() != null) {
            entity.setBusinessLineId(cmd.getBusinessLineId());
        }
        QuoteEntity saved = quoteRepo.save(entity);
        
        // Trigger 1: Check for HQ items and send CC notification
        checkAndSendWarehouseCcNotification(saved);
        
        return saved;
    }

    @Transactional
    public QuoteEntity updateHeader(Long id, UpdateQuoteCommand cmd) {
        QuoteEntity entity = quoteRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("quote not found: id=" + id));
        
        // Logic Freeze: If voidStatus == APPLYING, BLOCK the action
        if (entity.getVoidStatus() == QuoteVoidStatus.APPLYING) {
            throw new IllegalStateException("作废申请审批中，操作已冻结");
        }
        
        if (entity.getStatus() != QuoteStatus.DRAFT) {
            throw new IllegalStateException("Cannot update confirmed or cancelled quote");
        }
        if (cmd.getCurrency() != null) {
            entity.setCurrency(cmd.getCurrency());
        }
        if (cmd.getRecipient() != null) {
            entity.setRecipient(cmd.getRecipient());
        }
        if (cmd.getPhone() != null) {
            entity.setPhone(cmd.getPhone());
        }
        if (cmd.getAddress() != null) {
            entity.setAddress(cmd.getAddress());
        }
        if (cmd.getBusinessLineId() != null) {
            entity.setBusinessLineId(cmd.getBusinessLineId());
        }
        QuoteEntity saved = quoteRepo.save(entity);
        
        // Trigger 1: Check for HQ items and send CC notification (on update)
        checkAndSendWarehouseCcNotification(saved);
        
        return saved;
    }

    @Transactional
    public QuoteEntity confirm(Long id) {
        QuoteEntity entity = quoteRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("quote not found: id=" + id));
        
        // Validate all items BEFORE changing status to CONFIRMED
        List<QuoteItemEntity> items = itemRepo.findByQuoteIdOrderByLineOrderAsc(id);
        for (int i = 0; i < items.size(); i++) {
            QuoteItemEntity item = items.get(i);
            int lineNumber = i + 1;
            try {
                validateItemForConfirmation(item, lineNumber);
            } catch (IllegalStateException e) {
                throw new QuoteValidationException(
                        String.format("明细第%d行：%s", lineNumber, e.getMessage()), e);
            }
        }
        
        entity.setStatus(QuoteStatus.CONFIRMED);
        QuoteEntity saved = quoteRepo.save(entity);
        
        // Trigger: Send notifications to leaders after confirmation
        sendNotificationsToLeaders(saved);
        
        // Trigger 2: Check for HQ items and send Ship notification
        checkAndSendWarehouseShipNotification(saved);
        
        return saved;
    }
    
    /**
     * Send notifications to leaders based on Business Line configuration.
     * 
     * @param quote The confirmed quote
     */
    private void sendNotificationsToLeaders(QuoteEntity quote) {
        if (quote.getBusinessLineId() == null) {
            // No business line configured, skip notification
            return;
        }
        
        BusinessLineEntity businessLine = businessLineRepo.findById(quote.getBusinessLineId()).orElse(null);
        if (businessLine == null || !businessLine.getIsActive()) {
            // Business line not found or inactive, skip notification
            return;
        }
        
        String leaderIdsStr = businessLine.getLeaderIds();
        if (leaderIdsStr == null || leaderIdsStr.trim().isEmpty()) {
            // No leaders configured, skip notification
            return;
        }
        
        // Parse leader IDs (support JSON array or comma-separated)
        List<Long> leaderIds = parseLeaderIds(leaderIdsStr);
        
        // Calculate total amount
        List<QuoteItemEntity> items = itemRepo.findByQuoteIdOrderByLineOrderAsc(quote.getId());
        BigDecimal totalAmount = items.stream()
                .map(QuoteItemEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get customer name (mock for now, in production would fetch from Customer service)
        String customerName = "客户 #" + quote.getCustomerId();
        
        // Build notification content
        String title = String.format("报价单已确认: %s", quote.getQuoteNo());
        String content = String.format("客户: %s, 金额: %s %s. 点击查看详情。",
                customerName,
                totalAmount.toPlainString(),
                quote.getCurrency() != null ? quote.getCurrency().name() : "CNY");
        String targetUrl = String.format("/quotes/%d", quote.getId());
        
        // Send notification to each leader
        for (Long leaderId : leaderIds) {
            notificationService.send(leaderId, title, content, targetUrl);
        }
    }
    
    /**
     * Parse leader IDs from string (supports JSON array or comma-separated).
     * 
     * @param leaderIdsStr String containing leader IDs
     * @return List of leader IDs
     */
    private List<Long> parseLeaderIds(String leaderIdsStr) {
        List<Long> leaderIds = new java.util.ArrayList<>();
        String trimmed = leaderIdsStr.trim();
        
        // Try JSON array format first (e.g., "[1,2,3]")
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            if (!inner.isEmpty()) {
                String[] parts = inner.split(",");
                for (String part : parts) {
                    try {
                        leaderIds.add(Long.parseLong(part.trim()));
                    } catch (NumberFormatException e) {
                        // Skip invalid entries
                    }
                }
            }
        } else {
            // Comma-separated format (e.g., "1,2,3")
            String[] parts = trimmed.split(",");
            for (String part : parts) {
                try {
                    leaderIds.add(Long.parseLong(part.trim()));
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        }
        
        return leaderIds;
    }
    
    /**
     * Validates a QuoteItem for confirmation (stricter than draft validation).
     * 
     * @param item The item to validate
     * @param lineNumber Line number for error messages
     */
    private void validateItemForConfirmation(QuoteItemEntity item, int lineNumber) {
        // Rule 1: feeType must be from Dictionary and Active (Block "Free Text" fee types)
        if (item.getFeeTypeId() == null) {
            throw new IllegalStateException("费用类型必须从字典中选择，不能使用自由文本");
        }
        
        FeeTypeEntity feeType = feeTypeRepo.findById(item.getFeeTypeId())
                .orElseThrow(() -> new IllegalStateException("费用类型不存在：id=" + item.getFeeTypeId()));
        
        if (!feeType.getIsActive()) {
            throw new IllegalStateException("费用类型已禁用，请选择其他费用类型");
        }
        
        // Rule 2: unit must be in allowedUnits (if defined)
        // Auto-fix: If invalid, reset to defaultUnit and allow proceed
        if (feeType.getAllowedUnits() != null && !feeType.getAllowedUnits().trim().isEmpty()) {
            List<String> allowedUnits = parseCommaSeparated(feeType.getAllowedUnits());
            if (item.getUnit() == null || !allowedUnits.contains(item.getUnit())) {
                // Auto-fix: reset to defaultUnit
                String defaultUnit = feeType.getDefaultUnit() != null ? feeType.getDefaultUnit() : "次";
                item.setUnit(defaultUnit);
                itemRepo.save(item);
            }
        }
        
        // Rule 3: spec validation
        // If FeeType is PARTS: partId MUST be set and valid (must be a BOM item)
        if (item.getExpenseType() == QuoteExpenseType.PARTS) {
            if (item.getPartId() == null) {
                throw new IllegalStateException("配件费必须选择系统BOM配件");
            }
            
            PartEntity part = partRepo.findById(item.getPartId())
                    .orElseThrow(() -> new IllegalStateException("配件不存在：id=" + item.getPartId()));
            
            if (!part.getIsActive()) {
                throw new IllegalStateException("配件已禁用，请选择其他配件");
            }
        }
        
        // If FeeType has fixedSpecOptions: spec MUST be one of them
        if (feeType.getFixedSpecOptions() != null && !feeType.getFixedSpecOptions().trim().isEmpty()) {
            List<String> fixedOptions = parseCommaSeparated(feeType.getFixedSpecOptions());
            if (item.getSpec() == null || item.getSpec().isBlank()) {
                throw new IllegalStateException(
                        String.format("规格必须从以下选项中选择：%s", feeType.getFixedSpecOptions()));
            }
            if (!fixedOptions.contains(item.getSpec())) {
                throw new IllegalStateException(
                        String.format("规格'%s'不在允许的选项中，允许的选项：%s", 
                                item.getSpec(), feeType.getFixedSpecOptions()));
            }
        }
    }
    
    private List<String> parseCommaSeparated(String value) {
        if (value == null || value.trim().isEmpty()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Transactional
    public QuoteEntity cancel(Long id) {
        QuoteEntity entity = quoteRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("quote not found: id=" + id));
        entity.setStatus(QuoteStatus.CANCELLED);
        return quoteRepo.save(entity);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<QuoteEntity> findById(Long id) {
        return quoteRepo.findById(id);
    }

    @Transactional
    public QuoteEntity createFromWorkOrder(WorkOrderImportRequest req) {
        if (req.workOrderNo() == null || req.workOrderNo().isBlank()) {
            throw new IllegalStateException("workOrderNo is required");
        }
        if (req.serviceManagerUserId() == null) {
            throw new IllegalStateException("serviceManagerUserId is required");
        }

        String currentUser = currentUserProvider.getCurrentUser();
        String quoteNo = numberService.generate(QuoteSourceType.WORK_ORDER, currentUser);

        QuoteEntity quote = new QuoteEntity();
        quote.setQuoteNo(quoteNo);
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setSourceType(QuoteSourceType.WORK_ORDER);
        quote.setSourceId(req.workOrderNo());
        quote.setCustomerId(req.customerId() != null ? req.customerId() : 1L);
        quote.setRecipient(req.recipient());
        quote.setPhone(req.phone());
        quote.setAddress(req.address());
        quote.setMachineInfo(req.machineInfo());
        quote.setAssigneeId(req.serviceManagerUserId());
        if (req.collectorUserId() != null) {
            quote.setCollectorId(req.collectorUserId());
        }
        quote = quoteRepo.save(quote);

        Long quoteId = quote.getId();

        // Item 1: Repair Fee (always) - Map to "技术服务费"
        FeeTypeEntity repairFeeType = feeTypeRepo.findByName("技术服务费").orElse(null);
        var repairCmd = new QuoteItemService.CreateItemCommand();
        repairCmd.setExpenseType(QuoteExpenseType.REPAIR);
        repairCmd.setQuantity(1);
        repairCmd.setUnit(repairFeeType != null && repairFeeType.getDefaultUnit() != null 
                ? repairFeeType.getDefaultUnit() : "次");
        repairCmd.setUnitPrice(BigDecimal.ZERO);  // Manual pricing
        repairCmd.setDescription("维修费");
        repairCmd.setFeeTypeId(repairFeeType != null ? repairFeeType.getId() : null);
        QuoteItemEntity repairItem = itemService.addItem(quoteId, repairCmd);
        if (repairFeeType != null && repairItem != null) {
            repairItem.setFeeTypeId(repairFeeType.getId());
            itemRepo.save(repairItem);
        }

        // Item 2: Onsite Fee (if isOnsite == true) - Map to "登门费"
        if (Boolean.TRUE.equals(req.isOnsite())) {
            FeeTypeEntity onsiteFeeType = feeTypeRepo.findByName("登门费").orElse(null);
            var onsiteCmd = new QuoteItemService.CreateItemCommand();
            onsiteCmd.setExpenseType(QuoteExpenseType.ON_SITE);
            onsiteCmd.setQuantity(1);
            onsiteCmd.setUnit(onsiteFeeType != null && onsiteFeeType.getDefaultUnit() != null 
                    ? onsiteFeeType.getDefaultUnit() : "次");
            onsiteCmd.setUnitPrice(BigDecimal.ZERO);  // Manual pricing
            onsiteCmd.setDescription("上门费");
            onsiteCmd.setFeeTypeId(onsiteFeeType != null ? onsiteFeeType.getId() : null);
            QuoteItemEntity onsiteItem = itemService.addItem(quoteId, onsiteCmd);
            if (onsiteFeeType != null && onsiteItem != null) {
                onsiteItem.setFeeTypeId(onsiteFeeType.getId());
                itemRepo.save(onsiteItem);
            }
        }

        // Item 3+: Parts Fee (loop) - Map to "配件费"
        if (req.parts() != null) {
            FeeTypeEntity partsFeeType = feeTypeRepo.findByName("配件费").orElse(null);
            for (WorkOrderImportRequest.PartInfo partInfo : req.parts()) {
                // Try to find PartEntity by name
                PartEntity partEntity = null;
                if (partInfo.partName() != null && !partInfo.partName().isBlank()) {
                    partEntity = partRepo.findByName(partInfo.partName()).orElse(null);
                }
                
                var partCmd = new QuoteItemService.CreateItemCommand();
                partCmd.setExpenseType(QuoteExpenseType.PARTS);
                partCmd.setDescription(partInfo.partName() != null ? partInfo.partName() : "");
                partCmd.setQuantity(partInfo.qty() != null ? partInfo.qty() : 1);
                partCmd.setUnit(partsFeeType != null && partsFeeType.getDefaultUnit() != null 
                        ? partsFeeType.getDefaultUnit() : "个");
                partCmd.setUnitPrice(BigDecimal.ZERO);  // Manual pricing
                partCmd.setFeeTypeId(partsFeeType != null ? partsFeeType.getId() : null);
                partCmd.setPartId(partEntity != null ? partEntity.getId() : null);
                QuoteItemEntity partItem = itemService.addItem(quoteId, partCmd);
                if (partItem != null) {
                    if (partsFeeType != null) {
                        partItem.setFeeTypeId(partsFeeType.getId());
                    }
                    if (partEntity != null) {
                        partItem.setPartId(partEntity.getId());
                    }
                    itemRepo.save(partItem);
                }
            }
        }

        return quoteRepo.findById(quoteId).orElse(quote);
    }

    /** Command for creating a draft quote. */
    public static final class CreateQuoteCommand {
        private QuoteSourceType sourceType;
        private String sourceRefId;
        private Long customerId;
        private Long businessLineId;

        public QuoteSourceType getSourceType() { return sourceType; }
        public void setSourceType(QuoteSourceType sourceType) { this.sourceType = sourceType; }
        public String getSourceRefId() { return sourceRefId; }
        public void setSourceRefId(String sourceRefId) { this.sourceRefId = sourceRefId; }
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public Long getBusinessLineId() { return businessLineId; }
        public void setBusinessLineId(Long businessLineId) { this.businessLineId = businessLineId; }
    }

    /** Command for updating quote header (DRAFT only). */
    public static final class UpdateQuoteCommand {
        private Currency currency;
        private String recipient;
        private String phone;
        private String address;
        private Long businessLineId;

        public Currency getCurrency() { return currency; }
        public void setCurrency(Currency currency) { this.currency = currency; }
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public Long getBusinessLineId() { return businessLineId; }
        public void setBusinessLineId(Long businessLineId) { this.businessLineId = businessLineId; }
    }

    /**
     * Change collector for a quote.
     * Blocked if quote.paymentStatus == PAID.
     */
    @Transactional
    public QuoteEntity changeCollector(Long quoteId, Long newCollectorId, Long changedBy) {
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));

        // Check if quote.paymentStatus == PAID. If yes, BLOCK change.
        if (quote.getPaymentStatus() == QuotePaymentStatus.PAID) {
            throw new IllegalStateException("报价单已付清，不能更改收款人");
        }

        Long fromUserId = quote.getCollectorId();
        quote.setCollectorId(newCollectorId);
        quote = quoteRepo.save(quote);

        // Log to QuoteCollectorHistory
        QuoteCollectorHistoryEntity history = new QuoteCollectorHistoryEntity();
        history.setQuoteId(quoteId);
        history.setFromUserId(fromUserId);
        history.setToUserId(newCollectorId);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        collectorHistoryRepo.save(history);

        return quote;
    }

    /**
     * Create a balance adjustment quote for overpayment.
     * 
     * @param parentQuoteId Original quote ID
     * @param balanceAmount Balance amount (overpayment)
     */
    @Transactional
    public QuoteEntity createBalanceQuote(Long parentQuoteId, BigDecimal balanceAmount) {
        QuoteEntity parent = quoteRepo.findById(parentQuoteId)
                .orElseThrow(() -> new IllegalStateException("Parent quote not found: id=" + parentQuoteId));

        String currentUser = currentUserProvider.getCurrentUser();
        String quoteNo = numberService.generate(QuoteSourceType.MANUAL, currentUser);

        QuoteEntity balanceQuote = new QuoteEntity();
        balanceQuote.setQuoteNo(quoteNo);
        balanceQuote.setStatus(QuoteStatus.DRAFT);
        balanceQuote.setSourceType(QuoteSourceType.MANUAL);
        balanceQuote.setCustomerId(parent.getCustomerId());
        balanceQuote.setRecipient(parent.getRecipient());
        balanceQuote.setPhone(parent.getPhone());
        balanceQuote.setAddress(parent.getAddress());
        balanceQuote.setCurrency(parent.getCurrency());
        balanceQuote.setCollectorId(parent.getCollectorId());
        balanceQuote.setParentQuoteId(parentQuoteId);
        balanceQuote.setPaymentStatus(QuotePaymentStatus.UNPAID);

        // Note: totalAmount will be set when items are added
        return quoteRepo.save(balanceQuote);
    }
    
    /**
     * Check if quote has any items from HEADQUARTERS warehouse.
     * 
     * @param quote The quote to check
     * @return true if has HQ items, false otherwise
     */
    private boolean hasHeadquartersItems(QuoteEntity quote) {
        List<QuoteItemEntity> items = itemRepo.findByQuoteIdOrderByLineOrderAsc(quote.getId());
        return items.stream()
                .anyMatch(item -> item.getWarehouse() == QuoteItemWarehouse.HEADQUARTERS);
    }
    
    /**
     * Trigger 1: Check and send warehouse CC notification (on create/update).
     * Public method so it can be called from QuoteItemService.
     * 
     * @param quote The quote to check
     */
    public void checkAndSendWarehouseCcNotification(QuoteEntity quote) {
        if (!hasHeadquartersItems(quote)) {
            return;  // No HQ items, skip
        }
        
        if (Boolean.TRUE.equals(quote.getIsWarehouseCcSent())) {
            return;  // Already sent, skip (de-duplication)
        }
        
        List<Long> warehouseUserIds = warehouseConfigService.getWarehouseUserIds();
        if (warehouseUserIds.isEmpty()) {
            return;  // No warehouse users configured, skip
        }
        
        // Send notification to each warehouse user
        String title = String.format("新报价单需发货: %s", quote.getQuoteNo());
        String content = String.format("报价单 %s 包含总部仓库明细项，请准备发货。", quote.getQuoteNo());
        String targetUrl = String.format("/quotes/%d", quote.getId());
        
        for (Long userId : warehouseUserIds) {
            notificationService.send(userId, title, content, targetUrl);
        }
        
        // Mark as sent and save
        quote.setIsWarehouseCcSent(true);
        quoteRepo.save(quote);
    }
    
    /**
     * Trigger 2: Check and send warehouse Ship notification (on confirm).
     * 
     * @param quote The confirmed quote
     */
    private void checkAndSendWarehouseShipNotification(QuoteEntity quote) {
        if (!hasHeadquartersItems(quote)) {
            return;  // No HQ items, skip
        }
        
        if (Boolean.TRUE.equals(quote.getIsWarehouseShipSent())) {
            return;  // Already sent, skip (de-duplication)
        }
        
        List<Long> warehouseUserIds = warehouseConfigService.getWarehouseUserIds();
        if (warehouseUserIds.isEmpty()) {
            return;  // No warehouse users configured, skip
        }
        
        // Send notification to each warehouse user
        String title = String.format("报价单已确认，请安排发货: %s", quote.getQuoteNo());
        String content = String.format("报价单 %s 已确认，包含总部仓库明细项，请安排发货。", quote.getQuoteNo());
        String targetUrl = String.format("/quotes/%d", quote.getId());
        
        for (Long userId : warehouseUserIds) {
            notificationService.send(userId, title, content, targetUrl);
        }
        
        // Mark as sent and save
        quote.setIsWarehouseShipSent(true);
        quoteRepo.save(quote);
    }
}
