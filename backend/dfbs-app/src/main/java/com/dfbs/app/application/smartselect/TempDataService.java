package com.dfbs.app.application.smartselect;

import com.dfbs.app.application.smartselect.dto.GetOrCreateTempRequest;
import com.dfbs.app.application.smartselect.dto.GetOrCreateTempResult;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import com.dfbs.app.modules.masterdata.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Get-or-create temp records for Smart Select. Rule 3.3: existing temp returned as-is; new creates with is_temp=true.
 */
@Service
public class TempDataService {

    private final CustomerRepo customerRepo;
    private final MachineRepo machineRepo;
    private final ContractRepo contractRepo;
    private final SparePartRepo sparePartRepo;
    private final SimCardRepo simCardRepo;
    private final MachineModelRepo machineModelRepo;

    public TempDataService(CustomerRepo customerRepo, MachineRepo machineRepo, ContractRepo contractRepo,
                           SparePartRepo sparePartRepo, SimCardRepo simCardRepo, MachineModelRepo machineModelRepo) {
        this.customerRepo = customerRepo;
        this.machineRepo = machineRepo;
        this.contractRepo = contractRepo;
        this.sparePartRepo = sparePartRepo;
        this.simCardRepo = simCardRepo;
        this.machineModelRepo = machineModelRepo;
    }

    @Transactional
    public GetOrCreateTempResult getOrCreateTemp(GetOrCreateTempRequest req) {
        if (req.getEntityType() == null || req.getUniqueKey() == null || req.getUniqueKey().isBlank()) {
            throw new IllegalArgumentException("entityType and uniqueKey are required");
        }
        String key = req.getUniqueKey().trim();
        Map<String, Object> payload = req.getPayload() != null ? req.getPayload() : Map.of();

        return switch (req.getEntityType().toUpperCase()) {
            case "CUSTOMER" -> getOrCreateCustomer(key, payload);
            case "MACHINE" -> getOrCreateMachine(key, payload);
            case "PART" -> getOrCreatePart(key, payload);
            case "CONTRACT" -> getOrCreateContract(key, payload);
            case "SIM" -> getOrCreateSim(key, payload);
            case "MODEL" -> getOrCreateModel(key, payload);
            default -> throw new IllegalArgumentException("Unknown entityType: " + req.getEntityType());
        };
    }

    private GetOrCreateTempResult getOrCreateCustomer(String key, Map<String, Object> payload) {
        String name = key;
        String code = payload.containsKey("customerCode") ? String.valueOf(payload.get("customerCode")) : "TEMP-" + System.currentTimeMillis();
        CustomerEntity existing = customerRepo.findByCustomerCode(code).orElse(null);
        if (existing == null) {
            existing = customerRepo.findByNameAndDeletedAtIsNull(name).stream().findFirst().orElse(null);
        }
        if (existing != null) {
            touchLastUsed(existing);
            customerRepo.save(existing);
            return new GetOrCreateTempResult(existing.getId(), false, existing.getIsTemp(), existing.getName());
        }
        CustomerEntity e = CustomerEntity.create(code, name);
        e.setIsTemp(true);
        e = customerRepo.save(e);
        return new GetOrCreateTempResult(e.getId(), true, true, e.getName());
    }

    private GetOrCreateTempResult getOrCreateMachine(String key, Map<String, Object> payload) {
        String machineNo = key;
        Long modelId = payload.containsKey("modelId") ? longFrom(payload.get("modelId")) : null;
        MachineEntity existing = machineRepo.findByMachineNo(machineNo).orElse(null);
        if (existing != null) {
            touchLastUsed(existing);
            machineRepo.save(existing);
            return new GetOrCreateTempResult(existing.getId(), false, existing.getIsTemp(),
                    existing.getMachineNo() + " / " + existing.getSerialNo());
        }
        if (modelId == null) {
            throw new IllegalArgumentException("Machine requires modelId for new temp");
        }
        MachineEntity e = new MachineEntity();
        e.setMachineNo(machineNo);
        e.setSerialNo("TEMP-" + System.currentTimeMillis());
        e.setModelId(modelId);
        e.setStatus(MasterDataStatus.ENABLE);
        e.setIsTemp(true);
        setAudit(e);
        e = machineRepo.save(e);
        return new GetOrCreateTempResult(e.getId(), true, true, e.getMachineNo());
    }

