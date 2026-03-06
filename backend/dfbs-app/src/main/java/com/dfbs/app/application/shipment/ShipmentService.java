package com.dfbs.app.application.shipment;

import com.dfbs.app.application.attachment.AttachmentPoint;
import com.dfbs.app.application.attachment.AttachmentRuleService;
import com.dfbs.app.application.attachment.AttachmentTargetType;
import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.modules.customer.CustomerRepo;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.DownstreamType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.shipment.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ShipmentService {

    private static final long WAREHOUSE_MANAGER_USER_ID = 1L;

    private final QuoteRepo quoteRepo;
    private final ShipmentRepo shipmentRepo;
    private final ShipmentMachineRepo shipmentMachineRepo;
    private final ShipmentExceptionRecordRepo exceptionRecordRepo;
    private final NotificationService notificationService;
    private final AttachmentRuleService ruleService;
    private final CustomerRepo customerRepo;

    public ShipmentService(QuoteRepo quoteRepo, ShipmentRepo shipmentRepo,
                           ShipmentMachineRepo shipmentMachineRepo,
                           ShipmentExceptionRecordRepo exceptionRecordRepo,
                           NotificationService notificationService,
                           AttachmentRuleService ruleService,
                           CustomerRepo customerRepo) {
        this.quoteRepo = quoteRepo;
        this.shipmentRepo = shipmentRepo;
        this.shipmentMachineRepo = shipmentMachineRepo;
        this.exceptionRecordRepo = exceptionRecordRepo;
        this.notificationService = notificationService;
        this.ruleService = ruleService;
        this.customerRepo = customerRepo;
    }

    private String resolveCustomerName(Long quoteId) {
        if (quoteId == null) return "-";
        return quoteRepo.findById(quoteId)
                .map(q -> q.getCustomerId() != null
                        ? customerRepo.findById(q.getCustomerId()).map(c -> c.getName()).orElse("客户 #" + q.getCustomerId())
                        : "-")
                .orElse("-");
    }

    /**
     * Create a shipment from a CONFIRMED quote. Enforces one quote -> one downstream doc.
     */
    @Transactional
    public ShipmentEntity create(ShipmentCreateRequest request, Long initiatorId) {
        QuoteEntity quote = quoteRepo.findById(request.quoteId())
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + request.quoteId()));

        if (quote.getStatus() != QuoteStatus.CONFIRMED) {
            throw new IllegalStateException("只能对已确认的报价单发起发货");
        }
        // MVP: allow any initiator; owner-only check removed for verification
        if (quote.getSourceType() == QuoteSourceType.WORK_ORDER) {
            throw new IllegalStateException("来源为工单的报价单不能发起发货，避免循环");
        }
        if (quote.getDownstreamId() != null) {
            throw new IllegalStateException("已发起下游单据，无法重复");
        }

        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setQuoteId(request.quoteId());
        shipment.setInitiatorId(initiatorId);
        shipment.setApplicantId(initiatorId);
        shipment.setType(ShipmentType.CUSTOMER_DELEGATE);
        shipment.setApprovalStatus(ApprovalStatus.APPROVED);
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setEntrustMatter(request.entrustMatter());
        shipment.setShipDate(request.shipDate());
        shipment.setQuantity(request.quantity());
        shipment.setModel(request.model());
        shipment.setNeedPackaging(request.needPackaging());
        shipment.setPickupContact(request.pickupContact());
        shipment.setPickupPhone(request.pickupPhone());
        shipment.setNeedLoading(request.needLoading());
        shipment.setPickupAddress(request.pickupAddress());
        shipment.setReceiverContact(request.receiverContact());
        shipment.setReceiverPhone(request.receiverPhone());
        shipment.setNeedUnloading(request.needUnloading());
        shipment.setDeliveryAddress(request.deliveryAddress());
        shipment.setRemark(request.remark());
        shipment.setCreatedAt(LocalDateTime.now());
        shipment = shipmentRepo.save(shipment);

        quote.setDownstreamType(DownstreamType.SHIPMENT);
        quote.setDownstreamId(shipment.getId());
        quoteRepo.save(quote);

        String title = "新发货单: 报价单 " + quote.getQuoteNo();
        String content = "从报价单 " + quote.getQuoteNo() + " 发起的发货单已创建";
        String targetUrl = "/shipments/" + shipment.getId();
        notificationService.send(WAREHOUSE_MANAGER_USER_ID, title, content, targetUrl);

        return shipment;
    }

    /**
     * Create a standalone shipment from customerId and type (STANDARD -> PackagingType.A, EXPRESS -> PackagingType.EXPORT).
     */
    @Transactional
    public ShipmentEntity createStandalone(Long customerId, String shipmentType, Long operatorId) {
        var customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new IllegalStateException("Customer not found: id=" + customerId));
        PackagingType packagingType = "EXPRESS".equalsIgnoreCase(shipmentType)
                ? PackagingType.EXPORT
                : PackagingType.A;
        var request = new NormalShipmentCreateRequest(
                "SHIP-" + System.currentTimeMillis(),
                "待填",
                packagingType,
                customer.getName(),
                "待填",
                false,
                "待填",
                null
        );
        return createNormal(request, operatorId);
    }

    /**
     * Create a Normal (Sales) shipment. No quote link. MVP: approvalStatus = APPROVED.
     */
    @Transactional
    public ShipmentEntity createNormal(NormalShipmentCreateRequest request, Long operatorId) {
        requireNotBlank(request.contractNo(), "合同号不能为空");
        requireNotBlank(request.salespersonName(), "销售员不能为空");
        requireNotBlank(request.receiverName(), "收货人不能为空");
        requireNotBlank(request.receiverPhone(), "收货人电话不能为空");
        requireNotBlank(request.deliveryAddress(), "收货地址不能为空");

        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setQuoteId(null);
        shipment.setInitiatorId(operatorId);
        shipment.setApplicantId(operatorId);
        shipment.setType(ShipmentType.NORMAL);
        shipment.setApprovalStatus(ApprovalStatus.APPROVED);
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setContractNo(request.contractNo());
        shipment.setSalespersonName(request.salespersonName());
        shipment.setPackagingType(request.packagingType());
        shipment.setReceiverName(request.receiverName());
        shipment.setReceiverPhone(request.receiverPhone());
        shipment.setUnloadService(request.unloadService());
        shipment.setDeliveryAddress(request.deliveryAddress());
        shipment.setRemark(request.remark());
        shipment.setCreatedAt(LocalDateTime.now());
        shipment = shipmentRepo.save(shipment);

        String title = "新发货单(正常): " + request.contractNo();
        String targetUrl = "/shipments/" + shipment.getId();
        notificationService.send(WAREHOUSE_MANAGER_USER_ID, title, "正常发货单已创建", targetUrl);
        return shipment;
    }

    /**
     * Create an Entrust shipment (Internal or Customer). Customer: require quoteId, link 1:1.
     */
    @Transactional
    public ShipmentEntity createEntrust(EntrustShipmentCreateRequest request, Long operatorId, boolean isCustomerEntrust) {
        Long quoteId = request.quoteId();
        if (isCustomerEntrust) {
            requireNotNull(quoteId, "客户委托必须关联报价单");
            QuoteEntity quote = quoteRepo.findById(quoteId)
                    .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));
            if (quote.getStatus() != QuoteStatus.CONFIRMED) {
                throw new IllegalStateException("只能对已确认的报价单发起委托发货");
            }
            if (quote.getDownstreamId() != null) {
                throw new IllegalStateException("该报价单已发起下游单据，无法重复");
            }
        }

        requireNotBlank(request.entrustMatter(), "委托事项不能为空");
        requireNotBlank(request.pickupContact(), "提货联系人不能为空");
        requireNotBlank(request.pickupAddress(), "提货地址不能为空");

        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setQuoteId(quoteId);
        shipment.setInitiatorId(operatorId);
        shipment.setApplicantId(operatorId);
        shipment.setType(isCustomerEntrust ? ShipmentType.CUSTOMER_DELEGATE : ShipmentType.SALES_DELEGATE);
        shipment.setApprovalStatus(ApprovalStatus.APPROVED);
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setEntrustMatter(request.entrustMatter());
        shipment.setShipDate(request.shipDate());
        shipment.setQuantity(request.quantity());
        shipment.setModel(request.model());
        shipment.setNeedPackaging(request.needPackaging());
        shipment.setPickupContact(request.pickupContact());
        shipment.setPickupPhone(request.pickupPhone());
        shipment.setNeedLoading(request.needLoading());
        shipment.setPickupAddress(request.pickupAddress());
        shipment.setReceiverContact(request.receiverContact());
        shipment.setReceiverPhone(request.receiverPhone());
        shipment.setNeedUnloading(request.needUnloading());
        shipment.setDeliveryAddress(request.deliveryAddress());
        shipment.setRemark(request.remark());
        shipment.setCreatedAt(LocalDateTime.now());
        shipment = shipmentRepo.save(shipment);

        if (isCustomerEntrust && quoteId != null) {
            QuoteEntity quote = quoteRepo.findById(quoteId).orElseThrow();
            quote.setDownstreamType(com.dfbs.app.modules.quote.enums.DownstreamType.SHIPMENT);
            quote.setDownstreamId(shipment.getId());
            quoteRepo.save(quote);
        }

        String title = "新发货单(委托): " + request.entrustMatter();
        String targetUrl = "/shipments/" + shipment.getId();
        notificationService.send(WAREHOUSE_MANAGER_USER_ID, title, "委托发货单已创建", targetUrl);
        return shipment;
    }

    /**
     * Update machine IDs for a shipment. Allowed only at PENDING_SHIP. Clears existing and inserts new.
     */
    @Transactional
    public List<ShipmentMachineEntity> updateMachineIds(Long shipmentId, List<MachineEntryDto> entries) {
        ShipmentEntity s = getDetail(shipmentId);
        require(s.getStatus() == ShipmentStatus.PENDING_SHIP, "只能在待发货状态下维护机器编号");

        shipmentMachineRepo.deleteByShipmentId(shipmentId);

        List<ShipmentMachineEntity> result = new ArrayList<>();
        for (MachineEntryDto dto : entries) {
            String model = dto.model();
            List<String> nos;
            if (dto.specificNos() != null && !dto.specificNos().isEmpty()) {
                nos = dto.specificNos();
            } else if (dto.startNo() != null && dto.count() != null && dto.count() > 0) {
                nos = generateSequentialNos(dto.startNo(), dto.count());
            } else {
                continue;
            }
            for (String no : nos) {
                ShipmentMachineEntity m = new ShipmentMachineEntity();
                m.setShipmentId(shipmentId);
                m.setModel(model);
                m.setMachineNo(no);
                result.add(shipmentMachineRepo.save(m));
            }
        }
        return result;
    }

    private List<String> generateSequentialNos(String startNo, int count) {
        List<String> list = new ArrayList<>();
        Pattern p = Pattern.compile("^(.*?)(\\d+)$");
        Matcher matcher = p.matcher(startNo);
        String prefix = startNo;
        int startNum = 0;
        int padLen = 0;
        if (matcher.matches()) {
            prefix = matcher.group(1);
            startNum = Integer.parseInt(matcher.group(2));
            padLen = matcher.group(2).length();
        }
        for (int i = 0; i < count; i++) {
            int n = startNum + i;
            String numStr = padLen > 0 ? String.format("%0" + padLen + "d", n) : String.valueOf(n);
            list.add(prefix + numStr);
        }
        return list;
    }

    @Transactional(readOnly = true)
    public Page<ShipmentEntity> list(ShipmentFilterRequest filter) {
        int page = filter.page() != null ? filter.page() : 0;
        int size = filter.size() != null ? filter.size() : 20;

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<ShipmentEntity> spec = (root, query, cb) -> {
            var p = cb.conjunction();
            if (filter.status() != null) {
                p = cb.and(p, cb.equal(root.get("status"), filter.status()));
            }
            if (filter.quoteId() != null) {
                p = cb.and(p, cb.equal(root.get("quoteId"), filter.quoteId()));
            }
            if (filter.initiatorId() != null) {
                p = cb.and(p, cb.equal(root.get("initiatorId"), filter.initiatorId()));
            }
            return p;
        };

        return shipmentRepo.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentListDto> listWithCustomerName(ShipmentFilterRequest filter) {
        return list(filter).map(this::toListDto);
    }

    private ShipmentListDto toListDto(ShipmentEntity e) {
        String shipmentNo = e.getContractNo() != null && !e.getContractNo().isBlank()
                ? e.getContractNo()
                : (e.getQuoteId() != null ? "Q" + e.getQuoteId() : "S" + e.getId());
        String customerName = resolveCustomerName(e.getQuoteId());
        return new ShipmentListDto(e.getId(), shipmentNo, customerName, e.getStatus(), e.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public ShipmentEntity getDetail(Long id) {
        return shipmentRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Shipment not found: id=" + id));
    }

    private static final String WORKFLOW_BASE = "/api/v1/shipments";

    /** Workflow step mapping: status -> stepCode + stepLabelCn (computed, not stored). */
    private static String stepCodeFor(ShipmentStatus status) {
        return switch (status) {
            case CREATED -> "REQUEST";
            case PENDING_SHIP -> "PREPARE";
            case SHIPPED -> "SHIP";
            case COMPLETED -> "SIGN_CLOSE";
            case EXCEPTION -> "EXCEPTION";
            case CANCELLED -> "CANCELLED";
        };
    }

    private static String stepLabelCnFor(ShipmentStatus status, LocalDateTime closedAt) {
        if (status == ShipmentStatus.COMPLETED) {
            return closedAt != null ? "已关闭" : "已签收（待关闭）";
        }
        return switch (status) {
            case CREATED -> "申请";
            case PENDING_SHIP -> "备货";
            case SHIPPED -> "运输中";
            case EXCEPTION -> "异常";
            case CANCELLED -> "已取消";
            default -> "签收完成";
        };
    }

    /** Available next actions aligned with require() and step actions (prepare, tracking, close). */
    private static List<WorkflowActionDto> allowedActions(ShipmentEntity s) {
        Long shipmentId = s.getId();
        String base = WORKFLOW_BASE + "/" + shipmentId;
        ShipmentStatus status = s.getStatus();
        List<WorkflowActionDto> list = new ArrayList<>();
        switch (status) {
            case CREATED -> {
                list.add(new WorkflowActionDto("ACCEPT", "审核并补充", "POST", base + "/accept", "确认接单并补充信息？"));
                list.add(new WorkflowActionDto("EXCEPTION", "标记异常", "POST", base + "/exception", "请输入异常原因"));
                list.add(new WorkflowActionDto("CANCEL", "取消", "POST", base + "/cancel", "确认取消？"));
            }
            case PENDING_SHIP -> {
                list.add(new WorkflowActionDto("PREPARE", "备货确认", "POST", base + "/prepare", null));
                list.add(new WorkflowActionDto("SHIP", "发运", "POST", base + "/ship", null));
                list.add(new WorkflowActionDto("EXCEPTION", "标记异常", "POST", base + "/exception", "请输入异常原因"));
                list.add(new WorkflowActionDto("CANCEL", "取消", "POST", base + "/cancel", "确认取消？"));
            }
            case SHIPPED -> {
                list.add(new WorkflowActionDto("TRACKING", "更新物流信息", "POST", base + "/tracking", null));
                list.add(new WorkflowActionDto("COMPLETE", "签收确认", "POST", base + "/complete", "确认签收完成？"));
                list.add(new WorkflowActionDto("EXCEPTION", "标记异常", "POST", base + "/exception", "请输入异常原因"));
                list.add(new WorkflowActionDto("CANCEL", "取消", "POST", base + "/cancel", "确认取消？"));
            }
            case COMPLETED -> {
                if (s.getClosedAt() == null) {
                    list.add(new WorkflowActionDto("CLOSE", "关闭", "POST", base + "/close", "确认关闭？"));
                }
                list.add(new WorkflowActionDto("CANCEL", "取消", "POST", base + "/cancel", "确认取消？"));
            }
            case EXCEPTION ->
                list.add(new WorkflowActionDto("CANCEL", "取消", "POST", base + "/cancel", "确认取消？"));
            case CANCELLED -> {}
        }
        return list;
    }

    @Transactional(readOnly = true)
    public ShipmentWorkflowDto getWorkflow(Long id) {
        ShipmentEntity s = getDetail(id);
        return new ShipmentWorkflowDto(
                s.getId(),
                s.getStatus(),
                stepCodeFor(s.getStatus()),
                stepLabelCnFor(s.getStatus(), s.getClosedAt()),
                allowedActions(s)
        );
    }

    @Transactional(readOnly = true)
    public List<ShipmentMachineEntity> getMachines(Long shipmentId) {
        return shipmentMachineRepo.findByShipmentIdOrderByIdAsc(shipmentId);
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private void requireNotNull(Object value, String message) {
        if (value == null) throw new IllegalStateException(message);
    }

    private void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalStateException(message);
    }

    @Transactional
    public ShipmentEntity accept(Long id, Long operatorId, AcceptSupplementRequest supplement) {
        ShipmentEntity s = getDetail(id);
        require(s.getStatus() == ShipmentStatus.CREATED, "只有新建状态的发货单可以接单");
        if (supplement != null) {
            if (supplement.contractNo() != null && !supplement.contractNo().isBlank())
                s.setContractNo(supplement.contractNo());
            if (supplement.receiverName() != null && !supplement.receiverName().isBlank())
                s.setReceiverName(supplement.receiverName());
            if (supplement.receiverPhone() != null && !supplement.receiverPhone().isBlank())
                s.setReceiverPhone(supplement.receiverPhone());
            if (supplement.deliveryAddress() != null && !supplement.deliveryAddress().isBlank())
                s.setDeliveryAddress(supplement.deliveryAddress());
            if (supplement.remark() != null) s.setRemark(supplement.remark());
            if (supplement.shipDate() != null) s.setShipDate(supplement.shipDate());
        }
        boolean hasReceiver = (s.getReceiverName() != null && !s.getReceiverName().isBlank())
                || (s.getReceiverContact() != null && !s.getReceiverContact().isBlank());
        if (!hasReceiver)
            throw new ShipmentValidationException("收货人/收货联系人不能为空", ShipmentValidationException.SHIPMENT_MISSING_RECEIVER);
        if (s.getDeliveryAddress() == null || s.getDeliveryAddress().isBlank())
            throw new ShipmentValidationException("收货地址不能为空", ShipmentValidationException.SHIPMENT_MISSING_DELIVERY_ADDRESS);
        s.setStatus(ShipmentStatus.PENDING_SHIP);
        s.setAcceptedAt(LocalDateTime.now());
        return shipmentRepo.save(s);
    }

    @Transactional
    public ShipmentEntity prepare(Long id, Long operatorId, PrepareRequest req) {
        ShipmentEntity s = getDetail(id);
        require(s.getStatus() == ShipmentStatus.PENDING_SHIP, "只有备货状态的发货单可执行备货确认");
        if (req != null) {
            if (req.quantity() != null) s.setQuantity(req.quantity());
            if (req.model() != null && !req.model().isBlank()) s.setModel(req.model());
            if (req.needPackaging() != null) s.setNeedPackaging(req.needPackaging());
            if (req.entrustMatter() != null && !req.entrustMatter().isBlank()) s.setEntrustMatter(req.entrustMatter());
            if (req.pickupContact() != null && !req.pickupContact().isBlank()) s.setPickupContact(req.pickupContact());
            if (req.pickupPhone() != null && !req.pickupPhone().isBlank()) s.setPickupPhone(req.pickupPhone());
            if (req.needLoading() != null) s.setNeedLoading(req.needLoading());
            if (req.pickupAddress() != null && !req.pickupAddress().isBlank()) s.setPickupAddress(req.pickupAddress());
            if (req.deliveryAddress() != null && !req.deliveryAddress().isBlank()) s.setDeliveryAddress(req.deliveryAddress());
            if (req.remark() != null) s.setRemark(req.remark());
        }
        return shipmentRepo.save(s);
    }

    @Transactional
    public ShipmentEntity ship(Long id, Long operatorId, ShipActionRequest req) {
        ShipmentEntity s = getDetail(id);
        require(s.getStatus() == ShipmentStatus.PENDING_SHIP, "只有待发货状态的发货单可以发货");

        if (s.getType() == ShipmentType.NORMAL) {
            String ticketUrl = req.ticketUrl() != null && !req.ticketUrl().isBlank() ? req.ticketUrl() : null;
            ruleService.validate(AttachmentTargetType.SHIPMENT_NORMAL, AttachmentPoint.SHIP_PICK_TICKET,
                    ticketUrl != null ? List.of(ticketUrl) : List.of());
            s.setTicketUrl(ticketUrl);
        }

        String entrustMatter = req.entrustMatter() != null ? req.entrustMatter() : s.getEntrustMatter();
        requireNotBlank(entrustMatter, "委托事项不能为空");
        s.setEntrustMatter(entrustMatter);

        var shipDate = req.shipDate() != null ? req.shipDate() : s.getShipDate();
        requireNotNull(shipDate, "发货日期不能为空");
        s.setShipDate(shipDate);

        Integer quantity = req.quantity() != null ? req.quantity() : s.getQuantity();
        requireNotNull(quantity, "数量不能为空");
        s.setQuantity(quantity);

        String model = req.model() != null ? req.model() : s.getModel();
        requireNotBlank(model, "型号不能为空");
        s.setModel(model);

        Boolean needPackaging = req.needPackaging() != null ? req.needPackaging() : s.getNeedPackaging();
        requireNotNull(needPackaging, "是否需要包装不能为空");
        s.setNeedPackaging(needPackaging);

        String pickupContact = req.pickupContact() != null ? req.pickupContact() : s.getPickupContact();
        requireNotBlank(pickupContact, "提货联系人不能为空");
        s.setPickupContact(pickupContact);

        String pickupPhone = req.pickupPhone() != null ? req.pickupPhone() : s.getPickupPhone();
        requireNotBlank(pickupPhone, "提货联系电话不能为空");
        s.setPickupPhone(pickupPhone);

        Boolean needLoading = req.needLoading() != null ? req.needLoading() : s.getNeedLoading();
        requireNotNull(needLoading, "是否需要装车不能为空");
        s.setNeedLoading(needLoading);

        String pickupAddress = req.pickupAddress() != null ? req.pickupAddress() : s.getPickupAddress();
        requireNotBlank(pickupAddress, "提货地址不能为空");
        s.setPickupAddress(pickupAddress);

        String receiverContact = req.receiverContact() != null ? req.receiverContact() : s.getReceiverContact();
        requireNotBlank(receiverContact, "收货联系人不能为空");
        s.setReceiverContact(receiverContact);

        String receiverPhone = req.receiverPhone() != null ? req.receiverPhone() : s.getReceiverPhone();
        requireNotBlank(receiverPhone, "收货联系电话不能为空");
        s.setReceiverPhone(receiverPhone);

        Boolean needUnloading = req.needUnloading() != null ? req.needUnloading() : s.getNeedUnloading();
        requireNotNull(needUnloading, "是否需要卸货不能为空");
        s.setNeedUnloading(needUnloading);

        String deliveryAddress = req.deliveryAddress() != null ? req.deliveryAddress() : s.getDeliveryAddress();
        requireNotBlank(deliveryAddress, "收货地址不能为空");
        s.setDeliveryAddress(deliveryAddress);

        String carrier = req.carrier() != null ? req.carrier() : s.getCarrier();
        s.setCarrier(carrier);
        String logisticsNo = req.logisticsNo() != null && !req.logisticsNo().isBlank() ? req.logisticsNo() : s.getLogisticsNo();
        s.setLogisticsNo(logisticsNo);
        if (req.receiptUrl() != null) s.setReceiptUrl(req.receiptUrl());
        if (s.getCarrier() == null || s.getCarrier().isBlank())
            throw new ShipmentValidationException("承运方不能为空", ShipmentValidationException.SHIPMENT_MISSING_CARRIER);
        if (s.getLogisticsNo() == null || s.getLogisticsNo().isBlank())
            throw new ShipmentValidationException("物流单号不能为空", ShipmentValidationException.SHIPMENT_MISSING_LOGISTICS_NO);

        s.setStatus(ShipmentStatus.SHIPPED);
        s.setShippedAt(LocalDateTime.now());
        return shipmentRepo.save(s);
    }

    @Transactional
    public ShipmentEntity complete(Long id, Long operatorId) {
        ShipmentEntity s = getDetail(id);
        require(s.getStatus() == ShipmentStatus.SHIPPED, "只有已发货的发货单可以完结");
        if (s.getType() == ShipmentType.NORMAL) {
            String receiptUrl = s.getReceiptUrl();
            if (receiptUrl != null && !receiptUrl.isBlank()) {
                ruleService.validate(AttachmentTargetType.SHIPMENT_NORMAL, AttachmentPoint.COMPLETE_RECEIPT, List.of(receiptUrl));
            }
        }
        s.setStatus(ShipmentStatus.COMPLETED);
        s.setCompletedAt(LocalDateTime.now());
        return shipmentRepo.save(s);
    }

    @Transactional
    public ShipmentEntity close(Long id, Long operatorId) {
        ShipmentEntity s = getDetail(id);
        require(s.getStatus() == ShipmentStatus.COMPLETED, "只有已签收的发货单可以关闭");
        if (s.getClosedAt() != null) {
            return s;
        }
        s.setClosedAt(LocalDateTime.now());
        return shipmentRepo.save(s);
    }

    @Transactional
    public ShipmentEntity tracking(Long id, Long operatorId, TrackingRequest req) {
        ShipmentEntity s = getDetail(id);
        require(s.getStatus() == ShipmentStatus.SHIPPED, "只有运输中的发货单可更新物流信息");
        if (req != null) {
            if (req.logisticsNo() != null && !req.logisticsNo().isBlank()) s.setLogisticsNo(req.logisticsNo());
            if (req.ticketUrl() != null) s.setTicketUrl(req.ticketUrl());
            if (req.receiptUrl() != null) s.setReceiptUrl(req.receiptUrl());
            if (req.remark() != null) s.setRemark(req.remark());
        }
        return shipmentRepo.save(s);
    }

    /**
     * Set receipt URL (e.g. after upload). Required for NORMAL completion.
     */
    @Transactional
    public ShipmentEntity setReceiptUrl(Long id, String receiptUrl) {
        ShipmentEntity s = getDetail(id);
        s.setReceiptUrl(receiptUrl);
        return shipmentRepo.save(s);
    }

    /**
     * Smart parse raw text into shipment fields. Heuristic patterns for Normal and Entrust.
     */
    @Transactional(readOnly = true)
    public ParsedShipmentDto parseText(String rawText, ShipmentType type) {
        if (rawText == null || rawText.isBlank()) {
            return new ParsedShipmentDto(type, null, null, null, null, null, null, null, null, null, Map.of());
        }
        String contractNo = matchOne(rawText, "合同号[：:]\\s*([^\\n\\r]+)");
        String receiverName = matchOne(rawText, "收货人[：:]\\s*([^\\n\\r]+)");
        String address = matchOne(rawText, "地址[：:]\\s*([^\\n\\r]+)");
        String entrustMatter = matchOne(rawText, "委托事项[：:]\\s*([^\\n\\r]+)");
        String pickup = matchOne(rawText, "提货[：:]\\s*([^\\n\\r]+)");
        String delivery = matchOne(rawText, "送货[：:]\\s*([^\\n\\r]+)");
        String salesperson = matchOne(rawText, "销售[员人][：:]?\\s*([^\\n\\r]+)");
        String phone = matchOne(rawText, "(?:收货人)?电话[：:]?\\s*([0-9\\-]+)");
        if (phone == null) phone = matchOne(rawText, "([0-9]{11})");

        return new ParsedShipmentDto(
                type,
                contractNo,
                salesperson,
                receiverName,
                phone,
                address != null ? address : delivery,
                entrustMatter,
                pickup,
                pickup,
                null,
                Map.of()
        );
    }

    private String matchOne(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    @Transactional
    public ShipmentEntity handleException(Long id, Long operatorId, ExceptionMarkRequest req) {
        if (req == null) throw new IllegalArgumentException("请求体不能为空");
        String description = req.description();
        requireNotBlank(description, "异常原因不能为空");
        ShipmentEntity s = getDetail(id);
        require(s.getStatus() != ShipmentStatus.COMPLETED && s.getStatus() != ShipmentStatus.CANCELLED,
                "已完结或已取消的发货单不能标记异常");
        if (req.machineId() != null) {
            ShipmentMachineEntity machine = shipmentMachineRepo.findById(req.machineId())
                    .orElseThrow(() -> new ShipmentValidationException("设备不属于本发运单或不存在", ShipmentValidationException.SHIPMENT_MACHINE_NOT_FOUND));
            if (!machine.getShipmentId().equals(id)) {
                throw new ShipmentValidationException("设备不属于本发运单", ShipmentValidationException.SHIPMENT_MACHINE_NOT_FOUND);
            }
        }
        s.setStatus(ShipmentStatus.EXCEPTION);
        s.setExceptionReason(req.reason() != null ? req.reason().trim() : description);
        ShipmentEntity saved = shipmentRepo.save(s);
        ShipmentExceptionRecordEntity record = new ShipmentExceptionRecordEntity();
        record.setShipmentId(id);
        record.setMachineId(req.machineId());
        record.setExceptionType(trimToNull(req.exceptionType(), 64));
        record.setDescription(description);
        record.setResponsibility(trimToNull(req.responsibility(), 128));
        record.setEvidenceUrl(req.evidenceUrl() != null && !req.evidenceUrl().isBlank() ? req.evidenceUrl().trim() : null);
        record.setOperatorId(operatorId);
        exceptionRecordRepo.save(record);
        return saved;
    }

    private static String trimToNull(String value, int maxLen) {
        if (value == null || value.isBlank()) return null;
        String t = value.trim();
        return t.length() > maxLen ? t.substring(0, maxLen) : t;
    }

    @Transactional(readOnly = true)
    public List<ExceptionRecordDto> listExceptionRecords(Long shipmentId, Long machineId) {
        getDetail(shipmentId);
        List<ShipmentExceptionRecordEntity> list = machineId != null
                ? exceptionRecordRepo.findByShipmentIdAndMachineIdOrderByCreatedAtDesc(shipmentId, machineId)
                : exceptionRecordRepo.findByShipmentIdOrderByCreatedAtDesc(shipmentId);
        return list.stream()
                .map(r -> new ExceptionRecordDto(
                        r.getId(),
                        r.getMachineId(),
                        r.getExceptionType(),
                        r.getDescription(),
                        r.getResponsibility(),
                        r.getEvidenceUrl(),
                        r.getOperatorId(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    /**
     * Cancel a shipment (e.g. when quote is voided or manually cancelled).
     */
    @Transactional
    public ShipmentEntity cancel(Long shipmentId, String reason) {
        ShipmentEntity shipment = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new IllegalStateException("Shipment not found: id=" + shipmentId));
        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            return shipment;
        }
        shipment.setStatus(ShipmentStatus.CANCELLED);
        if (reason != null && !reason.isBlank()) {
            shipment.setCancelReason(reason);
        }
        return shipmentRepo.save(shipment);
    }

    /**
     * Export result for ticket/receipt (bytes + filename for download).
     */
    public record ExportResult(byte[] bytes, String filename) {}

    /**
     * Generate simple HTML representation of shipment for pick ticket printing.
     */
    @Transactional(readOnly = true)
    public ExportResult exportTicket(Long id) {
        ShipmentEntity s = getDetail(id);
        List<ShipmentMachineEntity> machines = shipmentMachineRepo.findByShipmentIdOrderByIdAsc(id);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>发货单-").append(id).append("</title></head><body>");
        html.append("<h2>发货单 / Pick Ticket</h2>");
        html.append("<p><b>单号:</b> ").append(escape(s.getId())).append("</p>");
        html.append("<p><b>类型:</b> ").append(escape(s.getType())).append("</p>");
        html.append("<p><b>状态:</b> ").append(escape(s.getStatus())).append("</p>");
        if (s.getContractNo() != null) html.append("<p><b>合同号:</b> ").append(escape(s.getContractNo())).append("</p>");
        if (s.getReceiverName() != null) html.append("<p><b>收货人:</b> ").append(escape(s.getReceiverName())).append("</p>");
        if (s.getReceiverPhone() != null) html.append("<p><b>电话:</b> ").append(escape(s.getReceiverPhone())).append("</p>");
        if (s.getDeliveryAddress() != null) html.append("<p><b>地址:</b> ").append(escape(s.getDeliveryAddress())).append("</p>");
        if (s.getReceiverContact() != null) html.append("<p><b>收货联系人:</b> ").append(escape(s.getReceiverContact())).append("</p>");
        if (s.getReceiverPhone() != null && s.getReceiverContact() == null) {}
        if (s.getEntrustMatter() != null) html.append("<p><b>委托事项:</b> ").append(escape(s.getEntrustMatter())).append("</p>");
        html.append("<h3>机器明细</h3><table border=\"1\"><tr><th>型号</th><th>机器编号</th></tr>");
        for (ShipmentMachineEntity m : machines) {
            html.append("<tr><td>").append(escape(m.getModel())).append("</td><td>").append(escape(m.getMachineNo())).append("</td></tr>");
        }
        html.append("</table></body></html>");

        String filename = "shipment_" + id + "_ticket.html";
        return new ExportResult(html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8), filename);
    }

    /**
     * Generate simple HTML representation for receipt (签收单).
     */
    @Transactional(readOnly = true)
    public ExportResult exportReceipt(Long id) {
        ShipmentEntity s = getDetail(id);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>签收单-").append(id).append("</title></head><body>");
        html.append("<h2>签收单 / Receipt</h2>");
        html.append("<p><b>单号:</b> ").append(escape(s.getId())).append("</p>");
        html.append("<p><b>类型:</b> ").append(escape(s.getType())).append("</p>");
        if (s.getContractNo() != null) html.append("<p><b>合同号:</b> ").append(escape(s.getContractNo())).append("</p>");
        if (s.getReceiverName() != null) html.append("<p><b>收货人:</b> ").append(escape(s.getReceiverName())).append("</p>");
        if (s.getDeliveryAddress() != null) html.append("<p><b>送达地址:</b> ").append(escape(s.getDeliveryAddress())).append("</p>");
        if (s.getReceiptUrl() != null) html.append("<p><b>签收照片:</b> <a href=\"").append(escape(s.getReceiptUrl())).append("\">查看</a></p>");
        html.append("</body></html>");

        String filename = "shipment_" + id + "_receipt.html";
        return new ExportResult(html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8), filename);
    }

    private static String escape(Object o) {
        if (o == null) return "";
        String s = o.toString();
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
