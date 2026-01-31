package com.dfbs.app.interfaces.quote;

import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.interfaces.quote.dto.CreateItemRequest;
import com.dfbs.app.interfaces.quote.dto.QuoteItemDto;
import com.dfbs.app.interfaces.quote.dto.UpdateItemRequest;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteItemController {

    private final QuoteItemService itemService;

    public QuoteItemController(QuoteItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/{quoteId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteItemDto addItem(@PathVariable Long quoteId, @RequestBody CreateItemRequest req) {
        var cmd = new QuoteItemService.CreateItemCommand();
        cmd.setExpenseType(req.expenseType());
        cmd.setDescription(req.description());
        cmd.setSpec(req.spec());
        cmd.setUnit(req.unit());
        cmd.setQuantity(req.quantity());
        cmd.setUnitPrice(req.unitPrice());
        cmd.setWarehouse(req.warehouse());
        cmd.setRemark(req.remark());
        cmd.setManualPriceReason(req.manualPriceReason());
        if (req.partId() != null) {
            cmd.setPartId(req.partId());
        }
        
        QuoteItemEntity created = itemService.addItem(quoteId, cmd);
        
        // Build DTO with alert message
        String alertMessage = (created.getWarehouse() == com.dfbs.app.modules.quote.enums.QuoteItemWarehouse.HEADQUARTERS)
                ? "需提醒总部发货"
                : null;
        QuoteItemService.QuoteItemDto serviceDto = QuoteItemService.QuoteItemDto.from(created, alertMessage);
        return QuoteItemDto.from(serviceDto);
    }

    @PutMapping("/items/{itemId}")
    public QuoteItemDto updateItem(@PathVariable Long itemId, @RequestBody UpdateItemRequest req) {
        var cmd = new QuoteItemService.UpdateItemCommand();
        cmd.setExpenseType(req.expenseType());
        cmd.setDescription(req.description());
        cmd.setSpec(req.spec());
        cmd.setUnit(req.unit());
        cmd.setQuantity(req.quantity());
        cmd.setUnitPrice(req.unitPrice());
        cmd.setWarehouse(req.warehouse());
        cmd.setRemark(req.remark());
        cmd.setManualPriceReason(req.manualPriceReason());
        if (req.partId() != null) {
            cmd.setPartId(req.partId());
        }
        
        QuoteItemEntity updated = itemService.updateItem(itemId, cmd);
        
        // Build DTO with alert message
        String alertMessage = (updated.getWarehouse() == com.dfbs.app.modules.quote.enums.QuoteItemWarehouse.HEADQUARTERS)
                ? "需提醒总部发货"
                : null;
        QuoteItemService.QuoteItemDto serviceDto = QuoteItemService.QuoteItemDto.from(updated, alertMessage);
        return QuoteItemDto.from(serviceDto);
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
    }

    @GetMapping("/{quoteId}/items")
    public List<QuoteItemDto> getItems(@PathVariable Long quoteId) {
        return itemService.getItems(quoteId).stream()
                .map(QuoteItemDto::from)
                .collect(Collectors.toList());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalState(IllegalStateException ex) {
        // 400 for "quote not found" / "Cannot add item" etc.
    }
}
