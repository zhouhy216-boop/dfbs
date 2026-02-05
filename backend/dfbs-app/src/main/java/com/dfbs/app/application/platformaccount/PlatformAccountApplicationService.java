package com.dfbs.app.application.platformaccount;

import com.dfbs.app.application.platformaccount.dto.*;
import com.dfbs.app.application.customer.CustomerMasterDataService;
import com.dfbs.app.application.platformorg.PlatformOrgService;
import com.dfbs.app.application.platformorg.dto.PlatformOrgRequest;
import com.dfbs.app.application.smartselect.TempDataService;
import com.dfbs.app.application.smartselect.dto.GetOrCreateTempRequest;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.masterdata.ContractRepo;
import com.dfbs.app.modules.platformaccount.ApplicationSourceType;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationEntity;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationRepo;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationStatus;
import com.dfbs.app.modules.platformorg.PlatformOrgCustomerEntity;
import com.dfbs.app.modules.platformorg.PlatformOrgEntity;
import com.dfbs.app.modules.platformorg.PlatformOrgPlatform;
import com.dfbs.app.modules.platformorg.PlatformOrgRepo;
import com.dfbs.app.modules.platformorg.PlatformOrgStatus;
import com.dfbs.app.modules.user.UserRepo;
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
import java.util.Map;
import java.util.Optional;

