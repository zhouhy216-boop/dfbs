package com.dfbs.app.interfaces.shipment;

import com.dfbs.app.application.carrier.CarrierService;
import com.dfbs.app.application.perm.PermEnforcementService;
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
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Shipment", description = "Shipment (发货) create, parse, export")
@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    /** Permission keys for shipment list/detail/workflow and step actions (follows shipment.shipments:ACTION). */
    public static final String PERM_VIEW = "shipment.shipments:VIEW";
    public static final String PERM_ACCEPT = "shipment.shipments:ACCEPT";
    public static final String PERM_PREPARE = "shipment.shipments:PREPARE";
    public static final String PERM_SHIP = "shipment.shipments:SHIP";
    public static final String PERM_TRACKING = "shipment.shipments:TRACKING";
    public static final String PERM_COMPLETE = "shipment.shipments:COMPLETE";
    public static final String PERM_EXCEPTION = "shipment.shipments:EXCEPTION";
    public static final String PERM_CANCEL = "shipment.shipments:CANCEL";
    public static final String PERM_CLOSE = "shipment.shipments:CLOSE";

    private final ShipmentService shipmentService;
    private final ShipmentTypeService shipmentTypeService;
    private final CarrierService carrierService;
    private final PermEnforcementService permEnforcement;

    public ShipmentController(ShipmentService shipmentService,
                               ShipmentTypeService shipmentTypeService,
                               CarrierService carrierService,
                               PermEnforcementService permEnforcement) {
        this.shipmentService = shipmentService;
        this.shipmentTypeService = shipmentTypeService;
        this.carrierService = carrierService;
        this.permEnforcement = permEnforcement;
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
        permEnforcement.requirePermission(PERM_VIEW);
        ShipmentFilterRequest filter = new ShipmentFilterRequest(status, quoteId, initiatorId, page, size);
        return shipmentService.listWithCustomerName(filter);
    }

    @GetMapping("/{id}")
    public ShipmentEntity getDetail(@PathVariable Long id) {
        permEnforcement.requirePermission(PERM_VIEW);
        return shipmentService.getDetail(id);
    }

    @Operation(summary = "Workflow state and next actions", description = "Current step (from status) + available actions aligned with transition require() rules; actions filtered by user permission")
    @GetMapping("/{id}/workflow")
    public ShipmentWorkflowDto getWorkflow(@PathVariable Long id) {
        permEnforcement.requirePermission(PERM_VIEW);
        ShipmentWorkflowDto dto = shipmentService.getWorkflow(id);
        Set<String> keys = permEnforcement.getEffectiveKeysForCurrentUser();
        List<WorkflowActionDto> filtered = dto.actions().stream()
                .filter(a -> hasPermissionForAction(a.actionCode(), keys))
                .collect(Collectors.toList());
        return new ShipmentWorkflowDto(dto.shipmentId(), dto.status(), dto.stepCode(), dto.stepLabelCn(), filtered);
    }

    private static boolean hasPermissionForAction(String actionCode, Set<String> effectiveKeys) {
        if (actionCode == null || actionCode.isBlank()) return false;
        String key = switch (actionCode.trim()) {
            case "ACCEPT" -> PERM_ACCEPT;
            case "PREPARE" -> PERM_PREPARE;
            case "SHIP" -> PERM_SHIP;
            case "TRACKING" -> PERM_TRACKING;
            case "COMPLETE" -> PERM_COMPLETE;
            case "EXCEPTION" -> PERM_EXCEPTION;
            case "CANCEL" -> PERM_CANCEL;
            case "CLOSE" -> PERM_CLOSE;
            default -> null;
        };
        return key != null && effectiveKeys.contains(key);
    }

    @Operation(summary = "List machines for shipment", description = "For after-sales machine selector")
    @GetMapping("/{id}/machines")
    public List<com.dfbs.app.modules.shipment.ShipmentMachineEntity> getMachines(@PathVariable Long id) {
        return shipmentService.getMachines(id);
    }

    @Operation(summary = "List exception records for shipment", description = "Optional machineId filter; requires VIEW")
    @GetMapping("/{id}/exceptions")
    public List<ExceptionRecordDto> getExceptions(@PathVariable Long id,
                                                  @RequestParam(required = false) Long machineId) {
        permEnforcement.requirePermission(PERM_VIEW);
        return shipmentService.listExceptionRecords(id, machineId);
    }

    @PostMapping("/{id}/accept")
    public ShipmentEntity accept(@PathVariable Long id,
                                 @RequestParam Long operatorId,
                                 @RequestBody(required = false) AcceptSupplementRequest supplement) {
        permEnforcement.requirePermission(PERM_ACCEPT);
        return shipmentService.accept(id, operatorId, supplement);
    }

    @PostMapping("/{id}/prepare")
    public ShipmentEntity prepare(@PathVariable Long id,
                                  @RequestParam Long operatorId,
                                  @RequestBody(required = false) PrepareRequest req) {
        permEnforcement.requirePermission(PERM_PREPARE);
        return shipmentService.prepare(id, operatorId, req);
    }

    @PostMapping("/{id}/ship")
    public ShipmentEntity ship(@PathVariable Long id,
                               @RequestParam Long operatorId,
                               @RequestBody ShipActionRequest req) {
        permEnforcement.requirePermission(PERM_SHIP);
        return shipmentService.ship(id, operatorId, req);
    }

    @PostMapping("/{id}/complete")
    public ShipmentEntity complete(@PathVariable Long id, @RequestParam Long operatorId) {
        permEnforcement.requirePermission(PERM_COMPLETE);
        return shipmentService.complete(id, operatorId);
    }

    @PostMapping("/{id}/tracking")
    public ShipmentEntity tracking(@PathVariable Long id,
                                   @RequestParam Long operatorId,
                                   @RequestBody(required = false) TrackingRequest req) {
        permEnforcement.requirePermission(PERM_TRACKING);
        return shipmentService.tracking(id, operatorId, req);
    }

    @PostMapping("/{id}/exception")
    public ShipmentEntity exception(@PathVariable Long id,
                                    @RequestParam Long operatorId,
                                    @RequestBody ExceptionMarkRequest req) {
        permEnforcement.requirePermission(PERM_EXCEPTION);
        return shipmentService.handleException(id, operatorId, req);
    }

    @PostMapping("/{id}/cancel")
    public ShipmentEntity cancel(@PathVariable Long id,
                                 @RequestParam(required = false) Long operatorId,
                                 @RequestBody ReasonRequest req) {
        permEnforcement.requirePermission(PERM_CANCEL);
        return shipmentService.cancel(id, req.reason());
    }

    @PostMapping("/{id}/close")
    public ShipmentEntity close(@PathVariable Long id, @RequestParam Long operatorId) {
        permEnforcement.requirePermission(PERM_CLOSE);
        return shipmentService.close(id, operatorId);
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
