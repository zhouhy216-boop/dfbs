/**
 * Shared helpers for permission quick ops (全选/只读/读写/清空).
 * Subtree = node + all descendants. keyFormat: moduleKey:actionKey.
 */
export interface ModuleNodeLike {
  key: string;
  label: string;
  actions: string[];
  children?: ModuleNodeLike[];
}

const READWRITE_ACTIONS = ['VIEW', 'CREATE', 'EDIT'];

/** Basic actions shown inline; the rest go under "更多动作". */
export const BASIC_ACTION_KEYS = ['VIEW', 'CREATE', 'EDIT'] as const;
export const ADVANCED_ACTION_KEYS = ['SUBMIT', 'APPROVE', 'REJECT', 'ASSIGN', 'CLOSE', 'DELETE', 'EXPORT'] as const;

export function splitActions(actions: string[]): { basic: string[]; advanced: string[] } {
  const basic: string[] = [];
  const advanced: string[] = [];
  for (const a of actions) {
    if (BASIC_ACTION_KEYS.includes(a as (typeof BASIC_ACTION_KEYS)[number])) {
      basic.push(a);
    } else {
      advanced.push(a);
    }
  }
  return { basic, advanced };
}

/** All permission keys in this node and its descendants. */
export function collectSubtreeKeys(node: ModuleNodeLike): string[] {
  const keys: string[] = [];
  function walk(n: ModuleNodeLike) {
    for (const a of n.actions || []) {
      keys.push(`${n.key}:${a}`);
    }
    for (const c of n.children || []) {
      walk(c);
    }
  }
  walk(node);
  return keys;
}

/** Total number of keys in subtree (for status). */
export function countSubtreeKeys(node: ModuleNodeLike): number {
  let n = 0;
  function walk(m: ModuleNodeLike) {
    n += (m.actions || []).length;
    for (const c of m.children || []) walk(c);
  }
  walk(node);
  return n;
}

export type QuickOp = 'all' | 'readonly' | 'readwrite' | 'clear';

/** Group permission keys by moduleKey; optional label map (moduleKey -> label). */
export function groupKeysByModule(
  keys: string[],
  moduleLabelMap?: Map<string, string>,
): { moduleKey: string; label: string; keys: string[] }[] {
  const byModule = new Map<string, string[]>();
  for (const k of keys) {
    const [moduleKey] = k.split(':');
    if (!moduleKey) continue;
    const list = byModule.get(moduleKey) ?? [];
    list.push(k);
    byModule.set(moduleKey, list);
  }
  return Array.from(byModule.entries())
    .map(([moduleKey, keysInModule]) => ({
      moduleKey,
      label: moduleLabelMap?.get(moduleKey) ?? moduleKey,
      keys: keysInModule.sort(),
    }))
    .sort((a, b) => a.moduleKey.localeCompare(b.moduleKey));
}

/** Build moduleKey -> label from tree (flatten). */
export function buildModuleLabelMap(modules: ModuleNodeLike[]): Map<string, string> {
  const m = new Map<string, string>();
  function walk(n: ModuleNodeLike) {
    m.set(n.key, n.label);
    for (const c of n.children ?? []) walk(c);
  }
  for (const mod of modules) walk(mod);
  return m;
}

/** Keys to set for this subtree for the given quick op (v1 conservative). */
export function getKeysForOp(node: ModuleNodeLike, op: QuickOp): string[] {
  const keys: string[] = [];
  function walk(n: ModuleNodeLike) {
    const actions = n.actions || [];
    if (op === 'clear') {
      // nothing
    } else if (op === 'readonly') {
      if (actions.includes('VIEW')) keys.push(`${n.key}:VIEW`);
    } else if (op === 'readwrite') {
      for (const a of READWRITE_ACTIONS) {
        if (actions.includes(a)) keys.push(`${n.key}:${a}`);
      }
    } else {
      // all
      for (const a of actions) keys.push(`${n.key}:${a}`);
    }
    for (const c of n.children || []) walk(c);
  }
  walk(node);
  return keys;
}
