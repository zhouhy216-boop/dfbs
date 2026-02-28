/**
 * 菜单树权限编辑：每个模块节点一行，菜单级复选框 + 基础动作常显 + 更多动作折叠。
 * 仅操作草稿，保存/还原由父组件负责。
 */
import { Checkbox, Collapse, Select, Space } from 'antd';
import type { ModuleNodeLike } from './permissionQuickOps';
import { splitActions } from './permissionQuickOps';

export type MenuTreeMode = 'role' | 'override';

export interface PermissionMenuTreeProps {
  modules: ModuleNodeLike[];
  getMenuChecked: (node: ModuleNodeLike) => boolean;
  onMenuCheck: (node: ModuleNodeLike, checked: boolean) => void;
  mode: MenuTreeMode;
  role?: {
    draft: Set<string>;
    onToggle: (key: string) => void;
  };
  override?: {
    templateKeys: Set<string>;
    draftAdd: Set<string>;
    draftRemove: Set<string>;
    onSetState: (key: string, state: 'none' | 'add' | 'remove') => void;
  };
}

function getOverrideState(
  key: string,
  _templateKeys: Set<string>,
  draftAdd: Set<string>,
  draftRemove: Set<string>,
): 'none' | 'add' | 'remove' {
  if (draftRemove.has(key)) return 'remove';
  if (draftAdd.has(key)) return 'add';
  return 'none';
}

function PermissionMenuTreeNode({
  node,
  depth,
  getMenuChecked,
  onMenuCheck,
  mode,
  role,
  override,
}: {
  node: ModuleNodeLike;
  depth: number;
  getMenuChecked: (n: ModuleNodeLike) => boolean;
  onMenuCheck: (n: ModuleNodeLike, checked: boolean) => void;
  mode: MenuTreeMode;
  role?: PermissionMenuTreeProps['role'];
  override?: PermissionMenuTreeProps['override'];
}) {
  const menuChecked = getMenuChecked(node);
  const { basic, advanced } = splitActions(node.actions || []);
  const hasAdvanced = advanced.length > 0;

  const renderAction = (actionKey: string) => {
    const key = `${node.key}:${actionKey}`;
    if (mode === 'role' && role) {
      const checked = role.draft.has(key);
      return (
        <Checkbox
          key={key}
          checked={checked}
          onChange={() => role.onToggle(key)}
        >
          {actionKey}
        </Checkbox>
      );
    }
    if (mode === 'override' && override) {
      const state = getOverrideState(key, override.templateKeys, override.draftAdd, override.draftRemove);
      return (
        <Space key={key} size={4}>
          <span style={{ fontSize: 12 }}>{actionKey}:</span>
          <Select
            size="small"
            value={state}
            onChange={(v) => override.onSetState(key, v as 'none' | 'add' | 'remove')}
            options={[
              { value: 'none', label: '默认' },
              { value: 'add', label: '添加' },
              { value: 'remove', label: '移除' },
            ]}
            style={{ width: 72 }}
            status={state === 'remove' ? 'error' : undefined}
          />
        </Space>
      );
    }
    return null;
  };

  return (
    <div style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: 8, marginLeft: depth * 16 }}>
        <Checkbox
          checked={menuChecked}
          onChange={(e) => onMenuCheck(node, e.target.checked)}
        />
        <span style={{ fontWeight: 600, marginRight: 8 }}>{node.label}</span>
        <span style={{ fontSize: 12, color: '#888' }}>（{node.key}）</span>
      </div>
      <div style={{ marginLeft: depth * 16 + 24, marginTop: 6 }}>
        {basic.length > 0 && (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 6 }}>
            {basic.map((a) => renderAction(a))}
          </div>
        )}
        {hasAdvanced && (
          <Collapse
            size="small"
            items={[
              {
                key: 'advanced',
                label: '更多动作',
                children: (
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {advanced.map((a) => renderAction(a))}
                  </div>
                ),
              },
            ]}
          />
        )}
      </div>
      {(node.children?.length ?? 0) > 0 && (
        <div style={{ marginTop: 8 }}>
          {(node.children ?? []).map((child) => (
            <PermissionMenuTreeNode
              key={child.key}
              node={child}
              depth={depth + 1}
              getMenuChecked={getMenuChecked}
              onMenuCheck={onMenuCheck}
              mode={mode}
              role={role}
              override={override}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export function PermissionMenuTree({
  modules,
  getMenuChecked,
  onMenuCheck,
  mode,
  role,
  override,
}: PermissionMenuTreeProps) {
  if (modules.length === 0) return <p style={{ color: '#999' }}>暂无模块</p>;
  return (
    <div style={{ marginTop: 8 }}>
      {modules.map((mod) => (
        <PermissionMenuTreeNode
          key={mod.key}
          node={mod}
          depth={0}
          getMenuChecked={getMenuChecked}
          onMenuCheck={onMenuCheck}
          mode={mode}
          role={mode === 'role' ? role : undefined}
          override={mode === 'override' ? override : undefined}
        />
      ))}
    </div>
  );
}
