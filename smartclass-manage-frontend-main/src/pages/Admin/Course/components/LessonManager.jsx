import React, { useEffect, useState } from 'react';
import {
  Button,
  Empty,
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
const PAGE_SIZE_LIMIT = 20;
const SECTION_AI_KNOWLEDGE_FIELD = 'assistantKnowledge';

const resolveSectionAiKnowledge = (section) =>
  section?.assistantKnowledge ||
  section?.aiKnowledgeContent ||
  section?.aiKnowledge ||
  section?.aiContent ||
  section?.knowledgeContent ||
  '';

const buildInitialValues = (section) => ({
  title: section?.title || '',
  description: section?.description || '',
  aiKnowledge: resolveSectionAiKnowledge(section),
  sort: section?.sort || 1,
  videoType: section?.resourceType === 2 ? VIDEO_TYPE_FILE : VIDEO_TYPE_URL,
  videoUrl: section?.resourceUrl || section?.videoUrl || '',
  videoFile: [],
});

const normalizeFileFromUpload = (videoFile) => {
  const fileItem = Array.isArray(videoFile) ? videoFile[0] : videoFile?.file;
  return fileItem?.originFileObj || fileItem;
};

const LessonManager = ({ courseId, chapterId, chapterTitle }) => {
  const [loading, setLoading] = useState(false);
  const [sections, setSections] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingSection, setEditingSection] = useState(null);
  const [form] = Form.useForm();

  const loadSectionsByPage = async (current, pageSize) => {
    const res = await listSectionsByPageUsingGet({
      current,
      pageSize,
      courseId,
      chapterId,
    });
    return res.data || {};
  };

  const loadSections = async () => {
    if (!courseId || !chapterId) {
      setSections([]);
      return;
    }

    setLoading(true);
    try {
      const pageSize = PAGE_SIZE_LIMIT;
      let current = 1;
      let allRecords = [];
      let hasNext = true;

      while (hasNext) {
        const pageData = await loadSectionsByPage(current, pageSize);
        const records = pageData.records || [];
        const total = Number(pageData.total || 0);
        allRecords = [...allRecords, ...records];

        if (total > 0) {
          hasNext = allRecords.length < total;
        } else {
          hasNext = records.length === pageSize;
        }

        current += 1;
      }

      setSections(allRecords);
    } catch (error) {
      message.error(error?.message || '加载小节失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSections();
  }, [courseId, chapterId]);

  const openCreateModal = () => {
    setEditingSection(null);
    form.setFieldsValue(buildInitialValues(null));
    setModalOpen(true);
  };

  const openEditModal = (section) => {
    setEditingSection(section);
    form.setFieldsValue(buildInitialValues(section));
    setModalOpen(true);
  };

  const validateVideo = (values) => {
    const videoType = values.videoType;
    const file = normalizeFileFromUpload(values.videoFile);
    const hasUrl = !!values.videoUrl;

    if (videoType === VIDEO_TYPE_URL && !hasUrl) {
      message.error('请填写视频 URL');
      return false;
    }
    if (videoType === VIDEO_TYPE_FILE && !file && !editingSection?.resourceUrl) {
      message.error('请上传视频文件');
      return false;
    }
    return true;
  };

  const submitSection = async () => {
    try {
      const values = await form.validateFields();
      if (!validateVideo(values)) {
        return;
      }

      const file = normalizeFileFromUpload(values.videoFile);
      const assistantKnowledge = values.aiKnowledge?.trim();
      const basePayload = {
        courseId,
        chapterId,
        title: values.title,
        description: values.description,
        sort: Number(values.sort || 1),
        contentType: 1,
        isFree: 0,
        [SECTION_AI_KNOWLEDGE_FIELD]: assistantKnowledge,
      };

      if (editingSection?.id) {
        await updateSectionUsingPost({
          id: editingSection.id,
          ...basePayload,
          resourceType: values.videoType === VIDEO_TYPE_FILE ? 2 : 1,
          resourceUrl: values.videoType === VIDEO_TYPE_URL ? values.videoUrl : editingSection.resourceUrl,
        });

        if (values.videoType === VIDEO_TYPE_FILE && file) {
          await updateSectionVideoUsingPost({ sectionId: editingSection.id }, {}, file);
        }
        message.success('小节更新成功');
      } else if (values.videoType === VIDEO_TYPE_FILE) {
        if (!file) {
          message.error('请上传视频文件');
          return;
        }
        await uploadVideoAndAddSectionUsingPost(
          {
            courseId,
            chapterId,
            title: values.title,
            description: values.description,
            sort: Number(values.sort || 1),
            [SECTION_AI_KNOWLEDGE_FIELD]: assistantKnowledge,
          },
          {},
          file,
        );
        message.success('小节创建成功');
      } else {
        await addSectionUsingPost({
          ...basePayload,
          resourceType: 1,
          resourceUrl: values.videoUrl,
        });
        message.success('小节创建成功');
      }

      setModalOpen(false);
      await loadSections();
    } catch (error) {
      if (error?.errorFields) {
        return;
      }
      message.error('小节保存失败');
    }
  };

  const removeSection = async (section) => {
    if (!section?.id) {
      return;
    }

    try {
      await deleteSectionUsingPost({ id: section.id });
      message.success('小节删除成功');
      await loadSections();
    } catch (error) {
      message.error('小节删除失败');
    }
  };

  const videoType = Form.useWatch('videoType', form);

  return (
    <div>
      <Space style={{ marginBottom: 12 }}>
        <span>{chapterTitle ? `章节：${chapterTitle}` : '小节管理'}</span>
        <Button type="primary" onClick={openCreateModal}>
          新增小节
        </Button>
      </Space>

      <Table
        rowKey="id"
        loading={loading}
        dataSource={sections}
        pagination={false}
        size="small"
        locale={{ emptyText: <Empty description="暂无小节，请先创建小节" /> }}
        columns={[
          { title: '小节标题', dataIndex: 'title' },
          {
            title: '视频来源',
            render: (_, record) => (record.resourceType === 2 ? '本地上传' : '视频 URL'),
          },
          {
            title: '视频地址',
            dataIndex: 'resourceUrl',
            render: (value) => value || '-',
          },
          { title: '排序', dataIndex: 'sort', width: 80 },
          {
            title: '操作',
            width: 180,
            render: (_, record) => (
              <Space>
                <Button type="link" onClick={() => openEditModal(record)}>
                  编辑
                </Button>
                <Popconfirm title="确认删除该小节吗？" onConfirm={() => removeSection(record)}>
                  <Button type="link" danger>
                    删除
                  </Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]}
      />

      <Modal
        open={modalOpen}
        title={editingSection ? '编辑小节' : '新增小节'}
        onCancel={() => setModalOpen(false)}
        onOk={submitSection}
        destroyOnClose
      >
        <Form form={form} layout="vertical" initialValues={buildInitialValues(editingSection)}>
          <Form.Item
            name="title"
            label="小节标题"
            rules={[{ required: true, message: '请输入小节标题' }]}
          >
            <Input maxLength={100} placeholder="请输入小节标题" />
          </Form.Item>

          <Form.Item name="description" label="小节描述">
            <Input.TextArea rows={3} placeholder="请输入小节描述" />
          </Form.Item>

          <Form.Item name="aiKnowledge" label="AI 学习助手知识内容">
            <Input.TextArea
              rows={5}
              placeholder="请输入本小节学习助手可参考的知识内容，例如视频文字稿、重点概念、例题解析、易错点、课堂总结等"
            />
          </Form.Item>

          <Form.Item
            name="videoType"
            label="视频类型"
            rules={[{ required: true, message: '请选择视频类型' }]}
          >
            <Select
              options={[
                { label: 'URL 输入', value: VIDEO_TYPE_URL },
                { label: '本地上传', value: VIDEO_TYPE_FILE },
              ]}
            />
          </Form.Item>

          {videoType === VIDEO_TYPE_URL ? (
            <Form.Item
              name="videoUrl"
              label="视频 URL"
              rules={[{ required: true, message: '请输入视频 URL' }]}
            >
              <Input placeholder="请输入视频 URL" />
            </Form.Item>
          ) : (
            <Form.Item
              name="videoFile"
              label="视频文件"
              valuePropName="fileList"
              getValueFromEvent={(e) => e?.fileList || []}
            >
              <Upload maxCount={1} accept="video/*" beforeUpload={() => false}>
                <Button icon={<UploadOutlined />}>选择视频文件</Button>
              </Upload>
            </Form.Item>
          )}

          <Form.Item name="sort" label="排序" rules={[{ required: true, message: '请输入排序值' }]}>
            <InputNumber min={1} precision={0} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default LessonManager;
