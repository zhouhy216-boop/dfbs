package com.dfbs.app.application.bizperm;

import com.dfbs.app.application.perm.PermAuditService;
import com.dfbs.app.application.perm.PermPermissionTreeService;
import com.dfbs.app.interfaces.bizperm.BizPermCatalogDto;
import com.dfbs.app.modules.bizperm.BizPermCatalogNodeEntity;
import com.dfbs.app.modules.bizperm.BizPermCatalogNodeRepo;
import com.dfbs.app.modules.bizperm.BizPermOperationPointEntity;
import com.dfbs.app.modules.bizperm.BizPermOperationPointRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Business Module Catalog: persistent CN tree + operation points (ordering, handled-only).
 * Unclassified = operation points with node_id IS NULL. No auto semantic mapping.
 */
@Service
public class BizPermCatalogService {

    private static final List<String> SKELETON_TOP_LEVEL = List.of(
            "报价单", "物流管理", "售后服务", "财务", "库存管理", "主数据", "平台&网卡管理", "系统"
    );

    private final BizPermCatalogNodeRepo nodeRepo;
    private final BizPermOperationPointRepo operationPointRepo;
    private final PermPermissionTreeService permissionTreeService;
    private final PermAuditService auditService;

    public BizPermCatalogService(BizPermCatalogNodeRepo nodeRepo,
                                 BizPermOperationPointRepo operationPointRepo,
                                 PermPermissionTreeService permissionTreeService,
                                 PermAuditService auditService) {
        this.nodeRepo = nodeRepo;
        this.operationPointRepo = operationPointRepo;
        this.permissionTreeService = permissionTreeService;
        this.auditService = auditService;
    }

    private Set<String> getPermissionUniverse() {
        return permissionTreeService.getAllPermissionKeys();
    }

    private void validatePermissionKey(String permissionKey) {
        if (!getPermissionUniverse().contains(permissionKey)) {
            throw new BizPermCatalogException(BizPermCatalogException.PERMISSION_KEY_NOT_FOUND, "权限键不在当前权限树中");
        }
    }

    /**
     * Returns catalog tree + unclassified list. Seeds minimal CN skeleton if no nodes exist.
     */
    @Transactional(readOnly = true)
    public BizPermCatalogDto.CatalogResponse getCatalog() {
        seedSkeletonIfEmpty();
        List<BizPermCatalogNodeEntity> allNodes = nodeRepo.findAllByOrderBySortOrderAscIdAsc();
        List<BizPermOperationPointEntity> allOps = operationPointRepo.findAll();

        Map<Long, List<BizPermOperationPointEntity>> opsByNodeId = allOps.stream()
                .filter(op -> op.getNodeId() != null)
                .collect(Collectors.groupingBy(BizPermOperationPointEntity::getNodeId));

        List<BizPermCatalogDto.OpPoint> persistedUnclassified = operationPointRepo.findByNodeIdIsNullOrderBySortOrderAscIdAsc()
                .stream()
                .map(this::toOpPoint)
                .toList();
        Set<String> persistedKeys = allOps.stream().map(BizPermOperationPointEntity::getPermissionKey).collect(Collectors.toSet());
        Set<String> universe = getPermissionUniverse();
        List<String> missingKeys = new ArrayList<>(new TreeSet<>(universe));
        missingKeys.removeAll(persistedKeys);
        List<BizPermCatalogDto.OpPoint> computedUnclassified = missingKeys.stream()
                .map(k -> new BizPermCatalogDto.OpPoint(null, k, null, 0, false))
                .toList();
        List<BizPermCatalogDto.OpPoint> unclassified = new ArrayList<>(persistedUnclassified);
        unclassified.addAll(computedUnclassified);

        List<BizPermCatalogNodeEntity> roots = allNodes.stream()
                .filter(n -> n.getParentId() == null)
                .sorted(Comparator.comparing(BizPermCatalogNodeEntity::getSortOrder).thenComparing(BizPermCatalogNodeEntity::getId))
                .toList();

        List<BizPermCatalogDto.CatalogNode> tree = roots.stream()
                .map(root -> toCatalogNode(root, allNodes, opsByNodeId))
                .toList();

        return new BizPermCatalogDto.CatalogResponse(tree, unclassified);
    }

