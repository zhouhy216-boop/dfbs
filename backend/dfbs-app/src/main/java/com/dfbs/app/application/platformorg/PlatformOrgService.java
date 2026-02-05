package com.dfbs.app.application.platformorg;

import com.dfbs.app.application.customer.CustomerMasterDataService;
import com.dfbs.app.application.platformorg.dto.PlatformOrgRequest;
import com.dfbs.app.application.platformorg.dto.PlatformOrgResponse;
import com.dfbs.app.application.platformorg.dto.SimpleCustomerDto;
import com.dfbs.app.application.platformorg.dto.SourceInfo;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.platformaccount.ApplicationSourceType;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationEntity;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationRepo;
import com.dfbs.app.modules.platformorg.PlatformOrgCustomerEntity;
import com.dfbs.app.modules.platformorg.PlatformOrgEntity;
import com.dfbs.app.modules.platformorg.PlatformOrgPlatform;
import com.dfbs.app.modules.platformorg.PlatformOrgRepo;
import com.dfbs.app.modules.platformorg.PlatformOrgStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class PlatformOrgService {

    private final PlatformOrgRepo repo;
    private final PlatformOrgValidator validator;
    private final CustomerMasterDataService customerMasterDataService;
    private final PlatformAccountApplicationRepo applicationRepo;

    public PlatformOrgService(PlatformOrgRepo repo, PlatformOrgValidator validator,
                              CustomerMasterDataService customerMasterDataService,
                              PlatformAccountApplicationRepo applicationRepo) {
        this.repo = repo;
        this.validator = validator;
        this.customerMasterDataService = customerMasterDataService;
        this.applicationRepo = applicationRepo;
    }

    @Transactional
    public PlatformOrgResponse create(PlatformOrgRequest request) {
        PlatformOrgEntity entity = toEntity(request);
        validator.validateForCreate(entity);
        entity = repo.save(entity);
        if (request.customerIds() != null && !request.customerIds().isEmpty()) {
            syncCustomerLinks(entity, request.customerIds());
            entity = repo.save(entity);
        }
        return toResponse(entity);
    }

    @Transactional
    public PlatformOrgResponse update(Long id, PlatformOrgRequest request) {
        PlatformOrgEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("平台组织不存在"));
        PlatformOrgStatus oldStatus = entity.getStatus() != null ? entity.getStatus() : PlatformOrgStatus.ACTIVE;
        PlatformOrgStatus newStatus = request.status() != null ? request.status() : oldStatus;
        applyRequest(entity, request);
        if (newStatus == PlatformOrgStatus.DELETED && oldStatus != PlatformOrgStatus.DELETED) {
            String suffix = "_DEL_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            String base = entity.getOrgCodeShort() != null ? entity.getOrgCodeShort().trim() : "";
            String newCode = base + suffix;
            if (newCode.length() > 128) {
                newCode = base.substring(0, Math.max(0, 128 - suffix.length())) + suffix;
            }
            entity.setOrgCodeShort(newCode);
        }
        validator.validateForUpdate(entity, id);
        entity = repo.save(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public PlatformOrgResponse get(Long id) {
        PlatformOrgEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("平台组织不存在"));
        SourceInfo sourceInfo = resolveSourceInfo(entity);
        List<SimpleCustomerDto> linked = resolveLinkedCustomers(entity);
        return PlatformOrgResponse.fromEntity(entity, linked, sourceInfo);
    }

    @Transactional(readOnly = true)
    public List<PlatformOrgResponse> list(Optional<PlatformOrgPlatform> platform, Optional<Long> customerId) {
        if (platform.isPresent() && customerId.isPresent()) {
            return findByPlatformAndCustomer(platform.get(), customerId.get());
        }
        Stream<PlatformOrgEntity> stream;
        if (platform.isPresent()) {
            stream = repo.findByPlatform(platform.get()).stream();
        } else if (customerId.isPresent()) {
            stream = repo.findByCustomerId(customerId.get()).stream();
        } else {
            stream = repo.findAll().stream();
        }
        return toResponseList(stream.toList());
    }

    @Transactional(readOnly = true)
    public List<PlatformOrgResponse> findByPlatformAndCustomer(PlatformOrgPlatform platform, Long customerId) {
        return toResponseList(repo.findByPlatformAndCustomerId(platform, customerId));
    }

    private PlatformOrgResponse toResponse(PlatformOrgEntity entity) {
        List<SimpleCustomerDto> linked = resolveLinkedCustomers(entity);
        return PlatformOrgResponse.fromEntity(entity, linked, null);
    }

    private SourceInfo resolveSourceInfo(PlatformOrgEntity entity) {
        String sourceType = entity.getSourceType() != null ? entity.getSourceType().trim() : null;
        if (sourceType == null || sourceType.isEmpty()) {
            return new SourceInfo(SourceInfo.TYPE_LEGACY, null, null, null, null);
        }
        if ("MANUAL".equalsIgnoreCase(sourceType)) {
            return new SourceInfo(SourceInfo.TYPE_MANUAL, null, null, null, null);
        }
        if (entity.getSourceApplicationId() == null) {
            return new SourceInfo(SourceInfo.TYPE_MANUAL, null, null, null, null);
        }
        return applicationRepo.findById(entity.getSourceApplicationId())
                .map(app -> {
                    String type = app.getSourceType() == ApplicationSourceType.FACTORY ? SourceInfo.TYPE_SALES
                            : app.getSourceType() == ApplicationSourceType.SERVICE ? SourceInfo.TYPE_SERVICE
                            : SourceInfo.TYPE_MANUAL;
                    String applicantName = app.getApplicantId() != null ? "User " + app.getApplicantId() : null;
                    String plannerName = app.getPlannerId() != null ? "User " + app.getPlannerId() : null;
                    String adminName = app.getAdminId() != null ? "User " + app.getAdminId() : null;
                    return new SourceInfo(type, app.getApplicationNo(), applicantName, plannerName, adminName);
                })
                .orElse(new SourceInfo(SourceInfo.TYPE_LEGACY, null, null, null, null));
    }

    private List<PlatformOrgResponse> toResponseList(List<PlatformOrgEntity> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }
        java.util.Set<Long> allCustomerIds = new java.util.HashSet<>();
        for (PlatformOrgEntity e : entities) {
            if (e.getCustomerLinks() != null) {
                e.getCustomerLinks().stream().map(PlatformOrgCustomerEntity::getCustomerId).forEach(allCustomerIds::add);
            }
        }
        java.util.Map<Long, String> idToName = resolveCustomerNames(new ArrayList<>(allCustomerIds));
        return entities.stream()
                .map(e -> {
                    List<SimpleCustomerDto> linked = e.getCustomerLinks() == null ? List.of()
                            : e.getCustomerLinks().stream()
                                    .map(PlatformOrgCustomerEntity::getCustomerId)
                                    .map(cid -> new SimpleCustomerDto(cid, idToName.getOrDefault(cid, "客户#" + cid)))
                                    .toList();
                    return PlatformOrgResponse.fromEntity(e, linked);
                })
                .toList();
    }

    private List<SimpleCustomerDto> resolveLinkedCustomers(PlatformOrgEntity entity) {
        if (entity.getCustomerLinks() == null || entity.getCustomerLinks().isEmpty()) {
            return List.of();
        }
        List<Long> ids = entity.getCustomerLinks().stream().map(PlatformOrgCustomerEntity::getCustomerId).toList();
        java.util.Map<Long, String> idToName = resolveCustomerNames(ids);
        return ids.stream()
                .map(cid -> new SimpleCustomerDto(cid, idToName.getOrDefault(cid, "客户#" + cid)))
                .toList();
    }

    private java.util.Map<Long, String> resolveCustomerNames(List<Long> customerIds) {
        java.util.Map<Long, String> map = new java.util.HashMap<>();
        for (Long id : customerIds) {
            try {
                CustomerEntity c = customerMasterDataService.getById(id);
                map.put(id, c.getName());
            } catch (Exception ignored) {
                map.put(id, "客户#" + id);
            }
        }
        return map;
    }

    private PlatformOrgEntity toEntity(PlatformOrgRequest request) {
        PlatformOrgEntity entity = new PlatformOrgEntity();
        applyRequest(entity, request);
        return entity;
    }

    private void applyRequest(PlatformOrgEntity entity, PlatformOrgRequest request) {
        entity.setPlatform(request.platform());
        entity.setOrgCodeShort(trimToNull(request.orgCodeShort()));
        entity.setOrgFullName(trimToNull(request.orgFullName()));
        entity.setContactPerson(trimToNull(request.contactPerson()));
        entity.setContactPhone(trimToNull(request.contactPhone()));
        entity.setContactEmail(trimToNull(request.contactEmail()));
        entity.setSalesPerson(trimToNull(request.salesPerson()));
        entity.setRegion(trimToNull(request.region()));
        entity.setRemark(request.remark());
        if (request.isActive() != null) {
            entity.setIsActive(request.isActive());
        } else if (entity.getId() == null) {
            entity.setIsActive(Boolean.TRUE);
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        if (request.sourceApplicationId() != null) {
            entity.setSourceApplicationId(request.sourceApplicationId());
        }
        if (request.sourceType() != null && !request.sourceType().isBlank()) {
            entity.setSourceType(request.sourceType().trim());
        } else if (entity.getId() == null) {
            entity.setSourceType("MANUAL");
        }
        if (entity.getId() != null && request.customerIds() != null) {
            syncCustomerLinks(entity, request.customerIds());
        }
    }

    private void syncCustomerLinks(PlatformOrgEntity entity, List<Long> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            entity.getCustomerLinks().clear();
            return;
        }
        java.util.Set<Long> target = new java.util.HashSet<>(customerIds);
        entity.getCustomerLinks().removeIf(link -> !target.contains(link.getCustomerId()));
        for (Long cid : target) {
            if (entity.getCustomerLinks().stream().noneMatch(link -> link.getCustomerId().equals(cid))) {
                PlatformOrgCustomerEntity link = new PlatformOrgCustomerEntity();
                link.setOrgId(entity.getId());
                link.setCustomerId(cid);
                link.setOrg(entity);
                entity.getCustomerLinks().add(link);
            }
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
