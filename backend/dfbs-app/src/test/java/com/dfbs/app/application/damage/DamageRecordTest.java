package com.dfbs.app.application.damage;

import com.dfbs.app.application.shipment.MachineEntryDto;
import com.dfbs.app.application.shipment.NormalShipmentCreateRequest;
import com.dfbs.app.application.shipment.ShipmentService;
import com.dfbs.app.modules.damage.CompensationStatus;
import com.dfbs.app.modules.damage.DamageRecordEntity;
import com.dfbs.app.modules.damage.RepairStage;
import com.dfbs.app.modules.damage.TreatmentBehavior;
import com.dfbs.app.modules.damage.config.DamageTreatmentEntity;
import com.dfbs.app.modules.damage.config.DamageTreatmentRepo;
import com.dfbs.app.modules.damage.config.DamageTypeRepo;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineRepo;
import com.dfbs.app.modules.shipment.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DamageRecordTest {

    @Autowired
    private DamageService damageService;

    @Autowired
    private DamageTypeRepo damageTypeRepo;

    @Autowired
    private DamageTreatmentRepo damageTreatmentRepo;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ShipmentMachineRepo shipmentMachineRepo;

    /** Create a shipment with two machines (M1-SN01, M1-SN02) and return shipment id and first machine id. */
    private ShipmentWithMachines createShipmentWithMachines(Long operatorId) {
        NormalShipmentCreateRequest req = new NormalShipmentCreateRequest(
                "DAMAGE-CONTRACT", "销售", com.dfbs.app.modules.shipment.PackagingType.A,
                "收货", "13900000000", false, "地址", null
        );
        ShipmentEntity s = shipmentService.createNormal(req, operatorId);
        shipmentService.accept(s.getId(), operatorId);
        List<MachineEntryDto> entries = List.of(
                new MachineEntryDto("M1", "SN01", null, 2, null)
        );
        shipmentService.updateMachineIds(s.getId(), entries);
        List<ShipmentMachineEntity> machines = shipmentMachineRepo.findByShipmentIdOrderByIdAsc(s.getId());
        return new ShipmentWithMachines(s.getId(), machines.get(0).getId(), machines.get(1).getId());
    }

    private long damageTypeId() {
        return damageTypeRepo.findByIsEnabledTrue().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No damage type")).getId();
    }

    private long treatmentId(TreatmentBehavior behavior) {
        return damageTreatmentRepo.findByIsEnabledTrue().stream()
                .filter(t -> t.getBehavior() == behavior)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No treatment: " + behavior)).getId();
    }

    /**
     * Test 1 (Validation): Create without attachments -> Fail. Create with invalid machine ID -> Fail.
     */
    @Test
    void test1_validation_noAttachments_fail_invalidMachineId_fail() {
        Long operatorId = 300L;
        ShipmentWithMachines swm = createShipmentWithMachines(operatorId);
        long typeId = damageTypeId();
        long treatmentId = treatmentId(TreatmentBehavior.COMPENSATION);

        DamageCreateRequest noAttachments = new DamageCreateRequest(
                swm.shipmentId, swm.machine1Id, LocalDateTime.now(), typeId, treatmentId,
                "desc", List.of()
        );
        assertThatThrownBy(() -> damageService.create(noAttachments, operatorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required attachment");

        long invalidMachineId = 999999L;
        DamageCreateRequest invalidMachine = new DamageCreateRequest(
                swm.shipmentId, invalidMachineId, LocalDateTime.now(), typeId, treatmentId,
                "desc", List.of("https://example.com/photo.jpg")
        );
        assertThatThrownBy(() -> damageService.create(invalidMachine, operatorId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("machine");

        DamageCreateRequest wrongShipmentForMachine = new DamageCreateRequest(
                swm.shipmentId + 9999, swm.machine1Id, LocalDateTime.now(), typeId, treatmentId,
                "desc", List.of("https://example.com/photo.jpg")
        );
        assertThatThrownBy(() -> damageService.create(wrongShipmentForMachine, operatorId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("不属于");
    }

    /**
     * Test 2 (Compensation Flow): Create (Behavior=COMPENSATION) -> Confirm Compensation -> Verify Status/Amount.
     */
    @Test
    void test2_compensationFlow_create_confirmCompensation_verifyStatusAndAmount() {
        Long operatorId = 301L;
        ShipmentWithMachines swm = createShipmentWithMachines(operatorId);
        long typeId = damageTypeId();
        long treatmentId = treatmentId(TreatmentBehavior.COMPENSATION);

        DamageCreateRequest createReq = new DamageCreateRequest(
                swm.shipmentId, swm.machine1Id, LocalDateTime.now(), typeId, treatmentId,
                "外箱破损", List.of("https://example.com/damage1.jpg")
        );
        DamageRecordEntity record = damageService.create(createReq, operatorId);
        assertThat(record.getCompensationStatus()).isEqualTo(CompensationStatus.UNPAID);
        assertThat(record.getCompensationAmount()).isNull();

        DamageRecordEntity updated = damageService.confirmCompensation(record.getId(),
                new CompensationConfirmRequest(BigDecimal.valueOf(500.00), "https://example.com/proof.jpg"), operatorId);
        assertThat(updated.getCompensationStatus()).isEqualTo(CompensationStatus.PAID);
        assertThat(updated.getCompensationAmount()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(updated.getAttachmentUrls()).contains("proof.jpg");
    }

    /**
     * Test 3 (Repair Flow): Create (Behavior=REPAIR) -> Update Stage to SETTLED -> Verify Fees/Details.
     */
    @Test
    void test3_repairFlow_create_updateStageToSettled_verifyFeesAndDetails() {
        Long operatorId = 302L;
        ShipmentWithMachines swm = createShipmentWithMachines(operatorId);
        long typeId = damageTypeId();
        long treatmentId = treatmentId(TreatmentBehavior.REPAIR);

        DamageCreateRequest createReq = new DamageCreateRequest(
                swm.shipmentId, swm.machine2Id, LocalDateTime.now(), typeId, treatmentId,
                "主板损坏需返厂", List.of("https://example.com/repair1.jpg")
        );
        DamageRecordEntity record = damageService.create(createReq, operatorId);
        assertThat(record.getRepairStage()).isEqualTo(RepairStage.RETURNED);

        DamageRecordEntity updated = damageService.updateRepairStage(record.getId(),
                new RepairStageUpdateRequest(RepairStage.SETTLED, "已维修完成结算", BigDecimal.valueOf(800.00), BigDecimal.valueOf(100.00)),
                operatorId);
        assertThat(updated.getRepairStage()).isEqualTo(RepairStage.SETTLED);
        assertThat(updated.getSettlementDetails()).isEqualTo("已维修完成结算");
        assertThat(updated.getRepairFee()).isEqualByComparingTo(BigDecimal.valueOf(800.00));
        assertThat(updated.getPenaltyAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    /**
     * Test 4 (Isolation): Ensure Damage Record is strictly linked to one machine.
     */
    @Test
    void test4_isolation_eachRecordLinkedToOneMachine() {
        Long operatorId = 303L;
        ShipmentWithMachines swm = createShipmentWithMachines(operatorId);
        long typeId = damageTypeId();
        long compId = treatmentId(TreatmentBehavior.COMPENSATION);

        DamageCreateRequest forMachine1 = new DamageCreateRequest(
                swm.shipmentId, swm.machine1Id, LocalDateTime.now(), typeId, compId,
                "机器1损坏", List.of("https://example.com/m1.jpg")
        );
        DamageRecordEntity r1 = damageService.create(forMachine1, operatorId);

        DamageCreateRequest forMachine2 = new DamageCreateRequest(
                swm.shipmentId, swm.machine2Id, LocalDateTime.now(), typeId, compId,
                "机器2损坏", List.of("https://example.com/m2.jpg")
        );
        DamageRecordEntity r2 = damageService.create(forMachine2, operatorId);

        assertThat(r1.getShipmentMachineId()).isEqualTo(swm.machine1Id);
        assertThat(r2.getShipmentMachineId()).isEqualTo(swm.machine2Id);
        assertThat(r1.getShipmentMachineId()).isNotEqualTo(r2.getShipmentMachineId());

        List<DamageRecordDto> list = damageService.listByShipment(swm.shipmentId);
        assertThat(list).hasSize(2);
        assertThat(list).extracting(DamageRecordDto::shipmentMachineId).containsExactlyInAnyOrder(swm.machine1Id, swm.machine2Id);
        assertThat(list).extracting(DamageRecordDto::machineNo).containsExactlyInAnyOrder("SN01", "SN02");
    }

    private record ShipmentWithMachines(long shipmentId, long machine1Id, long machine2Id) {}
}
