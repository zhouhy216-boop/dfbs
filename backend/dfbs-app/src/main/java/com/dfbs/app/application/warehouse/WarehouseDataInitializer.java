package com.dfbs.app.application.warehouse;

import com.dfbs.app.modules.warehouse.WarehouseType;
import com.dfbs.app.modules.warehouse.WhWarehouseEntity;
import com.dfbs.app.modules.warehouse.WhWarehouseRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds default warehouses on startup if none exist: Central (总部大库) and Demo Satellite (演示服务站小库).
 */
@Component
@Order(100)
public class WarehouseDataInitializer implements CommandLineRunner {

    private final WhWarehouseRepo whWarehouseRepo;

    public WarehouseDataInitializer(WhWarehouseRepo whWarehouseRepo) {
        this.whWarehouseRepo = whWarehouseRepo;
    }

    @Override
    public void run(String... args) {
        if (whWarehouseRepo.count() > 0) {
            return;
        }
        WhWarehouseEntity central = new WhWarehouseEntity();
        central.setName("总部大库");
        central.setType(WarehouseType.CENTRAL);
        central.setManagerId(null);
        central.setIsActive(true);
        whWarehouseRepo.save(central);

        WhWarehouseEntity satellite = new WhWarehouseEntity();
        satellite.setName("演示服务站小库");
        satellite.setType(WarehouseType.SATELLITE);
        satellite.setManagerId(null);
        satellite.setIsActive(true);
        whWarehouseRepo.save(satellite);
    }
}
