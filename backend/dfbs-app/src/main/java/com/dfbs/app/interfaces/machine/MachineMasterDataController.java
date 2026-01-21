package com.dfbs.app.interfaces.machine;

import com.dfbs.app.application.machine.MachineMasterDataService;
import com.dfbs.app.modules.machine.MachineEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/masterdata/machines")
public class MachineMasterDataController {

    private final MachineMasterDataService service;

    public MachineMasterDataController(MachineMasterDataService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MachineEntity create(@RequestBody Map<String, String> body) {
        return service.create(
                body.get("machineSn"),
                body.get("contractNo"),
                body.get("productCode")
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound(IllegalStateException ex) {
        // 404 only
    }
}