    /**
     * If no catalog nodes exist, insert minimal CN skeleton (top-level groups + placeholder child each).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seedSkeletonIfEmpty() {
        if (nodeRepo.count() > 0) {
            return;
        }
        int order = 0;
        for (String cnName : SKELETON_TOP_LEVEL) {
            BizPermCatalogNodeEntity parent = new BizPermCatalogNodeEntity();
            parent.setCnName(cnName);
            parent.setParentId(null);
            parent.setSortOrder(order++);
            parent.setCreatedAt(Instant.now());
            parent.setUpdatedAt(Instant.now());
            parent = nodeRepo.save(parent);

            BizPermCatalogNodeEntity child = new BizPermCatalogNodeEntity();
            child.setCnName("（占位）");
            child.setParentId(parent.getId());
            child.setSortOrder(0);
            child.setCreatedAt(Instant.now());
            child.setUpdatedAt(Instant.now());
            nodeRepo.save(child);
        }
    }

    @Transactional
    public BizPermCatalogNodeEntity createNode(BizPermCatalogDto.CreateNodeRequest req) {
        if (req.cnName() == null || req.cnName().isBlank()) {
            throw new BizPermCatalogException("BIZPERM_INVALID_REQUEST", "中文名称必填");
        }
        Long parentId = req.parentId();
        if (parentId != null && nodeRepo.findById(parentId).isEmpty()) {
            throw new BizPermCatalogException(BizPermCatalogException.NODE_NOT_FOUND, "父节点不存在");
        }
        int sortOrder = req.sortOrder() != null ? req.sortOrder() : nextSortOrderForParent(parentId);
        BizPermCatalogNodeEntity entity = new BizPermCatalogNodeEntity();
        entity.setCnName(req.cnName().trim());
        entity.setParentId(parentId);
        entity.setSortOrder(sortOrder);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity = nodeRepo.save(entity);
        auditService.log(PermAuditService.ACTION_BIZPERM_CATALOG_NODE_CREATE, PermAuditService.TARGET_SYSTEM, entity.getId(), null, "节点：" + entity.getCnName());
        return entity;
    }

    private int nextSortOrderForParent(Long parentId) {
        List<BizPermCatalogNodeEntity> siblings = parentId == null
                ? nodeRepo.findAllByOrderBySortOrderAscIdAsc().stream().filter(n -> n.getParentId() == null).toList()
                : nodeRepo.findByParentIdOrderBySortOrderAscIdAsc(parentId);
        return siblings.stream().mapToInt(n -> n.getSortOrder() != null ? n.getSortOrder() : 0).max().orElse(-1) + 1;
    }

    @Transactional
    public BizPermCatalogNodeEntity updateNode(Long id, BizPermCatalogDto.NodeUpdateRequest req) {
        BizPermCatalogNodeEntity entity = nodeRepo.findById(id).orElseThrow(() ->
                new BizPermCatalogException(BizPermCatalogException.NODE_NOT_FOUND, "节点不存在"));
        if (req.cnName() != null && !req.cnName().isBlank()) {
            entity.setCnName(req.cnName().trim());
        }
        if (req.parentId() != null) {
            if (req.parentId().equals(id)) {
                throw new BizPermCatalogException("BIZPERM_INVALID_REQUEST", "不能将节点设为自己父节点");
            }
            if (nodeRepo.findById(req.parentId()).isEmpty()) {
                throw new BizPermCatalogException(BizPermCatalogException.NODE_NOT_FOUND, "父节点不存在");
            }
            if (isDescendant(req.parentId(), id)) {
                throw new BizPermCatalogException("BIZPERM_INVALID_REQUEST", "不能将节点移动到其下级");
            }
            entity.setParentId(req.parentId());
        }
        if (req.sortOrder() != null) {
            entity.setSortOrder(req.sortOrder());
        }
        entity.setUpdatedAt(Instant.now());
        entity = nodeRepo.save(entity);
        auditService.log(PermAuditService.ACTION_BIZPERM_CATALOG_NODE_UPDATE, PermAuditService.TARGET_SYSTEM, entity.getId(), null, "节点：" + entity.getCnName());
        return entity;
    }

    private boolean isDescendant(Long candidateAncestorId, Long nodeId) {
        BizPermCatalogNodeEntity current = nodeRepo.findById(nodeId).orElse(null);
        while (current != null && current.getParentId() != null) {
            if (current.getParentId().equals(candidateAncestorId)) return true;
            current = nodeRepo.findById(current.getParentId()).orElse(null);
        }
        return false;
    }

    @Transactional
    public void deleteNode(Long id) {
        BizPermCatalogNodeEntity entity = nodeRepo.findById(id).orElseThrow(() ->
                new BizPermCatalogException(BizPermCatalogException.NODE_NOT_FOUND, "节点不存在"));
        if (!nodeRepo.findByParentIdOrderBySortOrderAscIdAsc(id).isEmpty()) {
            throw new BizPermCatalogException(BizPermCatalogException.NODE_HAS_CHILDREN_OR_OPS, "存在子节点，无法删除");
        }
        if (operationPointRepo.countByNodeId(id) > 0) {
            throw new BizPermCatalogException(BizPermCatalogException.NODE_HAS_CHILDREN_OR_OPS, "该节点下存在操作点，无法删除");
        }
        String cnName = entity.getCnName();
        nodeRepo.delete(entity);
        auditService.log(PermAuditService.ACTION_BIZPERM_CATALOG_NODE_DELETE, PermAuditService.TARGET_SYSTEM, id, null, "已删除节点：" + cnName);
    }

    @Transactional
    public void reorderChildren(Long nodeId, List<Long> orderedIds) {
        BizPermCatalogNodeEntity parent = nodeRepo.findById(nodeId).orElseThrow(() ->
                new BizPermCatalogException(BizPermCatalogException.NODE_NOT_FOUND, "节点不存在"));
        List<BizPermCatalogNodeEntity> children = nodeRepo.findByParentIdOrderBySortOrderAscIdAsc(nodeId);
        if (children.size() != orderedIds.size() || !children.stream().map(BizPermCatalogNodeEntity::getId).collect(Collectors.toSet()).containsAll(orderedIds)) {
            throw new BizPermCatalogException(BizPermCatalogException.REORDER_IDS_MISMATCH, "排序ID与当前子节点不一致");
        }
        Instant now = Instant.now();
        for (int i = 0; i < orderedIds.size(); i++) {
            Long id = orderedIds.get(i);
            BizPermCatalogNodeEntity child = children.stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
            child.setSortOrder(i);
            child.setUpdatedAt(now);
            nodeRepo.save(child);
        }
        auditService.log(PermAuditService.ACTION_BIZPERM_CATALOG_NODE_REORDER, PermAuditService.TARGET_SYSTEM, nodeId, null, "子节点已重排");
    }

    @Transactional
    public BizPermOperationPointEntity upsertOpPoint(BizPermCatalogDto.OpPointUpsertRequest req) {
        if (req.permissionKey() == null || req.permissionKey().isBlank()) {
            throw new BizPermCatalogException("BIZPERM_INVALID_REQUEST", "权限键必填");
        }
        validatePermissionKey(req.permissionKey().trim());
        if (req.nodeId() != null && nodeRepo.findById(req.nodeId()).isEmpty()) {
            throw new BizPermCatalogException(BizPermCatalogException.NODE_NOT_FOUND, "目标节点不存在");
        }
        BizPermOperationPointEntity entity = operationPointRepo.findByPermissionKey(req.permissionKey().trim()).orElse(null);
        boolean isCreate = entity == null;
        if (entity == null) {
            entity = new BizPermOperationPointEntity();
            entity.setPermissionKey(req.permissionKey().trim());
            entity.setCreatedAt(Instant.now());
        }
        if (req.cnName() != null) entity.setCnName(req.cnName().trim().isEmpty() ? null : req.cnName().trim());
        if (req.sortOrder() != null) entity.setSortOrder(req.sortOrder());
        if (req.handledOnly() != null) entity.setHandledOnly(req.handledOnly());
        entity.setNodeId(req.nodeId());
        entity.setUpdatedAt(Instant.now());
        entity = operationPointRepo.save(entity);
        String action = isCreate ? PermAuditService.ACTION_BIZPERM_OP_CREATE : PermAuditService.ACTION_BIZPERM_OP_UPDATE;
        auditService.log(action, PermAuditService.TARGET_SYSTEM, entity.getId(), entity.getPermissionKey(), "操作点：" + entity.getPermissionKey());
        return entity;
    }

    @Transactional
    public BizPermOperationPointEntity updateOpPoint(Long id, BizPermCatalogDto.OpPointUpdateRequest req) {
        BizPermOperationPointEntity entity = operationPointRepo.findById(id).orElseThrow(() ->
                new BizPermCatalogException(BizPermCatalogException.OP_POINT_NOT_FOUND, "操作点不存在"));
        if (req.cnName() != null) entity.setCnName(req.cnName().trim().isEmpty() ? null : req.cnName().trim());
        if (req.sortOrder() != null) entity.setSortOrder(req.sortOrder());
        if (req.handledOnly() != null) entity.setHandledOnly(req.handledOnly());
        if (req.nodeId() != null) {
            if (nodeRepo.findById(req.nodeId()).isEmpty()) {
                throw new BizPermCatalogException(BizPermCatalogException.NODE_NOT_FOUND, "目标节点不存在");
            }
            entity.setNodeId(req.nodeId());
        }
        entity.setUpdatedAt(Instant.now());
        entity = operationPointRepo.save(entity);
        auditService.log(PermAuditService.ACTION_BIZPERM_OP_UPDATE, PermAuditService.TARGET_SYSTEM, entity.getId(), entity.getPermissionKey(), "操作点已更新");
        return entity;
    }

    @Transactional
    public void claimOpPoints(BizPermCatalogDto.ClaimOpPointsRequest req) {
        if (req.nodeId() == null || req.permissionKeys() == null || req.permissionKeys().isEmpty()) {
            throw new BizPermCatalogException("BIZPERM_INVALID_REQUEST", "节点ID与权限键列表必填");
        }
        if (nodeRepo.findById(req.nodeId()).isEmpty()) {
            throw new BizPermCatalogException(BizPermCatalogException.NODE_NOT_FOUND, "目标节点不存在");
        }
        Set<String> universe = getPermissionUniverse();
        for (String key : req.permissionKeys()) {
            if (!universe.contains(key)) {
                throw new BizPermCatalogException(BizPermCatalogException.PERMISSION_KEY_NOT_FOUND, "权限键不在权限树中：" + key);
            }
        }
        int claimed = 0;
        for (String key : req.permissionKeys()) {
            var op = operationPointRepo.findByPermissionKey(key);
            if (op.isPresent()) {
                BizPermOperationPointEntity e = op.get();
                e.setNodeId(req.nodeId());
                e.setUpdatedAt(Instant.now());
                operationPointRepo.save(e);
                claimed++;
            } else {
                BizPermOperationPointEntity created = new BizPermOperationPointEntity();
                created.setPermissionKey(key);
                created.setNodeId(req.nodeId());
                created.setCnName(null);
                created.setSortOrder(0);
                created.setHandledOnly(false);
                created.setCreatedAt(Instant.now());
                created.setUpdatedAt(Instant.now());
                operationPointRepo.save(created);
                claimed++;
            }
        }
        auditService.log(PermAuditService.ACTION_BIZPERM_OP_CLAIM, PermAuditService.TARGET_SYSTEM, req.nodeId(), null, "认领 " + claimed + " 个操作点至节点");
    }

    @Transactional
    public BizPermOperationPointEntity updateHandledOnly(Long id, boolean handledOnly) {
        BizPermOperationPointEntity entity = operationPointRepo.findById(id).orElseThrow(() ->
                new BizPermCatalogException(BizPermCatalogException.OP_POINT_NOT_FOUND, "操作点不存在"));
        entity.setHandledOnly(handledOnly);
        entity.setUpdatedAt(Instant.now());
        entity = operationPointRepo.save(entity);
        auditService.log(PermAuditService.ACTION_BIZPERM_OP_TOGGLE_HANDLED, PermAuditService.TARGET_SYSTEM, entity.getId(), entity.getPermissionKey(), handledOnly ? "仅已处理" : "取消仅已处理");
        return entity;
    }

    private BizPermCatalogDto.CatalogNode toCatalogNode(BizPermCatalogNodeEntity entity,
                                                        List<BizPermCatalogNodeEntity> allNodes,
                                                        Map<Long, List<BizPermOperationPointEntity>> opsByNodeId) {
        List<BizPermCatalogNodeEntity> childEntities = allNodes.stream()
                .filter(n -> entity.getId().equals(n.getParentId()))
                .sorted(Comparator.comparing(BizPermCatalogNodeEntity::getSortOrder).thenComparing(BizPermCatalogNodeEntity::getId))
                .toList();
        List<BizPermCatalogDto.CatalogNode> children = childEntities.stream()
                .map(c -> toCatalogNode(c, allNodes, opsByNodeId))
                .toList();
        List<BizPermCatalogDto.OpPoint> ops = opsByNodeId.getOrDefault(entity.getId(), List.of()).stream()
                .sorted(Comparator.comparing(BizPermOperationPointEntity::getSortOrder).thenComparing(BizPermOperationPointEntity::getId))
                .map(this::toOpPoint)
                .toList();
        return new BizPermCatalogDto.CatalogNode(
                entity.getId(),
                entity.getCnName(),
                entity.getSortOrder() != null ? entity.getSortOrder() : 0,
                children,
                ops
        );
    }

    private BizPermCatalogDto.OpPoint toOpPoint(BizPermOperationPointEntity e) {
        return new BizPermCatalogDto.OpPoint(
                e.getId(),
                e.getPermissionKey(),
                e.getCnName(),
                e.getSortOrder() != null ? e.getSortOrder() : 0,
                Boolean.TRUE.equals(e.getHandledOnly())
        );
    }
}
