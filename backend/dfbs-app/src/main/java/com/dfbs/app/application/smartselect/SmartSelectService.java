package com.dfbs.app.application.smartselect;

import com.dfbs.app.application.customer.CustomerMasterDataService;
import com.dfbs.app.application.masterdata.*;
import com.dfbs.app.application.smartselect.dto.SmartSelectItemDto;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.masterdata.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unified search for Smart Select. Filter: is_temp = false (Rule 2.4). Sort: last_used_at DESC (MRU). Limit 20.
 */
@Service
public class SmartSelectService {

    private static final int SEARCH_LIMIT = 20;
    private static final Sort MRU_SORT = Sort.by(Sort.Direction.DESC, "lastUsedAt");

    private final CustomerMasterDataService customerMasterDataService;
    private final MachineMasterDataService machineMasterDataService;
    private final ContractMasterDataService contractMasterDataService;
    private final SparePartMasterDataService sparePartMasterDataService;
    private final SimCardMasterDataService simCardMasterDataService;
    private final MachineModelMasterDataService machineModelMasterDataService;

    public SmartSelectService(CustomerMasterDataService customerMasterDataService,
                              MachineMasterDataService machineMasterDataService,
                              ContractMasterDataService contractMasterDataService,
                              SparePartMasterDataService sparePartMasterDataService,
                              SimCardMasterDataService simCardMasterDataService,
                              MachineModelMasterDataService machineModelMasterDataService) {
        this.customerMasterDataService = customerMasterDataService;
        this.machineMasterDataService = machineMasterDataService;
        this.contractMasterDataService = contractMasterDataService;
        this.sparePartMasterDataService = sparePartMasterDataService;
        this.simCardMasterDataService = simCardMasterDataService;
        this.machineModelMasterDataService = machineModelMasterDataService;
    }

    @Transactional(readOnly = true)
    public List<SmartSelectItemDto> search(String keyword, String entityType) {
        if (entityType == null || entityType.isBlank()) {
            return List.of();
        }
        PageRequest page = PageRequest.of(0, SEARCH_LIMIT, MRU_SORT);
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        return switch (entityType.toUpperCase()) {
            case "CUSTOMER" -> customerMasterDataService.search(kw, page).getContent().stream()
                    .map(this::toCustomerItem).collect(Collectors.toList());
            case "MACHINE" -> machineMasterDataService.page(kw, MasterDataStatus.ENABLE, page).getContent().stream()
                    .map(this::toMachineItem).collect(Collectors.toList());
            case "CONTRACT" -> contractMasterDataService.page(kw, MasterDataStatus.ENABLE, page).getContent().stream()
                    .map(this::toContractItem).collect(Collectors.toList());
            case "PART" -> sparePartMasterDataService.page(kw, MasterDataStatus.ENABLE, page).getContent().stream()
                    .map(this::toPartItem).collect(Collectors.toList());
            case "SIM" -> simCardMasterDataService.page(kw, MasterDataStatus.ENABLE, page).getContent().stream()
                    .map(this::toSimItem).collect(Collectors.toList());
            case "MODEL" -> machineModelMasterDataService.page(kw, MasterDataStatus.ENABLE, page).getContent().stream()
                    .map(this::toModelItem).collect(Collectors.toList());
            default -> new ArrayList<>();
        };
    }

    private SmartSelectItemDto toCustomerItem(CustomerEntity e) {
        return new SmartSelectItemDto(e.getId(), e.getName(), e.getCustomerCode(), e.getIsTemp());
    }

    private SmartSelectItemDto toMachineItem(MachineEntity e) {
        return new SmartSelectItemDto(e.getId(), e.getMachineNo() + " / " + e.getSerialNo(), e.getMachineNo(), e.getIsTemp());
    }

    private SmartSelectItemDto toContractItem(ContractEntity e) {
        return new SmartSelectItemDto(e.getId(), e.getContractNo(), e.getContractNo(), e.getIsTemp());
    }

    private SmartSelectItemDto toPartItem(SparePartEntity e) {
        return new SmartSelectItemDto(e.getId(), e.getPartNo() + " - " + e.getName(), e.getPartNo(), e.getIsTemp());
    }

    private SmartSelectItemDto toSimItem(SimCardEntity e) {
        return new SmartSelectItemDto(e.getId(), e.getCardNo(), e.getCardNo(), e.getIsTemp());
    }

    private SmartSelectItemDto toModelItem(MachineModelEntity e) {
        String display = e.getModelNo() + (e.getModelName() != null ? " - " + e.getModelName() : "");
        return new SmartSelectItemDto(e.getId(), display, e.getModelNo(), e.getIsTemp());
    }
}
