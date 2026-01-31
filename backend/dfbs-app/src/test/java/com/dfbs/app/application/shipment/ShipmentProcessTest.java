package com.dfbs.app.application.shipment;

import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.dictionary.FeeTypeRepo;
import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import com.dfbs.app.modules.shipment.ShipmentRepo;
import com.dfbs.app.modules.shipment.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ShipmentProcessTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private FeeTypeRepo feeTypeRepo;

    @Autowired
    private QuoteRepo quoteRepo;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ShipmentRepo shipmentRepo;

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

    @Test
    void test1_happyPath_create_accept_ship_complete() {
        Long assigneeId = 100L;
        Long quoteId = createConfirmedQuoteWithAssignee(assigneeId);

        ShipmentCreateRequest createReq = new ShipmentCreateRequest(
                quoteId,
                "Entrust",
                LocalDate.now().plusDays(1),
                1,
                "Model X",
                true,
                "Pickup",
                "13800000000",
                true,
                "Pickup Addr",
                "Receiver",
                "13900000000",
                false,
                "Delivery Addr",
                "Remark"
        );
        ShipmentEntity shipment = shipmentService.create(createReq, assigneeId);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CREATED);

        shipment = shipmentService.accept(shipment.getId(), assigneeId);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PENDING_SHIP);
        assertThat(shipment.getAcceptedAt()).isNotNull();

        ShipActionRequest shipReq = new ShipActionRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Carrier A",
                "https://example.com/pick-ticket.pdf"
        );
        shipment = shipmentService.ship(shipment.getId(), assigneeId, shipReq);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.SHIPPED);
        assertThat(shipment.getShippedAt()).isNotNull();
        assertThat(shipment.getCarrier()).isEqualTo("Carrier A");

        shipment = shipmentService.complete(shipment.getId(), assigneeId);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.COMPLETED);
        assertThat(shipment.getCompletedAt()).isNotNull();
    }

    @Test
    void test2_validation_shipWithoutCarrier_shouldFail() {
        Long assigneeId = 101L;
        Long quoteId = createConfirmedQuoteWithAssignee(assigneeId);

        ShipmentEntity shipment = shipmentService.create(
                new ShipmentCreateRequest(
                        quoteId,
                        "Entrust",
                        LocalDate.now().plusDays(1),
                        1,
                        "Model Y",
                        true,
                        "Pickup",
                        "13800000001",
                        true,
                        "Pickup Addr",
                        "Receiver",
                        "13900000001",
                        false,
                        "Delivery Addr",
                        "Remark"
                ),
                assigneeId
        );

        shipmentService.accept(shipment.getId(), assigneeId);

        ShipActionRequest shipReqMissingCarrier = new ShipActionRequest(
                null, null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null,   // carrier missing
                null
        );

        assertThatThrownBy(() -> shipmentService.ship(shipment.getId(), assigneeId, shipReqMissingCarrier))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("承运方");
    }

    @Test
    void test3_exceptionFlow_accept_thenException() {
        Long assigneeId = 102L;
        Long quoteId = createConfirmedQuoteWithAssignee(assigneeId);

        ShipmentEntity shipment = shipmentService.create(
                new ShipmentCreateRequest(
                        quoteId,
                        "Entrust",
                        LocalDate.now().plusDays(1),
                        1,
                        "Model Z",
                        true,
                        "Pickup",
                        "13800000002",
                        true,
                        "Pickup Addr",
                        "Receiver",
                        "13900000002",
                        false,
                        "Delivery Addr",
                        "Remark"
                ),
                assigneeId
        );

        shipmentService.accept(shipment.getId(), assigneeId);

        ShipmentEntity exception = shipmentService.handleException(shipment.getId(), assigneeId, "Damaged goods");
        assertThat(exception.getStatus()).isEqualTo(ShipmentStatus.EXCEPTION);
        assertThat(exception.getExceptionReason()).isEqualTo("Damaged goods");
    }

    @Test
    void test4_list_paginationAndFilter() {
        Long assigneeId = 103L;
        Long q1 = createConfirmedQuoteWithAssignee(assigneeId);
        Long q2 = createConfirmedQuoteWithAssignee(assigneeId);

        shipmentService.create(new ShipmentCreateRequest(
                        q1, "E1", LocalDate.now().plusDays(1), 1, "M1",
                        true, "P1", "13800000003", true, "Addr1",
                        "R1", "13900000003", false, "D1", null),
                assigneeId);

        shipmentService.create(new ShipmentCreateRequest(
                        q2, "E2", LocalDate.now().plusDays(1), 2, "M2",
                        true, "P2", "13800000004", true, "Addr2",
                        "R2", "13900000004", false, "D2", null),
                assigneeId);

        ShipmentFilterRequest filter = new ShipmentFilterRequest(null, null, assigneeId, 0, 10);
        Page<ShipmentEntity> page = shipmentService.list(filter);

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent()).allMatch(s -> s.getInitiatorId().equals(assigneeId));
    }
}

