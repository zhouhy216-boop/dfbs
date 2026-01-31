package com.dfbs.app.application.freightbill;

import com.dfbs.app.application.shipment.MachineEntryDto;
import com.dfbs.app.application.shipment.NormalShipmentCreateRequest;
import com.dfbs.app.application.shipment.ShipActionRequest;
import com.dfbs.app.application.shipment.ShipmentService;
import com.dfbs.app.modules.freightbill.FreightBillEntity;
import com.dfbs.app.modules.freightbill.FreightBillItemEntity;
import com.dfbs.app.modules.freightbill.FreightBillStatus;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import com.dfbs.app.modules.shipment.ShipmentRepo;
import com.dfbs.app.modules.shipment.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class FreightBillTest {

    private static final String CARRIER = "CarrierX";

    @Autowired
    private FreightBillService freightBillService;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ShipmentRepo shipmentRepo;

    /** Create a SHIPPED shipment with carrier set and machines (Model-A x2). */
    private long createShippedShipment(Long operatorId, String carrier) {
        NormalShipmentCreateRequest req = new NormalShipmentCreateRequest(
                "FB-CONTRACT", "销售", com.dfbs.app.modules.shipment.PackagingType.A,
                "收货", "13900000000", false, "地址", null
        );
        ShipmentEntity s = shipmentService.createNormal(req, operatorId);
        shipmentService.accept(s.getId(), operatorId);
        shipmentService.updateMachineIds(s.getId(), List.of(new MachineEntryDto("Model-A", "A01", null, 2, null)));
        ShipActionRequest shipReq = new ShipActionRequest(
                "正常发货", LocalDate.now(), 2, "Model-A", true,
                "提货", "13800000000", false, "提货地址",
                "收货", "13900000000", false, "地址", carrier,
                "https://example.com/pick-ticket.pdf"
        );
        shipmentService.ship(s.getId(), operatorId, shipReq);
        return s.getId();
    }

    /**
     * Test 1 (Cycle): Select 2 Shipments -> Create Bill -> Check Items by model -> Check Shipments locked ->
     * Update Prices -> Confirm (Fail without file) -> Confirm (Success) -> Settle.
     */
    @Test
    void test1_cycle_createBill_itemsByModel_shipmentsLocked_updatePrices_confirm_settle() {
        Long operatorId = 400L;
        long ship1 = createShippedShipment(operatorId, CARRIER);
        long ship2 = createShippedShipment(operatorId, CARRIER);

        FreightBillEntity bill = freightBillService.create(CARRIER, List.of(ship1, ship2), operatorId);
        Long billId = bill.getId();
        assertThat(bill.getStatus()).isEqualTo(FreightBillStatus.DRAFT);
        assertThat(bill.getBillNo()).startsWith("FB-");

        List<FreightBillItemEntity> items = freightBillService.getItems(billId);
        assertThat(items).isNotEmpty();
        assertThat(items).allMatch(i -> i.getBillId().equals(billId));
        assertThat(items.stream().map(FreightBillItemEntity::getShipmentId).distinct()).containsExactlyInAnyOrder(ship1, ship2);
        assertThat(items.stream().map(FreightBillItemEntity::getMachineModel).distinct()).contains("Model-A");

        ShipmentEntity s1 = shipmentRepo.findById(ship1).orElseThrow();
        ShipmentEntity s2 = shipmentRepo.findById(ship2).orElseThrow();
        assertThat(s1.getFreightBillId()).isEqualTo(billId);
        assertThat(s2.getFreightBillId()).isEqualTo(billId);

        FreightBillItemEntity firstItem = items.get(0);
        freightBillService.updateItems(billId, List.of(
                new ItemUpdateDto(firstItem.getId(), BigDecimal.valueOf(100.00), null)
        ));
        bill = freightBillService.getBill(billId);
        assertThat(bill.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(200)); // 2 qty * 100

        assertThatThrownBy(() -> freightBillService.confirm(billId, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required attachment");

        bill = freightBillService.confirm(billId, "https://example.com/freight.pdf");
        assertThat(bill.getStatus()).isEqualTo(FreightBillStatus.CONFIRMED);
        assertThat(bill.getAttachmentUrl()).isEqualTo("https://example.com/freight.pdf");

        bill = freightBillService.settle(billId);
        assertThat(bill.getStatus()).isEqualTo(FreightBillStatus.SETTLED);
    }

    /**
     * Test 2 (Remove): Create with 2 Shipments -> Remove 1 -> Verify Items deleted, Shipment unlocked, Total updated.
     */
    @Test
    void test2_remove_createWithTwo_removeOne_verifyItemsDeleted_shipmentUnlocked_totalUpdated() {
        Long operatorId = 401L;
        long ship1 = createShippedShipment(operatorId, CARRIER);
        long ship2 = createShippedShipment(operatorId, CARRIER);

        FreightBillEntity bill = freightBillService.create(CARRIER, List.of(ship1, ship2), operatorId);
        List<FreightBillItemEntity> itemsBefore = freightBillService.getItems(bill.getId());
        int countForShip2 = (int) itemsBefore.stream().filter(i -> i.getShipmentId().equals(ship2)).count();
        assertThat(countForShip2).isGreaterThan(0);

        FreightBillEntity updated = freightBillService.removeShipment(bill.getId(), ship2);
        List<FreightBillItemEntity> itemsAfter = freightBillService.getItems(bill.getId());
        assertThat(itemsAfter.stream().noneMatch(i -> i.getShipmentId().equals(ship2))).isTrue();
        assertThat(itemsAfter.size()).isEqualTo(itemsBefore.size() - countForShip2);

        ShipmentEntity s2 = shipmentRepo.findById(ship2).orElseThrow();
        assertThat(s2.getFreightBillId()).isNull();

        assertThat(updated.getTotalAmount()).isNotNull();
    }

    /**
     * Test 3 (Locking): Try to create Bill with a Shipment already in another Bill -> Fail.
     */
    @Test
    void test3_locking_createBillWithShipmentAlreadyInAnotherBill_fail() {
        Long operatorId = 402L;
        long ship1 = createShippedShipment(operatorId, CARRIER);
        long ship2 = createShippedShipment(operatorId, CARRIER);

        freightBillService.create(CARRIER, List.of(ship1), operatorId);

        assertThatThrownBy(() -> freightBillService.create(CARRIER, List.of(ship1, ship2), operatorId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已被其他运单占用");
    }

    /**
     * Test 4 (Bill Export): Create 2 bills for Carrier A, call export, verify EasyExcel generation (content type / non-empty xlsx).
     */
    @Test
    void test4_billExport_createTwoBills_exportMerged_verifyXlsx() {
        Long operatorId = 403L;
        long ship1 = createShippedShipment(operatorId, CARRIER);
        long ship2 = createShippedShipment(operatorId, CARRIER);

        FreightBillEntity bill1 = freightBillService.create(CARRIER, List.of(ship1), operatorId);
        FreightBillEntity bill2 = freightBillService.create(CARRIER, List.of(ship2), operatorId);

        FreightBillService.ExportResult result = freightBillService.exportMergedBills(List.of(bill1.getId(), bill2.getId()));
        assertThat(result.filename()).endsWith(".xlsx");
        assertThat(result.bytes()).isNotEmpty();
        // EasyExcel xlsx starts with PK (zip)
        assertThat(result.bytes().length).isGreaterThan(100);
    }
}
