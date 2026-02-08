package com.dfbs.app.application.orgstructure;

import com.dfbs.app.application.orgstructure.dto.OrgPersonResponse;
import com.dfbs.app.application.orgstructure.dto.PersonOptionDto;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.orgstructure.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrgPersonService {

    private final OrgPersonRepo personRepo;
    private final PersonAffiliationRepo affiliationRepo;
    private final OrgNodeRepo nodeRepo;
    private final OrgNodeService nodeService;
    private final OrgChangeLogService changeLogService;
    private final CurrentUserIdResolver userIdResolver;
    private final com.dfbs.app.modules.orgstructure.JobLevelRepo jobLevelRepo;

    public OrgPersonService(OrgPersonRepo personRepo, PersonAffiliationRepo affiliationRepo,
                            OrgNodeRepo nodeRepo, OrgNodeService nodeService,
                            OrgChangeLogService changeLogService, CurrentUserIdResolver userIdResolver,
                            com.dfbs.app.modules.orgstructure.JobLevelRepo jobLevelRepo) {
        this.personRepo = personRepo;
        this.affiliationRepo = affiliationRepo;
        this.nodeRepo = nodeRepo;
        this.nodeService = nodeService;
        this.changeLogService = changeLogService;
        this.userIdResolver = userIdResolver;
        this.jobLevelRepo = jobLevelRepo;
    }

    public Page<OrgPersonEntity> search(String keyword, Long primaryOrgId, Pageable pageable) {
        return personRepo.search(keyword, primaryOrgId, pageable);
    }

    public Page<OrgPersonResponse> searchResponses(String keyword, Long primaryOrgId, Pageable pageable) {
        Page<OrgPersonEntity> page = personRepo.search(keyword, primaryOrgId, pageable);
        return page.map(p -> toResponse(p, null));
    }

    /**
     * People by org subtree (or all people when orgNodeId is null).
     * When orgNodeId is provided: self + descendants (if includeDescendants); match primary or any secondary (if includeSecondaries).
     */
    public Page<OrgPersonResponse> searchByOrgSubtree(Long orgNodeId, boolean includeDescendants, boolean includeSecondaries,
                                                      boolean activeOnly, String keyword, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        if (orgNodeId == null) {
            Page<OrgPersonEntity> page = personRepo.searchAllWithActive(kw, activeOnly, pageable);
            java.util.Set<Long> jobLevelIds = page.getContent().stream().map(OrgPersonEntity::getJobLevelId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
            java.util.Map<Long, String> jobLevelNames = new java.util.HashMap<>();
            jobLevelRepo.findAllById(jobLevelIds).forEach(j -> jobLevelNames.put(j.getId(), j.getDisplayName()));
            return page.map(p -> toResponse(p, jobLevelNames.get(p.getJobLevelId())));
        }
        List<Long> nodeIds = includeDescendants ? nodeService.getSubtreeNodeIds(orgNodeId) : List.of(orgNodeId);
        if (nodeIds.isEmpty()) {
            return org.springframework.data.domain.Page.empty(pageable);
        }
        Page<OrgPersonEntity> page = personRepo.findByOrgSubtree(nodeIds, includeSecondaries, kw, activeOnly, pageable);
        java.util.Set<Long> jobLevelIds = page.getContent().stream().map(OrgPersonEntity::getJobLevelId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, String> jobLevelNames = new java.util.HashMap<>();
        jobLevelRepo.findAllById(jobLevelIds).forEach(j -> jobLevelNames.put(j.getId(), j.getDisplayName()));
        return page.map(p -> toResponse(p, jobLevelNames.get(p.getJobLevelId())));
    }

    public List<OrgPersonEntity> listActive() {
        return personRepo.findByIsActiveTrueOrderByNameAsc();
    }

    /** Options for PersonSelect (id, name, phone, email); optional keyword filter. */
    public List<PersonOptionDto> listOptions(String keyword) {
        Page<OrgPersonEntity> page = personRepo.search(keyword == null || keyword.isBlank() ? null : keyword, null,
                org.springframework.data.domain.PageRequest.of(0, 200, org.springframework.data.domain.Sort.by("name")));
        return page.getContent().stream()
                .map(p -> new PersonOptionDto(p.getId(), p.getName(), p.getPhone(), p.getEmail()))
                .toList();
    }

    public OrgPersonEntity getById(Long id) {
        return personRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "人员不存在"));
    }

    public OrgPersonResponse getResponseById(Long id) {
        OrgPersonEntity p = getById(id);
        String jobLevelName = p.getJobLevelId() != null ? jobLevelRepo.findById(p.getJobLevelId()).map(JobLevelEntity::getDisplayName).orElse(null) : null;
        return toResponse(p, jobLevelName);
    }

    private OrgPersonResponse toResponse(OrgPersonEntity p, String jobLevelDisplayName) {
        Long primary = affiliationRepo.findByPersonIdAndIsPrimaryTrue(p.getId())
                .map(PersonAffiliationEntity::getOrgNodeId).orElse(null);
        List<Long> secondary = affiliationRepo.findByPersonId(p.getId()).stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsPrimary()))
                .map(PersonAffiliationEntity::getOrgNodeId).toList();
        return new OrgPersonResponse(p.getId(), p.getName(), p.getPhone(), p.getEmail(), p.getRemark(),
                p.getJobLevelId(), p.getIsActive(), primary, secondary,
                p.getCreatedAt(), p.getCreatedBy(), p.getUpdatedAt(), p.getUpdatedBy(), jobLevelDisplayName);
    }

    @Transactional
    public OrgPersonEntity create(String name, String phone, String email, String remark,
                                  Long jobLevelId, Long primaryOrgNodeId, List<Long> secondaryOrgNodeIds) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "姓名为必填");
        }
        if (phone == null || phone.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "手机号为必填");
        }
        if (jobLevelId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "职级为必选");
        }
        if (primaryOrgNodeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "主归属组织为必选");
        }
        nodeRepo.findById(primaryOrgNodeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "主归属组织不存在"));
        if (secondaryOrgNodeIds != null) {
            for (Long nid : secondaryOrgNodeIds) {
                nodeRepo.findById(nid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "次要归属组织不存在: " + nid));
            }
        }

        OrgPersonEntity p = new OrgPersonEntity();
        p.setName(name.trim());
        p.setPhone(phone.trim());
        p.setEmail(email != null ? email.trim() : null);
        p.setRemark(remark);
        p.setJobLevelId(jobLevelId);
        p.setIsActive(true);
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        p.setCreatedBy(uname);
        p.setUpdatedBy(uname);
        p = personRepo.save(p);

        PersonAffiliationEntity primary = new PersonAffiliationEntity();
        primary.setPersonId(p.getId());
        primary.setOrgNodeId(primaryOrgNodeId);
        primary.setIsPrimary(true);
        affiliationRepo.save(primary);

        if (secondaryOrgNodeIds != null && !secondaryOrgNodeIds.isEmpty()) {
            for (Long nid : secondaryOrgNodeIds) {
                if (nid.equals(primaryOrgNodeId)) continue;
                PersonAffiliationEntity sec = new PersonAffiliationEntity();
                sec.setPersonId(p.getId());
                sec.setOrgNodeId(nid);
                sec.setIsPrimary(false);
                affiliationRepo.save(sec);
            }
        }

        changeLogService.log("PERSON", p.getId(), "CREATE", uid, uname, "新建人员: " + p.getName(), null);
        return personRepo.findById(p.getId()).orElse(p);
    }

    @Transactional
    public OrgPersonEntity update(Long id, String name, String phone, String email, String remark,
                                  Long jobLevelId, Long primaryOrgNodeId, List<Long> secondaryOrgNodeIds) {
        OrgPersonEntity p = getById(id);
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;

        if (name != null && !name.isBlank()) p.setName(name.trim());
        if (phone != null && !phone.isBlank()) p.setPhone(phone.trim());
        if (email != null) p.setEmail(email.trim());
        if (remark != null) p.setRemark(remark);
        if (jobLevelId != null) p.setJobLevelId(jobLevelId);

        Long effectivePrimary = primaryOrgNodeId != null ? primaryOrgNodeId : affiliationRepo.findByPersonIdAndIsPrimaryTrue(id)
                .map(PersonAffiliationEntity::getOrgNodeId).orElse(null);
        List<Long> effectiveSecondary = secondaryOrgNodeIds != null ? new ArrayList<>(secondaryOrgNodeIds) : null;
        if (effectiveSecondary == null) {
            effectiveSecondary = new ArrayList<>();
            for (PersonAffiliationEntity a : affiliationRepo.findByPersonId(id)) {
                if (!Boolean.TRUE.equals(a.getIsPrimary())) effectiveSecondary.add(a.getOrgNodeId());
            }
        }
        if (primaryOrgNodeId != null || secondaryOrgNodeIds != null) {
            if (effectivePrimary == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "主归属组织为必选");
            }
            nodeRepo.findById(effectivePrimary).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "主归属组织不存在"));
            for (Long nid : effectiveSecondary) {
                if (nid.equals(effectivePrimary)) continue;
                nodeRepo.findById(nid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "次要归属组织不存在"));
            }
            affiliationRepo.findByPersonId(id).forEach(affiliationRepo::delete);
            if (effectivePrimary != null) {
                PersonAffiliationEntity primary = new PersonAffiliationEntity();
                primary.setPersonId(id);
                primary.setOrgNodeId(effectivePrimary);
                primary.setIsPrimary(true);
                affiliationRepo.save(primary);
            }
            for (Long nid : effectiveSecondary) {
                if (nid.equals(effectivePrimary)) continue;
                PersonAffiliationEntity sec = new PersonAffiliationEntity();
                sec.setPersonId(id);
                sec.setOrgNodeId(nid);
                sec.setIsPrimary(false);
                affiliationRepo.save(sec);
            }
        }

        p.setUpdatedBy(uname);
        p = personRepo.save(p);
        changeLogService.log("PERSON", p.getId(), "UPDATE", uid, uname, "更新人员: " + p.getName(), null);
        return personRepo.findById(p.getId()).orElse(p);
    }

    @Transactional
    public OrgPersonEntity disable(Long id) {
        OrgPersonEntity p = getById(id);
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        p.setIsActive(false);
        p.setUpdatedBy(uname);
        p = personRepo.save(p);
        changeLogService.log("PERSON", p.getId(), "DISABLE", uid, uname, "停用人员: " + p.getName(), null);
        return p;
    }

    @Transactional
    public OrgPersonEntity enable(Long id) {
        OrgPersonEntity p = getById(id);
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        p.setIsActive(true);
        p.setUpdatedBy(uname);
        p = personRepo.save(p);
        changeLogService.log("PERSON", p.getId(), "ENABLE", uid, uname, "启用人员: " + p.getName(), null);
        return p;
    }
}
