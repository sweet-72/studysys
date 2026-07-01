import { updateAiAvatarAdminUsingPut } from '@/services/backend/aiAvatarController';
import type { ProColumns } from '@ant-design/pro-components';
import { Button, Form, Input, InputNumber, message, Modal, Radio, Select } from 'antd';
import React, { useEffect, useState } from 'react';

interface Props {
  oldData?: API.AiAvatar;
  visible: boolean;
  columns: ProColumns<API.AiAvatar>[];
  onSubmit: (values: API.AiAvatar) => void;
  onCancel: () => void;
}

const parseTags = (raw?: string): string[] => {
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw);
    if (Array.isArray(parsed)) {
      return parsed.map((item) => String(item).trim()).filter(Boolean);
    }
  } catch (error) {
    // fallback for historical comma separated tags
  }
  return raw.split(',').map((item) => item.trim()).filter(Boolean);
};

const tagsToPayload = (tags?: string[]) => JSON.stringify((tags || []).map((item) => item.trim()).filter(Boolean));

const UpdateModal: React.FC<Props> = ({ oldData, visible, onSubmit, onCancel }) => {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!oldData || !visible) return;
    form.setFieldsValue({
      ...oldData,
      tags: parseTags(oldData.tags),
    });
  }, [oldData, visible, form]);

  const handleSubmit = async () => {
    if (!oldData?.id) {
      message.error('智能体 ID 无效');
      return;
    }
    try {
      const values = await form.validateFields();
      const payload: API.AiAvatar = {
        id: oldData.id,
        name: values.name,
        baseUrl: values.baseUrl,
        description: values.description,
        avatarImgUrl: values.avatarImgUrl,
        avatarAuth: values.avatarAuth,
        tags: tagsToPayload(values.tags),
        personality: values.personality,
        abilities: values.abilities,
        isPublic: values.isPublic,
        status: values.status,
        sort: values.sort,
      };

      setSubmitting(true);
      await updateAiAvatarAdminUsingPut({ id: oldData.id }, payload);
      message.success('更新成功');
      onSubmit(payload);
    } catch (error: any) {
      if (error?.errorFields) return;
      message.error(error?.message || '更新失败');
    } finally {
      setSubmitting(false);
    }
  };

  if (!oldData) return <></>;

  return (
    <Modal
      destroyOnClose
      title="编辑智能体"
      open={visible}
      onCancel={onCancel}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          取消
        </Button>,
        <Button key="submit" type="primary" loading={submitting} onClick={handleSubmit}>
          保存
        </Button>,
      ]}
      width={680}
    >
      <Form form={form} layout="vertical">
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入智能体名称' }]}>
          <Input maxLength={64} placeholder="例如：英语陪练助手-Pro" />
        </Form.Item>
        <Form.Item name="baseUrl" label="Base URL" rules={[{ required: true, message: '请输入 Base URL' }]}>
          <Input placeholder="例如：http://localhost:8088/v1" />
        </Form.Item>
        <Form.Item name="description" label="描述" rules={[{ required: true, message: '请输入描述' }]}>
          <Input.TextArea rows={3} maxLength={500} showCount placeholder="请输入智能体描述" />
        </Form.Item>
        <Form.Item name="avatarImgUrl" label="头像 URL">
          <Input placeholder="请输入头像 URL" />
        </Form.Item>
        <Form.Item name="avatarAuth" label="鉴权信息" rules={[{ required: true, message: '请输入鉴权信息' }]}>
          <Input.TextArea rows={2} maxLength={500} showCount placeholder="例如：app-xxxx" />
        </Form.Item>
        <Form.Item name="tags" label="标签" help={'会以 JSON 字符串格式提交，例如 ["英语","口语"]'}>
          <Select mode="tags" tokenSeparators={[',']} placeholder="输入后按 Enter" />
        </Form.Item>
        <Form.Item name="personality" label="人格设定">
          <Input.TextArea rows={2} maxLength={500} showCount placeholder="例如：专业、耐心" />
        </Form.Item>
        <Form.Item name="abilities" label="能力描述">
          <Input.TextArea rows={2} maxLength={500} showCount placeholder="例如：口语纠错、面试模拟" />
        </Form.Item>
        <Form.Item name="isPublic" label="是否公开" rules={[{ required: true, message: '请选择是否公开' }]}>
          <Radio.Group>
            <Radio value={1}>公开</Radio>
            <Radio value={0}>私密</Radio>
          </Radio.Group>
        </Form.Item>
        <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
          <Radio.Group>
            <Radio value={1}>启用</Radio>
            <Radio value={0}>禁用</Radio>
          </Radio.Group>
        </Form.Item>
        <Form.Item name="sort" label="排序" rules={[{ required: true, message: '请输入排序值' }]}>
          <InputNumber min={0} precision={0} style={{ width: '100%' }} />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default UpdateModal;
