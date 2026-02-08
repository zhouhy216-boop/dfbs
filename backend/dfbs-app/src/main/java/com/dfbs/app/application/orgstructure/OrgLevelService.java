package com.dfbs.app.application.orgstructure;

import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.orgstructure.OrgLevelEntity;
import com.dfbs.app.modules.orgstructure.OrgLevelRepo;
import com.dfbs.app.modules.orgstructure.OrgNodeEntity;
import com.dfbs.app.modules.orgstructure.OrgNodeRepo;
import com.dfbs.app.modules.orgstructure.PersonAffiliationRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrgLevelService {

    private static final int MAX_LEVELS = 8;

    /** System-fixed root level; not configurable (cannot rename/reorder/disable). */
    public static final String COMPANY_LEVEL_DISPLAY_NAME = "公司";

    private final OrgLevelRepo levelRepo;
    private final OrgNodeRepo nodeRepo;
    private final PersonAffiliationRepo affiliationRepo;
    private final OrgChangeLogService changeLogService;
    private final CurrentUserIdResolver userIdResolver;

    public OrgLevelService(OrgLevelRepo levelRepo, OrgNodeRepo nodeRepo,
                           PersonAffiliationRepo affiliationRepo,
                           OrgChangeLogService changeLogService,
                           CurrentUserIdResolver userIdResolver) {
        this.levelRepo = levelRepo;
        this.nodeRepo = nodeRepo;
        this.affiliationRepo = affiliationRepo;
        this.changeLogService = changeLogService;
        this.userIdResolver = userIdResolver;
    }

    public List<OrgLevelEntity> listOrdered() {
        return levelRepo.findAllByOrderByOrderIndexAsc();
    }

    /** Configurable levels only (excludes system-fixed 公司). For Level Config page. */
    public List<OrgLevelEntity> listConfigurableOrdered() {
        return levelRepo.findAllByOrderByOrderIndexAsc().stream()
                .filter(l -> !COMPANY_LEVEL_DISPLAY_NAME.equals(l.getDisplayName()))
                .toList();
    }

    /** Enabled levels only (for create-org-node level selector). */
    public List<OrgLevelEntity> listEnabledOrdered() {
        return levelRepo.findByIsEnabledTrueOrderByOrderIndexAsc();
    }

    @Transactional
    public OrgLevelEntity create(Integer orderIndex, String displayName) {
        if (COMPANY_LEVEL_DISPLAY_NAME.equals(displayName != null ? displayName.trim() : null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不可创建系统固定层级「公司」");
        }
        if (levelRepo.count() >= MAX_LEVELS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "层级数量已达上限（最多 " + MAX_LEVELS + " 个）");
        }
        OrgLevelEntity e = new OrgLevelEntity();
        e.setOrderIndex(orderIndex != null ? orderIndex : 0);
        e.setDisplayName(displayName);
        e.setIsEnabled(true);
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        e.setCreatedBy(uname);
        e.setUpdatedBy(uname);
        e = levelRepo.save(e);
        changeLogService.log("LEVEL", e.getId(), "CREATE", uid, uname,
                "新建层级: " + displayName, null);
        return e;
    }

    @Transactional
    public OrgLevelEntity update(Long id, Integer orderIndex, String displayName, Boolean isEnabled) {
        OrgLevelEntity e = levelRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "层级不存在"));
        if (COMPANY_LEVEL_DISPLAY_NAME.equals(e.getDisplayName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "公司为系统固定层级，不可修改、停用或删除");
        }
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;

        if (orderIndex != null) e.setOrderIndex(orderIndex);
        if (displayName != null && !displayName.isBlank()) e.setDisplayName(displayName.trim());

        if (Boolean.FALSE.equals(isEnabled)) {
            long nodeCount = nodeRepo.countByLevelId(id);
            List<Long> nodeIds = nodeRepo.findByLevelId(id).stream()
                    .map(OrgNodeEntity::getId)
                    .collect(Collectors.toList());
            long personCount = nodeIds.isEmpty() ? 0 : affiliationRepo.countDistinctPersonIdByOrgNodeIdIn(nodeIds);
            if (nodeCount > 0 || personCount > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "先迁移组织节点/人员归属再停用。当前该层级下组织节点数: " + nodeCount + "，关联人员数: " + personCount);
            }
            e.setIsEnabled(false);
        } else if (Boolean.TRUE.equals(isEnabled)) {
            e.setIsEnabled(true);
        }

        e.setUpdatedBy(uname);
        e = levelRepo.save(e);
        changeLogService.log("LEVEL", e.getId(), "UPDATE", uid, uname,
                "更新层级: " + e.getDisplayName(), null);
        return e;
    }

    public OrgLevelEntity getById(Long id) {
        return levelRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "层级不存在"));
    }

    private static final List<DefaultLevel> DEFAULT_LEVELS = List.of(
            new DefaultLevel(1, "公司"),
            new DefaultLevel(2, "本部"),
            new DefaultLevel(3, "部"),
            new DefaultLevel(4, "课"),
            new DefaultLevel(5, "系"),
            new DefaultLevel(6, "班")
    );

    private record DefaultLevel(int orderIndex, String displayName) {}

    /** True when no org nodes and no person affiliations (safe to replace all levels). */
    public boolean canResetLevels() {
        return nodeRepo.count() == 0 && affiliationRepo.count() == 0;
    }

    /** Full can-reset result for API: canReset, message, nodeCount, affiliationCount. */
    public Map<String, Object> getCanResetLevelsResult() {
        long nodeCount = nodeRepo.count();
        long affiliationCount = affiliationRepo.count();
        boolean can = nodeCount == 0 && affiliationCount == 0;
        Map<String, Object> result = new HashMap<>();
        result.put("canReset", can);
        result.put("nodeCount", nodeCount);
        result.put("affiliationCount", affiliationCount);
        result.put("message", can ? null : "已有组织节点/人员归属使用层级，需先迁移/清理后才能重置。组织节点数 " + nodeCount + "，人员归属数 " + affiliationCount);
        return result;
    }

    /** Replace all levels with defaults (公司/本部/部/课/系/班). Only when canResetLevels(). */
    @Transactional
    public List<OrgLevelEntity> resetLevelsToDefault() {
        long nodeCount = nodeRepo.count();
        long affiliationCount = affiliationRepo.count();
        if (nodeCount > 0 || affiliationCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "已有组织节点/人员归属使用层级，需先迁移/清理后才能重置。当前组织节点数: " + nodeCount + "，人员归属数: " + affiliationCount);
        }
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;

        levelRepo.deleteAll();
        List<OrgLevelEntity> created = new java.util.ArrayList<>();
        for (DefaultLevel d : DEFAULT_LEVELS) {
            OrgLevelEntity e = new OrgLevelEntity();
            e.setOrderIndex(d.orderIndex());
            e.setDisplayName(d.displayName());
            e.setIsEnabled(true);
            e.setCreatedBy(uname);
            e.setUpdatedBy(uname);
            created.add(levelRepo.save(e));
        }
        changeLogService.log("LEVEL", 0L, "UPDATE", uid, uname, "重置为默认层级: 公司/本部/部/课/系/班", null);
        return created;
    }
}
