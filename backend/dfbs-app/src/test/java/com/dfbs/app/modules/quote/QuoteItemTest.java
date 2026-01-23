package com.dfbs.app.modules.quote;

import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteItemWarehouse;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class QuoteItemTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Test
    void testCrudOperations() {
        // Create Quote (Draft)
        var createCmd = new QuoteService.CreateQuoteCommand();
        createCmd.setSourceType(QuoteSourceType.MANUAL);
        createCmd.setCustomerId(1L);
        var quote = quoteService.createDraft(createCmd, "test-user");
        Long quoteId = quote.getId();

        // Add Item (Type=PARTS, Qty=2, Price=10.00)
        var addCmd = new QuoteItemService.CreateItemCommand();
        addCmd.setExpenseType(QuoteExpenseType.PARTS);
        addCmd.setQuantity(2);
        addCmd.setUnitPrice(new BigDecimal("10.00"));
        addCmd.setDescription("Test Parts");

        var item = itemService.addItem(quoteId, addCmd);

        // Verify Amount=20.00, Unit="个" (auto-filled from expenseType)
        assertThat(item.getAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(item.getUnit()).isEqualTo("个");
        assertThat(item.getExpenseType()).isEqualTo(QuoteExpenseType.PARTS);
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getLineOrder()).isEqualTo(1);

        Long itemId = item.getId();

        // Update Item
        var updateCmd = new QuoteItemService.UpdateItemCommand();
        updateCmd.setQuantity(3);
        updateCmd.setUnitPrice(new BigDecimal("15.00"));

        var updated = itemService.updateItem(itemId, updateCmd);

        // Verify Amount automatically updated = 3 * 15.00 = 45.00
        assertThat(updated.getAmount()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(updated.getQuantity()).isEqualTo(3);
        assertThat(updated.getUnitPrice()).isEqualByComparingTo(new BigDecimal("15.00"));

        // Delete Item
        itemService.deleteItem(itemId);

        // Verify item is deleted
        List<QuoteItemService.QuoteItemDto> items = itemService.getItems(quoteId);
        assertThat(items).isEmpty();
    }

    @Test
    void testStatusProtection() {
        // Create Quote and confirm it
        var createCmd = new QuoteService.CreateQuoteCommand();
        createCmd.setSourceType(QuoteSourceType.MANUAL);
        createCmd.setCustomerId(1L);
        var quote = quoteService.createDraft(createCmd, "test-user");
        Long quoteId = quote.getId();

        quoteService.confirm(quoteId);

        // Try to add item -> should throw exception
        var addCmd = new QuoteItemService.CreateItemCommand();
        addCmd.setExpenseType(QuoteExpenseType.PARTS);
        addCmd.setQuantity(1);
        addCmd.setUnitPrice(new BigDecimal("10.00"));

        assertThatThrownBy(() -> itemService.addItem(quoteId, addCmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot add item");

        // Create a new draft quote, add item, then confirm
        var quote2 = quoteService.createDraft(createCmd, "test-user");
        Long quoteId2 = quote2.getId();

        var addCmd2 = new QuoteItemService.CreateItemCommand();
        addCmd2.setExpenseType(QuoteExpenseType.REPAIR);
        addCmd2.setQuantity(1);
        addCmd2.setUnitPrice(new BigDecimal("20.00"));
        var item2 = itemService.addItem(quoteId2, addCmd2);
        Long itemId2 = item2.getId();

        quoteService.confirm(quoteId2);

        // Try to update item -> should throw exception
        var updateCmd = new QuoteItemService.UpdateItemCommand();
        updateCmd.setQuantity(2);

        assertThatThrownBy(() -> itemService.updateItem(itemId2, updateCmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update item");

        // Try to delete item -> should throw exception
        assertThatThrownBy(() -> itemService.deleteItem(itemId2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete item");
    }

    @Test
    void testWarehouseAlert() {
        // Create Quote (Draft)
        var createCmd = new QuoteService.CreateQuoteCommand();
        createCmd.setSourceType(QuoteSourceType.MANUAL);
        createCmd.setCustomerId(1L);
        var quote = quoteService.createDraft(createCmd, "test-user");
        Long quoteId = quote.getId();

        // Add Item (Warehouse=HEADQUARTERS)
        var addCmd = new QuoteItemService.CreateItemCommand();
        addCmd.setExpenseType(QuoteExpenseType.PARTS);
        addCmd.setQuantity(1);
        addCmd.setUnitPrice(new BigDecimal("10.00"));
        addCmd.setWarehouse(QuoteItemWarehouse.HEADQUARTERS);
        addCmd.setDescription("Parts from HQ");

        itemService.addItem(quoteId, addCmd);

        // Query List -> verify DTO has alertMessage
        List<QuoteItemService.QuoteItemDto> items = itemService.getItems(quoteId);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getAlertMessage()).isEqualTo("需提醒总部发货");
        assertThat(items.get(0).getWarehouse()).isEqualTo(QuoteItemWarehouse.HEADQUARTERS);

        // Add another item with LOCAL warehouse
        var addCmd2 = new QuoteItemService.CreateItemCommand();
        addCmd2.setExpenseType(QuoteExpenseType.REPAIR);
        addCmd2.setQuantity(1);
        addCmd2.setUnitPrice(new BigDecimal("20.00"));
        addCmd2.setWarehouse(QuoteItemWarehouse.LOCAL);
        addCmd2.setDescription("Local repair");

        itemService.addItem(quoteId, addCmd2);

        // Query again -> verify first has alert, second doesn't
        List<QuoteItemService.QuoteItemDto> items2 = itemService.getItems(quoteId);
        assertThat(items2).hasSize(2);
        assertThat(items2.get(0).getAlertMessage()).isEqualTo("需提醒总部发货");
        assertThat(items2.get(1).getAlertMessage()).isNull();
    }
}
