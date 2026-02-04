package com.dfbs.app.application.smartselect;

import com.dfbs.app.application.smartselect.dto.TempPoolConfirmRequest;
import com.dfbs.app.application.smartselect.dto.TempPoolItemDto;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import com.dfbs.app.modules.masterdata.*;
import com.dfbs.app.modules.workorder.WorkOrderEntity;
import com.dfbs.app.modules.workorder.WorkOrderRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * List temp records by entity type; confirm (set is_temp=false, apply finalValues); reject (MVP: no-op to preserve FK).
 */
@Service
public class ConfirmationService {

    private final CustomerRepo customerRepo;
    private final MachineRepo machineRepo;
    private final ContractRepo contractRepo;
    private final SparePartRepo sparePartRepo;
    private final SimCardRepo simCardRepo;
    private final MachineModelRepo machineModelRepo;
    private final WorkOrderRepo workOrderRepo;

    public ConfirmationService(CustomerRepo customerRepo, MachineRepo machineRepo, ContractRepo contractRepo,
                               SparePartRepo sparePartRepo, SimCardRepo simCardRepo, MachineModelRepo machineModelRepo,
                               WorkOrderRepo workOrderRepo) {
        this.customerRepo = customerRepo;
        this.machineRepo = machineRepo;
        this.contractRepo = contractRepo;
        this.sparePartRepo = sparePartRepo;
        this.simCardRepo = simCardRepo;
        this.machineModelRepo = machineModelRepo;
        this.workOrderRepo = workOrderRepo;
    }

    @Transactional(readOnly = true)
    public Map<String, List<TempPoolItemDto>> listAllTemp() {
        Map<String, List<TempPoolItemDto>> out = new LinkedHashMap<>();
        out.put("CUSTOMER", customerRepo.findAll().stream().filter(c -> Boolean.TRUE.equals(c.getIsTemp()))
                .map(c -> new TempPoolItemDto(c.getId(), "CUSTOMER", c.getCustomerCode(), c.getName()))
                .collect(Collectors.toList()));
        out.put("MACHINE", machineRepo.findAll().stream().filter(m -> Boolean.TRUE.equals(m.getIsTemp()))
                .map(m -> new TempPoolItemDto(m.getId(), "MACHINE", m.getMachineNo(), m.getMachineNo() + " / " + m.getSerialNo()))
                .collect(Collectors.toList()));
        out.put("PART", sparePartRepo.findAll().stream().filter(p -> Boolean.TRUE.equals(p.getIsTemp()))
                .map(p -> new TempPoolItemDto(p.getId(), "PART", p.getPartNo(), p.getPartNo() + " - " + p.getName()))
                .collect(Collectors.toList()));
        out.put("CONTRACT", contractRepo.findAll().stream().filter(c -> Boolean.TRUE.equals(c.getIsTemp()))
                .map(c -> new TempPoolItemDto(c.getId(), "CONTRACT", c.getContractNo(), c.getContractNo()))
                .collect(Collectors.toList()));
        out.put("SIM", simCardRepo.findAll().stream().filter(s -> Boolean.TRUE.equals(s.getIsTemp()))
                .map(s -> new TempPoolItemDto(s.getId(), "SIM", s.getCardNo(), s.getCardNo()))
                .collect(Collectors.toList()));
        out.put("MODEL", machineModelRepo.findAll().stream().filter(m -> Boolean.TRUE.equals(m.getIsTemp()))
                .map(m -> new TempPoolItemDto(m.getId(), "MODEL", m.getModelNo(), m.getModelNo() + " - " + m.getModelName()))
                .collect(Collectors.toList()));
        return out;
    }

    @Transactional
    public TempPoolItemDto confirm(TempPoolConfirmRequest req) {
        if (req.getId() == null || req.getEntityType() == null) {
            throw new IllegalArgumentException("id and entityType required");
        }
        // Branch A (Merge): targetId present -> re-link FKs to targetId, delete temp
        if (req.getTargetId() != null) {
            return mergeTempIntoExisting(req.getEntityType().toUpperCase(), req.getId(), req.getTargetId());
        }
        // Branch B (Promote): targetId null -> update temp with finalValues, auto-gen code if needed, set is_temp=false
        Map<String, Object> fv = req.getFinalValues() != null ? req.getFinalValues() : Map.of();
        return switch (req.getEntityType().toUpperCase()) {
            case "CUSTOMER" -> promoteCustomer(req.getId(), fv);
            case "MACHINE" -> promoteMachine(req.getId(), fv);
            case "PART" -> promotePart(req.getId(), fv);
            case "CONTRACT" -> promoteContract(req.getId(), fv);
            case "SIM" -> promoteSim(req.getId(), fv);
            case "MODEL" -> promoteModel(req.getId(), fv);
            default -> throw new IllegalArgumentException("Unknown entityType: " + req.getEntityType());
        };
    }

