package com.dfbs.app.application.orgstructure;

import com.dfbs.app.application.orgstructure.dto.OrgNodeDto;
import com.dfbs.app.application.orgstructure.dto.OrgTreeNodeDto;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.orgstructure.OrgLevelEntity;
import com.dfbs.app.modules.orgstructure.OrgNodeEntity;
import com.dfbs.app.modules.orgstructure.OrgNodeRepo;
import com.dfbs.app.modules.orgstructure.OrgLevelRepo;
import com.dfbs.app.modules.orgstructure.PersonAffiliationRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrgNodeService {

    private final OrgNodeRepo nodeRepo;
    private final OrgLevelRepo levelRepo;
    private final PersonAffiliationRepo affiliationRepo;
    private final OrgChangeLogService changeLogService;
    private final CurrentUserIdResolver userIdResolver;
    private final OrgPositionConfigService positionConfigService;

    public OrgNodeService(OrgNodeRepo nodeRepo, OrgLevelRepo levelRepo,
                          PersonAffiliationRepo affiliationRepo,
                          OrgChangeLogService changeLogService,
                          CurrentUserIdResolver userIdResolver,
                          OrgPositionConfigService positionConfigService) {
        this.nodeRepo = nodeRepo;
        this.levelRepo = levelRepo;
        this.affiliationRepo = affiliationRepo;
        this.changeLogService = changeLogService;
        this.userIdResolver = userIdResolver;
        this.positionConfigService = positionConfigService;
    }

    /** All descendant node IDs (children, grandchildren, ...). */
    public List<Long> getDescendantIds(Long nodeId) {
        List<Long> out = new ArrayList<>();
        List<Long> current = List.of(nodeId);
        while (!current.isEmpty()) {
            List<Long> next = new ArrayList<>();
            for (Long id : current) {
                for (OrgNodeEntity c : nodeRepo.findByParentIdOrderByNameAsc(id)) {
                    next.add(c.getId());
                    out.add(c.getId());
                }
            }
            current = next;
        }
        return out;
    }

    /** Node id + all descendant ids (for subtree impact). */
    public List<Long> getSubtreeNodeIds(Long nodeId) {
        List<Long> ids = new ArrayList<>();
        ids.add(nodeId);
        ids.addAll(getDescendantIds(nodeId));
        return ids;
    }

    /** Impact summary for move/disable: descendant node count and active-person count in subtree (在岗/启用人员). */
    public ImpactSummary getImpactSummary(Long nodeId) {
        List<Long> subtreeIds = getSubtreeNodeIds(nodeId);
        long nodeCount = subtreeIds.size() - 1; // exclude self
        long personCount = subtreeIds.isEmpty() ? 0 : affiliationRepo.countDistinctActivePersonIdByOrgNodeIdIn(subtreeIds);
        return new ImpactSummary(nodeCount, personCount);
    }

    public record ImpactSummary(long descendantNodeCount, long personCountInSubtree) {}

    public List<OrgNodeEntity> getRoots(boolean includeDisabled) {
        if (includeDisabled) {
            return nodeRepo.findByParentIdIsNullOrderByNameAsc();
        }
        return nodeRepo.findByParentIdIsNullAndIsEnabledTrueOrderByNameAsc();
    }

    public List<OrgNodeEntity> getChildren(Long parentId, boolean includeDisabled) {
        if (parentId == null) {
            return getRoots(includeDisabled);
        }
        if (includeDisabled) {
            return nodeRepo.findByParentIdOrderByNameAsc(parentId);
        }
        return nodeRepo.findByParentIdAndIsEnabledTrueOrderByNameAsc(parentId);
    }

    /** Tree roots (for API). */
    public List<OrgNodeEntity> getTree(boolean includeDisabled) {
        return getRoots(includeDisabled);
    }

    /** Build full tree as DTO (recursive). */
    public List<OrgTreeNodeDto> getTreeDto(boolean includeDisabled) {
        List<OrgNodeEntity> roots = getRoots(includeDisabled);
        return roots.stream().map(r -> toNodeDto(r, includeDisabled)).toList();
    }

    private OrgTreeNodeDto toNodeDto(OrgNodeEntity node, boolean includeDisabled) {
        List<OrgNodeEntity> children = getChildren(node.getId(), includeDisabled);
        List<OrgTreeNodeDto> childDtos = children.stream().map(c -> toNodeDto(c, includeDisabled)).toList();
        return OrgTreeNodeDto.from(node, childDtos);
    }

    public OrgNodeEntity getById(Long id) {
        return nodeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "组织节点不存在"));
    }

    /** Single node as DTO (no lazy relations) for API. */
    public OrgNodeDto getByIdAsDto(Long id) {
        return OrgNodeDto.from(getById(id));
    }

    /** Name path from root to node (e.g. "公司 / 本部 / 研发部"). */
    public String getNodeNamePath(Long nodeId) {
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

    /** Children as DTOs for API. */
    public List<OrgNodeDto> getChildrenAsDtos(Long parentId, boolean includeDisabled) {
        return getChildren(parentId, includeDisabled).stream().map(OrgNodeDto::from).toList();
    }

    @Transactional
    public OrgNodeDto create(Long levelId, Long parentId, String name, String remark, Boolean isEnabled) {
        if (levelId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择层级");
        }
        OrgLevelEntity level = levelRepo.findById(levelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "层级不存在"));
        if (!Boolean.TRUE.equals(level.getIsEnabled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只能选择已启用的层级");
        }
        if (parentId != null) {
            OrgNodeEntity parent = getById(parentId);
            if (!Boolean.TRUE.equals(parent.getIsEnabled())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "父节点已停用");
            }
        }
        OrgNodeEntity e = new OrgNodeEntity();
        e.setLevelId(levelId);
        e.setParentId(parentId);
        e.setName(name != null ? name.trim() : "");
        e.setRemark(remark);
        e.setIsEnabled(isEnabled != null ? isEnabled : true);
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        e.setCreatedBy(uname);
        e.setUpdatedBy(uname);
        e = nodeRepo.save(e);
        changeLogService.log("ORG_NODE", e.getId(), "CREATE", uid, uname,
                "新建组织节点: " + e.getName(), null);
        positionConfigService.applyTemplateForNewNode(e.getId(), e.getLevelId());
        return OrgNodeDto.from(e);
    }

    @Transactional
    public OrgNodeDto update(Long id, String name, String remark, Boolean isEnabled) {
        OrgNodeEntity e = getById(id);
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        if (name != null && !name.isBlank()) e.setName(name.trim());
        if (remark != null) e.setRemark(remark);
        if (isEnabled != null) e.setIsEnabled(isEnabled);
        e.setUpdatedBy(uname);
        e = nodeRepo.save(e);
        changeLogService.log("ORG_NODE", e.getId(), "UPDATE", uid, uname, "更新组织节点: " + e.getName(), null);
        return OrgNodeDto.from(e);
    }

    /** Move node to new parent. Validates cycle (new parent must not be self or descendant). */
    @Transactional
    public OrgNodeDto move(Long nodeId, Long newParentId) {
        OrgNodeEntity node = getById(nodeId);
        if (newParentId != null) {
            if (newParentId.equals(nodeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能将节点移动到自身");
            }
            Set<Long> descendantIds = getDescendantIds(nodeId).stream().collect(Collectors.toSet());
            if (descendantIds.contains(newParentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能将节点移动到其下级，否则会形成循环");
            }
            getById(newParentId); // ensure exists
        }
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        node.setParentId(newParentId);
        node.setUpdatedBy(uname);
        node = nodeRepo.save(node);
        changeLogService.log("ORG_NODE", node.getId(), "MOVE", uid, uname,
                "移动组织节点至新上级: " + node.getName(), null);
        return OrgNodeDto.from(node);
    }

    /** Disable node. Blocked if has active children or has people in subtree. */
    @Transactional
    public OrgNodeDto disable(Long id) {
        OrgNodeEntity node = getById(id);
        List<OrgNodeEntity> children = nodeRepo.findByParentIdOrderByNameAsc(id);
        long activeChildren = children.stream().filter(c -> Boolean.TRUE.equals(c.getIsEnabled())).count();
        if (activeChildren > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "该节点下存在 " + activeChildren + " 个启用中的下级节点，请先迁移或停用下级后再停用本节点");
        }
        List<Long> subtreeIds = getSubtreeNodeIds(id);
        long personCount = affiliationRepo.countDistinctActivePersonIdByOrgNodeIdIn(subtreeIds);
        if (personCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "该节点及其下级下关联 " + personCount + " 名在岗/启用人员，请先迁移人员归属再停用");
        }
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        node.setIsEnabled(false);
        node.setUpdatedBy(uname);
        node = nodeRepo.save(node);
        changeLogService.log("ORG_NODE", node.getId(), "DISABLE", uid, uname, "停用组织节点: " + node.getName(), null);
        return OrgNodeDto.from(node);
    }

    /** Enable node. Blocked if parent is disabled. */
    @Transactional
    public OrgNodeDto enable(Long id) {
        OrgNodeEntity node = getById(id);
        if (node.getParentId() != null) {
            OrgNodeEntity parent = getById(node.getParentId());
            if (!Boolean.TRUE.equals(parent.getIsEnabled())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "父节点已停用，请先启用父节点");
            }
        }
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        node.setIsEnabled(true);
        node.setUpdatedBy(uname);
        node = nodeRepo.save(node);
        changeLogService.log("ORG_NODE", node.getId(), "ENABLE", uid, uname, "启用组织节点: " + node.getName(), null);
        return OrgNodeDto.from(node);
    }
}