    private GetOrCreateTempResult getOrCreatePart(String key, Map<String, Object> payload) {
        String partNo = key;
        Long modelId = payload.containsKey("modelId") ? longFrom(payload.get("modelId")) : null;
        String name = payload.containsKey("name") ? String.valueOf(payload.get("name")) : partNo;
        SparePartEntity existing = sparePartRepo.findByPartNo(partNo).orElse(null);
        if (existing != null) {
            touchLastUsed(existing);
            sparePartRepo.save(existing);
            return new GetOrCreateTempResult(existing.getId(), false, existing.getIsTemp(), existing.getPartNo() + " - " + existing.getName());
        }
        if (modelId == null || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Part requires modelId and name for new temp");
        }
        SparePartEntity e = new SparePartEntity();
        e.setPartNo(partNo);
        e.setName(name);
        e.setStatus(MasterDataStatus.ENABLE);
        e.setIsTemp(true);
        setAudit(e);
        e = sparePartRepo.save(e);
        return new GetOrCreateTempResult(e.getId(), true, true, e.getPartNo() + " - " + e.getName());
    }

    private GetOrCreateTempResult getOrCreateContract(String key, Map<String, Object> payload) {
        String contractNo = key;
        Long customerId = payload.containsKey("customerId") ? longFrom(payload.get("customerId")) : null;
        ContractEntity existing = contractRepo.findByContractNo(contractNo).orElse(null);
        if (existing != null) {
            touchLastUsed(existing);
            contractRepo.save(existing);
            return new GetOrCreateTempResult(existing.getId(), false, existing.getIsTemp(), existing.getContractNo());
        }
        ContractEntity e = new ContractEntity();
        e.setContractNo(contractNo);
        e.setCustomerId(customerId != null ? customerId : 1L);
        e.setAttachment("");
        e.setStatus(MasterDataStatus.ENABLE);
        e.setIsTemp(true);
        setAudit(e);
        e = contractRepo.save(e);
        return new GetOrCreateTempResult(e.getId(), true, true, e.getContractNo());
    }

    private GetOrCreateTempResult getOrCreateSim(String key, Map<String, Object> payload) {
        String cardNo = key;
        SimCardEntity existing = simCardRepo.findByCardNo(cardNo).orElse(null);
        if (existing != null) {
            touchLastUsed(existing);
            simCardRepo.save(existing);
            return new GetOrCreateTempResult(existing.getId(), false, existing.getIsTemp(), existing.getCardNo());
        }
        SimCardEntity e = new SimCardEntity();
        e.setCardNo(cardNo);
        e.setStatus(MasterDataStatus.ENABLE);
        e.setIsTemp(true);
        setAudit(e);
        e = simCardRepo.save(e);
        return new GetOrCreateTempResult(e.getId(), true, true, e.getCardNo());
    }

    private GetOrCreateTempResult getOrCreateModel(String key, Map<String, Object> payload) {
        String modelNo = key;
        String modelName = payload.containsKey("modelName") ? String.valueOf(payload.get("modelName")) : null;
        MachineModelEntity existing = machineModelRepo.findByModelNo(modelNo).orElse(null);
        if (existing != null) {
            touchLastUsed(existing);
            machineModelRepo.save(existing);
            return new GetOrCreateTempResult(existing.getId(), false, existing.getIsTemp(),
                    existing.getModelNo() + (existing.getModelName() != null ? " - " + existing.getModelName() : ""));
        }
        MachineModelEntity e = new MachineModelEntity();
        e.setModelNo(modelNo);
        e.setModelName(modelName);
        e.setStatus(MasterDataStatus.ENABLE);
        e.setIsTemp(true);
        setAudit(e);
        e = machineModelRepo.save(e);
        return new GetOrCreateTempResult(e.getId(), true, true, e.getModelNo());
    }

    private void touchLastUsed(CustomerEntity e) {
        e.setLastUsedAt(LocalDateTime.now());
    }

    private void touchLastUsed(MachineEntity e) {
        e.setLastUsedAt(LocalDateTime.now());
    }

    private void touchLastUsed(SparePartEntity e) {
        e.setLastUsedAt(LocalDateTime.now());
    }

    private void touchLastUsed(ContractEntity e) {
        e.setLastUsedAt(LocalDateTime.now());
    }

    private void touchLastUsed(SimCardEntity e) {
        e.setLastUsedAt(LocalDateTime.now());
    }

    private void touchLastUsed(MachineModelEntity e) {
        e.setLastUsedAt(LocalDateTime.now());
    }

    private void setAudit(BaseMasterEntity e) {
        LocalDateTime now = LocalDateTime.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
    }

    private static Long longFrom(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }
}
