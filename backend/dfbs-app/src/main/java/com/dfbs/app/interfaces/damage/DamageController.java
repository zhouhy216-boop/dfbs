package com.dfbs.app.interfaces.damage;

import com.dfbs.app.application.damage.*;
import com.dfbs.app.modules.damage.DamageRecordEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DamageController {

    private final DamageService damageService;

    public DamageController(DamageService damageService) {
        this.damageService = damageService;
    }

    @PostMapping("/api/v1/damages")
    @ResponseStatus(HttpStatus.CREATED)
    public DamageRecordEntity create(@RequestBody DamageCreateRequest request, @RequestParam Long operatorId) {
        return damageService.create(request, operatorId);
    }

    @GetMapping("/api/v1/shipments/{shipmentId}/damages")
    public List<DamageRecordDto> listByShipment(@PathVariable Long shipmentId) {
        return damageService.listByShipment(shipmentId);
    }

    @PutMapping("/api/v1/damages/{id}/repair-stage")
    public DamageRecordEntity updateRepairStage(@PathVariable Long id,
                                                 @RequestBody RepairStageUpdateRequest request,
                                                 @RequestParam Long operatorId) {
        return damageService.updateRepairStage(id, request, operatorId);
    }

    @PutMapping("/api/v1/damages/{id}/compensation")
    public DamageRecordEntity confirmCompensation(@PathVariable Long id,
                                                  @RequestBody CompensationConfirmRequest request,
                                                  @RequestParam Long operatorId) {
        return damageService.confirmCompensation(id, request, operatorId);
    }

    @GetMapping("/api/v1/damages/config/types")
    public List<com.dfbs.app.modules.damage.config.DamageTypeEntity> listTypes() {
        return damageService.listActiveTypes();
    }

    @GetMapping("/api/v1/damages/config/treatments")
    public List<com.dfbs.app.modules.damage.config.DamageTreatmentEntity> listTreatments() {
        return damageService.listActiveTreatments();
    }
}
