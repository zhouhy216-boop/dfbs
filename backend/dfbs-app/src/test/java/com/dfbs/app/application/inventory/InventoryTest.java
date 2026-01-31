package com.dfbs.app.application.inventory;

import com.dfbs.app.modules.inventory.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class InventoryTest {

    private static final String SKU = "MODEL-X";

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private SpecialOutboundService specialOutboundService;

    @Autowired
    private WarehouseSelectionService warehouseSelectionService;

    @Autowired
    private WarehouseRepo warehouseRepo;

    /** Assume seed: 1=HQ, 2=Branch A, 3=Branch B. */
    private long hqId() {
        return warehouseRepo.findByType(WarehouseType.HQ).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No HQ warehouse")).getId();
    }

    private long branchId(int index) {
        List<WarehouseEntity> branches = warehouseRepo.findByType(WarehouseType.BRANCH);
        if (index >= branches.size()) throw new IllegalStateException("Not enough branch warehouses");
        return branches.get(index).getId();
    }

    /**
     * Test 1 (Atomic): Add -> Deduct -> Check Log. Deduct > Balance -> Expect Exception.
     */
    @Test
    void test1_atomic_add_deduct_checkLog_deductExcess_expectException() {
        long whId = hqId();
        Long operatorId = 500L;

        inventoryService.inbound(whId, SKU, 100, "采购入库", operatorId);
        assertThat(inventoryService.getQuantity(whId, SKU)).isEqualTo(100);

        inventoryService.deductStock(whId, SKU, 30, TransactionType.OUTBOUND_WO, null, operatorId);
        assertThat(inventoryService.getQuantity(whId, SKU)).isEqualTo(70);

        List<InventoryLogEntity> logs = inventoryService.getLogs(whId, SKU, 10);
        assertThat(logs).hasSizeGreaterThanOrEqualTo(2);
        assertThat(logs.get(0).getChangeAmount()).isEqualTo(-30);
        assertThat(logs.get(0).getAfterQuantity()).isEqualTo(70);
        assertThat(logs.get(1).getChangeAmount()).isEqualTo(100);
        assertThat(logs.get(1).getAfterQuantity()).isEqualTo(100);

        assertThatThrownBy(() -> inventoryService.deductStock(whId, SKU, 100, TransactionType.OUTBOUND_QUOTE, null, operatorId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("库存不足");
    }

    /**
     * Test 2 (Transfer): Apply -> Ship (Source -10) -> Receive (Target +10).
     */
    @Test
    void test2_transfer_apply_ship_receive() {
        long sourceId = hqId();
        long targetId = branchId(0);
        Long operatorId = 501L;

        inventoryService.inbound(sourceId, SKU, 50, "初始", operatorId);
        assertThat(inventoryService.getQuantity(sourceId, SKU)).isEqualTo(50);
        assertThat(inventoryService.getQuantity(targetId, SKU)).isEqualTo(0);

        TransferOrderEntity order = transferService.applyTransfer(sourceId, targetId, SKU, 10, operatorId);
        assertThat(order.getStatus()).isEqualTo(TransferStatus.PENDING);

        transferService.shipTransfer(order.getId(), "https://example.com/logistics.pdf");
        order = transferService.getTransfer(order.getId());
        assertThat(order.getStatus()).isEqualTo(TransferStatus.IN_TRANSIT);
        assertThat(inventoryService.getQuantity(sourceId, SKU)).isEqualTo(40);
        assertThat(inventoryService.getQuantity(targetId, SKU)).isEqualTo(0);

        transferService.receiveTransfer(order.getId());
        order = transferService.getTransfer(order.getId());
        assertThat(order.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(inventoryService.getQuantity(sourceId, SKU)).isEqualTo(40);
        assertThat(inventoryService.getQuantity(targetId, SKU)).isEqualTo(10);
    }

    /**
     * Test 3 (Special): Apply -> Deduct (Fail, not approved) -> Approve -> Confirm (Source -10).
     */
    @Test
    void test3_special_apply_deductFail_approve_confirm() {
        long whId = branchId(0);
        Long operatorId = 502L;

        inventoryService.inbound(whId, SKU, 20, "初始", operatorId);
        SpecialOutboundRequestEntity req = specialOutboundService.applySpecial(whId, SKU, 10,
                SpecialOutboundType.SCRAP, "报废", operatorId);
        assertThat(req.getStatus()).isEqualTo(SpecialOutboundStatus.PENDING_APPROVAL);

        Long requestIdForExecute = req.getId();
        assertThatThrownBy(() -> specialOutboundService.executeSpecial(requestIdForExecute))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已批准");

        specialOutboundService.approveSpecial(req.getId(), true, "同意报废", 1L);
        req = specialOutboundService.getRequest(req.getId());
        assertThat(req.getStatus()).isEqualTo(SpecialOutboundStatus.APPROVED);

        specialOutboundService.executeSpecial(req.getId());
        req = specialOutboundService.getRequest(req.getId());
        assertThat(req.getStatus()).isEqualTo(SpecialOutboundStatus.COMPLETED);
        assertThat(inventoryService.getQuantity(whId, SKU)).isEqualTo(10);
    }

    /**
     * Test 4 (Cross-Warehouse): Local has 10, ask HQ for 5 -> Expect "Reason Required" flag.
     */
    @Test
    void test4_crossWarehouse_localHas10_askHqFor5_expectRequiresReason() {
        long hqId = hqId();
        long branchId = branchId(0);
        Long operatorId = 503L;

        inventoryService.inbound(branchId, SKU, 10, "办事处库存", operatorId);
        assertThat(inventoryService.getQuantity(branchId, SKU)).isEqualTo(10);

        WarehouseSelectionResult result = warehouseSelectionService.validateWarehouseSelection(
                branchId, hqId, SKU, 5);
        assertThat(result).isEqualTo(WarehouseSelectionResult.REQUIRES_REASON);

        // If branch has 0 for sku, select HQ -> OK
        String otherSku = "OTHER-SKU";
        WarehouseSelectionResult okResult = warehouseSelectionService.validateWarehouseSelection(
                branchId, hqId, otherSku, 5);
        assertThat(okResult).isEqualTo(WarehouseSelectionResult.OK);
    }
}
