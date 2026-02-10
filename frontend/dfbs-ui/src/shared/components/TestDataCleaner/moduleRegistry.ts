/**
 * v0 module registry for Test Data Cleaner.
 * Derived from App.tsx routes and BasicLayout.tsx MENU_ROUTES_BASE + ORG_STRUCTURE_MENU.
 */

export interface ModuleDef {
  id: string;
  label: string;
  /** Route prefixes that own this module (pathname.startsWith(prefix)); longest match wins. */
  routePrefixes: string[];
}

export interface ModuleGroupDef {
  id: string;
  label: string;
  modules: ModuleDef[];
}

/** Evidence-based: routes from App.tsx and menu labels from BasicLayout.tsx. Labels Chinese-only for UI. */
export const MODULE_GROUPS: ModuleGroupDef[] = [
  {
    id: 'core',
    label: '核心',
    modules: [
      { id: 'dashboard', label: '工作台', routePrefixes: ['/dashboard'] },
      { id: 'quotes', label: '报价单', routePrefixes: ['/quotes'] },
    ],
  },
  {
    id: 'logistics',
    label: '物流管理',
    modules: [
      { id: 'shipments', label: '发货列表', routePrefixes: ['/shipments'] },
      { id: 'after-sales', label: '运输异常', routePrefixes: ['/after-sales'] },
    ],
  },
  {
    id: 'after-sales-service',
    label: '售后服务',
    modules: [
      { id: 'work-orders', label: '工单管理', routePrefixes: ['/work-orders'] },
    ],
  },
  {
    id: 'finance',
    label: '财务',
    modules: [{ id: 'finance', label: '财务', routePrefixes: ['/finance'] }],
  },
  {
    id: 'warehouse',
    label: '仓库',
    modules: [
      { id: 'warehouse-inventory', label: '库存管理', routePrefixes: ['/warehouse/inventory'] },
      { id: 'warehouse-replenish', label: '补货审批', routePrefixes: ['/warehouse/replenish'] },
    ],
  },
  {
    id: 'import',
    label: '数据导入',
    modules: [{ id: 'import-center', label: '数据导入', routePrefixes: ['/import-center'] }],
  },
  {
    id: 'master-data',
    label: '主数据',
    modules: [
      { id: 'customers', label: '客户', routePrefixes: ['/customers'] },
      { id: 'contracts', label: '合同', routePrefixes: ['/master-data/contracts'] },
      { id: 'machines', label: '机器', routePrefixes: ['/master-data/machines'] },
      { id: 'machine-models', label: '机器型号', routePrefixes: ['/master-data/machine-models'] },
      { id: 'model-part-lists', label: '型号BOM', routePrefixes: ['/master-data/model-part-lists'] },
      { id: 'spare-parts', label: '零部件', routePrefixes: ['/master-data/spare-parts'] },
      { id: 'sim-cards', label: 'SIM卡', routePrefixes: ['/master-data/sim-cards'] },
    ],
  },
  {
    id: 'platform',
    label: '平台与网卡管理',
    modules: [
      { id: 'platform-orgs', label: '平台管理', routePrefixes: ['/platform/orgs'] },
      { id: 'platform-applications', label: '申请管理', routePrefixes: ['/platform/applications', '/platform/apply'] },
      { id: 'sim-applications', label: 'SIM管理', routePrefixes: ['/platform/sim-applications'] },
    ],
  },
  {
    id: 'admin',
    label: '系统',
    modules: [
      { id: 'confirmation-center', label: '数据确认中心', routePrefixes: ['/admin/confirmation-center'] },
      { id: 'platform-config', label: '平台配置', routePrefixes: ['/system/platform-config'] },
    ],
  },
  {
    id: 'org-structure',
    label: '组织架构（超管）',
    modules: [
      { id: 'org-levels', label: '层级配置', routePrefixes: ['/admin/org-levels'] },
      { id: 'org-tree', label: '组织架构', routePrefixes: ['/admin/org-tree'] },
      { id: 'org-change-logs', label: '变更记录', routePrefixes: ['/admin/org-change-logs'] },
    ],
  },
];

export const UNMAPPED_KEY = '__unmapped__';
const UNMAPPED_LABEL = '未知/未映射';

