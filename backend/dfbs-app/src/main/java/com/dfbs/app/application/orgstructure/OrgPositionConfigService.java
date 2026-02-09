package com.dfbs.app.application.orgstructure;

import com.dfbs.app.application.orgstructure.dto.*;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.orgstructure.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrgPositionConfigService {

    private final OrgPositionCatalogRepo catalogRepo;
    private final OrgPositionEnabledRepo enabledRepo;
    private final OrgPositionBindingRepo bindingRepo;
    private final OrgLevelPositionTemplateRepo templateRepo;
    private final OrgNodeRepo nodeRepo;
    private final OrgPersonRepo personRepo;
    private final PersonAffiliationRepo affiliationRepo;
    private final OrgChangeLogService changeLogService;
    private final CurrentUserIdResolver userIdResolver;

    public OrgPositionConfigService(OrgPositionCatalogRepo catalogRepo,
                                    OrgPositionEnabledRepo enabledRepo,
                                    OrgPositionBindingRepo bindingRepo,
                                    OrgLevelPositionTemplateRepo templateRepo,
                                    OrgNodeRepo nodeRepo,
                                    OrgPersonRepo personRepo,
                                    PersonAffiliationRepo affiliationRepo,
                                    OrgChangeLogService changeLogService,
                                    CurrentUserIdResolver userIdResolver) {
        this.catalogRepo = catalogRepo;
        this.enabledRepo = enabledRepo;
        this.bindingRepo = bindingRepo;
        this.templateRepo = templateRepo;
        this.nodeRepo = nodeRepo;
        this.personRepo = personRepo;
        this.affiliationRepo = affiliationRepo;
        this.changeLogService = changeLogService;
        this.userIdResolver = userIdResolver;
    }

    private String getNodeNamePath(Long nodeId) {
        List<String> names = new ArrayList<>();
        Long currentId = nodeId;
        while (currentId != null) {
            OrgNodeEntity node = nodeRepo.findById(currentId).orElse(null);
            if (node == null) break;
            names.add(0, node.getName());
            currentId = node.getParentId();
        }
        return String.join(" / ", names);
    }

    public PositionsByOrgResponseDto getByOrg(Long orgNodeId) {
        OrgNodeEntity node = nodeRepo.findById(orgNodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "组织节点不存在"));
        List<OrgPositionEnabledEntity> enabledList = enabledRepo.findByOrgNodeIdAndIsEnabledTrueOrderByIdAsc(orgNodeId);
        List<Long> positionIds = enabledList.stream().map(OrgPositionEnabledEntity::getPositionId).distinct().toList();
        Map<Long, OrgPositionCatalogEntity> catalogMap = catalogRepo.findAllById(positionIds).stream()
                .collect(Collectors.toMap(OrgPositionCatalogEntity::getId, p -> p));
        List<EnabledPositionWithBindingsDto> positions = new ArrayList<>();
        for (OrgPositionEnabledEntity en : enabledList) {
            OrgPositionCatalogEntity pos = catalogMap.get(en.getPositionId());
            if (pos == null) continue;
            List<OrgPositionBindingEntity> bindings = bindingRepo.findByOrgNodeIdAndPositionIdAndIsActiveTrue(orgNodeId, en.getPositionId());
            List<PositionBoundPersonDto> people = toBoundPeople(bindings, orgNodeId);
            positions.add(new EnabledPositionWithBindingsDto(
                    pos.getId(),
                    pos.getBaseName(),
                    pos.getGrade(),
                    pos.getDisplayName(),
                    pos.getShortName(),
                    people
            ));
        }
        return new PositionsByOrgResponseDto(orgNodeId, node.getName(), positions);
    }

    private List<PositionBoundPersonDto> toBoundPeople(List<OrgPositionBindingEntity> bindings, Long currentOrgNodeId) {
        if (bindings.isEmpty()) return List.of();
        Set<Long> personIds = bindings.stream().map(OrgPositionBindingEntity::getPersonId).collect(Collectors.toSet());
        Map<Long, OrgPersonEntity> personMap = personRepo.findAllById(personIds).stream().collect(Collectors.toMap(OrgPersonEntity::getId, p -> p));
        Map<Long, Long> primaryOrgByPerson = new java.util.HashMap<>();
        for (Long pid : personIds) {
            affiliationRepo.findByPersonIdAndIsPrimaryTrue(pid)
                    .map(PersonAffiliationEntity::getOrgNodeId)
                    .ifPresent(primaryId -> primaryOrgByPerson.put(pid, primaryId));
        }
        List<PositionBoundPersonDto> out = new ArrayList<>();
        for (OrgPositionBindingEntity b : bindings) {
            OrgPersonEntity person = personMap.get(b.getPersonId());
            if (person == null) continue;
            Long primaryOrgId = primaryOrgByPerson.get(b.getPersonId());
            boolean isPartTime = primaryOrgId != null && !primaryOrgId.equals(currentOrgNodeId);
            String primaryPath = primaryOrgId != null ? getNodeNamePath(primaryOrgId) : null;
            out.add(new PositionBoundPersonDto(
                    person.getId(),
                    person.getName(),
                    person.getPhone(),
                    person.getEmail(),
                    primaryOrgId,
                    primaryPath,
                    isPartTime
            ));
        }
        return out;
    }

    @Transactional
    public void enablePosition(Long orgNodeId, Long positionId) {
        nodeRepo.findById(orgNodeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "组织节点不存在"));
        catalogRepo.findById(positionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "职位不存在"));
        Optional<OrgPositionEnabledEntity> existing = enabledRepo.findByOrgNodeIdAndPositionId(orgNodeId, positionId);
        if (existing.isPresent()) {
            if (Boolean.TRUE.equals(existing.get().getIsEnabled())) return;
            existing.get().setIsEnabled(true);
            enabledRepo.save(existing.get());
        } else {
            OrgPositionEnabledEntity e = new OrgPositionEnabledEntity();
            e.setOrgNodeId(orgNodeId);
            e.setPositionId(positionId);
            e.setIsEnabled(true);
            enabledRepo.save(e);
        }
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        changeLogService.log("ORG_NODE", orgNodeId, "ORG_POSITION_ENABLE", uid, uname,
                "启用职位: orgNodeId=" + orgNodeId + ", positionId=" + positionId, null);
    }

    @Transactional
    public void disablePosition(Long orgNodeId, Long positionId) {
        nodeRepo.findById(orgNodeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "组织节点不存在"));
        long bound = bindingRepo.countActiveByOrgNodeIdAndPositionId(orgNodeId, positionId);
        if (bound > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "该职位下已绑定 " + bound + " 人，请先解除绑定后再停用职位");
        }
        enabledRepo.findByOrgNodeIdAndPositionId(orgNodeId, positionId).ifPresent(e -> {
            e.setIsEnabled(false);
            enabledRepo.save(e);
        });
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        changeLogService.log("ORG_NODE", orgNodeId, "ORG_POSITION_DISABLE", uid, uname,
                "停用职位: orgNodeId=" + orgNodeId + ", positionId=" + positionId, null);
    }

    @Transactional
    public void putBindings(Long orgNodeId, Long positionId, List<Long> personIds) {
        nodeRepo.findById(orgNodeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "组织节点不存在"));
        if (!enabledRepo.existsByOrgNodeIdAndPositionId(orgNodeId, positionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该组织未启用此职位，请先启用职位");
        }
        List<Long> ids = personIds != null ? personIds.stream().filter(Objects::nonNull).distinct().toList() : List.of();
        for (Long pid : ids) {
            personRepo.findById(pid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "人员不存在: " + pid));
        }
        bindingRepo.deleteByOrgNodeIdAndPositionId(orgNodeId, positionId);
        for (Long personId : ids) {
            OrgPositionBindingEntity b = new OrgPositionBindingEntity();
            b.setOrgNodeId(orgNodeId);
            b.setPositionId(positionId);
            b.setPersonId(personId);
            b.setIsActive(true);
            bindingRepo.save(b);
        }
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        changeLogService.log("ORG_NODE", orgNodeId, "ORG_POSITION_BINDINGS_UPDATE", uid, uname,
                "更新职位绑定: positionId=" + positionId + ", 绑定 " + ids.size() + " 人", null);
    }

    public List<BindingQueryPersonDto> queryBindings(Long orgNodeId, Long positionId) {
        List<OrgPositionBindingEntity> bindings = bindingRepo.findByOrgNodeIdAndPositionIdAndIsActiveTrue(orgNodeId, positionId);
        if (bindings.isEmpty()) return List.of();
        Set<Long> personIds = bindings.stream().map(OrgPositionBindingEntity::getPersonId).collect(Collectors.toSet());
        Map<Long, OrgPersonEntity> personMap = personRepo.findAllById(personIds).stream().collect(Collectors.toMap(OrgPersonEntity::getId, p -> p));
        Map<Long, Long> primaryByPerson = new java.util.HashMap<>();
        for (Long pid : personIds) {
            affiliationRepo.findByPersonIdAndIsPrimaryTrue(pid)
                    .map(PersonAffiliationEntity::getOrgNodeId)
                    .ifPresent(primaryId -> primaryByPerson.put(pid, primaryId));
        }
        List<BindingQueryPersonDto> out = new ArrayList<>();
        for (OrgPositionBindingEntity b : bindings) {
            OrgPersonEntity person = personMap.get(b.getPersonId());
            if (person == null) continue;
            boolean isPartTime = !Objects.equals(primaryByPerson.get(b.getPersonId()), orgNodeId);
            out.add(new BindingQueryPersonDto(person.getId(), person.getName(), person.getPhone(), person.getEmail(), isPartTime));
        }
        return out;
    }

    public List<PersonPositionAssignmentDto> getPersonPositions(Long personId) {
        personRepo.findById(personId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "人员不存在"));
        List<OrgPositionBindingEntity> bindings = bindingRepo.findByPersonIdAndIsActiveTrue(personId);
        if (bindings.isEmpty()) return List.of();
        Set<Long> positionIds = bindings.stream().map(OrgPositionBindingEntity::getPositionId).collect(Collectors.toSet());
        Set<Long> orgIds = bindings.stream().map(OrgPositionBindingEntity::getOrgNodeId).collect(Collectors.toSet());
        Map<Long, OrgPositionCatalogEntity> catalogMap = catalogRepo.findAllById(positionIds).stream().collect(Collectors.toMap(OrgPositionCatalogEntity::getId, p -> p));
        Long primaryOrgId = affiliationRepo.findByPersonIdAndIsPrimaryTrue(personId).map(PersonAffiliationEntity::getOrgNodeId).orElse(null);
        List<PersonPositionAssignmentDto> out = new ArrayList<>();
        for (OrgPositionBindingEntity b : bindings) {
            OrgPositionCatalogEntity pos = catalogMap.get(b.getPositionId());
            if (pos == null) continue;
            String orgPath = getNodeNamePath(b.getOrgNodeId());
            boolean isPartTime = primaryOrgId != null && !primaryOrgId.equals(b.getOrgNodeId());
            out.add(new PersonPositionAssignmentDto(
                    b.getOrgNodeId(),
                    orgPath,
                    pos.getId(),
                    pos.getDisplayName(),
                    pos.getShortName(),
                    isPartTime
            ));
        }
        return out;
    }

    /** Apply level template to a new org node (called after node creation). */
    @Transactional
    public void applyTemplateForNewNode(Long orgNodeId, Long levelId) {
        List<OrgLevelPositionTemplateEntity> templates = templateRepo.findByLevelIdAndIsEnabledTrue(levelId);
        for (OrgLevelPositionTemplateEntity t : templates) {
            if (enabledRepo.findByOrgNodeIdAndPositionId(orgNodeId, t.getPositionId()).isEmpty()) {
                OrgPositionEnabledEntity e = new OrgPositionEnabledEntity();
                e.setOrgNodeId(orgNodeId);
                e.setPositionId(t.getPositionId());
                e.setIsEnabled(true);
                enabledRepo.save(e);
            }
        }
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        changeLogService.log("ORG_NODE", orgNodeId, "TEMPLATE_APPLIED_ON_ORG_CREATE", uid, uname,
                "新建组织节点后应用层级职位模板: levelId=" + levelId + ", 启用 " + templates.size() + " 个职位", null);
    }
}
