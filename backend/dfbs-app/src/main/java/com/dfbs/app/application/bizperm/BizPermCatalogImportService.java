package com.dfbs.app.application.bizperm;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.dfbs.app.application.perm.PermAuditService;
import com.dfbs.app.application.perm.PermPermissionTreeService;
import com.dfbs.app.interfaces.bizperm.BizPermCatalogImportDto;
import com.dfbs.app.interfaces.bizperm.BizPermCatalogImportDto.ApplySummary;
import com.dfbs.app.interfaces.bizperm.BizPermCatalogImportDto.ImportApplyResponse;
import com.dfbs.app.interfaces.bizperm.BizPermCatalogImportDto.ImportPreviewResponse;
import com.dfbs.app.interfaces.bizperm.BizPermCatalogImportDto.PreviewSummary;
import com.dfbs.app.interfaces.bizperm.BizPermCatalogImportDto.ValidationError;
import com.dfbs.app.modules.bizperm.BizPermCatalogNodeEntity;
import com.dfbs.app.modules.bizperm.BizPermCatalogNodeRepo;
import com.dfbs.app.modules.bizperm.BizPermOperationPointEntity;
import com.dfbs.app.modules.bizperm.BizPermOperationPointRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Import BizPerm Catalog from XLSX (preview + apply). Allowlist-only caller.
 * Format: Sheet 「节点」 节点ID,父节点ID,中文名称,排序. Sheet 「操作点」 操作点ID,节点ID(空=未归类),权限键,中文名称,排序,支持仅已处理(是/否).
 */
@Service
public class BizPermCatalogImportService {

    private static final String SHEET_NODES = "节点";
    private static final String SHEET_OPS = "操作点";
    private static final Pattern CHINESE = Pattern.compile(".*[\\u4e00-\\u9fff].*");

    private final BizPermCatalogNodeRepo nodeRepo;
    private final BizPermOperationPointRepo operationPointRepo;
    private final PermPermissionTreeService permissionTreeService;
    private final PermAuditService auditService;

    public BizPermCatalogImportService(BizPermCatalogNodeRepo nodeRepo,
                                       BizPermOperationPointRepo operationPointRepo,
                                       PermPermissionTreeService permissionTreeService,
                                       PermAuditService auditService) {
        this.nodeRepo = nodeRepo;
        this.operationPointRepo = operationPointRepo;
        this.permissionTreeService = permissionTreeService;
        this.auditService = auditService;
    }

    public ImportPreviewResponse preview(MultipartFile file) {
        Parsed parsed = parse(file);
        if (!parsed.parseErrors.isEmpty()) {
            return new ImportPreviewResponse(false,
                    new PreviewSummary(parsed.nodeRows.size(), parsed.opRows.size(), parsed.parseErrors.size()),
                    parsed.parseErrors);
        }
        Set<Long> existingNodeIds = new HashSet<>();
        nodeRepo.findAll().forEach(n -> existingNodeIds.add(n.getId()));
        Map<String, BizPermOperationPointEntity> existingOpsByKey = new HashMap<>();
        operationPointRepo.findAll().forEach(o -> existingOpsByKey.put(o.getPermissionKey(), o));
        Set<String> universe = permissionTreeService.getAllPermissionKeys();

        List<ValidationError> errors = validate(parsed.nodeRows, parsed.opRows, existingNodeIds, existingOpsByKey, universe);
        return new ImportPreviewResponse(
                errors.isEmpty(),
                new PreviewSummary(parsed.nodeRows.size(), parsed.opRows.size(), errors.size()),
                errors);
    }

