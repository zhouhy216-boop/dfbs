package com.dfbs.app.application.orgstructure;

import com.dfbs.app.application.orgstructure.dto.PositionCatalogItemDto;
import com.dfbs.app.modules.orgstructure.OrgPositionCatalogEntity;
import com.dfbs.app.modules.orgstructure.OrgPositionCatalogRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrgPositionCatalogService {

    private final OrgPositionCatalogRepo catalogRepo;

    public OrgPositionCatalogService(OrgPositionCatalogRepo catalogRepo) {
        this.catalogRepo = catalogRepo;
    }

    /** Ordered list of enabled catalog positions (for dropdown/checklist). */
    public List<PositionCatalogItemDto> listEnabledCatalog() {
        return catalogRepo.findByIsEnabledTrueOrderByOrderIndexAsc().stream()
                .map(this::toItem)
                .collect(Collectors.toList());
    }

    /** All catalog for admin (ordered). */
    public List<PositionCatalogItemDto> listAllCatalog() {
        return catalogRepo.findAllByOrderByOrderIndexAsc().stream()
                .map(this::toItem)
                .collect(Collectors.toList());
    }

    private PositionCatalogItemDto toItem(OrgPositionCatalogEntity e) {
        return new PositionCatalogItemDto(
                e.getId(),
                e.getBaseName(),
                e.getGrade(),
                e.getDisplayName(),
                e.getShortName(),
                e.getIsEnabled()
        );
    }
}
