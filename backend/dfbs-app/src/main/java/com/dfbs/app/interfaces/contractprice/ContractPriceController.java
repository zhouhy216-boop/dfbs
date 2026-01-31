package com.dfbs.app.interfaces.contractprice;

import com.dfbs.app.application.contractprice.ContractPriceService;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.contractprice.ContractPriceHeaderEntity;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/contract-prices")
public class ContractPriceController {

    private final ContractPriceService contractPriceService;
    private final CurrentUserIdResolver userIdResolver;

    public ContractPriceController(ContractPriceService contractPriceService, CurrentUserIdResolver userIdResolver) {
        this.contractPriceService = contractPriceService;
        this.userIdResolver = userIdResolver;
    }

    private void requireFinanceOrAdmin() {
        if (!userIdResolver.isFinanceOrAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无合同价格管理权限");
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractPriceHeaderEntity create(@RequestBody CreateContractRequest req) {
        requireFinanceOrAdmin();
        var cmd = new ContractPriceService.CreateContractCommand();
        cmd.setContractName(req.contractName());
        cmd.setCustomerId(req.customerId());
        cmd.setEffectiveDate(req.effectiveDate());
        cmd.setExpirationDate(req.expirationDate());
        cmd.setPriority(req.priority());
        if (req.items() != null) {
            cmd.setItems(req.items().stream().map(i -> {
                var e = new ContractPriceService.CreateContractCommand.ItemEntry();
                e.setItemType(i.itemType());
                e.setUnitPrice(i.unitPrice());
                e.setCurrency(i.currency());
                return e;
            }).collect(Collectors.toList()));
        }
        return contractPriceService.create(cmd);
    }

    @PutMapping("/{id}")
    public ContractPriceHeaderEntity update(@PathVariable Long id, @RequestBody UpdateContractRequest req) {
        requireFinanceOrAdmin();
        var cmd = new ContractPriceService.UpdateContractCommand();
        cmd.setContractName(req.contractName());
        cmd.setEffectiveDate(req.effectiveDate());
        cmd.setExpirationDate(req.expirationDate());
        cmd.setPriority(req.priority());
        if (req.items() != null) {
            cmd.setItems(req.items().stream().map(i -> {
                var e = new ContractPriceService.UpdateContractCommand.ItemEntry();
                e.setItemType(i.itemType());
                e.setUnitPrice(i.unitPrice());
                e.setCurrency(i.currency());
                return e;
            }).collect(Collectors.toList()));
        }
        return contractPriceService.update(id, cmd);
    }

    @PostMapping("/{id}/deactivate")
    public ContractPriceHeaderEntity deactivate(@PathVariable Long id) {
        requireFinanceOrAdmin();
        return contractPriceService.deactivate(id);
    }

    public record CreateContractRequest(
            String contractName,
            Long customerId,
            LocalDate effectiveDate,
            LocalDate expirationDate,
            Integer priority,
            List<ItemRequest> items
    ) {}

    public record UpdateContractRequest(
            String contractName,
            LocalDate effectiveDate,
            LocalDate expirationDate,
            Integer priority,
            List<ItemRequest> items
    ) {}

    public record ItemRequest(QuoteExpenseType itemType, BigDecimal unitPrice, Currency currency) {}
}