@Service
public class PlatformAccountApplicationService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PlatformAccountApplicationRepo repo;
    private final PlatformOrgRepo platformOrgRepo;
    private final PlatformOrgService platformOrgService;
    private final CurrentUserIdResolver currentUserIdResolver;
    private final CustomerMasterDataService customerMasterDataService;
    private final TempDataService tempDataService;
    private final ContractRepo contractRepo;
    private final UserRepo userRepo;

    public PlatformAccountApplicationService(PlatformAccountApplicationRepo repo,
                                             PlatformOrgRepo platformOrgRepo,
                                             PlatformOrgService platformOrgService,
                                             CurrentUserIdResolver currentUserIdResolver,
                                             CustomerMasterDataService customerMasterDataService,
                                             TempDataService tempDataService,
                                             ContractRepo contractRepo,
                                             UserRepo userRepo) {
        this.repo = repo;
        this.platformOrgRepo = platformOrgRepo;
        this.platformOrgService = platformOrgService;
        this.currentUserIdResolver = currentUserIdResolver;
        this.customerMasterDataService = customerMasterDataService;
        this.tempDataService = tempDataService;
        this.contractRepo = contractRepo;
        this.userRepo = userRepo;
    }

    private String resolveApplicantName(Long applicantId) {
        if (applicantId == null) return null;
        return userRepo.findById(applicantId)
                .map(u -> (u.getNickname() != null && !u.getNickname().isBlank()) ? u.getNickname() : u.getUsername())
                .orElse("User " + applicantId);
    }

    private PlatformAccountApplicationResponse toResponse(PlatformAccountApplicationEntity entity) {
        return PlatformAccountApplicationResponse.fromEntity(entity, resolveApplicantName(entity.getApplicantId()));
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
        return toResponse(repo.save(entity));
    }

    @Transactional
    public PlatformAccountApplicationResponse plannerSubmit(Long id, PlatformAccountPlannerSubmitRequest request) {
        PlatformAccountApplicationEntity entity = getOrThrow(id);
        if (entity.getStatus() != PlatformAccountApplicationStatus.PENDING_PLANNER
                && entity.getStatus() != PlatformAccountApplicationStatus.CLOSED
                && entity.getStatus() != PlatformAccountApplicationStatus.PENDING_CONFIRM) {
            throw new IllegalStateException("当前申请不处于待规划/待确认或已关闭状态，无法提交");
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
        return toResponse(repo.save(entity));
    }

    @Transactional
    public PlatformAccountApplicationResponse close(Long id) {
        PlatformAccountApplicationEntity entity = getOrThrow(id);
        if (entity.getStatus() != PlatformAccountApplicationStatus.PENDING_PLANNER
                && entity.getStatus() != PlatformAccountApplicationStatus.CLOSED
                && entity.getStatus() != PlatformAccountApplicationStatus.PENDING_CONFIRM) {
            throw new IllegalStateException("只有待规划/待确认或已关闭的申请可执行关闭操作");
        }
        entity.setStatus(PlatformAccountApplicationStatus.CLOSED);
        return toResponse(repo.save(entity));
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
            return toResponse(repo.save(entity));
        }
        applyAdminRequest(entity, request);
        syncMasterDataBindOnly(entity);
        entity.setAdminId(currentUserIdResolver.getCurrentUserId());
        entity.setStatus(PlatformAccountApplicationStatus.APPROVED);
        entity.setRejectReason(null);
        PlatformAccountApplicationEntity saved = repo.save(entity);
        createTempContractIfNeeded(saved);
        return toResponse(saved);
    }

    @Transactional
    public PlatformAccountApplicationResponse reject(Long id, PlatformAccountRejectRequest request) {
        PlatformAccountApplicationEntity entity = getOrThrow(id);
        if (entity.getStatus() != PlatformAccountApplicationStatus.PENDING_ADMIN) {
            throw new IllegalStateException("只有待管理员审核的申请才能驳回");
        }
        entity.setAdminId(currentUserIdResolver.getCurrentUserId());
        entity.setRejectReason(request.reason() != null ? request.reason().trim() : null);
        if (entity.getSourceType() == ApplicationSourceType.SERVICE) {
            entity.setStatus(PlatformAccountApplicationStatus.PENDING);
        } else {
            entity.setStatus(PlatformAccountApplicationStatus.PENDING_CONFIRM);
        }
        return toResponse(repo.save(entity));
    }

    @Transactional(readOnly = true)
    public PlatformAccountApplicationResponse get(Long id) {
        return toResponse(getOrThrow(id));
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
                .map(this::toResponse);
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

    /** Check for existing orgs on same platform with matching customer name, email, or phone (normalized). */
    @Transactional(readOnly = true)
    public List<CheckDuplicateMatchItem> checkDuplicates(CheckDuplicatesRequest request) {
        String nCustomer = normalize(request.customerName());
        String nEmail = normalize(request.email());
        String nPhone = normalize(request.contactPhone());
        if (!StringUtils.hasText(nCustomer) && !StringUtils.hasText(nEmail) && !StringUtils.hasText(nPhone)) {
            return List.of();
        }
        List<PlatformOrgEntity> orgs = platformOrgRepo.findByPlatform(request.platform());
        List<CheckDuplicateMatchItem> result = new ArrayList<>();
        for (PlatformOrgEntity org : orgs) {
            List<String> linkedNames = getLinkedCustomerNames(org);
            boolean nameMatch = StringUtils.hasText(nCustomer) && linkedNames.stream()
                    .anyMatch(name -> normalize(name).equals(nCustomer));
            boolean emailMatch = StringUtils.hasText(nEmail) && nEmail.equals(normalize(org.getContactEmail()));
            boolean phoneMatch = StringUtils.hasText(nPhone) && nPhone.equals(normalize(org.getContactPhone()));
            if (!nameMatch && !emailMatch && !phoneMatch) continue;
            List<String> reasons = new ArrayList<>();
            if (nameMatch) reasons.add("客户名称已存在");
            if (emailMatch) reasons.add("邮箱已存在");
            if (phoneMatch) reasons.add("电话已存在");
            String customerDisplay = linkedNames.isEmpty() ? "—" : String.join(", ", linkedNames);
            result.add(new CheckDuplicateMatchItem(
                    org.getOrgCodeShort(),
                    customerDisplay,
                    org.getContactEmail() != null ? org.getContactEmail() : "—",
                    org.getContactPhone() != null ? org.getContactPhone() : "—",
                    String.join("；", reasons)
            ));
        }
        return result;
    }

    private List<String> getLinkedCustomerNames(PlatformOrgEntity org) {
        if (org.getCustomerLinks() == null || org.getCustomerLinks().isEmpty()) return List.of();
        List<String> names = new ArrayList<>();
        for (PlatformOrgCustomerEntity link : org.getCustomerLinks()) {
            try {
                names.add(customerMasterDataService.getById(link.getCustomerId()).getName());
            } catch (Exception ignored) {
                names.add("客户#" + link.getCustomerId());
            }
        }
        return names;
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase();
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

    /** BIND_ONLY: If org exists, only add application's customer to org (never overwrite). If not exists, create org. Missing customer/contract create temp records (show in Data Confirmation Center). */
    private void syncMasterDataBindOnly(PlatformAccountApplicationEntity entity) {
        if (!StringUtils.hasText(entity.getOrgCodeShort())) {
            throw new IllegalStateException("审批前必须提供机构代码/简称");
        }
        String code = entity.getOrgCodeShort().trim();
        entity.setOrgCodeShort(code);
        Long customerId = entity.getCustomerId();
        if (customerId == null && StringUtils.hasText(entity.getCustomerName())) {
            Optional<com.dfbs.app.modules.customer.CustomerEntity> found = customerMasterDataService.findFirstByName(entity.getCustomerName());
            if (found.isPresent()) {
                customerId = found.get().getId();
                entity.setCustomerId(customerId);
            } else {
                long tempId = tempDataService.getOrCreateTemp(
                        new GetOrCreateTempRequest("CUSTOMER", entity.getCustomerName().trim(), Map.of())).getId();
                customerId = tempId;
                entity.setCustomerId(customerId);
            }
        }
        if (customerId == null && !StringUtils.hasText(entity.getCustomerName())) {
            throw new IllegalStateException("审批前必须确认客户");
        }
        final Long finalCustomerId = customerId;
        List<Long> customerIdsForOrg = List.of(finalCustomerId);
        Optional<PlatformOrgEntity> existingOpt = platformOrgRepo.findByPlatformAndOrgCodeShort(entity.getPlatform(), code);
        if (existingOpt.isPresent()) {
            bindCustomerToOrg(existingOpt.get(), finalCustomerId);
        } else {
            try {
                PlatformOrgRequest orgRequest = new PlatformOrgRequest(
                        entity.getPlatform(),
                        code,
                        trimToNull(entity.getOrgFullName()),
                        customerIdsForOrg,
                        trimToNull(entity.getContactPerson()),
                        trimToNull(entity.getPhone()),
                        trimToNull(entity.getEmail()),
                        trimToNull(entity.getSalesPerson()),
                        trimToNull(entity.getRegion()),
                        null,
                        Boolean.TRUE,
                        PlatformOrgStatus.ACTIVE,
                        entity.getId(),
                        "APP"
                );
                platformOrgService.create(orgRequest);
            } catch (DataIntegrityViolationException e) {
                platformOrgRepo.findByPlatformAndOrgCodeShort(entity.getPlatform(), code)
                        .ifPresent(existing -> bindCustomerToOrg(existing, finalCustomerId));
            }
        }
    }

    private void createTempContractIfNeeded(PlatformAccountApplicationEntity entity) {
        if (entity.getSourceType() != ApplicationSourceType.FACTORY) return;
        String contractNo = trimToNull(entity.getContractNo());
        if (contractNo == null) return;
        if (contractRepo.existsByContractNo(contractNo)) return;
        tempDataService.getOrCreateTemp(new GetOrCreateTempRequest("CONTRACT", contractNo, Map.of()));
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
