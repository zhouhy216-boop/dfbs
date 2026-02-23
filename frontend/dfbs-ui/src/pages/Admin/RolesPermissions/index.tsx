/**
 * 角色与权限 — permission model v1 read-only display (allowlisted super-admin only).
 * Section 1: 默认动作（v1）. Section 2: 权限树（模块 → 动作）.
 */
import { useEffect, useState } from 'react';
import { Table, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  fetchPermissionTree,
  type PermissionTreeResponse,
  type ActionItem,
  type ModuleNode,
} from './permService';

type LoadState = 'loading' | 'success' | 'error' | 'forbidden';

export default function RolesPermissionsPage() {
  const [state, setState] = useState<LoadState>('loading');
  const [data, setData] = useState<PermissionTreeResponse | null>(null);

  useEffect(() => {
    fetchPermissionTree()
      .then((res) => {
        setData(res);
        setState('success');
      })
      .catch((err: { response?: { status: number } }) => {
        if (err.response?.status === 403) {
          setState('forbidden');
        } else {
          setState('error');
          message.error('加载失败，请重试');
        }
      });
  }, []);

  if (state === 'loading') {
    return (
      <div style={{ padding: 24 }}>
        <h2>角色与权限</h2>
        <p>加载中...</p>
      </div>
    );
  }
  if (state === 'forbidden') {
    return (
      <div style={{ padding: 24 }}>
        <h2>角色与权限</h2>
        <p>无权限</p>
      </div>
    );
  }
  if (state === 'error' || !data) {
    return (
      <div style={{ padding: 24 }}>
        <h2>角色与权限</h2>
        <p>加载失败，请重试</p>
      </div>
    );
  }

  const actionColumns: ColumnsType<ActionItem> = [
    { title: '动作 key', dataIndex: 'key', key: 'key', width: 140 },
    { title: '中文名', dataIndex: 'label', key: 'label' },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h2>角色与权限</h2>

      <section style={{ marginTop: 24 }}>
        <h3>默认动作（v1）</h3>
        <Table<ActionItem>
          rowKey="key"
          size="small"
          pagination={false}
          columns={actionColumns}
          dataSource={data.actions}
        />
      </section>

      <section style={{ marginTop: 32 }}>
        <h3>权限树（模块 → 动作）</h3>
        <p style={{ color: '#666', fontSize: 12 }}>keyFormat: {data.keyFormat}</p>
        <div style={{ marginTop: 12 }}>
          {data.modules.map((mod) => (
            <ModuleNodeBlock key={mod.key} node={mod} />
          ))}
        </div>
      </section>
    </div>
  );
}

function ModuleNodeBlock({ node }: { node: ModuleNode }) {
  return (
    <div style={{ marginBottom: 16, padding: 12, border: '1px solid #eee', borderRadius: 4 }}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>
        {node.label}（{node.key}）
      </div>
      <div>
        {node.actions.map((actionKey) => (
          <Tag key={actionKey} style={{ marginBottom: 4 }}>
            {actionKey}
          </Tag>
        ))}
      </div>
      {node.children?.length > 0 &&
        node.children.map((child) => <ModuleNodeBlock key={child.key} node={child} />)}
    </div>
  );
}
