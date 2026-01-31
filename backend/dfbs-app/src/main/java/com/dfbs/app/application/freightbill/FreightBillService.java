package com.dfbs.app.application.freightbill;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.dfbs.app.application.attachment.AttachmentPoint;
import com.dfbs.app.application.attachment.AttachmentRuleService;
import com.dfbs.app.application.attachment.AttachmentTargetType;
import com.dfbs.app.modules.carrier.CarrierEntity;
import com.dfbs.app.modules.carrier.CarrierRepo;
import com.dfbs.app.modules.freightbill.*;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineRepo;
import com.dfbs.app.modules.shipment.ShipmentRepo;
import com.dfbs.app.modules.shipment.ShipmentStatus;
import com.dfbs.app.modules.shipment.ShipmentType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FreightBillService {

    private static final List<ShipmentStatus> ELIGIBLE_STATUSES = List.of(ShipmentStatus.SHIPPED, ShipmentStatus.COMPLETED);

    private final FreightBillRepo freightBillRepo;
    private final FreightBillItemRepo freightBillItemRepo;
    private final ShipmentRepo shipmentRepo;
    private final ShipmentMachineRepo shipmentMachineRepo;
    private final CarrierRepo carrierRepo;
    private final AttachmentRuleService ruleService;

    public FreightBillService(FreightBillRepo freightBillRepo,
                              FreightBillItemRepo freightBillItemRepo,
                              ShipmentRepo shipmentRepo,
                              ShipmentMachineRepo shipmentMachineRepo,
                              CarrierRepo carrierRepo,
                              AttachmentRuleService ruleService) {
        this.freightBillRepo = freightBillRepo;
        this.freightBillItemRepo = freightBillItemRepo;
        this.shipmentRepo = shipmentRepo;
        this.shipmentMachineRepo = shipmentMachineRepo;
        this.carrierRepo = carrierRepo;
        this.ruleService = ruleService;
    }

    /**
     * Available shipments for a carrier: status SHIPPED or COMPLETED, not yet linked to any bill.
     */
    @Transactional(readOnly = true)
    public List<ShipmentEntity> getAvailableShipments(String carrier) {
        if (carrier == null || carrier.isBlank()) return List.of();
        return shipmentRepo.findByCarrierAndFreightBillIdIsNullAndStatusIn(carrier, ELIGIBLE_STATUSES);
    }

    /**
     * Create freight bill by carrier and period: find all eligible shipments (carrierId, createdAt in period, not linked), create bill and items.
     * Period format: "yyyy-MM" or "yyyyMM".
     */
    @Transactional
    public FreightBillEntity createBill(Long carrierId, String period, Long operatorId) {
        requireNotNull(carrierId, "carrierId不能为空");
        requireNotBlank(period, "period不能为空");
        CarrierEntity carrier = carrierRepo.findById(carrierId)
                .orElseThrow(() -> new IllegalStateException("Carrier not found: id=" + carrierId));
        var range = parsePeriod(period);
        List<ShipmentEntity> shipments = shipmentRepo.findByCarrierIdAndFreightBillIdIsNullAndStatusInAndCreatedAtBetween(
                carrierId, ELIGIBLE_STATUSES, range.from(), range.to());
        if (shipments.isEmpty()) {
            throw new IllegalStateException("该承运方在周期 " + period + " 内无可用发货单");
        }

        String billNo = generateBillNo();
        FreightBillEntity bill = new FreightBillEntity();
        bill.setBillNo(billNo);
        bill.setCarrierId(carrierId);
        bill.setCarrier(carrier.getName());
        bill.setPeriod(period);
        bill.setStatus(FreightBillStatus.DRAFT);
        bill.setTotalAmount(BigDecimal.ZERO);
        bill.setCreatedTime(LocalDateTime.now());
        bill.setCreatorId(operatorId);
        bill = freightBillRepo.save(bill);

        for (ShipmentEntity shipment : shipments) {
            List<ShipmentMachineEntity> machines = shipmentMachineRepo.findByShipmentIdOrderByIdAsc(shipment.getId());
            Map<String, Long> modelToCount = machines.stream()
                    .filter(m -> m.getModel() != null && !m.getModel().isBlank())
                    .collect(Collectors.groupingBy(ShipmentMachineEntity::getModel, Collectors.counting()));
            if (modelToCount.isEmpty()) {
                modelToCount.put(shipment.getModel() != null ? shipment.getModel() : "未分类", (long) Math.max(1, machines.size()));
            }
            FinancialCategory category = mapToCategory(shipment);
            String shipmentNo = "S" + shipment.getId();

            for (Map.Entry<String, Long> e : modelToCount.entrySet()) {
                FreightBillItemEntity item = new FreightBillItemEntity();
                item.setBillId(bill.getId());
                item.setShipmentId(shipment.getId());
                item.setShipmentNo(shipmentNo);
                item.setFinancialCategory(category);
                item.setMachineModel(e.getKey());
                item.setQuantity(e.getValue().intValue());
                item.setUnitPrice(null);
                item.setLineTotal(BigDecimal.ZERO);
                freightBillItemRepo.save(item);
            }

            shipment.setFreightBillId(bill.getId());
            shipmentRepo.save(shipment);
        }

        recalcBillTotal(bill.getId());
        return freightBillRepo.findById(bill.getId()).orElseThrow();
    }

    private static record PeriodRange(LocalDateTime from, LocalDateTime to) {}

    private static PeriodRange parsePeriod(String period) {
        String p = period.trim();
        YearMonth ym;
        if (p.length() == 6 && p.indexOf('-') < 0) {
            // yyyyMM
            int y = Integer.parseInt(p.substring(0, 4));
            int m = Integer.parseInt(p.substring(4, 6));
            ym = YearMonth.of(y, m);
        } else if (p.length() >= 7 && p.indexOf('-') == 4) {
            // yyyy-MM
            ym = YearMonth.parse(p.substring(0, 7));
        } else {
            throw new IllegalArgumentException("Invalid period format, use yyyy-MM or yyyyMM: " + period);
        }
        return new PeriodRange(
                ym.atDay(1).atStartOfDay(),
                ym.atEndOfMonth().atTime(23, 59, 59, 999_000_000));
    }

    /**
     * Export merged freight bills to Excel: Sheet 1 Summary (Bill No, Carrier, Amount), Sheet 2 Details (Shipment No, Address, Amount).
     */
    @Transactional(readOnly = true)
    public ExportResult exportMergedBills(List<Long> billIds) {
        if (billIds == null || billIds.isEmpty()) {
            throw new IllegalStateException("至少选择一个运单");
        }
        List<FreightBillEntity> bills = freightBillRepo.findAllById(billIds);
        if (bills.size() != billIds.size()) {
            throw new IllegalStateException("部分运单不存在");
        }
        List<FreightBillItemEntity> items = freightBillItemRepo.findByBillIdIn(billIds);
        Set<Long> shipmentIds = items.stream().map(FreightBillItemEntity::getShipmentId).collect(Collectors.toSet());
        Map<Long, String> addressByShipmentId = new HashMap<>();
        if (!shipmentIds.isEmpty()) {
            for (ShipmentEntity s : shipmentRepo.findAllById(shipmentIds)) {
                addressByShipmentId.put(s.getId(), s.getDeliveryAddress() != null ? s.getDeliveryAddress() : "");
            }
        }
        Map<Long, String> carrierNameById = new HashMap<>();
        for (FreightBillEntity b : bills) {
            if (b.getCarrierId() != null) {
                carrierRepo.findById(b.getCarrierId()).ifPresent(c -> carrierNameById.put(c.getId(), c.getName()));
            }
        }

        List<FreightBillSummaryRow> summaryRows = new ArrayList<>();
        for (FreightBillEntity b : bills) {
            String carrierName = b.getCarrierId() != null ? carrierNameById.getOrDefault(b.getCarrierId(), b.getCarrier()) : b.getCarrier();
            if (carrierName == null) carrierName = "";
            summaryRows.add(FreightBillSummaryRow.of(b.getBillNo(), carrierName, b.getTotalAmount()));
        }

        List<FreightBillDetailRow> detailRows = new ArrayList<>();
        for (FreightBillItemEntity it : items) {
            String address = addressByShipmentId.getOrDefault(it.getShipmentId(), "");
            detailRows.add(FreightBillDetailRow.of(it.getShipmentNo(), address, it.getLineTotal()));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelWriter excelWriter = EasyExcel.write(out)
                .autoCloseStream(false)
                .build();
        WriteSheet summarySheet = EasyExcel.writerSheet(0, "Summary")
                .head(FreightBillSummaryRow.class)
                .build();
        excelWriter.write(summaryRows, summarySheet);
        WriteSheet detailsSheet = EasyExcel.writerSheet(1, "Details")
                .head(FreightBillDetailRow.class)
                .build();
        excelWriter.write(detailRows, detailsSheet);
        excelWriter.finish();

        String filename = "freight_bills_merged_" + System.currentTimeMillis() + ".xlsx";
        return new ExportResult(out.toByteArray(), filename);
    }

    /**
     * Map shipment type to financial category.
     */
    public FinancialCategory mapToCategory(ShipmentEntity shipment) {
        if (shipment == null || shipment.getType() == null) return FinancialCategory.NORMAL;
        return switch (shipment.getType()) {
            case NORMAL -> FinancialCategory.NORMAL;
            case CUSTOMER_DELEGATE -> FinancialCategory.ENTRUST_CUSTOMER;
            case SALES_DELEGATE -> FinancialCategory.ENTRUST_SALES;
            case PRODUCTION_DELEGATE -> FinancialCategory.ENTRUST_PRODUCTION;
        };
    }

    /**
     * Create freight bill from selected shipments. Groups machines by model per shipment, creates items, locks shipments.
     */
    @Transactional
    public FreightBillEntity create(String carrier, List<Long> shipmentIds, Long operatorId) {
        requireNotBlank(carrier, "承运方不能为空");
        requireNotNull(shipmentIds, "shipmentIds不能为空");
        if (shipmentIds.isEmpty()) throw new IllegalStateException("至少选择一个发货单");

        List<ShipmentEntity> shipments = new ArrayList<>();
        for (Long sid : shipmentIds) {
            ShipmentEntity s = shipmentRepo.findById(sid)
                    .orElseThrow(() -> new IllegalStateException("Shipment not found: id=" + sid));
            if (!carrier.equals(s.getCarrier())) {
                throw new IllegalStateException("发货单承运方与运单承运方不一致: shipmentId=" + sid);
            }
            if (s.getFreightBillId() != null) {
                throw new IllegalStateException("发货单已被其他运单占用: shipmentId=" + sid);
            }
            if (!ELIGIBLE_STATUSES.contains(s.getStatus())) {
                throw new IllegalStateException("只有已发货或已完结的发货单可加入运单: shipmentId=" + sid);
            }
            shipments.add(s);
        }

        String billNo = generateBillNo();
        FreightBillEntity bill = new FreightBillEntity();
        bill.setBillNo(billNo);
        bill.setCarrier(carrier);
        bill.setStatus(FreightBillStatus.DRAFT);
        bill.setTotalAmount(BigDecimal.ZERO);
        bill.setCreatedTime(LocalDateTime.now());
        bill.setCreatorId(operatorId);
        bill = freightBillRepo.save(bill);

        for (ShipmentEntity shipment : shipments) {
            List<ShipmentMachineEntity> machines = shipmentMachineRepo.findByShipmentIdOrderByIdAsc(shipment.getId());
            Map<String, Long> modelToCount = machines.stream()
                    .filter(m -> m.getModel() != null && !m.getModel().isBlank())
                    .collect(Collectors.groupingBy(ShipmentMachineEntity::getModel, Collectors.counting()));
            if (modelToCount.isEmpty()) {
                modelToCount.put(shipment.getModel() != null ? shipment.getModel() : "未分类", (long) Math.max(1, machines.size()));
            }
            FinancialCategory category = mapToCategory(shipment);
            String shipmentNo = "S" + shipment.getId();

            for (Map.Entry<String, Long> e : modelToCount.entrySet()) {
                FreightBillItemEntity item = new FreightBillItemEntity();
                item.setBillId(bill.getId());
                item.setShipmentId(shipment.getId());
                item.setShipmentNo(shipmentNo);
                item.setFinancialCategory(category);
                item.setMachineModel(e.getKey());
                item.setQuantity(e.getValue().intValue());
                item.setUnitPrice(null);
                item.setLineTotal(BigDecimal.ZERO);
                freightBillItemRepo.save(item);
            }

            shipment.setFreightBillId(bill.getId());
            shipmentRepo.save(shipment);
        }

        recalcBillTotal(bill.getId());
        return freightBillRepo.findById(bill.getId()).orElseThrow();
    }

    /**
     * Void a freight bill (data correction). Only DRAFT or CONFIRMED can be voided.
     */
    @Transactional
    public FreightBillEntity voidBill(Long billId) {
        FreightBillEntity bill = freightBillRepo.findById(billId)
                .orElseThrow(() -> new IllegalStateException("Freight bill not found: id=" + billId));
        if (bill.getStatus() == FreightBillStatus.SETTLED) {
            throw new IllegalStateException("已结算的运单不能作废");
        }
        if (bill.getStatus() == FreightBillStatus.VOID) {
            throw new IllegalStateException("运单已作废");
        }
        bill.setStatus(FreightBillStatus.VOID);
        return freightBillRepo.save(bill);
    }

    /**
     * Update item prices. Bill must be DRAFT. Recalculates lineTotal and bill totalAmount.
     */
    @Transactional
    public FreightBillEntity updateItems(Long billId, List<ItemUpdateDto> updates) {
        FreightBillEntity bill = freightBillRepo.findById(billId)
                .orElseThrow(() -> new IllegalStateException("Freight bill not found: id=" + billId));
        if (bill.getStatus() != FreightBillStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的运单可修改明细");
        }

        if (updates != null) {
            for (ItemUpdateDto dto : updates) {
                freightBillItemRepo.findById(dto.itemId()).ifPresent(item -> {
                    if (!item.getBillId().equals(billId)) return;
                    item.setUnitPrice(dto.unitPrice());
                    if (dto.remark() != null) item.setRemark(dto.remark());
                    BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                    item.setLineTotal(dto.unitPrice() != null ? dto.unitPrice().multiply(qty) : BigDecimal.ZERO);
                    freightBillItemRepo.save(item);
                });
            }
        }

        recalcBillTotal(billId);
        return freightBillRepo.findById(billId).orElseThrow();
    }

    /**
     * Remove a shipment from the bill. Bill must be DRAFT. Deletes items, releases shipment lock, recalc total.
     */
    @Transactional
    public FreightBillEntity removeShipment(Long billId, Long shipmentId) {
        FreightBillEntity bill = freightBillRepo.findById(billId)
                .orElseThrow(() -> new IllegalStateException("Freight bill not found: id=" + billId));
        if (bill.getStatus() != FreightBillStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的运单可移除发货单");
        }

        freightBillItemRepo.deleteByBillIdAndShipmentId(billId, shipmentId);

        ShipmentEntity shipment = shipmentRepo.findById(shipmentId).orElse(null);
        if (shipment != null && billId.equals(shipment.getFreightBillId())) {
            shipment.setFreightBillId(null);
            shipmentRepo.save(shipment);
        }

        recalcBillTotal(billId);
        return freightBillRepo.findById(billId).orElseThrow();
    }

    /**
     * Confirm bill. attachmentUrl is mandatory (Bill Photo).
     */
    @Transactional
    public FreightBillEntity confirm(Long billId, String attachmentUrl) {
        FreightBillEntity bill = freightBillRepo.findById(billId)
                .orElseThrow(() -> new IllegalStateException("Freight bill not found: id=" + billId));
        ruleService.validate(AttachmentTargetType.FREIGHT_BILL, AttachmentPoint.CONFIRM,
                attachmentUrl != null ? List.of(attachmentUrl) : List.of());
        bill.setStatus(FreightBillStatus.CONFIRMED);
        bill.setAttachmentUrl(attachmentUrl);
        bill.setAuditTime(LocalDateTime.now());
        return freightBillRepo.save(bill);
    }

    /**
     * Settle bill.
     */
    @Transactional
    public FreightBillEntity settle(Long billId) {
        FreightBillEntity bill = freightBillRepo.findById(billId)
                .orElseThrow(() -> new IllegalStateException("Freight bill not found: id=" + billId));
        bill.setStatus(FreightBillStatus.SETTLED);
        return freightBillRepo.save(bill);
    }

    @Transactional(readOnly = true)
    public FreightBillEntity getBill(Long billId) {
        return freightBillRepo.findById(billId)
                .orElseThrow(() -> new IllegalStateException("Freight bill not found: id=" + billId));
    }

    @Transactional(readOnly = true)
    public List<FreightBillItemEntity> getItems(Long billId) {
        return freightBillItemRepo.findByBillIdOrderByIdAsc(billId);
    }

    /**
     * Export draft: HTML grouped by FinancialCategory.
     */
    @Transactional(readOnly = true)
    public ExportResult exportDraft(Long billId) {
        FreightBillEntity bill = getBill(billId);
        List<FreightBillItemEntity> items = getItems(billId);

        Map<FinancialCategory, List<FreightBillItemEntity>> byCategory = items.stream()
                .collect(Collectors.groupingBy(FreightBillItemEntity::getFinancialCategory));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>运单-").append(bill.getBillNo()).append("</title></head><body>");
        html.append("<h2>运单 / Freight Bill</h2>");
        html.append("<p><b>单号:</b> ").append(escape(bill.getBillNo())).append("</p>");
        html.append("<p><b>承运方:</b> ").append(escape(bill.getCarrier())).append("</p>");
        html.append("<p><b>状态:</b> ").append(escape(bill.getStatus())).append("</p>");
        html.append("<p><b>合计:</b> ").append(escape(bill.getTotalAmount())).append("</p>");

        for (FinancialCategory cat : FinancialCategory.values()) {
            List<FreightBillItemEntity> list = byCategory.get(cat);
            if (list == null || list.isEmpty()) continue;
            html.append("<h3>").append(escape(cat)).append("</h3>");
            html.append("<table border=\"1\"><tr><th>发货单号</th><th>型号</th><th>数量</th><th>单价</th><th>行合计</th></tr>");
            for (FreightBillItemEntity it : list) {
                html.append("<tr><td>").append(escape(it.getShipmentNo())).append("</td><td>").append(escape(it.getMachineModel()))
                        .append("</td><td>").append(escape(it.getQuantity())).append("</td><td>").append(escape(it.getUnitPrice()))
                        .append("</td><td>").append(escape(it.getLineTotal())).append("</td></tr>");
            }
            html.append("</table>");
        }
        html.append("</body></html>");

        String filename = "freight_bill_" + bill.getBillNo().replaceAll("[^a-zA-Z0-9_-]", "_") + ".html";
        return new ExportResult(html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8), filename);
    }

    public record ExportResult(byte[] bytes, String filename) {}

    private void recalcBillTotal(Long billId) {
        List<FreightBillItemEntity> items = freightBillItemRepo.findByBillIdOrderByIdAsc(billId);
        BigDecimal total = items.stream()
                .map(i -> i.getLineTotal() != null ? i.getLineTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        freightBillRepo.findById(billId).ifPresent(b -> {
            b.setTotalAmount(total);
            freightBillRepo.save(b);
        });
    }

    private String generateBillNo() {
        String base = "FB-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String candidate = base;
        int suffix = 0;
        while (freightBillRepo.existsByBillNo(candidate)) {
            candidate = base + "-" + (++suffix);
        }
        return candidate;
    }

    private static String escape(Object o) {
        if (o == null) return "";
        String s = o.toString();
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalStateException(message);
    }

    private static void requireNotNull(Object value, String message) {
        if (value == null) throw new IllegalStateException(message);
    }
}
