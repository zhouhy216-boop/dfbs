package com.dfbs.app.application.platformaccount;

import com.dfbs.app.application.platformaccount.dto.*;
import com.dfbs.app.application.customer.CustomerMasterDataService;
import com.dfbs.app.application.platformorg.PlatformOrgService;
import com.dfbs.app.application.platformorg.dto.PlatformOrgRequest;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.platformaccount.ApplicationSourceType;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationEntity;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationRepo;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationStatus;
import com.dfbs.app.modules.platformorg.PlatformOrgCustomerEntity;
import com.dfbs.app.modules.platformorg.PlatformOrgEntity;
import com.dfbs.app.modules.platformorg.PlatformOrgPlatform;
import com.dfbs.app.modules.platformorg.PlatformOrgRepo;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlatformAccountApplicationService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PlatformAccountApplicationRepo repo;
    private final PlatformOrgRepo platformOrgRepo;
    private final PlatformOrgService platformOrgService;
    private final CurrentUserIdResolver currentUserIdResolver;
    private final CustomerMasterDataService customerMasterDataService;

    public PlatformAccountApplicationService(PlatformAccountApplicationRepo repo,
                                             PlatformOrgRepo platformOrgRepo,
                                             PlatformOrgService platformOrgService,
                                             CurrentUserIdResolver currentUserIdResolver,
                                             CustomerMasterDataService customerMasterDataService) {
        this.repo = repo;
        this.platformOrgRepo = platformOrgRepo;
        this.platformOrgService = platformOrgService;
        this.currentUserIdResolver = currentUserIdResolver;
        this.customerMasterDataService = customerMasterDataService;
    }

    @Transactional
    public PlatformAccountApplicationResponse create(PlatformAccountApplicationCreateRequest request) {
        validateCreate(request);
        PlatformAccountApplicationEntity entity = new PlatformAccountApplicationEntity();
        entity.setApplicationNo(generateApplicationNo());
        entity.setPlatform(request.platform());
        entity.setSourceType(request.sourceType() != null ? request.sourceType() : ApplicationSourceType.FACTORY);
        if (request.customerId() != null) {
            entity.setCustomerId(request.customerId());
            entity.setCustomerName(customerMasterDataService.getById(request.customerId()).getName());
        } else {
            entity.setCustomerId(null);
            entity.setCustomerName(trimToNull(request.customerName()));
        }
        entity.setOrgCodeShort(trimToNull(request.orgCodeShort()));
        entity.setOrgFullName(trimBlank(request.orgFullName()));
        entity.setContactPerson(trimToNull(request.contactPerson()));
        entity.setPhone(trimToNull(request.phone()));
        entity.setEmail(trimToNull(request.email()));
        entity.setRegion(trimToNull(request.region()));
        entity.setSalesPerson(trimToNull(request.salesPerson()));
        entity.setContractNo(trimToNull(request.contractNo()));
        entity.setPrice(request.price());
        entity.setQuantity(request.quantity());
        entity.setReason(trimToNull(request.reason()));
        entity.setIsCcPlanner(Boolean.TRUE.equals(request.isCcPlanner()));
        entity.setApplicantId(currentUserIdResolver.getCurrentUserId());
        ApplicationSourceType sourceType = request.sourceType() != null ? request.sourceType() : ApplicationSourceType.FACTORY;
        if (sourceType == ApplicationSourceType.SERVICE) {
            entity.setStatus(PlatformAccountApplicationStatus.PENDING_ADMIN);
        } else {
            entity.setStatus(Boolean.TRUE.equals(request.skipPlanner()) ? PlatformAccountApplicationStatus.PENDING_ADMIN
                    : PlatformAccountApplicationStatus.PENDING_PLANNER);
        }
        return PlatformAccountApplicationResponse.fromEntity(repo.save(entity));
    }

    @Transactional
    public PlatformAccountApplicationResponse plannerSubmit(Long id, PlatformAccountPlannerSubmitRequest request) {
        PlatformAccountApplicationEntity entity = getOrThrow(id);
        if (entity.getStatus() != PlatformAccountApplicationStatus.PENDING_PLANNER) {
            throw new IllegalStateException("当前申请不处于待规划处理状态");
        }
        if (request.platform() != null) entity.setPlatform(request.platform());
        if (request.customerId() != null) {
            entity.setCustomerId(request.customerId());
            entity.setCustomerName(customerMasterDataService.getById(request.customerId()).getName());
        } else if (request.customerName() != null) {
            entity.setCustomerId(null);
            entity.setCustomerName(trimToNull(request.customerName()));
        }
        if (request.orgCodeShort() != null) entity.setOrgCodeShort(trimBlank(request.orgCodeShort()));
        if (request.orgFullName() != null) entity.setOrgFullName(trimBlank(request.orgFullName()));
        if (request.contactPerson() != null) entity.setContactPerson(trimToNull(request.contactPerson()));
        if (request.phone() != null) entity.setPhone(trimToNull(request.phone()));
        if (request.email() != null) entity.setEmail(trimToNull(request.email()));
        if (request.region() != null) entity.setRegion(trimToNull(request.region()));
        if (request.salesPerson() != null) entity.setSalesPerson(trimToNull(request.salesPerson()));
        if (request.contractNo() != null) entity.setContractNo(trimToNull(request.contractNo()));
        if (request.price() != null) entity.setPrice(request.price());
        if (request.quantity() != null) entity.setQuantity(request.quantity());
        if (request.reason() != null) entity.setReason(trimToNull(request.reason()));
        if (request.isCcPlanner() != null) entity.setIsCcPlanner(request.isCcPlanner());
        entity.setPlannerId(currentUserIdResolver.getCurrentUserId());
        entity.setStatus(PlatformAccountApplicationStatus.PENDING_ADMIN);
        return PlatformAccountApplicationResponse.fromEntity(repo.save(entity));
    }

    @Transactional
    public PlatformAccountApplicationResponse approve(Long id, PlatformAccountApproveRequest request) {
        PlatformAccountApplicationEntity entity = getOrThrow(id);
        if (entity.getStatus() != PlatformAccountApplicationStatus.PENDING_ADMIN) {
            throw new IllegalStateException("当前申请不处于待管理员审核状态");
        }
        if (request.action() != null && request.action() == PlatformAccountApproveRequest.ApproveAction.CANCEL) {
            entity.setStatus(PlatformAccountApplicationStatus.REJECTED);
            entity.setAdminId(currentUserIdResolver.getCurrentUserId());
            entity.setRejectReason("管理员取消绑定");
            return PlatformAccountApplicationResponse.fromEntity(repo.save(entity));
        }
        applyAdminRequest(entity, request);
        syncMasterDataBindOnly(entity);
        entity.setAdminId(currentUserIdResolver.getCurrentUserId());
        entity.setStatus(PlatformAccountApplicationStatus.APPROVED);
        entity.setRejectReason(null);
        return PlatformAccountApplicationResponse.fromEntity(repo.save(entity));
    }

    @Transactional
    public PlatformAccountApplicationResponse reject(Long id, PlatformAccountRejectRequest request) {
        PlatformAccountApplicationEntity entity = getOrThrow(id);
        if (entity.getStatus() != PlatformAccountApplicationStatus.PENDING_ADMIN) {
            throw new IllegalStateException("只有待管理员审核的申请才能驳回");
        }
        entity.setStatus(PlatformAccountApplicationStatus.REJECTED);
        entity.setAdminId(currentUserIdResolver.getCurrentUserId());
        entity.setRejectReason(request.reason());
        return PlatformAccountApplicationResponse.fromEntity(repo.save(entity));
    }

    @Transactional(readOnly = true)
    public PlatformAccountApplicationResponse get(Long id) {
        return PlatformAccountApplicationResponse.fromEntity(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PlatformAccountApplicationResponse> page(Optional<PlatformAccountApplicationStatus> status,
                                                         Optional<PlatformOrgPlatform> platform,
                                                         Optional<Long> customerId,
                                                         int page,
                                                         int size) {
        return repo.findAll((root, query, cb) -> {
            var predicates = cb.conjunction();
            status.ifPresent(st -> predicates.getExpressions().add(cb.equal(root.get("status"), st)));
            platform.ifPresent(pf -> predicates.getExpressions().add(cb.equal(root.get("platform"), pf)));
            customerId.ifPresent(cid -> predicates.getExpressions().add(cb.equal(root.get("customerId"), cid)));
            return predicates;
        }, PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(PlatformAccountApplicationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public boolean existsCustomerByName(String name) {
        if (name == null || !StringUtils.hasText(name)) return false;
        return customerMasterDataService.findFirstByName(name.trim()).isPresent();
    }

    @Transactional(readOnly = true)
    public OrgMatchCheckResult checkOrgMatch(PlatformOrgPlatform platform, String orgCodeShort) {
        Optional<PlatformOrgEntity> existing = platformOrgRepo.findByPlatformAndOrgCodeShort(platform, orgCodeShort == null ? "" : orgCodeShort.trim());
        if (existing.isEmpty()) {
            return new OrgMatchCheckResult(false, null, List.of(), 0);
        }
        PlatformOrgEntity org = existing.get();
        List<Long> customerIds = org.getCustomerLinks() == null
                ? List.of()
                : org.getCustomerLinks().stream().map(PlatformOrgCustomerEntity::getCustomerId).toList();
        return new OrgMatchCheckResult(true, org.getId(), customerIds, 0);
    }

    private PlatformAccountApplicationEntity getOrThrow(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("账户开通申请不存在"));
    }

    private void validateCreate(PlatformAccountApplicationCreateRequest request) {
        if (!StringUtils.hasText(request.orgFullName())) {
            throw new IllegalArgumentException("机构全称不能为空");
        }
        // orgCodeShort is Admin-only; not validated at creation
        if (request.customerId() == null && !StringUtils.hasText(request.customerName())) {
            throw new IllegalArgumentException("请选择或输入客户");
        }
        ApplicationSourceType sourceType = request.sourceType() != null ? request.sourceType() : ApplicationSourceType.FACTORY;
        if (sourceType == ApplicationSourceType.FACTORY) {
            if (!StringUtils.hasText(request.contractNo())) {
                throw new IllegalArgumentException("工厂渠道必须填写合同号");
            }
        } else {
            if (request.price() == null || request.quantity() == null || !StringUtils.hasText(request.reason())) {
                throw new IllegalArgumentException("服务渠道必须填写价格、数量和原因");
            }
        }
    }

    private void applyAdminRequest(PlatformAccountApplicationEntity entity, PlatformAccountApproveRequest request) {
        entity.setPlatform(request.platform());
        if (request.customerId() != null) {
            entity.setCustomerId(request.customerId());
            entity.setCustomerName(customerMasterDataService.getById(request.customerId()).getName());
        }
        entity.setOrgCodeShort(trimBlank(request.orgCodeShort()));
        entity.setOrgFullName(trimBlank(request.orgFullName()));
        entity.setContactPerson(trimToNull(request.contactPerson()));
        entity.setPhone(trimToNull(request.phone()));
        entity.setEmail(trimToNull(request.email()));
        entity.setRegion(trimToNull(request.region()));
        entity.setSalesPerson(trimToNull(request.salesPerson()));
        entity.setContractNo(trimToNull(request.contractNo()));
        entity.setPrice(request.price());
        entity.setQuantity(request.quantity());
        entity.setReason(trimToNull(request.reason()));
        if (request.isCcPlanner() != null) entity.setIsCcPlanner(request.isCcPlanner());
    }

    /** BIND_ONLY: If org exists, only add application's customer to org (never overwrite). If not exists, create org. */
    private void syncMasterDataBindOnly(PlatformAccountApplicationEntity entity) {
        if (!StringUtils.hasText(entity.getOrgCodeShort())) {
            throw new IllegalStateException("审批前必须提供机构代码/简称");
        }
        String code = entity.getOrgCodeShort().trim();
        entity.setOrgCodeShort(code);
        Long customerId = entity.getCustomerId();
        if (customerId == null && StringUtils.hasText(entity.getCustomerName())) {
            customerId = customerMasterDataService.findFirstByName(entity.getCustomerName())
                    .map(c -> c.getId())
                    .orElseThrow(() -> new IllegalStateException("未找到客户: " + entity.getCustomerName() + "，请先在客户主数据中创建或由规划节点确认"));
            entity.setCustomerId(customerId);
        }
        if (customerId == null) {
            throw new IllegalStateException("审批前必须确认客户");
        }
        final Long finalCustomerId = customerId;
        Optional<PlatformOrgEntity> existingOpt = platformOrgRepo.findByPlatformAndOrgCodeShort(entity.getPlatform(), code);
        if (existingOpt.isPresent()) {
            bindCustomerToOrg(existingOpt.get(), finalCustomerId);
        } else {
            try {
                PlatformOrgRequest orgRequest = new PlatformOrgRequest(
                        entity.getPlatform(),
                        code,
                        entity.getOrgFullName(),
                        List.of(finalCustomerId),
                        entity.getContactPerson(),
                        entity.getPhone(),
                        entity.getEmail(),
                        entity.getSalesPerson(),
                        entity.getRegion(),
                        null,
                        Boolean.TRUE
                );
                platformOrgService.create(orgRequest);
            } catch (DataIntegrityViolationException e) {
                platformOrgRepo.findByPlatformAndOrgCodeShort(entity.getPlatform(), code)
                        .ifPresent(existing -> bindCustomerToOrg(existing, finalCustomerId));
            }
        }
    }

    private void bindCustomerToOrg(PlatformOrgEntity existing, Long customerId) {
        if (existing.getCustomerLinks() != null) {
            boolean already = existing.getCustomerLinks().stream()
                    .anyMatch(l -> customerId.equals(l.getCustomerId()));
            if (!already) {
                PlatformOrgCustomerEntity link = new PlatformOrgCustomerEntity();
                link.setOrgId(existing.getId());
                link.setCustomerId(customerId);
                link.setOrg(existing);
                existing.getCustomerLinks().add(link);
                platformOrgRepo.save(existing);
            }
        }
    }

    private String generateApplicationNo() {
        String prefix = "APP-" + LocalDate.now().format(DATE_FORMAT) + "-";
        int nextSeq = 1;
        String latest = repo.findTopByApplicationNoStartingWithOrderByApplicationNoDesc(prefix)
                .map(PlatformAccountApplicationEntity::getApplicationNo)
                .orElse(null);
        if (latest != null && latest.startsWith(prefix)) {
            String seqStr = latest.substring(prefix.length());
            try {
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%03d", nextSeq);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimBlank(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
