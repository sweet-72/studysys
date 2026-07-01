import { addCourseHomework, updateCourseHomework, type CourseHomeworkVO } from '@/api/courseHomework';
import { Form, Input, InputNumber, Modal, Select, message } from 'antd';
import React, { useEffect } from 'react';

const TEXT = {
  missingSectionId: String.fromCharCode(0x7f3a, 0x5c11, 0x5c0f, 0x8282, 0x20, 0x49, 0x44, 0xff0c, 0x65e0, 0x6cd5, 0x4fdd, 0x5b58, 0x4f5c, 0x4e1a),
  updateSuccess: String.fromCharCode(0x4f5c, 0x4e1a, 0x66f4, 0x65b0, 0x6210, 0x529f),
  createSuccess: String.fromCharCode(0x4f5c, 0x4e1a, 0x521b, 0x5efa, 0x6210, 0x529f),
  saveFailed: String.fromCharCode(0x4fdd, 0x5b58, 0x4f5c, 0x4e1a, 0x5931, 0x8d25),
  editHomework: String.fromCharCode(0x7f16, 0x8f91, 0x4f5c, 0x4e1a),
  createHomework: String.fromCharCode(0x65b0, 0x589e, 0x4f5c, 0x4e1a),
  title: String.fromCharCode(0x4f5c, 0x4e1a, 0x6807, 0x9898),
  enterTitle: String.fromCharCode(0x8bf7, 0x8f93, 0x5165, 0x4f5c, 0x4e1a, 0x6807, 0x9898),
  content: String.fromCharCode(0x9898, 0x76ee, 0x5185, 0x5bb9),
  enterContent: String.fromCharCode(0x8bf7, 0x8f93, 0x5165, 0x9898, 0x76ee, 0x5185, 0x5bb9),
  standardAnswer: String.fromCharCode(0x6807, 0x51c6, 0x7b54, 0x6848),
  enterStandardAnswer: String.fromCharCode(0x8bf7, 0x8f93, 0x5165, 0x6807, 0x51c6, 0x7b54, 0x6848, 0xff0c, 0x53ef, 0x9009),
  analysis: String.fromCharCode(0x89e3, 0x6790),
  enterAnalysis: String.fromCharCode(0x8bf7, 0x8f93, 0x5165, 0x89e3, 0x6790, 0xff0c, 0x53ef, 0x9009),
  scoreAndSort: String.fromCharCode(0x5206, 0x503c, 0x4e0e, 0x6392, 0x5e8f),
  score: String.fromCharCode(0x5206, 0x503c),
  enterScore: String.fromCharCode(0x8bf7, 0x8f93, 0x5165, 0x5206, 0x503c),
  sort: String.fromCharCode(0x6392, 0x5e8f),
  enterSort: String.fromCharCode(0x8bf7, 0x8f93, 0x5165, 0x6392, 0x5e8f),
  status: String.fromCharCode(0x72b6, 0x6001),
  chooseStatus: String.fromCharCode(0x8bf7, 0x9009, 0x62e9, 0x72b6, 0x6001),
  draft: String.fromCharCode(0x8349, 0x7a3f),
  enabled: String.fromCharCode(0x542f, 0x7528),
  disabled: String.fromCharCode(0x505c, 0x7528),
} as const;

export interface HomeworkEditorModalProps {
  open: boolean;
  courseId: number;
  sectionId?: number;
  sectionTitle?: string;
  homework?: CourseHomeworkVO | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const HomeworkEditorModal: React.FC<HomeworkEditorModalProps> = ({
  open,
  courseId,
  sectionId,
  sectionTitle,
  homework,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm();
  const editing = !!homework?.id;

  useEffect(() => {
    if (!open) {
      return;
    }

    form.resetFields();
    form.setFieldsValue({
      title: homework?.title,
      content: homework?.content,
      standardAnswer: homework?.standardAnswer,
      analysis: homework?.analysis,
      score: homework?.score ?? 100,
      sort: homework?.sort ?? 1,
      status: homework?.status ?? 1,
    });
  }, [open, homework, form]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (!sectionId) {
        message.error(TEXT.missingSectionId);
        return;
      }

      const payload = {
        courseId,
        sectionId,
        title: values.title,
        content: values.content,
        standardAnswer: values.standardAnswer,
        analysis: values.analysis,
        score: Number(values.score ?? 100),
        sort: Number(values.sort ?? 1),
        status: Number(values.status ?? 1),
      };

      if (editing && homework?.id) {
        await updateCourseHomework({
          id: homework.id,
          ...payload,
        });
        message.success(TEXT.updateSuccess);
      } else {
        await addCourseHomework(payload);
        message.success(TEXT.createSuccess);
      }

      onSuccess();
    } catch (error: any) {
      if (error?.errorFields) {
        return;
      }
      message.error(error?.message || TEXT.saveFailed);
    }
  };

  return (
    <Modal
      title={`${editing ? TEXT.editHomework : TEXT.createHomework}${sectionTitle ? ` - ${sectionTitle}` : ''}`}
      open={open}
      onCancel={onCancel}
      onOk={handleSubmit}
      destroyOnClose
      forceRender
      width={720}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="title"
          label={TEXT.title}
          rules={[{ required: true, message: TEXT.enterTitle }]}
        >
          <Input maxLength={100} placeholder={TEXT.enterTitle} />
        </Form.Item>

        <Form.Item
          name="content"
          label={TEXT.content}
          rules={[{ required: true, message: TEXT.enterContent }]}
        >
          <Input.TextArea rows={4} placeholder={TEXT.enterContent} />
        </Form.Item>

        <Form.Item name="standardAnswer" label={TEXT.standardAnswer}>
          <Input.TextArea rows={3} placeholder={TEXT.enterStandardAnswer} />
        </Form.Item>

        <Form.Item name="analysis" label={TEXT.analysis}>
          <Input.TextArea rows={3} placeholder={TEXT.enterAnalysis} />
        </Form.Item>

        <Form.Item label={TEXT.scoreAndSort} style={{ marginBottom: 0 }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12 }}>
            <Form.Item
              name="score"
              label={TEXT.score}
              rules={[{ required: true, message: TEXT.enterScore }]}
            >
              <InputNumber min={0} precision={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item
              name="sort"
              label={TEXT.sort}
              rules={[{ required: true, message: TEXT.enterSort }]}
            >
              <InputNumber min={1} precision={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item
              name="status"
              label={TEXT.status}
              rules={[{ required: true, message: TEXT.chooseStatus }]}
            >
              <Select
                options={[
                  { label: TEXT.draft, value: 0 },
                  { label: TEXT.enabled, value: 1 },
                  { label: TEXT.disabled, value: 2 },
                ]}
              />
            </Form.Item>
          </div>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default HomeworkEditorModal;