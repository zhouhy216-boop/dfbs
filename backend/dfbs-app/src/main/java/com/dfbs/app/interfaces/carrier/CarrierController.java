package com.dfbs.app.interfaces.carrier;

import com.dfbs.app.application.carrier.CarrierService;
import com.dfbs.app.modules.carrier.CarrierEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carriers")
public class CarrierController {

    private final CarrierService carrierService;

    public CarrierController(CarrierService carrierService) {
        this.carrierService = carrierService;
    }

    @GetMapping
    public List<CarrierEntity> list(@RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        return carrierService.list(activeOnly);
    }

    @GetMapping("/recommend")
    public CarrierEntity recommend(@RequestParam String address) {
        return carrierService.recommendCarrier(address);
    }

    @GetMapping("/{id}")
    public CarrierEntity getById(@PathVariable Long id) {
        return carrierService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarrierEntity create(@RequestBody Map<String, Object> body) {
        String name = body.get("name") != null ? body.get("name").toString() : null;
        Boolean isActive = body.get("isActive") instanceof Boolean b ? b : null;
        return carrierService.create(name, isActive);
    }

    @PutMapping("/{id}")
    public CarrierEntity update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = body.get("name") != null ? body.get("name").toString() : null;
        Boolean isActive = body.get("isActive") instanceof Boolean b ? b : null;
        return carrierService.update(id, name, isActive);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        carrierService.delete(id);
    }
}
