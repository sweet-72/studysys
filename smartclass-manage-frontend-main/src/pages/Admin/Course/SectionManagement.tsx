import React, { useState } from 'react';
import {
  Alert,
  Button,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Upload,
  message,
} from 'antd';
import { UploadOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { useLocation } from '@umijs/max';
import {
  addSectionUsingPost,
  deleteSectionUsingPost,
  listSectionsByPageUsingGet,
  updateSectionUsingPost,
  updateSectionVideoUsingPost,
  uploadVideoAndAddSectionUsingPost,
} from '@/services/backend/courseSectionController';

const VIDEO_TYPE_URL = 'url';
const VIDEO_TYPE_FILE = 'file';
const SECTION_AI_KNOWLEDGE_FIELD = 'assistantKnowledge';

const TEXT = {
  loadFailed: '\u52a0\u8f7d\u5c0f\u8282\u5931\u8d25',
  enterVideoUrl: '\u8bf7\u586b\u5199\u89c6\u9891 URL',
  uploadVideo: '\u8bf7\u4e0a\u4f20\u89c6\u9891\u6587\u4ef6',
  updateSuccess: '\u5c0f\u8282\u66f4\u65b0\u6210\u529f',
  createSuccess: '\u5c0f\u8282\u521b\u5efa\u6210\u529f',
  saveFailed: '\u5c0f\u8282\u4fdd\u5b58\u5931\u8d25',
  deleteSuccess: '\u5c0f\u8282\u5220\u9664\u6210\u529f',
  deleteFailed: '\u5c0f\u8282\u5220\u9664\u5931\u8d25',
  missingParams: '\u7f3a\u5c11 courseId/chapterId \u53c2\u6570\uff0c\u65e0\u6cd5\u7ba1\u7406\u5c0f\u8282\u3002',
  createSection: '\u65b0\u589e\u5c0f\u8282',
  editSection: '\u7f16\u8f91\u5c0f\u8282',
  sectionId: '\u5c0f\u8282 ID',
  sectionTitle: '\u5c0f\u8282\u6807\u9898',
  sectionDesc: '\u5c0f\u8282\u63cf\u8ff0',
  aiKnowledge: 'AI \u5b66\u4e60\u52a9\u624b\u77e5\u8bc6\u5185\u5bb9',
  enterAiKnowledge:
    '\u8bf7\u8f93\u5165\u672c\u5c0f\u8282\u5b66\u4e60\u52a9\u624b\u53ef\u53c2\u8003\u7684\u77e5\u8bc6\u5185\u5bb9\uff0c\u4f8b\u5982\u89c6\u9891\u6587\u5b57\u7a3f\u3001\u91cd\u70b9\u6982\u5ff5\u3001\u4f8b\u9898\u89e3\u6790\u3001\u6613\u9519\u70b9\u3001\u8bfe\u5802\u603b\u7ed3\u7b49',
  videoSource: '\u89c6\u9891\u6765\u6e90',
  localUpload: '\u672c\u5730\u4e0a\u4f20',
  videoAddress: '\u89c6\u9891\u5730\u5740',
  sort: '\u6392\u5e8f',
  actions: '\u64cd\u4f5c',
  edit: '\u7f16\u8f91',
  remove: '\u5220\u9664',
  removeConfirm: '\u786e\u8ba4\u5220\u9664\u8be5\u5c0f\u8282\u5417\uff1f',
  videoType: '\u89c6\u9891\u7c7b\u578b',
  urlInput: 'URL \u8f93\u5165',
  videoFile: '\u89c6\u9891\u6587\u4ef6',
  selectVideoFile: '\u9009\u62e9\u89c6\u9891\u6587\u4ef6',
  chooseVideoType: '\u8bf7\u9009\u62e9\u89c6\u9891\u7c7b\u578b',
  enterSectionTitle: '\u8bf7\u8f93\u5165\u5c0f\u8282\u6807\u9898',
  enterSectionDesc: '\u8bf7\u8f93\u5165\u5c0f\u8282\u63cf\u8ff0',
  enterSort: '\u8bf7\u8f93\u5165\u6392\u5e8f\u503c',
} as const;

type SectionFormValues = {
  title: string;
  description?: string;
  aiKnowledge?: string;
  sort: number;
  videoType: string;
  videoUrl?: string;
  videoFile?: any[];
};

const fileFromUpload = (videoFile?: any[] | any) => {
  const source = Array.isArray(videoFile) ? videoFile[0] : videoFile?.file;
  return source?.originFileObj || source;
};

const resolveSectionAiKnowledge = (row: API.CourseSection) =>
  (row as any).assistantKnowledge ||
  (row as any).aiKnowledgeContent ||
  (row as any).aiKnowledge ||
  (row as any).aiContent ||
  (row as any).knowledgeContent ||
  '';

const SectionManagement: React.FC = () => {
  const location = useLocation();
  const query = new URLSearchParams(location.search);
  const courseId = Number(query.get('courseId'));
  const chapterId = Number(query.get('chapterId'));
  const validParams = Number.isFinite(courseId) && courseId > 0 && Number.isFinite(chapterId) && chapterId > 0;

  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<API.CourseSection[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRow, setEditingRow] = useState<API.CourseSection | null>(null);
  const [form] = Form.useForm<SectionFormValues>();

  const loadSections = async () => {
    if (!validParams) {
      setRows([]);
      return;
    }

    setLoading(true);
    try {
      const res = await listSectionsByPageUsingGet({
        current: 1,
        pageSize: 200,
        courseId,
        chapterId,
      });
      setRows(res.data?.records || []);
    } catch (error) {
      message.error(TEXT.loadFailed);
    } finally {
      setLoading(false);
    }
  };

  React.useEffect(() => {
    loadSections();
  }, [courseId, chapterId]);

  const openCreateModal = () => {
    setEditingRow(null);
    form.resetFields();
    form.setFieldsValue({ sort: 1, videoType: VIDEO_TYPE_URL, videoFile: [], aiKnowledge: '' });
    setModalOpen(true);
  };

  const openEditModal = (row: API.CourseSection) => {
    setEditingRow(row);
    form.setFieldsValue({
      title: row.title || '',
      description: row.description || '',
      aiKnowledge: resolveSectionAiKnowledge(row),
      sort: row.sort || 1,
      videoType: row.resourceType === 2 ? VIDEO_TYPE_FILE : VIDEO_TYPE_URL,
      videoUrl: row.resourceUrl || row.videoUrl || '',
      videoFile: [],
    });
    setModalOpen(true);
  };

  const submitSection = async () => {
    try {
      const values = await form.validateFields();
      const videoFile = fileFromUpload(values.videoFile);
      const isFileMode = values.videoType === VIDEO_TYPE_FILE;
      const aiKnowledge = values.aiKnowledge?.trim();

      if (!isFileMode && !values.videoUrl) {
        message.error(TEXT.enterVideoUrl);
        return;
      }

      if (isFileMode && !videoFile && !editingRow?.resourceUrl) {
        message.error(TEXT.uploadVideo);
        return;
      }

      const basePayload = {
        courseId,
        chapterId,
        title: values.title,
        description: values.description,
        sort: Number(values.sort || 1),
        contentType: 1,
        isFree: 0,
        resourceType: isFileMode ? 2 : 1,
        // TODO: Add a section-level assistantKnowledge field in the backend entity for persistence.
        [SECTION_AI_KNOWLEDGE_FIELD]: aiKnowledge,
      } as API.CourseSection;

      if (editingRow?.id) {
        await updateSectionUsingPost({
          id: editingRow.id,
          ...basePayload,
          resourceUrl: isFileMode ? editingRow.resourceUrl : values.videoUrl,
        });

        if (isFileMode && videoFile) {
          await updateSectionVideoUsingPost({ sectionId: editingRow.id }, {}, videoFile);
        }
        message.success(TEXT.updateSuccess);
      } else if (isFileMode) {
        if (!videoFile) {
          message.error(TEXT.uploadVideo);
          return;
        }
        await uploadVideoAndAddSectionUsingPost(
          {
            courseId,
            chapterId,
          },
          {
            title: values.title,
            description: values.description,
            sort: Number(values.sort || 1),
            [SECTION_AI_KNOWLEDGE_FIELD]: aiKnowledge,
          },
          videoFile,
        );
        message.success(TEXT.createSuccess);
      } else {
        await addSectionUsingPost({
          ...basePayload,
          resourceUrl: values.videoUrl,
        });
        message.success(TEXT.createSuccess);
      }

      setModalOpen(false);
      await loadSections();
    } catch (error: any) {
      if (error?.errorFields) {
        return;
      }
      message.error(TEXT.saveFailed);
    }
  };

  const removeSection = async (row: API.CourseSection) => {
    if (!row.id) {
      return;
    }

    try {
      await deleteSectionUsingPost({ id: row.id });
      message.success(TEXT.deleteSuccess);
      await loadSections();
    } catch (error) {
      message.error(TEXT.deleteFailed);
    }
  };

  const videoType = Form.useWatch('videoType', form);

  return (
    <PageContainer>
      {!validParams && <Alert type="warning" showIcon message={TEXT.missingParams} style={{ marginBottom: 16 }} />}

      <Space style={{ marginBottom: 12 }}>
        <Button type="primary" onClick={openCreateModal} disabled={!validParams}>
          {TEXT.createSection}
        </Button>
      </Space>

      <Table<API.CourseSection>
        rowKey="id"
        loading={loading}
        dataSource={rows}
        columns={[
          { title: TEXT.sectionId, dataIndex: 'id', width: 90 },
          { title: TEXT.sectionTitle, dataIndex: 'title' },
          { title: TEXT.videoSource, render: (_, row) => (row.resourceType === 2 ? TEXT.localUpload : 'URL') },
          { title: TEXT.videoAddress, dataIndex: 'resourceUrl', render: (value) => value || '-' },
          { title: TEXT.sort, dataIndex: 'sort', width: 80 },
          {
            title: TEXT.actions,
            width: 180,
            render: (_, row) => (
              <Space size={4}>
                <Button type="link" onClick={() => openEditModal(row)}>
                  {TEXT.edit}
                </Button>
                <Popconfirm title={TEXT.removeConfirm} onConfirm={() => removeSection(row)}>
                  <Button type="link" danger>
                    {TEXT.remove}
                  </Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]}
        pagination={false}
      />

      <Modal
        open={modalOpen}
        title={editingRow ? TEXT.editSection : TEXT.createSection}
        onCancel={() => setModalOpen(false)}
        onOk={submitSection}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label={TEXT.sectionTitle} rules={[{ required: true, message: TEXT.enterSectionTitle }]}>
            <Input maxLength={100} placeholder={TEXT.enterSectionTitle} />
          </Form.Item>
          <Form.Item name="description" label={TEXT.sectionDesc}>
            <Input.TextArea rows={3} placeholder={TEXT.enterSectionDesc} />
          </Form.Item>
          <Form.Item name="aiKnowledge" label={TEXT.aiKnowledge}>
            <Input.TextArea rows={5} placeholder={TEXT.enterAiKnowledge} />
          </Form.Item>
          <Form.Item name="videoType" label={TEXT.videoType} rules={[{ required: true, message: TEXT.chooseVideoType }]}>
            <Select
              options={[
                { label: TEXT.urlInput, value: VIDEO_TYPE_URL },
                { label: TEXT.localUpload, value: VIDEO_TYPE_FILE },
              ]}
            />
          </Form.Item>

          {videoType === VIDEO_TYPE_FILE ? (
            <Form.Item
              name="videoFile"
              label={TEXT.videoFile}
              valuePropName="fileList"
              getValueFromEvent={(e) => e?.fileList || []}
            >
              <Upload maxCount={1} accept="video/*" beforeUpload={() => false}>
                <Button icon={<UploadOutlined />}>{TEXT.selectVideoFile}</Button>
              </Upload>
            </Form.Item>
          ) : (
            <Form.Item name="videoUrl" label={"\u89c6\u9891 URL"} rules={[{ required: true, message: TEXT.enterVideoUrl }]}>
              <Input placeholder={TEXT.enterVideoUrl} />
            </Form.Item>
          )}

          <Form.Item name="sort" label={TEXT.sort} rules={[{ required: true, message: TEXT.enterSort }]}>
            <InputNumber min={1} precision={0} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default SectionManagement;
