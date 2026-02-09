package com.dfbs.app.application.orgstructure;

import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.orgstructure.OrgLevelEntity;
import com.dfbs.app.modules.orgstructure.OrgLevelPositionTemplateEntity;
import com.dfbs.app.modules.orgstructure.OrgLevelPositionTemplateRepo;
import com.dfbs.app.modules.orgstructure.OrgLevelRepo;
import com.dfbs.app.modules.orgstructure.OrgNodeEntity;
import com.dfbs.app.modules.orgstructure.OrgNodeRepo;
import com.dfbs.app.modules.orgstructure.OrgPositionCatalogEntity;
import com.dfbs.app.modules.orgstructure.OrgPositionCatalogRepo;
import com.dfbs.app.modules.orgstructure.PersonAffiliationRepo;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final EntityManager entityManager;
    private final OrgLevelPositionTemplateRepo levelPositionTemplateRepo;
    private final OrgPositionCatalogRepo positionCatalogRepo;

    public OrgLevelService(OrgLevelRepo levelRepo, OrgNodeRepo nodeRepo,
                           PersonAffiliationRepo affiliationRepo,
                           OrgChangeLogService changeLogService,
                           CurrentUserIdResolver userIdResolver,
                           EntityManager entityManager,
                           OrgLevelPositionTemplateRepo levelPositionTemplateRepo,
                           OrgPositionCatalogRepo positionCatalogRepo) {
        this.levelRepo = levelRepo;
        this.nodeRepo = nodeRepo;
        this.affiliationRepo = affiliationRepo;
        this.changeLogService = changeLogService;
        this.userIdResolver = userIdResolver;
        this.entityManager = entityManager;
        this.levelPositionTemplateRepo = levelPositionTemplateRepo;
        this.positionCatalogRepo = positionCatalogRepo;
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

    /**
     * Create a new configurable level at position K (2..8). Two-phase shift to avoid unique constraint:
     * (1) offset rows with orderIndex >= K by +1000, (2) insert new at K, (3) set offset rows to order_index - 999.
     */
    @Transactional
    public OrgLevelEntity create(Integer orderIndex, String displayName) {
        if (COMPANY_LEVEL_DISPLAY_NAME.equals(displayName != null ? displayName.trim() : null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不可创建系统固定层级「公司」");
        }
        int K = orderIndex != null ? orderIndex : 2;
        if (K < 2 || K > MAX_LEVELS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "层级顺序必须在 2 到 " + MAX_LEVELS + " 之间（公司固定为第1层）");
        }
        List<OrgLevelEntity> configurable = listConfigurableOrdered();
        if (configurable.size() >= MAX_LEVELS - 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "可配置层级已满（公司 + 最多7个可配置层级 = 8层），请先停用或删除层级后再插入");
        }
        int maxOrder = configurable.stream()
                .mapToInt(l -> l.getOrderIndex() != null ? l.getOrderIndex() : 0)
                .max()
                .orElse(0);
        if (maxOrder >= MAX_LEVELS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "层级最多 8 层，请先调整/停用后再插入");
        }
        final int tempOffset = 1000;
        levelRepo.addOrderIndexOffsetWhereGte(K, tempOffset, COMPANY_LEVEL_DISPLAY_NAME);
        entityManager.flush();
        OrgLevelEntity e = new OrgLevelEntity();
        e.setOrderIndex(K);
        e.setDisplayName(displayName != null ? displayName.trim() : "");
        e.setIsEnabled(true);
        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        e.setCreatedBy(uname);
        e.setUpdatedBy(uname);
        e = levelRepo.save(e);
        entityManager.flush();
        levelRepo.addOrderIndexOffsetWhereGte(1000, -999, COMPANY_LEVEL_DISPLAY_NAME);
        entityManager.flush();
        assertNoDuplicateOrderIndex();
        changeLogService.log("LEVEL", e.getId(), "CREATE", uid, uname,
                "新建层级: " + e.getDisplayName(), null);
        return e;
    }

    /** Assert no duplicate orderIndex among configurable levels; throw and rollback if any. */
    private void assertNoDuplicateOrderIndex() {
        List<OrgLevelEntity> configurable = listConfigurableOrdered();
        long distinct = configurable.stream().map(OrgLevelEntity::getOrderIndex).distinct().count();
        if (distinct != configurable.size()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "层级顺序出现重复，请重试或联系管理员");
        }
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
            long personCount = nodeIds.isEmpty() ? 0 : affiliationRepo.countDistinctActivePersonIdByOrgNodeIdIn(nodeIds);
            if (nodeCount > 0 || personCount > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "先迁移组织节点/人员归属再停用。当前该层级下组织节点数: " + nodeCount + "，在岗/启用人员数: " + personCount);
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

    /**
     * Reorder configurable levels by assigning orderIndex 2, 3, ... in the given id order.
     * Uses a single bulk UPDATE with CASE so order_index is always 2..8 (no temp values).
     * orderedIds must equal the FULL set of configurable level IDs (no missing, no extra).
     */
    @Transactional
    public List<OrgLevelEntity> reorder(List<Long> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            return listConfigurableOrdered();
        }
        List<OrgLevelEntity> allConfigurable = listConfigurableOrdered();
        Set<Long> configurableIds = allConfigurable.stream().map(OrgLevelEntity::getId).collect(Collectors.toSet());
        Set<Long> requested = new HashSet<>(orderedIds);
        if (orderedIds.size() > MAX_LEVELS - 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "可配置层级最多 " + (MAX_LEVELS - 1) + " 个（公司固定为第1层）");
        }
        List<Long> distinct = orderedIds.stream().distinct().toList();
        if (distinct.size() != orderedIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "层级ID不能重复");
        }
        List<OrgLevelEntity> levels = levelRepo.findAllById(orderedIds);
        if (levels.size() != orderedIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "存在无效的层级ID");
        }
        for (OrgLevelEntity l : levels) {
            if (COMPANY_LEVEL_DISPLAY_NAME.equals(l.getDisplayName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能对系统固定层级「公司」排序");
            }
        }
        if (!requested.equals(configurableIds)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "排序必须包含全部可配置层级ID，不能多也不能少");
        }
        java.util.Map<Long, Integer> oldOrder = levels.stream()
                .collect(Collectors.toMap(OrgLevelEntity::getId, e -> e.getOrderIndex() != null ? e.getOrderIndex() : 0));
        // Two-phase UPDATE to avoid Postgres unique constraint mid-statement: (1) offset all configurable +1000, (2) CASE to 2..N+1
        final int tempOffset = 1000;
        levelRepo.addOrderIndexOffset(tempOffset, COMPANY_LEVEL_DISPLAY_NAME);
        entityManager.flush();
        StringBuilder sql = new StringBuilder("UPDATE org_level SET order_index = CASE id ");
        for (int i = 0; i < orderedIds.size(); i++) {
            sql.append(" WHEN :id").append(i).append(" THEN ").append(2 + i);
        }
        sql.append(" END WHERE id IN :ids AND display_name <> :company");
        var q = entityManager.createNativeQuery(sql.toString());
        for (int i = 0; i < orderedIds.size(); i++) {
            q.setParameter("id" + i, orderedIds.get(i));
        }
        q.setParameter("ids", orderedIds);
        q.setParameter("company", COMPANY_LEVEL_DISPLAY_NAME);
        q.executeUpdate();
        entityManager.flush();
        entityManager.clear();
        List<OrgLevelEntity> after = listConfigurableOrdered();
        boolean anyOver1000 = after.stream().anyMatch(l -> l.getOrderIndex() != null && l.getOrderIndex() >= tempOffset);
        if (anyOver1000) {
            throw new IllegalStateException("Reorder left order_index >= " + tempOffset + "; data inconsistent");
        }

        Long uid = userIdResolver.getCurrentUserId();
        String uname = userIdResolver.getCurrentUserEntity().getNickname();
        if (uname == null) uname = "user-" + uid;
        List<String> parts = new java.util.ArrayList<>();
        for (int i = 0; i < orderedIds.size(); i++) {
            Long id = orderedIds.get(i);
            int newIdx = 2 + i;
            int oldIdx = oldOrder.getOrDefault(id, 0);
            String name = levels.stream().filter(l -> id.equals(l.getId())).findFirst().map(OrgLevelEntity::getDisplayName).orElse("?");
            parts.add(name + "(" + oldIdx + "→" + newIdx + ")");
        }
        String summary = String.join(", ", parts);
        if (summary.length() > 200) summary = summary.substring(0, 197) + "...";
        changeLogService.log("LEVEL", 0L, "UPDATE", uid, uname, "调整层级顺序: " + summary, null);
        return listConfigurableOrdered();
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

    /** Replace all levels with defaults (公司=1, 本部=2, 部=3, 课=4, 系=5, 班=6). Only when canResetLevels(). */
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

        levelRepo.deleteAllInBatch();
        entityManager.flush();
        entityManager.clear();

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
        restoreDefaultLevelTemplates(created);
        return created;
    }

    /** Rebuild org_level_position_template for default levels (same mapping as V0064 seed). Call after resetLevelsToDefault. */
    private void restoreDefaultLevelTemplates(List<OrgLevelEntity> defaultLevels) {
        List<OrgPositionCatalogEntity> allPositions = positionCatalogRepo.findByIsEnabledTrueOrderByOrderIndexAsc();
        for (OrgLevelEntity level : defaultLevels) {
            Long levelId = level.getId();
            String displayName = level.getDisplayName();
            List<OrgPositionCatalogEntity> positions = switch (displayName) {
                case "公司" -> allPositions.stream()
                        .filter(p -> "总经理".equals(p.getBaseName()) || "职员".equals(p.getBaseName()))
                        .toList();
                case "本部" -> allPositions.stream()
                        .filter(p -> ("本部长".equals(p.getBaseName()) && List.of("PRIMARY", "DEPUTY", "ACTING").contains(p.getGrade())) || "职员".equals(p.getBaseName()))
                        .toList();
                case "部" -> allPositions.stream()
                        .filter(p -> ("部长".equals(p.getBaseName()) && List.of("PRIMARY", "DEPUTY", "ACTING").contains(p.getGrade())) || "职员".equals(p.getBaseName()))
                        .toList();
                case "课" -> allPositions.stream()
                        .filter(p -> ("课长".equals(p.getBaseName()) && List.of("PRIMARY", "DEPUTY", "ACTING").contains(p.getGrade())) || "职员".equals(p.getBaseName()))
                        .toList();
                case "系" -> allPositions.stream()
                        .filter(p -> ("系长".equals(p.getBaseName()) && List.of("PRIMARY", "DEPUTY", "ACTING").contains(p.getGrade())) || "职员".equals(p.getBaseName()))
                        .toList();
                case "班" -> allPositions.stream()
                        .filter(p -> ("班长".equals(p.getBaseName()) && List.of("PRIMARY", "DEPUTY", "ACTING").contains(p.getGrade())) || "职员".equals(p.getBaseName()))
                        .toList();
                default -> List.of();
            };
            for (OrgPositionCatalogEntity pos : positions) {
                OrgLevelPositionTemplateEntity t = new OrgLevelPositionTemplateEntity();
                t.setLevelId(levelId);
                t.setPositionId(pos.getId());
                t.setIsEnabled(true);
                levelPositionTemplateRepo.save(t);
            }
        }
    }
}
