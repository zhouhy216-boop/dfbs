package com.dfbs.app.interfaces.freightbill;

import com.dfbs.app.application.freightbill.FreightBillService;
import com.dfbs.app.application.freightbill.ItemUpdateDto;
import com.dfbs.app.modules.freightbill.FreightBillEntity;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/freight-bills")
public class FreightBillController {

    private final FreightBillService freightBillService;

    public FreightBillController(FreightBillService freightBillService) {
        this.freightBillService = freightBillService;
    }

    @GetMapping("/available-shipments")
    public List<ShipmentEntity> getAvailableShipments(@RequestParam String carrier) {
        return freightBillService.getAvailableShipments(carrier);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public FreightBillEntity create(@RequestBody CreateFreightBillRequest request, @RequestParam Long operatorId) {
        return freightBillService.create(request.carrier(), request.shipmentIds(), operatorId);
    }

    /** Create freight bill by carrier and period (finds eligible shipments automatically). */
    @PostMapping("/create-by-period")
    @ResponseStatus(HttpStatus.CREATED)
    public FreightBillEntity createByPeriod(@RequestBody CreateByPeriodRequest request, @RequestParam Long operatorId) {
        return freightBillService.createBill(request.carrierId(), request.period(), operatorId);
    }

    /** Export merged bills to Excel (Summary + Details sheets). */
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportMerged(@RequestBody List<Long> billIds) {
        FreightBillService.ExportResult result = freightBillService.exportMergedBills(billIds);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        try {
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(result.filename(), StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
        headers.setContentLength(result.bytes().length);
        return ResponseEntity.ok().headers(headers).body(result.bytes());
    }

    @PutMapping("/{id}/items")
    public FreightBillEntity updateItems(@PathVariable Long id, @RequestBody List<ItemUpdateDto> updates) {
        return freightBillService.updateItems(id, updates);
    }

    @PostMapping("/{id}/remove-shipment/{shipmentId}")
    public FreightBillEntity removeShipment(@PathVariable Long id, @PathVariable Long shipmentId) {
        return freightBillService.removeShipment(id, shipmentId);
    }

    @PostMapping("/{id}/confirm")
    public FreightBillEntity confirm(@PathVariable Long id, @RequestBody ConfirmRequest request) {
        return freightBillService.confirm(id, request.attachmentUrl());
    }

    @PostMapping("/{id}/settle")
    public FreightBillEntity settle(@PathVariable Long id) {
        return freightBillService.settle(id);
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id) {
        FreightBillService.ExportResult result = freightBillService.exportDraft(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentDispositionFormData("attachment", result.filename());
        headers.setContentLength(result.bytes().length);
        return ResponseEntity.ok().headers(headers).body(result.bytes());
    }

    public record CreateFreightBillRequest(String carrier, List<Long> shipmentIds) {}
    public record CreateByPeriodRequest(Long carrierId, String period) {}
    public record ConfirmRequest(String attachmentUrl) {}
}
