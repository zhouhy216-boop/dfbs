package com.dfbs.app.application.correction;

import com.dfbs.app.application.freightbill.FreightBillService;
import com.dfbs.app.modules.freightbill.FreightBillEntity;
import com.dfbs.app.modules.freightbill.FreightBillItemEntity;
import com.dfbs.app.modules.freightbill.FreightBillItemRepo;
import com.dfbs.app.modules.freightbill.FreightBillRepo;
import com.dfbs.app.modules.freightbill.FreightBillStatus;
import com.dfbs.app.modules.shipment.ShipmentRepo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class FreightBillCorrectionExecutor implements CorrectionExecutor {

    private final FreightBillService freightBillService;
    private final FreightBillRepo freightBillRepo;
    private final FreightBillItemRepo freightBillItemRepo;
    private final ShipmentRepo shipmentRepo;

    public FreightBillCorrectionExecutor(FreightBillService freightBillService,
                                          FreightBillRepo freightBillRepo,
                                          FreightBillItemRepo freightBillItemRepo,
                                          ShipmentRepo shipmentRepo) {
        this.freightBillService = freightBillService;
        this.freightBillRepo = freightBillRepo;
        this.freightBillItemRepo = freightBillItemRepo;
        this.shipmentRepo = shipmentRepo;
    }

    @Override
    @Transactional
    public void voidOld(Long id) {
        freightBillService.voidBill(id);
    }

    @Override
    @Transactional
    public Long createNew(Long oldId, String changesJson, Long createdBy) {
        FreightBillEntity old = freightBillRepo.findById(oldId)
                .orElseThrow(() -> new IllegalStateException("Freight bill not found: id=" + oldId));
        List<FreightBillItemEntity> oldItems = freightBillItemRepo.findByBillIdOrderByIdAsc(oldId);

        String newBillNo = "FB-" + System.currentTimeMillis();
        while (freightBillRepo.existsByBillNo(newBillNo)) newBillNo = "FB-" + System.currentTimeMillis();

        FreightBillEntity neu = new FreightBillEntity();
        neu.setBillNo(newBillNo);
        neu.setCarrierId(old.getCarrierId());
        neu.setCarrier(old.getCarrier());
        neu.setPeriod(old.getPeriod());
        neu.setStatus(FreightBillStatus.DRAFT);
        neu.setTotalAmount(old.getTotalAmount() != null ? old.getTotalAmount() : BigDecimal.ZERO);
        neu.setAttachmentUrl(null);
        neu.setAuditTime(null);
        neu.setAuditorId(null);
        neu.setCreatedTime(LocalDateTime.now());
        neu.setCreatorId(createdBy);
        FreightBillEntity savedBill = freightBillRepo.save(neu);

        for (FreightBillItemEntity oi : oldItems) {
            FreightBillItemEntity ni = new FreightBillItemEntity();
            ni.setBillId(savedBill.getId());
            ni.setShipmentId(oi.getShipmentId());
            ni.setShipmentNo(oi.getShipmentNo());
            ni.setFinancialCategory(oi.getFinancialCategory());
            ni.setMachineModel(oi.getMachineModel());
            ni.setQuantity(oi.getQuantity());
            ni.setUnitPrice(oi.getUnitPrice());
            ni.setLineTotal(oi.getLineTotal());
            ni.setRemark(oi.getRemark());
            freightBillItemRepo.save(ni);
        }

        for (FreightBillItemEntity oi : oldItems) {
            shipmentRepo.findById(oi.getShipmentId()).ifPresent(s -> {
                s.setFreightBillId(savedBill.getId());
                shipmentRepo.save(s);
            });
        }

        return savedBill.getId();
    }
}
