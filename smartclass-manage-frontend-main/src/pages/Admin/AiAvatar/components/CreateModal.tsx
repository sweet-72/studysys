import { addAiAvatarUsingPost } from '@/services/backend/aiAvatarController';
import type { ProColumns } from '@ant-design/pro-components';
import { Button, Form, Input, InputNumber, message, Modal, Radio, Select } from 'antd';
import React, { useState } from 'react';

interface Props {
  visible: boolean;
  columns: ProColumns<API.AiAvatar>[];
  onSubmit: (values: API.AiAvatarAddRequest) => void;
  onCancel: () => void;
}

const tagsToPayload = (tags?: string[]) => JSON.stringify((tags || []).map((item) => item.trim()).filter(Boolean));

const CreateModal: React.FC<Props> = ({ visible, onSubmit, onCancel }) => {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const payload: API.AiAvatarAddRequest = {
        name: values.name,
        baseUrl: values.baseUrl,
        description: values.description,
        avatarImgUrl: values.avatarImgUrl,
        avatarAuth: values.avatarAuth,
        tags: tagsToPayload(values.tags),
        personality: values.personality,
        abilities: values.abilities,
        isPublic: values.isPublic,
        sort: values.sort,
      };

      setSubmitting(true);
      await addAiAvatarUsingPost(payload);
      message.success('创建成功');
      form.resetFields();
      onSubmit(payload);
    } catch (error: any) {
      if (error?.errorFields) return;
      message.error(error?.message || '创建失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      destroyOnClose
      title="新建智能体"
      open={visible}
      onCancel={() => {
        form.resetFields();
        onCancel();
      }}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          取消
        </Button>,
        <Button key="submit" type="primary" loading={submitting} onClick={handleSubmit}>
          创建
        </Button>,
      ]}
      width={680}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          isPublic: 1,
          sort: 0,
          tags: [],
        }}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入智能体名称' }]}>
          <Input maxLength={64} placeholder="例如：英语陪练助手" />
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
          <Input.TextArea rows={2} maxLength={500} showCount placeholder="例如：耐心、鼓励型" />
        </Form.Item>
        <Form.Item name="abilities" label="能力描述">
          <Input.TextArea rows={2} maxLength={500} showCount placeholder="例如：口语纠错、情景对话" />
        </Form.Item>
        <Form.Item name="isPublic" label="是否公开" rules={[{ required: true, message: '请选择是否公开' }]}>
          <Radio.Group>
            <Radio value={1}>公开</Radio>
            <Radio value={0}>私密</Radio>
          </Radio.Group>
        </Form.Item>
        <Form.Item name="sort" label="排序" rules={[{ required: true, message: '请输入排序值' }]}>
          <InputNumber min={0} precision={0} style={{ width: '100%' }} />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default CreateModal;
