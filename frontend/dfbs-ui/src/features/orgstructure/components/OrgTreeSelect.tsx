import { Select } from 'antd';
import { useOrgTree } from '@/features/orgstructure/hooks/useOrgTree';

export interface OrgTreeSelectProps {
  value?: number | null;
  onChange?: (value: number | null) => void;
  placeholder?: string;
  includeDisabled?: boolean;
  allowClear?: boolean;
  style?: React.CSSProperties;
  disabled?: boolean;
}

/** Flatten tree to options with path label for dropdown. */
function flattenWithPath(nodes: { id: number; name: string; children?: { id: number; name: string; children?: unknown[] }[] }[], path = ''): { id: number; path: string }[] {
  const out: { id: number; path: string }[] = [];
  for (const n of nodes) {
    const p = path ? `${path} / ${n.name}` : n.name;
    out.push({ id: n.id, path: p });
    if (n.children?.length) out.push(...flattenWithPath(n.children as { id: number; name: string; children?: unknown[] }[], p));
  }
  return out;
}

/**
 * Reusable org tree selector. Options show full path (e.g. "总部 / 生产部 / 一车间").
 * Requires org-structure read API (Super Admin in v1).
 */
export function OrgTreeSelect({
  value,
  onChange,
  placeholder = '选择组织',
  includeDisabled = false,
  allowClear = true,
  style,
  disabled = false,
}: OrgTreeSelectProps) {
  const { tree, loading } = useOrgTree(includeDisabled);
  const options = flattenWithPath(tree);

  return (
    <Select
      placeholder={placeholder}
      allowClear={allowClear}
      value={value === undefined || value === null ? undefined : value}
      onChange={(v) => onChange?.(v ?? null)}
      loading={loading}
      style={style}
      disabled={disabled}
      showSearch
      optionFilterProp="label"
      options={options.map((o) => ({ label: o.path, value: o.id }))}
    />
  );
}
