package com.dfbs.app.application.iccid;

import com.dfbs.app.modules.iccid.IccidEntity;
import com.dfbs.app.modules.iccid.IccidRepo;
import com.dfbs.app.modules.machine.MachineRepo;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class IccidMasterDataService {

    private final IccidRepo iccidRepo;
    private final MachineRepo machineRepo;

    public IccidMasterDataService(
            IccidRepo iccidRepo,
            MachineRepo machineRepo
    ) {
        this.iccidRepo = iccidRepo;
        this.machineRepo = machineRepo;
    }

    public IccidEntity create(String iccidNo, String machineSn) {
        if (machineSn != null) {
            machineRepo.findByMachineSn(machineSn)
                    .orElseThrow(() -> new IllegalStateException("machine not found"));
        }

        IccidEntity entity = new IccidEntity();
        entity.setId(UUID.randomUUID());
        entity.setIccidNo(iccidNo);
        entity.setMachineSn(machineSn);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());

        return iccidRepo.save(entity);
    }
}