/** All modules flat for prefix matching (group id + module id for stable key). */
function getAllModules(): (ModuleDef & { groupId: string })[] {
  const out: (ModuleDef & { groupId: string })[] = [];
  for (const g of MODULE_GROUPS) {
    for (const m of g.modules) {
      out.push({ ...m, groupId: g.id });
    }
  }
  return out;
}

/**
 * Resolves which module(s) own the given pathname.
 * Uses longest matching route prefix. Returns one module or unmapped.
 */
export function resolveModulesByPath(pathname: string): { moduleKey: string; moduleLabel: string; groupLabel: string }[] {
  const normalized = pathname.startsWith('/') ? pathname : `/${pathname}`;
  const all = getAllModules();
  let best: { moduleKey: string; moduleLabel: string; groupLabel: string; len: number } | null = null;
  for (const m of all) {
    for (const prefix of m.routePrefixes) {
      const p = prefix.startsWith('/') ? prefix : `/${prefix}`;
      if (normalized === p || (p.length > 1 && normalized.startsWith(p + '/')) || normalized.startsWith(p)) {
        const matchLen = p.length;
        if (!best || matchLen > best.len) {
          best = { moduleKey: m.id, moduleLabel: m.label, groupLabel: MODULE_GROUPS.find((g) => g.id === m.groupId)?.label ?? m.groupId, len: matchLen };
        }
      }
    }
  }
  if (best) return [{ moduleKey: best.moduleKey, moduleLabel: best.moduleLabel, groupLabel: best.groupLabel }];
  return [{ moduleKey: UNMAPPED_KEY, moduleLabel: UNMAPPED_LABEL, groupLabel: UNMAPPED_LABEL }];
}

export function getUnmappedLabel(): string {
  return UNMAPPED_LABEL;
}

/** All selectable module ids (excludes UNMAPPED_KEY). */
export function getAllSelectableModuleIds(): string[] {
  return MODULE_GROUPS.flatMap((g) => g.modules.map((m) => m.id));
}

/** Get module label by id, or undefined if not found. */
export function getModuleLabelById(moduleId: string): string | undefined {
  for (const g of MODULE_GROUPS) {
    const m = g.modules.find((x) => x.id === moduleId);
    if (m) return m.label;
  }
  return undefined;
}

/** v0 dependency rulebook: moduleId -> list of required dependency module ids (must exist in registry). */
export const MODULE_DEPENDENCIES: Record<string, string[]> = {
  // v0: minimal/empty; add entries as needed, e.g. { 'quotes': ['contracts', 'customers'] }
};

export interface ResolveSelectionWithDepsResult {
  /** Selected ids plus transitive closure of dependencies. */
  effectiveIds: string[];
  /** Ids that were auto-added (not in original selection). */
  addedDeps: string[];
  /** Reasons for UI: "因选择【A】，自动补全【B、C】". */
  reasons: { becauseId: string; becauseLabel: string; addedIds: string[] }[];
}

/**
 * Resolves selection with dependency closure. Excludes UNMAPPED_KEY and unknown ids.
 */
export function resolveSelectionWithDeps(selectedIds: string[]): ResolveSelectionWithDepsResult {
  const allIds = new Set(getAllSelectableModuleIds());
  const selectedSet = new Set(selectedIds.filter((id) => id !== UNMAPPED_KEY && allIds.has(id)));
  const effectiveSet = new Set<string>(selectedSet);
  const worklist: string[] = Array.from(selectedSet);

  while (worklist.length > 0) {
    const id = worklist.pop()!;
    const deps = MODULE_DEPENDENCIES[id] ?? [];
    for (const depId of deps) {
      if (allIds.has(depId) && !effectiveSet.has(depId)) {
        effectiveSet.add(depId);
        worklist.push(depId);
      }
    }
  }

  const effectiveIds = Array.from(effectiveSet);
  const addedDeps = effectiveIds.filter((id) => !selectedSet.has(id));
  const reasons: ResolveSelectionWithDepsResult['reasons'] = [];
  for (const sid of selectedSet) {
    const deps = MODULE_DEPENDENCIES[sid] ?? [];
    const addedByThis = deps.filter((d) => addedDeps.includes(d));
    if (addedByThis.length > 0) {
      reasons.push({
        becauseId: sid,
        becauseLabel: getModuleLabelById(sid) ?? sid,
        addedIds: addedByThis,
      });
    }
  }

  return { effectiveIds, addedDeps, reasons };
}
