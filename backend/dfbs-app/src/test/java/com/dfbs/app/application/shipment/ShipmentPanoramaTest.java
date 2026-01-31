package com.dfbs.app.application.shipment;

import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.dictionary.FeeTypeRepo;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineRepo;
import com.dfbs.app.modules.shipment.ShipmentRepo;
import com.dfbs.app.modules.shipment.ShipmentStatus;
import com.dfbs.app.modules.shipment.ShipmentType;
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
class ShipmentPanoramaTest {

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ShipmentRepo shipmentRepo;

    @Autowired
    private ShipmentMachineRepo shipmentMachineRepo;

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private QuoteRepo quoteRepo;

    @Autowired
    private FeeTypeRepo feeTypeRepo;

    private Long createConfirmedQuoteWithAssignee(Long assigneeId) {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");
        quote.setAssigneeId(assigneeId);
        quoteRepo.save(quote);

        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(1000.00));
        itemCmd.setDescription("Test Item");
        itemCmd.setUnit("次");
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quote.getId(), itemCmd);

        quoteService.confirm(quote.getId());
        return quote.getId();
    }

    /**
     * Test 1 (Normal Flow): Create Normal -> Add Machines (Seq generation) -> Ship ->
     * Complete (Fail without photo) -> Upload Photo -> Complete (Success).
     */
    @Test
    void test1_normalFlow_create_addMachines_ship_completeFailWithoutPhoto_thenCompleteWithPhoto() {
        Long operatorId = 200L;

        NormalShipmentCreateRequest createReq = new NormalShipmentCreateRequest(
                "CONTRACT-001",
                "销售员张三",
                com.dfbs.app.modules.shipment.PackagingType.A,
                "收货人李四",
                "13900001111",
                false,
                "北京市朝阳区某某路1号",
                null
        );
        ShipmentEntity shipment = shipmentService.createNormal(createReq, operatorId);
        assertThat(shipment.getType()).isEqualTo(ShipmentType.NORMAL);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(shipment.getContractNo()).isEqualTo("CONTRACT-001");

        shipment = shipmentService.accept(shipment.getId(), operatorId);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PENDING_SHIP);

        List<MachineEntryDto> machineEntries = List.of(
                new MachineEntryDto("Model-X", "SN01", null, 3, null)
        );
        List<ShipmentMachineEntity> machines = shipmentService.updateMachineIds(shipment.getId(), machineEntries);
        assertThat(machines).hasSize(3);
        assertThat(machines.get(0).getMachineNo()).isEqualTo("SN01");
        assertThat(machines.get(1).getMachineNo()).isEqualTo("SN02");
        assertThat(machines.get(2).getMachineNo()).isEqualTo("SN03");

        ShipActionRequest shipReq = new ShipActionRequest(
                "正常发货",
                LocalDate.now(),
                1,
                "Model-X",
                true,
                "提货人",
                "13800000000",
                false,
                "提货地址",
                "收货人李四",
                "13900001111",
                false,
                "北京市朝阳区某某路1号",
                "承运方A",
                "https://example.com/pick-ticket.pdf"
        );
        shipment = shipmentService.ship(shipment.getId(), operatorId, shipReq);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.SHIPPED);

        Long shipmentIdForComplete = shipment.getId();
        assertThatThrownBy(() -> shipmentService.complete(shipmentIdForComplete, operatorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required attachment");

        shipmentService.setReceiptUrl(shipment.getId(), "https://example.com/receipt.jpg");
        shipment = shipmentService.complete(shipment.getId(), operatorId);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.COMPLETED);
    }

    /**
     * Test 2 (Entrust Flow): Create Entrust -> Link Quote -> Ship -> Complete.
     */
    @Test
    void test2_entrustFlow_createLinkQuote_ship_complete() {
        Long assigneeId = 201L;
        Long quoteId = createConfirmedQuoteWithAssignee(assigneeId);

        EntrustShipmentCreateRequest createReq = new EntrustShipmentCreateRequest(
                quoteId,
                "委托运输设备",
                LocalDate.now().plusDays(1),
                2,
                "Model-Y",
                true,
                "提货人王五",
                "13800002222",
                true,
                "上海浦东提货点",
                "收货人赵六",
                "13900002222",
                false,
                "杭州某某园区",
                null
        );
        ShipmentEntity shipment = shipmentService.createEntrust(createReq, assigneeId, true);
        assertThat(shipment.getType()).isEqualTo(ShipmentType.CUSTOMER_DELEGATE);
        assertThat(shipment.getQuoteId()).isEqualTo(quoteId);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CREATED);

        shipment = shipmentService.accept(shipment.getId(), assigneeId);
        ShipActionRequest shipReq = new ShipActionRequest(
                null, null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                "承运方B",
                null
        );
        shipment = shipmentService.ship(shipment.getId(), assigneeId, shipReq);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.SHIPPED);

        shipment = shipmentService.complete(shipment.getId(), assigneeId);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.COMPLETED);
    }

    /**
     * Test 3 (Smart Parse): Input raw text -> Verify JSON output matches fields.
     */
    @Test
    void test3_smartParse_inputRawText_verifyOutputFields() {
        String rawText = "合同号: HT-2024-001\n收货人: 张三\n地址: 北京市海淀区中关村大街1号\n电话: 13912345678";
        ParsedShipmentDto parsed = shipmentService.parseText(rawText, ShipmentType.NORMAL);

        assertThat(parsed.type()).isEqualTo(ShipmentType.NORMAL);
        assertThat(parsed.contractNo()).isEqualTo("HT-2024-001");
        assertThat(parsed.receiverName()).isEqualTo("张三");
        assertThat(parsed.deliveryAddress()).isEqualTo("北京市海淀区中关村大街1号");
        assertThat(parsed.receiverPhone()).isNotNull();
        assertThat(parsed.receiverPhone()).contains("13912345678");
    }

    /**
     * Test 4 (Machine Logic): Input "SN01" count 3 -> Verify DB has SN01, SN02, SN03.
     */
    @Test
    void test4_machineLogic_startNoCount3_verifyDbHasSn01Sn02Sn03() {
        Long operatorId = 202L;
        NormalShipmentCreateRequest createReq = new NormalShipmentCreateRequest(
                "C-002", "销售", com.dfbs.app.modules.shipment.PackagingType.D,
                "收货", "13900003333", false, "地址", null
        );
        ShipmentEntity shipment = shipmentService.createNormal(createReq, operatorId);
        shipment = shipmentService.accept(shipment.getId(), operatorId);

        List<MachineEntryDto> entries = List.of(
                new MachineEntryDto("M1", "SN01", null, 3, null)
        );
        shipmentService.updateMachineIds(shipment.getId(), entries);

        List<ShipmentMachineEntity> saved = shipmentMachineRepo.findByShipmentIdOrderByIdAsc(shipment.getId());
        assertThat(saved).hasSize(3);
        assertThat(saved.get(0).getMachineNo()).isEqualTo("SN01");
        assertThat(saved.get(1).getMachineNo()).isEqualTo("SN02");
        assertThat(saved.get(2).getMachineNo()).isEqualTo("SN03");
        assertThat(saved.get(0).getModel()).isEqualTo("M1");
    }
}
