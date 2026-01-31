package com.dfbs.app.interfaces.inventory;

import com.dfbs.app.application.inventory.TransferService;
import com.dfbs.app.modules.inventory.TransferOrderEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfer")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferOrderEntity apply(@RequestBody TransferApplyRequest request) {
        return transferService.applyTransfer(
                request.sourceWarehouseId(), request.targetWarehouseId(),
                request.sku(), request.quantity(), request.operatorId());
    }

    @PostMapping("/ship")
    public TransferOrderEntity ship(@RequestBody TransferShipRequest request) {
        return transferService.shipTransfer(request.transferId(), request.logisticsUrl());
    }

    @PostMapping("/receive")
    public TransferOrderEntity receive(@RequestParam Long transferId) {
        return transferService.receiveTransfer(transferId);
    }

    public record TransferApplyRequest(Long sourceWarehouseId, Long targetWarehouseId, String sku, Integer quantity, Long operatorId) {}
    public record TransferShipRequest(Long transferId, String logisticsUrl) {}
}
