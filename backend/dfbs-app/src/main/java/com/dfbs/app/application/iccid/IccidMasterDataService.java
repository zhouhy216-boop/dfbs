package com.dfbs.app.application.iccid;

import com.dfbs.app.application.iccid.dto.IccidListDto;
import com.dfbs.app.modules.iccid.IccidEntity;
import com.dfbs.app.modules.iccid.IccidRepo;
import com.dfbs.app.modules.masterdata.MachineRepo;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineEntity;
import com.dfbs.app.modules.shipment.ShipmentMachineRepo;
import com.dfbs.app.modules.shipment.ShipmentRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class IccidMasterDataService {

    private final IccidRepo iccidRepo;
    private final MachineRepo machineRepo;
    private final ShipmentMachineRepo shipmentMachineRepo;
    private final ShipmentRepo shipmentRepo;

    public IccidMasterDataService(
            IccidRepo iccidRepo,
            MachineRepo machineRepo,
            ShipmentMachineRepo shipmentMachineRepo,
            ShipmentRepo shipmentRepo
    ) {
        this.iccidRepo = iccidRepo;
        this.machineRepo = machineRepo;
        this.shipmentMachineRepo = shipmentMachineRepo;
        this.shipmentRepo = shipmentRepo;
    }

    /**
     * Search ICCIDs by keyword (iccidNo), optional isBound filter. Enrich with shipment (customerName, contractNo, orgCode) when bound.
     */
    public Page<IccidListDto> searchIccids(String keyword, Boolean isBound, Pageable pageable) {
        Page<IccidEntity> page;
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        if (Boolean.TRUE.equals(isBound)) {
            page = kw == null
                    ? iccidRepo.findByMachineSnIsNotNull(pageable)
                    : iccidRepo.findByIccidNoContainingIgnoreCaseAndMachineSnIsNotNull(kw, pageable);
        } else if (Boolean.FALSE.equals(isBound)) {
            page = kw == null
                    ? iccidRepo.findByMachineSnIsNull(pageable)
                    : iccidRepo.findByIccidNoContainingIgnoreCaseAndMachineSnIsNull(kw, pageable);
        } else {
            page = kw == null
                    ? iccidRepo.findAll(pageable)
                    : iccidRepo.findByIccidNoContainingIgnoreCase(kw, pageable);
        }

        return page.map(ic -> {
            boolean bound = ic.getMachineSn() != null && !ic.getMachineSn().isBlank();
            String customerName = null;
            String contractNo = null;
            String orgCode = null;

            if (bound) {
                Optional<ShipmentMachineEntity> latest = shipmentMachineRepo.findTopByMachineNoOrderByShipmentIdDesc(ic.getMachineSn());
                if (latest.isPresent()) {
                    ShipmentEntity shipment = shipmentRepo.findById(latest.get().getShipmentId()).orElse(null);
                    if (shipment != null) {
                        customerName = shipment.getReceiverName();
                        contractNo = shipment.getContractNo();
                    }
                }
            }

            return new IccidListDto(
                    ic.getId(),
                    ic.getIccidNo(),
                    ic.getMachineSn(),
                    customerName,
                    contractNo,
                    orgCode,
                    ic.getPlan(),
                    ic.getPlatform(),
                    ic.getExpiryDate(),
                    bound
            );
        });
    }

    public IccidEntity create(String iccidNo, String machineSn) {
        if (machineSn != null) {
            machineRepo.findByMachineNo(machineSn)
                    .or(() -> machineRepo.findBySerialNo(machineSn))
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
