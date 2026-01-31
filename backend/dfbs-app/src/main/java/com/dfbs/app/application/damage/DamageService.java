package com.dfbs.app.application.damage;

import com.dfbs.app.application.attachment.AttachmentPoint;
import com.dfbs.app.application.attachment.AttachmentRuleService;
import com.dfbs.app.application.attachment.AttachmentTargetType;
import com.dfbs.app.modules.damage.*;
import com.dfbs.app.modules.damage.config.DamageTreatmentEntity;
import com.dfbs.app.modules.damage.config.DamageTreatmentRepo;
import com.dfbs.app.modules.damage.config.DamageTypeEntity;
import com.dfbs.app.modules.damage.config.DamageTypeRepo;
import com.dfbs.app.modules.shipment.ShipmentMachineEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DamageService {

    private final DamageRecordRepo damageRecordRepo;
    private final DamageTypeRepo damageTypeRepo;
    private final DamageTreatmentRepo damageTreatmentRepo;
    private final ShipmentMachineRepo shipmentMachineRepo;
    private final ObjectMapper objectMapper;
    private final AttachmentRuleService ruleService;

    public DamageService(DamageRecordRepo damageRecordRepo,
                         DamageTypeRepo damageTypeRepo,
                         DamageTreatmentRepo damageTreatmentRepo,
                         ShipmentMachineRepo shipmentMachineRepo,
                         ObjectMapper objectMapper,
                         AttachmentRuleService ruleService) {
        this.damageRecordRepo = damageRecordRepo;
        this.damageTypeRepo = damageTypeRepo;
        this.damageTreatmentRepo = damageTreatmentRepo;
        this.shipmentMachineRepo = shipmentMachineRepo;
        this.objectMapper = objectMapper;
        this.ruleService = ruleService;
    }

    /**
     * Create a damage record. Validates: machine belongs to shipment, attachments not empty.
     * Initializes stage/status based on treatment behavior.
     */
    @Transactional
    public DamageRecordEntity create(DamageCreateRequest request, Long operatorId) {
        requireNotNull(request.shipmentId(), "shipmentId不能为空");
        requireNotNull(request.shipmentMachineId(), "shipmentMachineId不能为空");
        requireNotNull(request.occurrenceTime(), "occurrenceTime不能为空");
        requireNotNull(request.damageTypeId(), "damageTypeId不能为空");
        requireNotNull(request.treatmentId(), "treatmentId不能为空");
        ruleService.validate(AttachmentTargetType.DAMAGE_RECORD, AttachmentPoint.CREATE,
                request.attachmentUrls() != null ? request.attachmentUrls() : List.of());

        ShipmentMachineEntity machine = shipmentMachineRepo.findById(request.shipmentMachineId())
                .orElseThrow(() -> new IllegalStateException("Shipment machine not found: id=" + request.shipmentMachineId()));
        if (!machine.getShipmentId().equals(request.shipmentId())) {
            throw new IllegalStateException("机器编号不属于该发货单(shipmentMachineId与shipmentId不匹配)");
        }

        DamageTypeEntity damageType = damageTypeRepo.findById(request.damageTypeId())
                .orElseThrow(() -> new IllegalStateException("Damage type not found: id=" + request.damageTypeId()));
        DamageTreatmentEntity treatment = damageTreatmentRepo.findById(request.treatmentId())
                .orElseThrow(() -> new IllegalStateException("Damage treatment not found: id=" + request.treatmentId()));

        DamageRecordEntity record = new DamageRecordEntity();
        record.setShipmentId(request.shipmentId());
        record.setShipmentMachineId(request.shipmentMachineId());
        record.setOccurrenceTime(request.occurrenceTime());
        record.setDamageTypeId(request.damageTypeId());
        record.setTreatmentId(request.treatmentId());
        record.setDescription(request.description());
        record.setAttachmentUrls(serializeAttachmentUrls(request.attachmentUrls()));
        record.setCreatedAt(LocalDateTime.now());
        record.setOperatorId(operatorId);

        TreatmentBehavior behavior = treatment.getBehavior();
        if (behavior == TreatmentBehavior.REPAIR) {
            record.setRepairStage(RepairStage.RETURNED);
        } else if (behavior == TreatmentBehavior.COMPENSATION) {
            record.setCompensationStatus(CompensationStatus.UNPAID);
        }

        return damageRecordRepo.save(record);
    }

    /**
     * List damage records by shipment with type name, treatment name, and machine info.
     */
    @Transactional(readOnly = true)
    public List<DamageRecordDto> listByShipment(Long shipmentId) {
        List<DamageRecordEntity> records = damageRecordRepo.findByShipmentIdOrderByOccurrenceTimeDesc(shipmentId);
        Map<Long, DamageTypeEntity> typeMap = typeMap();
        Map<Long, DamageTreatmentEntity> treatmentMap = treatmentMap();
        List<DamageRecordDto> result = new ArrayList<>();
        for (DamageRecordEntity r : records) {
            ShipmentMachineEntity machine = shipmentMachineRepo.findById(r.getShipmentMachineId()).orElse(null);
            DamageTypeEntity type = typeMap.get(r.getDamageTypeId());
            DamageTreatmentEntity treatment = treatmentMap.get(r.getTreatmentId());
            result.add(toDto(r, machine, type, treatment));
        }
        return result;
    }

    /**
     * Update repair stage. Only for REPAIR behavior. If stage is SETTLED, require settlementDetails and costs.
     */
    @Transactional
    public DamageRecordEntity updateRepairStage(Long id, RepairStageUpdateRequest request, Long operatorId) {
        DamageRecordEntity record = damageRecordRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Damage record not found: id=" + id));
        DamageTreatmentEntity treatment = damageTreatmentRepo.findById(record.getTreatmentId())
                .orElseThrow(() -> new IllegalStateException("Treatment not found"));
        if (treatment.getBehavior() != TreatmentBehavior.REPAIR) {
            throw new IllegalStateException("只有维修类型的处理方式才能更新维修阶段");
        }

        requireNotNull(request.stage(), "stage不能为空");
        record.setRepairStage(request.stage());
        record.setUpdatedAt(LocalDateTime.now());
        record.setOperatorId(operatorId);

        if (request.stage() == RepairStage.SETTLED) {
            requireNotBlank(request.settlementDetails(), "结算时必须填写结算说明(settlementDetails)");
            requireNotNull(request.repairFee(), "结算时必须填写维修费(repairFee)");
            record.setSettlementDetails(request.settlementDetails());
            record.setRepairFee(request.repairFee());
            record.setPenaltyAmount(request.penaltyAmount());
        }

        return damageRecordRepo.save(record);
    }

    /**
     * Confirm compensation payment. Only for COMPENSATION behavior. Sets PAID, saves amount, appends proofUrl.
     */
    @Transactional
    public DamageRecordEntity confirmCompensation(Long id, CompensationConfirmRequest request, Long operatorId) {
        DamageRecordEntity record = damageRecordRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Damage record not found: id=" + id));
        DamageTreatmentEntity treatment = damageTreatmentRepo.findById(record.getTreatmentId())
                .orElseThrow(() -> new IllegalStateException("Treatment not found"));
        if (treatment.getBehavior() != TreatmentBehavior.COMPENSATION) {
            throw new IllegalStateException("只有赔偿类型的处理方式才能确认赔偿");
        }

        requireNotNull(request.amount(), "赔偿金额不能为空");
        requireNotBlank(request.proofUrl(), "支付凭证(proofUrl)不能为空");

        record.setCompensationStatus(CompensationStatus.PAID);
        record.setCompensationAmount(request.amount());
        record.setUpdatedAt(LocalDateTime.now());
        record.setOperatorId(operatorId);

        List<String> urls = parseAttachmentUrls(record.getAttachmentUrls());
        urls.add(request.proofUrl());
        record.setAttachmentUrls(serializeAttachmentUrls(urls));

        return damageRecordRepo.save(record);
    }

    @Transactional(readOnly = true)
    public List<com.dfbs.app.modules.damage.config.DamageTypeEntity> listActiveTypes() {
        return damageTypeRepo.findByIsEnabledTrue();
    }

    @Transactional(readOnly = true)
    public List<DamageTreatmentEntity> listActiveTreatments() {
        return damageTreatmentRepo.findByIsEnabledTrue();
    }

    private Map<Long, DamageTypeEntity> typeMap() {
        List<DamageTypeEntity> all = damageTypeRepo.findAll();
        return all.stream().collect(java.util.stream.Collectors.toMap(DamageTypeEntity::getId, t -> t));
    }

    private Map<Long, DamageTreatmentEntity> treatmentMap() {
        List<DamageTreatmentEntity> all = damageTreatmentRepo.findAll();
        return all.stream().collect(java.util.stream.Collectors.toMap(DamageTreatmentEntity::getId, t -> t));
    }

    private DamageRecordDto toDto(DamageRecordEntity r,
                                  ShipmentMachineEntity machine,
                                  DamageTypeEntity type,
                                  DamageTreatmentEntity treatment) {
        return new DamageRecordDto(
                r.getId(),
                r.getShipmentId(),
                r.getShipmentMachineId(),
                machine != null ? machine.getModel() : null,
                machine != null ? machine.getMachineNo() : null,
                r.getOccurrenceTime(),
                r.getDamageTypeId(),
                type != null ? type.getName() : null,
                r.getTreatmentId(),
                treatment != null ? treatment.getName() : null,
                r.getDescription(),
                parseAttachmentUrls(r.getAttachmentUrls()),
                r.getRepairStage(),
                r.getCompensationStatus(),
                r.getSettlementDetails(),
                r.getCompensationAmount(),
                r.getRepairFee(),
                r.getPenaltyAmount(),
                r.getCreatedAt(),
                r.getOperatorId()
        );
    }

    private String serializeAttachmentUrls(List<String> urls) {
        try {
            return objectMapper.writeValueAsString(urls);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize attachment URLs", e);
        }
    }

    private List<String> parseAttachmentUrls(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void requireNotNull(Object value, String message) {
        if (value == null) throw new IllegalStateException(message);
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalStateException(message);
    }
}
