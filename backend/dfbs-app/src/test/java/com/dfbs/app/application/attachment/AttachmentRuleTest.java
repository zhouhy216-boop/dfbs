package com.dfbs.app.application.attachment;

import com.dfbs.app.application.damage.DamageCreateRequest;
import com.dfbs.app.application.damage.DamageService;
import com.dfbs.app.application.freightbill.FreightBillService;
import com.dfbs.app.application.inventory.TransferService;
import com.dfbs.app.application.shipment.*;
import com.dfbs.app.modules.damage.config.DamageTreatmentEntity;
import com.dfbs.app.modules.damage.config.DamageTreatmentRepo;
import com.dfbs.app.modules.damage.config.DamageTypeEntity;
import com.dfbs.app.modules.damage.config.DamageTypeRepo;
import com.dfbs.app.modules.freightbill.FreightBillEntity;
import com.dfbs.app.modules.freightbill.FreightBillRepo;
import com.dfbs.app.modules.inventory.TransferOrderEntity;
import com.dfbs.app.modules.inventory.TransferOrderRepo;
import com.dfbs.app.modules.inventory.TransferStatus;
import com.dfbs.app.modules.shipment.PackagingType;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineRepo;
import com.dfbs.app.modules.shipment.ShipmentRepo;
import com.dfbs.app.modules.shipment.ShipmentStatus;
import com.dfbs.app.modules.shipment.ShipmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AttachmentRuleTest {

    @Autowired
    private AttachmentRuleService ruleService;

    @Autowired
    private AttachmentUploadService uploadService;

    @Autowired
    private FreightBillService freightBillService;

    @Autowired
    private FreightBillRepo freightBillRepo;

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferOrderRepo transferOrderRepo;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ShipmentRepo shipmentRepo;

    @Autowired
    private ShipmentMachineRepo shipmentMachineRepo;

    @Autowired
    private DamageService damageService;

    @Autowired
    private DamageTypeRepo damageTypeRepo;

    @Autowired
    private DamageTreatmentRepo damageTreatmentRepo;

    private Long operatorId;
    private Long shipmentIdForDamage;
    private Long machineIdForDamage;
    private Long damageTypeId;
    private Long treatmentId;

    @BeforeEach
    void setUp() {
        operatorId = 500L;
    }

    /**
     * Test 1 (Uploader): Upload file > 10MB -> Fail. Upload valid -> Success.
     */
    @Test
    void test1_uploader_over10Mb_fail_valid_success() {
        byte[] oversized = new byte[11 * 1024 * 1024]; // 11MB
        MultipartFile bigFile = new MockMultipartFile("file", "big.bin", "application/octet-stream", oversized);
        assertThatThrownBy(() -> uploadService.upload(bigFile, AttachmentType.BILL_PHOTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("10MB");

        byte[] small = new byte[100];
        MultipartFile validFile = new MockMultipartFile("file", "photo.jpg", "image/jpeg", small);
        var result = uploadService.upload(validFile, AttachmentType.DAMAGE_PHOTO);
        assertThat(result.url()).isNotBlank();
        assertThat(result.name()).isEqualTo("photo.jpg");
    }

    /**
     * Test 2 (Rule Logic): Call validate with empty list -> Fail for each scenario. Call with 11 items -> Fail.
     */
    @Test
    void test2_ruleLogic_emptyList_fail_elevenItems_fail() {
        assertThatThrownBy(() -> ruleService.validate(
                AttachmentTargetType.FREIGHT_BILL, AttachmentPoint.CONFIRM, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required attachment");

        assertThatThrownBy(() -> ruleService.validate(
                AttachmentTargetType.SHIPMENT_NORMAL, AttachmentPoint.SHIP_PICK_TICKET, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pick Ticket");

        assertThatThrownBy(() -> ruleService.validate(
                AttachmentTargetType.HQ_TRANSFER, AttachmentPoint.EXECUTE, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Logistics Bill");

        List<String> eleven = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k");
        assertThatThrownBy(() -> ruleService.validate(
                AttachmentTargetType.DAMAGE_RECORD, AttachmentPoint.CREATE, eleven))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Too many attachments");
    }

    /**
     * Test 3 (Integration - Freight): Confirm without url -> Fail.
     */
    @Test
    void test3_integration_freight_confirmWithoutUrl_fail() {
        FreightBillEntity bill = new FreightBillEntity();
        bill.setBillNo("FB-TEST-001");
        bill.setCarrier("Carrier");
        bill.setStatus(com.dfbs.app.modules.freightbill.FreightBillStatus.DRAFT);
        bill.setTotalAmount(java.math.BigDecimal.ZERO);
        bill.setCreatedTime(LocalDateTime.now());
        bill = freightBillRepo.save(bill);

        Long billId = bill.getId();
        assertThatThrownBy(() -> freightBillService.confirm(billId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required attachment");
        assertThatThrownBy(() -> freightBillService.confirm(billId, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required attachment");
    }

    /**
     * Test 4 (Integration - Transfer): Ship without logisticsUrl -> Fail.
     */
    @Test
    void test4_integration_transfer_shipWithoutLogisticsUrl_fail() {
        TransferOrderEntity order = new TransferOrderEntity();
        order.setSourceWarehouseId(1L);
        order.setTargetWarehouseId(2L);
        order.setSku("SKU-1");
        order.setQuantity(10);
        order.setStatus(TransferStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setOperatorId(operatorId);
        order = transferOrderRepo.save(order);
        Long transferId = order.getId();
        assertThatThrownBy(() -> transferService.shipTransfer(transferId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Logistics Bill");
        assertThatThrownBy(() -> transferService.shipTransfer(transferId, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Logistics Bill");
    }

    /**
     * Test 5 (Integration - Shipment): Ship Normal without ticketUrl -> Fail. Complete Normal without receiptUrl -> Fail.
     */
    @Test
    void test5_integration_shipment_shipNormalWithoutTicket_fail_completeWithoutReceipt_fail() {
        NormalShipmentCreateRequest createReq = new NormalShipmentCreateRequest(
                "FB-C", "销售", PackagingType.A,
                "收货", "13900000000", false, "地址", null
        );
        ShipmentEntity shipment = shipmentService.createNormal(createReq, operatorId);
        Long shipmentId = shipment.getId();
        shipmentService.accept(shipmentId, operatorId);
        shipmentService.updateMachineIds(shipmentId, List.of(
                new MachineEntryDto("Model-A", "A01", null, 1, null)));

        ShipActionRequest shipReqNoTicket = new ShipActionRequest(
                "事项", LocalDate.now(), 1, "Model-A", true,
                "提货", "13800000000", false, "提货地址",
                "收货", "13900000000", false, "地址", "Carrier",
                null   // no ticketUrl
        );
        assertThatThrownBy(() -> shipmentService.ship(shipmentId, operatorId, shipReqNoTicket))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pick Ticket");

        ShipActionRequest shipReqWithTicket = new ShipActionRequest(
                "事项", LocalDate.now(), 1, "Model-A", true,
                "提货", "13800000000", false, "提货地址",
                "收货", "13900000000", false, "地址", "Carrier",
                "https://example.com/ticket.pdf"
        );
        shipment = shipmentService.ship(shipmentId, operatorId, shipReqWithTicket);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.SHIPPED);

        assertThatThrownBy(() -> shipmentService.complete(shipmentId, operatorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required attachment");
    }

    /**
     * Test 6 (Integration - Damage): Create without photos -> Fail.
     */
    @Test
    void test6_integration_damage_createWithoutPhotos_fail() {
        ensureDamageSetup();
        DamageCreateRequest noPhotos = new DamageCreateRequest(
                shipmentIdForDamage, machineIdForDamage, LocalDateTime.now(),
                damageTypeId, treatmentId, "desc", List.of()
        );
        assertThatThrownBy(() -> damageService.create(noPhotos, operatorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required attachment");
    }

    private void ensureDamageSetup() {
        if (shipmentIdForDamage != null) return;
        NormalShipmentCreateRequest createReq = new NormalShipmentCreateRequest(
                "FB-D", "销售", PackagingType.A, "收货", "13900000000", false, "地址", null);
        ShipmentEntity s = shipmentService.createNormal(createReq, operatorId);
        shipmentService.accept(s.getId(), operatorId);
        shipmentService.updateMachineIds(s.getId(), List.of(new MachineEntryDto("M", "SN1", null, 1, null)));
        ShipActionRequest shipReq = new ShipActionRequest(
                "事项", LocalDate.now(), 1, "M", true,
                "提货", "13800000000", false, "提货地址",
                "收货", "13900000000", false, "地址", "Carrier",
                "https://example.com/ticket.pdf"
        );
        shipmentService.ship(s.getId(), operatorId, shipReq);
        shipmentIdForDamage = s.getId();
        List<ShipmentMachineEntity> machines = shipmentMachineRepo.findByShipmentIdOrderByIdAsc(s.getId());
        machineIdForDamage = machines.isEmpty() ? null : machines.get(0).getId();
        List<DamageTypeEntity> types = damageTypeRepo.findByIsEnabledTrue();
        damageTypeId = types.isEmpty() ? createDamageType() : types.get(0).getId();
        List<DamageTreatmentEntity> treatments = damageTreatmentRepo.findByIsEnabledTrue();
        treatmentId = treatments.isEmpty() ? createDamageTreatment() : treatments.get(0).getId();
    }

    private Long createDamageType() {
        DamageTypeEntity t = new DamageTypeEntity();
        t.setName("TestType");
        t.setIsEnabled(true);
        return damageTypeRepo.save(t).getId();
    }

    private Long createDamageTreatment() {
        DamageTreatmentEntity t = new DamageTreatmentEntity();
        t.setName("TestTreatment");
        t.setBehavior(com.dfbs.app.modules.damage.TreatmentBehavior.COMPENSATION);
        t.setIsEnabled(true);
        return damageTreatmentRepo.save(t).getId();
    }
}
