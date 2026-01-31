package com.dfbs.app.interfaces.shipment;

import com.dfbs.app.application.carrier.CarrierService;
import com.dfbs.app.application.shipment.*;
import com.dfbs.app.modules.carrier.CarrierEntity;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import com.dfbs.app.modules.shipment.ShipmentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Shipment", description = "Shipment (发货) create, parse, export")
@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentTypeService shipmentTypeService;
    private final CarrierService carrierService;

    public ShipmentController(ShipmentService shipmentService,
                               ShipmentTypeService shipmentTypeService,
                               CarrierService carrierService) {
        this.shipmentService = shipmentService;
        this.shipmentTypeService = shipmentTypeService;
        this.carrierService = carrierService;
    }

    @Operation(summary = "Create standalone shipment", description = "Creates a shipment from customerId and type (STANDARD/EXPRESS)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentEntity create(@RequestBody SimpleShipmentCreateRequest request, @RequestParam(defaultValue = "1") Long operatorId) {
        return shipmentService.createStandalone(request.customerId(), request.shipmentType(), operatorId);
    }

    @PostMapping("/create-normal")
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentEntity createNormal(@RequestBody NormalShipmentCreateRequest request, @RequestParam Long operatorId) {
        return shipmentService.createNormal(request, operatorId);
    }

    @PostMapping("/create-entrust")
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentEntity createEntrust(@RequestBody EntrustShipmentCreateRequest request,
                                        @RequestParam boolean isCustomerEntrust,
                                        @RequestParam Long operatorId) {
        return shipmentService.createEntrust(request, operatorId, isCustomerEntrust);
    }

    @PostMapping("/{id}/machines")
    public List<com.dfbs.app.modules.shipment.ShipmentMachineEntity> updateMachines(
            @PathVariable Long id, @RequestBody List<MachineEntryDto> entries) {
        return shipmentService.updateMachineIds(id, entries);
    }

    @Operation(summary = "Parse shipment text", description = "Parses pasted text into shipment fields")
    @PostMapping("/parse-text")
    public ParsedShipmentDto parseText(@RequestBody ParseTextRequest request) {
        return shipmentService.parseText(request.rawText(), request.type());
    }

    /** Infer shipment type and recommend carrier from pasted text. Returns type (or null) and carrier (or null). */
    @PostMapping("/infer-type")
    public InferTypeResponse inferType(@RequestBody InferTypeRequest request) {
        String text = request != null && request.text() != null ? request.text() : "";
        ShipmentType type = shipmentTypeService.inferType(text);
        CarrierEntity carrier = carrierService.recommendCarrier(text);
        CarrierRecommendDto carrierDto = carrier != null
                ? new CarrierRecommendDto(carrier.getId(), carrier.getName())
                : null;
        return new InferTypeResponse(type, carrierDto);
    }

    @GetMapping("/{id}/export-ticket")
    public ResponseEntity<byte[]> exportTicket(@PathVariable Long id) {
        ShipmentService.ExportResult result = shipmentService.exportTicket(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentDispositionFormData("attachment", result.filename());
        headers.setContentLength(result.bytes().length);
        return ResponseEntity.ok().headers(headers).body(result.bytes());
    }

    @GetMapping("/{id}/export-receipt")
    public ResponseEntity<byte[]> exportReceipt(@PathVariable Long id) {
        ShipmentService.ExportResult result = shipmentService.exportReceipt(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentDispositionFormData("attachment", result.filename());
        headers.setContentLength(result.bytes().length);
        return ResponseEntity.ok().headers(headers).body(result.bytes());
    }

    @PostMapping("/create-from-quote")
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentEntity createFromQuote(@RequestBody CreateFromQuoteRequest req) {
        return shipmentService.create(
                new ShipmentCreateRequest(
                        req.quoteId(),
                        req.entrustMatter(),
                        req.shipDate(),
                        req.quantity(),
                        req.model(),
                        req.needPackaging(),
                        req.pickupContact(),
                        req.pickupPhone(),
                        req.needLoading(),
                        req.pickupAddress(),
                        req.receiverContact(),
                        req.receiverPhone(),
                        req.needUnloading(),
                        req.deliveryAddress(),
                        req.remark()
                ),
                req.initiatorId()
        );
    }

    @Operation(summary = "List shipments", description = "Paginated list with shipmentNo, customerName, status, createdAt")
    @GetMapping
    public Page<ShipmentListDto> list(
            @RequestParam(required = false) com.dfbs.app.modules.shipment.ShipmentStatus status,
            @RequestParam(required = false) Long quoteId,
            @RequestParam(required = false) Long initiatorId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        ShipmentFilterRequest filter = new ShipmentFilterRequest(status, quoteId, initiatorId, page, size);
        return shipmentService.listWithCustomerName(filter);
    }

    @GetMapping("/{id}")
    public ShipmentEntity getDetail(@PathVariable Long id) {
        return shipmentService.getDetail(id);
    }

    @Operation(summary = "List machines for shipment", description = "For after-sales machine selector")
    @GetMapping("/{id}/machines")
    public List<com.dfbs.app.modules.shipment.ShipmentMachineEntity> getMachines(@PathVariable Long id) {
        return shipmentService.getMachines(id);
    }

    @PostMapping("/{id}/accept")
    public ShipmentEntity accept(@PathVariable Long id, @RequestParam Long operatorId) {
        return shipmentService.accept(id, operatorId);
    }

    @PostMapping("/{id}/ship")
    public ShipmentEntity ship(@PathVariable Long id,
                               @RequestParam Long operatorId,
                               @RequestBody ShipActionRequest req) {
        return shipmentService.ship(id, operatorId, req);
    }

    @PostMapping("/{id}/complete")
    public ShipmentEntity complete(@PathVariable Long id, @RequestParam Long operatorId) {
        return shipmentService.complete(id, operatorId);
    }

    @PostMapping("/{id}/exception")
    public ShipmentEntity exception(@PathVariable Long id,
                                    @RequestParam Long operatorId,
                                    @RequestBody ReasonRequest req) {
        return shipmentService.handleException(id, operatorId, req.reason());
    }

    @PostMapping("/{id}/cancel")
    public ShipmentEntity cancel(@PathVariable Long id,
                                 @RequestParam(required = false) Long operatorId,
                                 @RequestBody ReasonRequest req) {
        // operatorId is accepted for symmetry with other endpoints; current cancel logic does not use it.
        return shipmentService.cancel(id, req.reason());
    }

    public record CreateFromQuoteRequest(
            Long quoteId,
            Long initiatorId,
            String entrustMatter,
            java.time.LocalDate shipDate,
            Integer quantity,
            String model,
            Boolean needPackaging,
            String pickupContact,
            String pickupPhone,
            Boolean needLoading,
            String pickupAddress,
            String receiverContact,
            String receiverPhone,
            Boolean needUnloading,
            String deliveryAddress,
            String remark
    ) {}

    public record ParseTextRequest(String rawText, ShipmentType type) {}

    public record InferTypeRequest(String text) {}
    public record InferTypeResponse(ShipmentType type, CarrierRecommendDto carrier) {}
    public record CarrierRecommendDto(Long id, String name) {}
}
