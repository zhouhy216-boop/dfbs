package com.dfbs.app.application.inventory;

import com.dfbs.app.modules.inventory.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbound operations bound to Work Order or Quote.
 * WO/Quote cumulative check is mocked for MVP (always allow if stock suffices).
 */
@Service
public class OutboundService {

    private final InventoryService inventoryService;

    public OutboundService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Outbound for Work Order. Validate: cumulative out <= required (mocked). Then deductStock.
     */
    @Transactional
    public void outboundForWorkOrder(Long warehouseId, String sku, int qty,
                                      Long workOrderId, Long workOrderItemId) {
        // MVP: no WO entity check; just ensure stock exists and qty <= available
        inventoryService.deductStock(warehouseId, sku, qty,
                TransactionType.OUTBOUND_WO, workOrderItemId != null ? workOrderItemId : workOrderId, null);
    }

    /**
     * Outbound for Quote. Validate: cumulative out <= required (mocked). Then deductStock.
     */
    @Transactional
    public void outboundForQuote(Long warehouseId, String sku, int qty,
                                  Long quoteId, Long quoteItemId) {
        inventoryService.deductStock(warehouseId, sku, qty,
                TransactionType.OUTBOUND_QUOTE, quoteItemId != null ? quoteItemId : quoteId, null);
    }
}