    /** Re-link all FKs pointing to tempId to targetId, then delete the temp record. */
    private TempPoolItemDto mergeTempIntoExisting(String entityType, Long tempId, Long targetId) {
        return switch (entityType) {
            case "CUSTOMER" -> mergeCustomer(tempId, targetId);
            case "MACHINE", "PART", "CONTRACT", "SIM", "MODEL" ->
                throw new UnsupportedOperationException("Merge not yet implemented for " + entityType);
            default -> throw new IllegalArgumentException("Unknown entityType: " + entityType);
        };
    }

    private TempPoolItemDto mergeCustomer(Long tempId, Long targetId) {
        CustomerEntity target = customerRepo.findById(targetId).orElseThrow(() -> new IllegalArgumentException("Target customer not found: " + targetId));
        for (WorkOrderEntity wo : workOrderRepo.findByCustomerId(tempId)) {
            wo.setCustomerId(targetId);
            wo.setCustomerName(target.getName() != null ? target.getName() : wo.getCustomerName());
            workOrderRepo.save(wo);
        }
        customerRepo.deleteById(tempId);
        return new TempPoolItemDto(targetId, "CUSTOMER", target.getCustomerCode(), target.getName());
    }

    /** Promote: update name, auto-gen code if missing/temp, set is_temp=false */
    private TempPoolItemDto promoteCustomer(Long id, Map<String, Object> fv) {
        CustomerEntity e = customerRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        if (fv.containsKey("name")) e.setName(String.valueOf(fv.get("name")));
        String code = e.getCustomerCode();
        if (code == null || code.isBlank() || code.startsWith("TEMP-")) {
            e.setCustomerCode("CUST-" + System.currentTimeMillis());
        }
        e.setIsTemp(false);
        e = customerRepo.save(e);
        return new TempPoolItemDto(e.getId(), "CUSTOMER", e.getCustomerCode(), e.getName());
    }

    private TempPoolItemDto promoteMachine(Long id, Map<String, Object> fv) {
        MachineEntity e = machineRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Machine not found: " + id));
        if (fv.containsKey("modelId")) e.setModelId(longFrom(fv.get("modelId")));
        if (fv.containsKey("customerId")) e.setCustomerId(longFrom(fv.get("customerId")));
        e.setIsTemp(false);
        e = machineRepo.save(e);
        return new TempPoolItemDto(e.getId(), "MACHINE", e.getMachineNo(), e.getMachineNo() + " / " + e.getSerialNo());
    }

    private TempPoolItemDto promotePart(Long id, Map<String, Object> fv) {
        SparePartEntity e = sparePartRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Part not found: " + id));
        if (fv.containsKey("name")) e.setName(String.valueOf(fv.get("name")));
        String partNo = e.getPartNo();
        if (partNo == null || partNo.isBlank() || partNo.startsWith("TEMP-")) {
            e.setPartNo("PART-" + Math.abs(ThreadLocalRandom.current().nextLong(100000, 999999)));
        }
        e.setIsTemp(false);
        e = sparePartRepo.save(e);
        return new TempPoolItemDto(e.getId(), "PART", e.getPartNo(), e.getPartNo() + " - " + e.getName());
    }

    private TempPoolItemDto promoteContract(Long id, Map<String, Object> fv) {
        ContractEntity e = contractRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Contract not found: " + id));
        if (fv.containsKey("contractNo")) e.setContractNo(String.valueOf(fv.get("contractNo")));
        else if (fv.containsKey("name")) e.setContractNo(String.valueOf(fv.get("name")));
        e.setIsTemp(false);
        e = contractRepo.save(e);
        return new TempPoolItemDto(e.getId(), "CONTRACT", e.getContractNo(), e.getContractNo());
    }

    private TempPoolItemDto promoteSim(Long id, Map<String, Object> fv) {
        SimCardEntity e = simCardRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("SIM not found: " + id));
        if (fv.containsKey("cardNo")) e.setCardNo(String.valueOf(fv.get("cardNo")));
        else if (fv.containsKey("name")) e.setCardNo(String.valueOf(fv.get("name")));
        e.setIsTemp(false);
        e = simCardRepo.save(e);
        return new TempPoolItemDto(e.getId(), "SIM", e.getCardNo(), e.getCardNo());
    }

    private TempPoolItemDto promoteModel(Long id, Map<String, Object> fv) {
        MachineModelEntity e = machineModelRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Model not found: " + id));
        if (fv.containsKey("modelName")) e.setModelName(String.valueOf(fv.get("modelName")));
        if (fv.containsKey("modelNo")) e.setModelNo(String.valueOf(fv.get("modelNo")));
        else if (fv.containsKey("name")) e.setModelNo(String.valueOf(fv.get("name")));
        e.setIsTemp(false);
        e = machineModelRepo.save(e);
        return new TempPoolItemDto(e.getId(), "MODEL", e.getModelNo(), e.getModelNo() + " - " + e.getModelName());
    }

    @Transactional
    public void reject(Long id, String entityType) {
        // MVP: Preserve FK integrity; admin should "Confirm" with corrected data. Reject = no-op.
    }

    private static Long longFrom(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }
}
