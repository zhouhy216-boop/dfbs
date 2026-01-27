package com.dfbs.app.interfaces.settings;

import com.dfbs.app.application.settings.WarehouseConfigService;
import com.dfbs.app.modules.settings.WarehouseConfigEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouse-config")
public class WarehouseConfigController {

    private final WarehouseConfigService service;

    public WarehouseConfigController(WarehouseConfigService service) {
        this.service = service;
    }

    @GetMapping("/user-ids")
    public WarehouseUserIdsResponse getWarehouseUserIds() {
        List<Long> userIds = service.getWarehouseUserIds();
        return new WarehouseUserIdsResponse(userIds);
    }

    @PutMapping("/user-ids")
    public WarehouseConfigEntity updateWarehouseUserIds(@RequestBody UpdateWarehouseUserIdsRequest req) {
        return service.updateWarehouseUserIds(req.userIds());
    }

    public record WarehouseUserIdsResponse(List<Long> userIds) {}

    public record UpdateWarehouseUserIdsRequest(List<Long> userIds) {}
}