    @Transactional
    public ImportApplyResponse apply(MultipartFile file) {
        Parsed parsed = parse(file);
        if (!parsed.parseErrors.isEmpty()) {
            throw new BizPermCatalogException("BIZPERM_IMPORT_PARSE", "解析失败：" + parsed.parseErrors.get(0).message());
        }
        Set<Long> existingNodeIds = new HashSet<>();
        nodeRepo.findAll().forEach(n -> existingNodeIds.add(n.getId()));
        Map<String, BizPermOperationPointEntity> existingOpsByKey = new HashMap<>();
        operationPointRepo.findAll().forEach(o -> existingOpsByKey.put(o.getPermissionKey(), o));
        Set<String> universe = permissionTreeService.getAllPermissionKeys();

        List<ValidationError> errors = validate(parsed.nodeRows, parsed.opRows, existingNodeIds, existingOpsByKey, universe);
        if (!errors.isEmpty()) {
            throw new BizPermCatalogImportException(errors);
        }

        int nodesUpdated = 0;
        for (int i = 0; i < parsed.nodeRows.size(); i++) {
            CatalogNodeExportRow r = parsed.nodeRows.get(i);
            BizPermCatalogNodeEntity e = nodeRepo.findById(r.getId()).orElse(null);
            if (e == null) continue;
            e.setCnName(normalizeCnName(r.getCnName()));
            e.setParentId(r.getParentId());
            e.setSortOrder(normalizeSortFromObject(r.getSortOrder()));
            e.setUpdatedAt(Instant.now());
            nodeRepo.save(e);
            nodesUpdated++;
        }

        int opsCreated = 0, opsUpdated = 0;
        for (CatalogOpPointExportRow r : parsed.opRows) {
            boolean created;
            BizPermOperationPointEntity e = operationPointRepo.findByPermissionKey(r.getPermissionKey()).orElse(null);
            if (e == null) {
                e = new BizPermOperationPointEntity();
                e.setPermissionKey(r.getPermissionKey());
                e.setCreatedAt(Instant.now());
                created = true;
            } else {
                created = false;
            }
            e.setNodeId(r.getNodeId());
            e.setCnName(r.getCnName() != null && !r.getCnName().isBlank() ? r.getCnName().trim() : null);
            e.setSortOrder(normalizeSortFromObject(r.getSortOrder()));
            e.setHandledOnly(normalizeHandledOnly(r.getHandledOnlyYesNo()));
            e.setUpdatedAt(Instant.now());
            operationPointRepo.save(e);
            if (created) opsCreated++; else opsUpdated++;
        }

        auditService.log(PermAuditService.ACTION_BIZPERM_CATALOG_IMPORT, PermAuditService.TARGET_SYSTEM, null, null,
                "导入业务模块目录：节点" + nodesUpdated + "、操作点新建" + opsCreated + "、更新" + opsUpdated);
        return new ImportApplyResponse(new ApplySummary(nodesUpdated, opsCreated, opsUpdated));
    }

    private Parsed parse(MultipartFile file) {
        Parsed parsed = new Parsed();
        if (file == null || file.isEmpty()) {
            parsed.parseErrors.add(new ValidationError("", 0, "", "未上传文件"));
            return parsed;
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            parsed.parseErrors.add(new ValidationError("", 0, "", "读取文件失败"));
            return parsed;
        }
        try {
            List<CatalogNodeExportRow> nodeRows = new ArrayList<>();
            EasyExcel.read(new ByteArrayInputStream(bytes), CatalogNodeExportRow.class, new ReadListener<CatalogNodeExportRow>() {
                @Override
                public void invoke(CatalogNodeExportRow data, AnalysisContext context) {
                    if (data != null) nodeRows.add(data);
                }
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {}
            }).sheet(0).doRead();
            parsed.nodeRows = nodeRows;

            List<CatalogOpPointExportRow> opRows = new ArrayList<>();
            EasyExcel.read(new ByteArrayInputStream(bytes), CatalogOpPointExportRow.class, new ReadListener<CatalogOpPointExportRow>() {
                @Override
                public void invoke(CatalogOpPointExportRow data, AnalysisContext context) {
                    if (data != null) opRows.add(data);
                }
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {}
            }).sheet(1).doRead();
            parsed.opRows = opRows;
        } catch (Exception ex) {
            parsed.parseErrors.add(new ValidationError("", 0, "", "文件格式错误或缺少工作表", "BIZPERM_IMPORT_PARSE"));
        }
        return parsed;
    }

