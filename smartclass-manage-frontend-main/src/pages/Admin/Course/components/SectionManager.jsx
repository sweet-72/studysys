import React, { useEffect, useState } from 'react';
import {
  Button,
  Card,
  Collapse,
  Empty,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Space,
  Spin,
  Typography,
  message,
} from 'antd';
import {
  addChapterUsingPost,
  deleteChapterUsingPost,
  listChaptersUsingGet,
  updateChapterUsingPost,
} from '@/services/backend/courseChapterController';
import LessonManager from './LessonManager';

const { Text } = Typography;

const defaultChapterForm = {
  title: '',
  description: '',
  sort: 1,
};

const SectionManager = ({ courseId }) => {
  const [loading, setLoading] = useState(false);
  const [chapters, setChapters] = useState([]);
  const [chapterModalOpen, setChapterModalOpen] = useState(false);
  const [editingChapter, setEditingChapter] = useState(null);
  const [form] = Form.useForm();

  const loadChapters = async () => {
    if (!courseId) {
      setChapters([]);
      return;
    }

    setLoading(true);
    try {
      const res = await listChaptersUsingGet({ courseId });
      setChapters(res.data || []);
    } catch (error) {
      message.error('加载章节失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadChapters();
  }, [courseId]);

  const openCreateModal = () => {
    setEditingChapter(null);
    form.setFieldsValue(defaultChapterForm);
    setChapterModalOpen(true);
  };

  const openEditModal = (chapter) => {
    setEditingChapter(chapter);
    form.setFieldsValue({
      title: chapter.title,
      description: chapter.description,
      sort: chapter.sort || 1,
    });
    setChapterModalOpen(true);
  };

  const submitChapter = async () => {
    try {
      const values = await form.validateFields();
      if (!courseId) {
        message.error('课程 ID 无效');
        return;
      }

      if (editingChapter?.id) {
        await updateChapterUsingPost({
          id: editingChapter.id,
          courseId,
          title: values.title,
          description: values.description,
          sort: Number(values.sort || 1),
        });
        message.success('章节更新成功');
      } else {
        await addChapterUsingPost({
          courseId,
          title: values.title,
          description: values.description,
          sort: Number(values.sort || 1),
        });
        message.success('章节创建成功');
      }

      setChapterModalOpen(false);
      await loadChapters();
    } catch (error) {
      if (error?.errorFields) {
        return;
      }
      message.error('章节保存失败');
    }
  };

  const removeChapter = async (chapter) => {
    if (!chapter?.id) {
      return;
    }

    try {
      await deleteChapterUsingPost({ id: chapter.id });
      message.success('章节删除成功');
      await loadChapters();
    } catch (error) {
      message.error('章节删除失败');
    }
  };

  return (
    <Card
      title="章节管理"
      extra={
        <Button type="primary" onClick={openCreateModal}>
          新增章节
        </Button>
      }
    >
      <Spin spinning={loading}>
        {chapters.length === 0 ? (
          <Empty description="暂无章节，请先创建章节" />
        ) : (
          <Collapse
            items={chapters.map((chapter) => {
              const chapterId = chapter.id ?? chapter.chapterId;
              return {
                key: String(chapterId ?? chapter.title ?? Math.random()),
                label: (
                  <Space>
                    <Text strong>{chapter.title}</Text>
                    <Text type="secondary">排序: {chapter.sort ?? '-'}</Text>
                  </Space>
                ),
                extra: (
                  <Space onClick={(e) => e.stopPropagation()}>
                    <Button type="link" onClick={() => openEditModal(chapter)}>
                      编辑
                    </Button>
                    <Popconfirm title="确认删除该章节吗？" onConfirm={() => removeChapter(chapter)}>
                      <Button type="link" danger>
                        删除
                      </Button>
                    </Popconfirm>
                  </Space>
                ),
                children: chapterId ? (
                  <LessonManager courseId={courseId} chapterId={chapterId} chapterTitle={chapter.title} />
                ) : (
                  <Empty description="章节ID缺失，无法加载小节" />
                ),
              };
            })}
          />
        )}
      </Spin>

      <Modal
        open={chapterModalOpen}
        title={editingChapter ? '编辑章节' : '新增章节'}
        onCancel={() => setChapterModalOpen(false)}
        onOk={submitChapter}
        destroyOnClose
      >
        <Form form={form} layout="vertical" initialValues={defaultChapterForm}>
          <Form.Item
            name="title"
            label="章节标题"
            rules={[{ required: true, message: '请输入章节标题' }]}
          >
            <Input maxLength={100} placeholder="请输入章节标题" />
          </Form.Item>
          <Form.Item name="description" label="章节描述">
            <Input.TextArea rows={4} placeholder="请输入章节描述" />
          </Form.Item>
          <Form.Item name="sort" label="排序" rules={[{ required: true, message: '请输入排序值' }]}>
            <InputNumber min={1} precision={0} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default SectionManager;
