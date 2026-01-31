package com.dfbs.app.application.quote;

import com.dfbs.app.application.quote.workflow.QuoteWorkflowService;
import com.dfbs.app.application.shipment.ShipmentCreateRequest;
import com.dfbs.app.application.shipment.ShipmentService;
import com.dfbs.app.application.workorder.WorkOrderService;
import com.dfbs.app.application.quote.void_.QuoteVoidService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.DownstreamType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import com.dfbs.app.modules.shipment.ShipmentRepo;
import com.dfbs.app.modules.shipment.ShipmentStatus;
import com.dfbs.app.modules.workorder.WorkOrderEntity;
import com.dfbs.app.modules.workorder.WorkOrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class QuoteDownstreamTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private QuoteRepo quoteRepo;

    @Autowired
    private com.dfbs.app.modules.quote.dictionary.FeeTypeRepo feeTypeRepo;

    @Autowired
    private QuoteWorkflowService workflowService;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private QuoteVoidService voidService;

    @Autowired
    private ShipmentRepo shipmentRepo;

    private Long createConfirmedQuoteWithAssignee(Long assigneeId) {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");
        quote.setAssigneeId(assigneeId);
        quoteRepo.save(quote);
        addItem(quote.getId());
        quoteService.confirm(quote.getId());
        return quote.getId();
    }

    private void addItem(Long quoteId) {
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(com.dfbs.app.modules.quote.enums.QuoteExpenseType.REPAIR);
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(1000.00));
        itemCmd.setDescription("Test Item");
        itemCmd.setUnit("次");
        var feeTypes = feeTypeRepo.findByIsActiveTrue();
        if (!feeTypes.isEmpty()) {
            itemCmd.setFeeTypeId(feeTypes.get(0).getId());
        }
        itemService.addItem(quoteId, itemCmd);
    }

    @Test
    void test1_shipmentHappyPath_quoteConfirmed_createShipment_success_verifyDownstream() {
        Long assigneeId = 10L;
        Long quoteId = createConfirmedQuoteWithAssignee(assigneeId);

        ShipmentCreateRequest req = new ShipmentCreateRequest(
                quoteId,
                "Entrust matter",
                LocalDate.now().plusDays(1),
                2,
                "Model X",
                true,
                "Pickup Contact",
                "13800000000",
                true,
                "Pickup Address",
                "Receiver Contact",
                "13900000000",
                false,
                "Delivery Address",
                "Remark"
        );
        ShipmentEntity shipment = shipmentService.create(req, assigneeId);

        assertThat(shipment).isNotNull();
        assertThat(shipment.getId()).isNotNull();
        assertThat(shipment.getQuoteId()).isEqualTo(quoteId);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CREATED);

        QuoteEntity quote = quoteService.findById(quoteId).orElseThrow();
        assertThat(quote.getDownstreamType()).isEqualTo(DownstreamType.SHIPMENT);
        assertThat(quote.getDownstreamId()).isEqualTo(shipment.getId());
    }

    @Test
    void test2_workOrderHappyPath_quoteConfirmed_createWorkOrder_success() {
        Long assigneeId = 20L;
        Long quoteId = createConfirmedQuoteWithAssignee(assigneeId);

        WorkOrderEntity workOrder = workOrderService.createPlaceholder(quoteId, assigneeId);

        assertThat(workOrder).isNotNull();
        assertThat(workOrder.getId()).isNotNull();
        assertThat(workOrder.getQuoteId()).isEqualTo(quoteId);
        assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.CREATED);

        QuoteEntity quote = quoteService.findById(quoteId).orElseThrow();
        assertThat(quote.getDownstreamType()).isEqualTo(DownstreamType.WORK_ORDER);
        assertThat(quote.getDownstreamId()).isEqualTo(workOrder.getId());
    }

    @Test
    void test3_mutex_createShipment_thenCreateWorkOrder_fail() {
        Long assigneeId = 30L;
        Long quoteId = createConfirmedQuoteWithAssignee(assigneeId);

        ShipmentCreateRequest req = new ShipmentCreateRequest(
                quoteId, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        shipmentService.create(req, assigneeId);

        assertThatThrownBy(() -> workOrderService.createPlaceholder(quoteId, assigneeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已发起下游单据，无法重复");
    }

    @Test
    void test4_statusCheck_quoteApprovalPending_tryCreate_fail() {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");
        quote.setAssigneeId(40L);
        quoteRepo.save(quote);
        addItem(quote.getId());
        workflowService.submit(quote.getId(), 40L);
        Long quoteId = quote.getId();

        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.APPROVAL_PENDING);

        assertThatThrownBy(() -> shipmentService.create(
                new ShipmentCreateRequest(quoteId, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
                40L
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("只能对已确认的报价单发起发货");
    }

    @Test
    void test5_permission_nonInitiator_triesCreate_fail() {
        Long assigneeId = 50L;
        Long quoteId = createConfirmedQuoteWithAssignee(assigneeId);

        assertThatThrownBy(() -> shipmentService.create(
                new ShipmentCreateRequest(quoteId, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
                999L
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("只有报价单负责人可发起发货");

        assertThatThrownBy(() -> workOrderService.createPlaceholder(quoteId, 999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("只有报价单负责人可发起工单");
    }

    @Test
    void test6_voidCascade_createShipment_voidQuote_shipmentCancelled() {
        Long assigneeId = 60L;
        Long quoteId = createConfirmedQuoteWithAssignee(assigneeId);

        ShipmentEntity shipment = shipmentService.create(
                new ShipmentCreateRequest(quoteId, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
                assigneeId
        );
        Long shipmentId = shipment.getId();

        QuoteEntity quote = quoteService.findById(quoteId).orElseThrow();
        voidService.executeVoid(quote, assigneeId);

        assertThat(quoteService.findById(quoteId).orElseThrow().getStatus()).isEqualTo(QuoteStatus.CANCELLED);
        ShipmentEntity cancelledShipment = shipmentRepo.findById(shipmentId).orElseThrow();
        assertThat(cancelledShipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
    }
}
