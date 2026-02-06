import { useRef, useState } from 'react';
import { Button, Form, Input, message, Modal, Radio, Switch, Tag } from 'antd';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import {
  getPlatformConfigs,
  addPlatformConfig,
  updatePlatformConfig,
  togglePlatformActive,
  type PlatformConfigItem,
  type PlatformConfigRequest,
} from '@/services/platformConfig';

const CODE_VALIDATOR_LABELS: Record<string, string> = {
  UPPERCASE: '仅大写字母',
  CHINESE: '建议汉字 (旧)', // legacy; prefer MIXED
  MIXED: '混合/无限制 (建议汉字)',
  NONE: '无校验',
};

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  message.error(err?.response?.data?.message ?? err?.message ?? '操作失败');
}

export default function PlatformConfigPage() {
  const actionRef = useRef<ActionType>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm<PlatformConfigRequest & { id?: number }>();

  const handleToggleActive = async (row: PlatformConfigItem, checked: boolean) => {
    try {
      await togglePlatformActive(row.id);
      message.success('状态已更新');
      actionRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  };

  const openCreate = () => {
    setEditingId(null);
    form.resetFields();
    form.setFieldsValue({
      platformName: '',
      platformCode: '',
      isActive: true,
      ruleUniqueEmail: false,
      ruleUniquePhone: false,
      ruleUniqueOrgName: false,
      codeValidatorType: 'NONE',
    });
    setModalOpen(true);
  };

  const openEdit = (row: PlatformConfigItem) => {
    setEditingId(row.id);
    const validatorType = row.codeValidatorType ?? 'NONE';
    form.setFieldsValue({
      platformName: row.platformName,
      platformCode: row.platformCode,
      isActive: row.isActive,
      ruleUniqueEmail: row.ruleUniqueEmail,
      ruleUniquePhone: row.ruleUniquePhone,
      ruleUniqueOrgName: row.ruleUniqueOrgName,
      codeValidatorType: validatorType === 'CHINESE' ? 'MIXED' : validatorType,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const body: PlatformConfigRequest = {
        platformName: values.platformName?.trim() ?? '',
        platformCode: (values.platformCode ?? '').toString().trim().toUpperCase(),
        isActive: values.isActive ?? true,
        ruleUniqueEmail: values.ruleUniqueEmail ?? false,
        ruleUniquePhone: values.ruleUniquePhone ?? false,
        ruleUniqueOrgName: values.ruleUniqueOrgName ?? false,
        codeValidatorType: values.codeValidatorType ?? 'NONE',
      };
      if (editingId != null) {
        await updatePlatformConfig(editingId, body);
        message.success('更新成功');
      } else {
        await addPlatformConfig(body);
        message.success('添加成功');
      }
      setModalOpen(false);
      actionRef.current?.reload();
    } catch (e) {
      if ((e as { errorFields?: unknown[] })?.errorFields) return;
      showError(e);
    }
  };

  const columns: ProColumns<PlatformConfigItem>[] = [
    { title: '平台名称', dataIndex: 'platformName', width: 140, ellipsis: true },
    {
      title: '平台代码',
      dataIndex: 'platformCode',
      width: 140,
      render: (_, row) => (
        <span
          style={{ cursor: 'pointer' }}
          onClick={() => {
            navigator.clipboard.writeText(row.platformCode ?? '');
            message.success('已复制');
          }}
          title="点击复制"
        >
          {row.platformCode ?? '—'}
        </span>
      ),
    },
    {
      title: '查重规则',
      dataIndex: 'rules',
      width: 220,
      search: false,
      render: (_, row) => (
        <>
          {row.ruleUniqueEmail && <Tag color="blue" style={{ marginBottom: 4 }}>邮箱唯一</Tag>}
          {row.ruleUniquePhone && <Tag color="cyan" style={{ marginBottom: 4 }}>电话唯一</Tag>}
          {row.ruleUniqueOrgName && <Tag color="geekblue" style={{ marginBottom: 4 }}>全称唯一</Tag>}
          {!row.ruleUniqueEmail && !row.ruleUniquePhone && !row.ruleUniqueOrgName && (
            <span style={{ color: '#999' }}>—</span>
          )}
        </>
      ),
    },
    {
      title: '机构代码/简称校验',
      dataIndex: 'codeValidatorType',
      width: 180,
      search: false,
      render: (_, row) => CODE_VALIDATOR_LABELS[row.codeValidatorType ?? 'NONE'] ?? row.codeValidatorType ?? '—',
    },
    {
      title: '启用',
      dataIndex: 'isActive',
      width: 80,
      search: false,
      render: (_, row) => (
        <Switch
          checked={!!row.isActive}
          onChange={(checked) => handleToggleActive(row, checked)}
        />
      ),
    },
    {
      title: '操作',
      valueType: 'option',
      width: 80,
      fixed: 'right',
      render: (_, row) => [
        <Button key="edit" type="link" size="small" onClick={() => openEdit(row)}>
          编辑
        </Button>,
      ],
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <ProTable<PlatformConfigItem>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={async () => {
          const list = await getPlatformConfigs();
          return { data: list, success: true };
        }}
        search={false}
        pagination={{ pageSize: 20 }}
        toolBarRender={() => [
          <Button key="add" type="primary" onClick={openCreate}>
            新建
          </Button>,
        ]}
        headerTitle="平台配置"
      />

      <Modal
        title={editingId == null ? '新建平台配置' : '编辑平台配置'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        okText="保存"
        destroyOnClose
        width={520}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="platformName"
            label="平台名称"
            rules={[{ required: true, message: '请输入平台名称' }]}
          >
            <Input placeholder="如：映翰通、京品咖啡" />
          </Form.Item>
          <Form.Item
            name="platformCode"
            label="平台代码"
            rules={[{ required: true, message: '请输入平台代码' }]}
          >
            <Input placeholder="建议大写英文，如 INHAND、JINGPIN" />
          </Form.Item>
          <div style={{ marginBottom: 16 }}>
            <div style={{ marginBottom: 8, color: 'rgba(0,0,0,0.88)' }}>查重规则</div>
            <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
              <Form.Item name="ruleUniqueEmail" valuePropName="checked" noStyle>
                <Switch checkedChildren="邮箱唯一" unCheckedChildren="邮箱唯一" />
              </Form.Item>
              <Form.Item name="ruleUniquePhone" valuePropName="checked" noStyle>
                <Switch checkedChildren="电话唯一" unCheckedChildren="电话唯一" />
              </Form.Item>
              <Form.Item name="ruleUniqueOrgName" valuePropName="checked" noStyle>
                <Switch checkedChildren="全称唯一" unCheckedChildren="全称唯一" />
              </Form.Item>
            </div>
          </div>
          <Form.Item
            name="codeValidatorType"
            label="机构代码/简称校验"
            rules={[{ required: true }]}
          >
            <Radio.Group>
              <Radio value="NONE">无校验</Radio>
              <Radio value="UPPERCASE">仅大写字母</Radio>
              <Radio value="MIXED">混合/无限制 (建议汉字)</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item name="isActive" label="启用" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
