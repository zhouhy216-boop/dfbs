package com.dfbs.app.application.machine;

import com.dfbs.app.modules.contract.ContractRepo;
import com.dfbs.app.modules.machine.MachineEntity;
import com.dfbs.app.modules.machine.MachineRepo;
import com.dfbs.app.modules.product.ProductRepo;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class MachineMasterDataService {

    private final MachineRepo machineRepo;
    private final ContractRepo contractRepo;
    private final ProductRepo productRepo;

    public MachineMasterDataService(
            MachineRepo machineRepo,
            ContractRepo contractRepo,
            ProductRepo productRepo
    ) {
        this.machineRepo = machineRepo;
        this.contractRepo = contractRepo;
        this.productRepo = productRepo;
    }

    public MachineEntity create(String machineSn, String contractNo, String productCode) {
        contractRepo.findByContractNo(contractNo)
                .orElseThrow(() -> new IllegalStateException("contract not found"));

        productRepo.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalStateException("product not found"));

        MachineEntity entity = new MachineEntity();
        entity.setId(UUID.randomUUID());
        entity.setMachineSn(machineSn);
        entity.setContractNo(contractNo);
        entity.setProductCode(productCode);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());

        return machineRepo.save(entity);
    }
}