    private List<ValidationError> validate(List<CatalogNodeExportRow> nodeRows, List<CatalogOpPointExportRow> opRows,
                                          Set<Long> existingNodeIds, Map<String, BizPermOperationPointEntity> existingOpsByKey,
                                          Set<String> universe) {
        List<ValidationError> errors = new ArrayList<>();
        Set<Long> seenNodeIds = new HashSet<>();
        Map<Long, Long> tentativeParent = new HashMap<>();

        for (int i = 0; i < nodeRows.size(); i++) {
            CatalogNodeExportRow r = nodeRows.get(i);
            int rowNum = i + 2;
            if (r.getId() == null) {
                errors.add(new ValidationError(SHEET_NODES, rowNum, "节点ID", "节点ID必填"));
                continue;
            }
            if (!existingNodeIds.contains(r.getId())) {
                errors.add(new ValidationError(SHEET_NODES, rowNum, "节点ID", "节点ID不存在于系统中（本期仅支持更新已有节点）", "BIZPERM_NODE_NOT_FOUND"));
            }
            if (seenNodeIds.contains(r.getId())) {
                errors.add(new ValidationError(SHEET_NODES, rowNum, "节点ID", "节点ID重复"));
            }
            seenNodeIds.add(r.getId());
            if (r.getParentId() != null) {
                if (!existingNodeIds.contains(r.getParentId())) {
                    errors.add(new ValidationError(SHEET_NODES, rowNum, "父节点ID", "父节点不存在"));
                } else if (r.getParentId().equals(r.getId())) {
                    errors.add(new ValidationError(SHEET_NODES, rowNum, "父节点ID", "不能将节点设为自己的父节点"));
                } else {
                    tentativeParent.put(r.getId(), r.getParentId());
                }
            } else {
                tentativeParent.put(r.getId(), null);
            }
            if (r.getCnName() == null || r.getCnName().isBlank()) {
                errors.add(new ValidationError(SHEET_NODES, rowNum, "中文名称", "中文名称必填"));
            } else if (!CHINESE.matcher(r.getCnName()).matches()) {
                errors.add(new ValidationError(SHEET_NODES, rowNum, "中文名称", "中文名称须包含中文字符"));
            }
            int sort = normalizeSortFromObject(r.getSortOrder());
            if (sort < 0) {
                errors.add(new ValidationError(SHEET_NODES, rowNum, "排序", "排序须为大于等于0的整数"));
            }
        }

        for (Long nodeId : tentativeParent.keySet()) {
            if (hasCycle(nodeId, tentativeParent, new HashSet<>())) {
                errors.add(new ValidationError(SHEET_NODES, 0, "父节点ID", "父节点关系存在环"));
                break;
            }
        }

        Set<String> seenOpKeys = new HashSet<>();
        for (int i = 0; i < opRows.size(); i++) {
            CatalogOpPointExportRow r = opRows.get(i);
            int rowNum = i + 2;
            if (r.getPermissionKey() == null || r.getPermissionKey().isBlank()) {
                errors.add(new ValidationError(SHEET_OPS, rowNum, "权限键", "权限键必填"));
                continue;
            }
            String key = r.getPermissionKey().trim();
            if (!universe.contains(key)) {
                errors.add(new ValidationError(SHEET_OPS, rowNum, "权限键", "权限键不在权限树中", "BIZPERM_PERMISSION_KEY_NOT_FOUND"));
            }
            if (seenOpKeys.contains(key)) {
                errors.add(new ValidationError(SHEET_OPS, rowNum, "权限键", "权限键重复"));
            }
            seenOpKeys.add(key);
            if (r.getNodeId() != null && !existingNodeIds.contains(r.getNodeId())) {
                errors.add(new ValidationError(SHEET_OPS, rowNum, "节点ID(空=未归类)", "节点不存在"));
            }
            if (normalizeSortFromObject(r.getSortOrder()) < 0) {
                errors.add(new ValidationError(SHEET_OPS, rowNum, "排序", "排序须为大于等于0的整数"));
            }
            if (r.getHandledOnlyYesNo() != null && !r.getHandledOnlyYesNo().isBlank()) {
                Boolean h = parseHandledOnly(r.getHandledOnlyYesNo().trim());
                if (h == null) {
                    errors.add(new ValidationError(SHEET_OPS, rowNum, "支持仅已处理(是/否)", "须为「是」或「否」"));
                }
            }
            if (r.getId() != null) {
                BizPermOperationPointEntity existing = existingOpsByKey.get(key);
                if (existing != null && !existing.getId().equals(r.getId())) {
                    errors.add(new ValidationError(SHEET_OPS, rowNum, "操作点ID", "操作点ID与系统中该权限键的ID不一致，请勿修改导出的操作点ID"));
                }
            }
        }

        return errors;
    }

    private boolean hasCycle(Long start, Map<Long, Long> parentMap, Set<Long> visiting) {
        if (visiting.contains(start)) return true;
        visiting.add(start);
        Long p = parentMap.get(start);
        if (p != null && hasCycle(p, parentMap, visiting)) return true;
        visiting.remove(start);
        return false;
    }

    private static int normalizeSortFromObject(Object sortOrder) {
        if (sortOrder == null) return 0;
        if (sortOrder instanceof Number n) {
            int v = n.intValue();
            return v >= 0 ? v : -1;
        }
        return 0;
    }

    private static String normalizeCnName(String s) {
        return s != null && !s.isBlank() ? s.trim() : "";
    }

    private static Boolean parseHandledOnly(String v) {
        if ("是".equals(v) || "true".equalsIgnoreCase(v) || "1".equals(v)) return true;
        if ("否".equals(v) || "false".equalsIgnoreCase(v) || "0".equals(v)) return false;
        return null;
    }

    private static boolean normalizeHandledOnly(String v) {
        if (v == null || v.isBlank()) return false;
        Boolean b = parseHandledOnly(v.trim());
        return b != null && b;
    }

    private static class Parsed {
        List<CatalogNodeExportRow> nodeRows = List.of();
        List<CatalogOpPointExportRow> opRows = List.of();
        List<ValidationError> parseErrors = new ArrayList<>();
    }

    /** Thrown when validation fails on apply; controller maps to 400 with errors. */
    public static class BizPermCatalogImportException extends RuntimeException {
        private final List<ValidationError> errors;

        public BizPermCatalogImportException(List<ValidationError> errors) {
            super("导入校验未通过");
            this.errors = errors;
        }

        public List<ValidationError> getErrors() {
            return errors;
        }
    }
}
