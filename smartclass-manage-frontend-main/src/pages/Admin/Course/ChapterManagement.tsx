import React, { useRef, useState } from 'react';
import { Alert, Button, Form, Input, InputNumber, Modal, Popconfirm, Space, message } from 'antd';
import { PageContainer } from '@ant-design/pro-components';
import ProTable, { ActionType, ProColumns } from '@ant-design/pro-table';
import { history, useLocation } from '@umijs/max';
import {
  addChapterUsingPost,
  deleteChapterUsingPost,
  listChaptersByPageUsingGet,
  updateChapterUsingPost,
} from '@/services/backend/courseChapterController';

const TEXT = {
  invalidCourseId: '\u8bfe\u7a0b ID \u65e0\u6548',
  chapterUpdated: '\u7ae0\u8282\u66f4\u65b0\u6210\u529f',
  chapterCreated: '\u7ae0\u8282\u521b\u5efa\u6210\u529f',
  chapterSaveFailed: '\u7ae0\u8282\u4fdd\u5b58\u5931\u8d25',
  chapterDeleted: '\u7ae0\u8282\u5220\u9664\u6210\u529f',
  chapterDeleteFailed: '\u7ae0\u8282\u5220\u9664\u5931\u8d25',
  chapterId: '\u7ae0\u8282 ID',
  chapterTitle: '\u7ae0\u8282\u6807\u9898',
  chapterDesc: '\u7ae0\u8282\u63cf\u8ff0',
  sort: '\u6392\u5e8f',
  createTime: '\u521b\u5efa\u65f6\u95f4',
  actions: '\u64cd\u4f5c',
  sectionManage: '\u5c0f\u8282\u7ba1\u7406',
  edit: '\u7f16\u8f91',
  remove: '\u5220\u9664',
  removeConfirm: '\u786e\u8ba4\u5220\u9664\u8be5\u7ae0\u8282\u5417\uff1f',
  missingCourseId: '\u7f3a\u5c11 courseId \u53c2\u6570\uff0c\u65e0\u6cd5\u7ba1\u7406\u7ae0\u8282\u3002',
  pageTitle: '\u7ae0\u8282\u7ba1\u7406',
  createChapter: '\u65b0\u589e\u7ae0\u8282',
  editChapter: '\u7f16\u8f91\u7ae0\u8282',
  enterChapterTitle: '\u8bf7\u8f93\u5165\u7ae0\u8282\u6807\u9898',
  enterChapterDesc: '\u8bf7\u8f93\u5165\u7ae0\u8282\u63cf\u8ff0',
  enterSort: '\u8bf7\u8f93\u5165\u6392\u5e8f\u503c',
} as const;

const ChapterManagement: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const location = useLocation();
  const query = new URLSearchParams(location.search);
  const courseId = Number(query.get('courseId'));
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRow, setEditingRow] = useState<API.CourseChapter | null>(null);
  const [form] = Form.useForm<API.CourseChapter>();

  const validCourseId = Number.isFinite(courseId) && courseId > 0;

  const openCreateModal = () => {
    setEditingRow(null);
    form.resetFields();
    form.setFieldsValue({ sort: 1 });
    setModalOpen(true);
  };

  const openEditModal = (row: API.CourseChapter) => {
    setEditingRow(row);
    form.setFieldsValue({
      title: row.title,
      description: row.description,
      sort: row.sort,
    });
    setModalOpen(true);
  };

  const submitChapter = async () => {
    try {
      const values = await form.validateFields();
      if (!validCourseId) {
        message.error(TEXT.invalidCourseId);
        return;
      }

      if (editingRow?.id) {
        await updateChapterUsingPost({
          id: editingRow.id,
          courseId,
          title: values.title,
          description: values.description,
          sort: Number(values.sort || 1),
        });
        message.success(TEXT.chapterUpdated);
      } else {
        await addChapterUsingPost({
          courseId,
          title: values.title,
          description: values.description,
          sort: Number(values.sort || 1),
        });
        message.success(TEXT.chapterCreated);
      }

      setModalOpen(false);
      actionRef.current?.reload();
    } catch (error: any) {
      if (error?.errorFields) {
        return;
      }
      message.error(TEXT.chapterSaveFailed);
    }
  };

  const removeChapter = async (row: API.CourseChapter) => {
    if (!row.id) {
      return;
    }

    try {
      await deleteChapterUsingPost({ id: row.id });
      message.success(TEXT.chapterDeleted);
      actionRef.current?.reload();
    } catch (error) {
      message.error(TEXT.chapterDeleteFailed);
    }
  };

  const columns: ProColumns<API.CourseChapter>[] = [
    { title: TEXT.chapterId, dataIndex: 'id', width: 90, hideInSearch: true },
    { title: TEXT.chapterTitle, dataIndex: 'title' },
    { title: TEXT.chapterDesc, dataIndex: 'description', hideInSearch: true, ellipsis: true },
    { title: TEXT.sort, dataIndex: 'sort', hideInSearch: true, width: 80 },
    { title: TEXT.createTime, dataIndex: 'createTime', hideInSearch: true, valueType: 'dateTime' },
    {
      title: TEXT.actions,
      valueType: 'option',
      width: 220,
      render: (_, row) => (
        <Space size={4}>
          <Button
            type="link"
            onClick={() => history.push(`/admin/courseManagement/section?courseId=${courseId}&chapterId=${row.id}`)}
          >
            {TEXT.sectionManage}
          </Button>
          <Button type="link" onClick={() => openEditModal(row)}>
            {TEXT.edit}
          </Button>
          <Popconfirm title={TEXT.removeConfirm} onConfirm={() => removeChapter(row)}>
            <Button type="link" danger>
              {TEXT.remove}
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer>
      {!validCourseId && <Alert type="warning" showIcon message={TEXT.missingCourseId} style={{ marginBottom: 16 }} />}

      <ProTable<API.CourseChapter>
        rowKey="id"
        actionRef={actionRef}
        headerTitle={TEXT.pageTitle}
        columns={columns}
        search={{ labelWidth: 80 }}
        toolBarRender={() => [
          <Button key="create" type="primary" onClick={openCreateModal} disabled={!validCourseId}>
            {TEXT.createChapter}
          </Button>,
        ]}
        request={async (params) => {
          if (!validCourseId) {
            return { data: [], total: 0, success: true };
          }

          const res = await listChaptersByPageUsingGet({
            current: params.current,
            pageSize: params.pageSize,
            courseId,
          });
          return {
            data: res.data?.records || [],
            total: res.data?.total || 0,
            success: true,
          };
        }}
      />

      <Modal
        open={modalOpen}
        title={editingRow ? TEXT.editChapter : TEXT.createChapter}
        onCancel={() => setModalOpen(false)}
        onOk={submitChapter}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label={TEXT.chapterTitle} rules={[{ required: true, message: TEXT.enterChapterTitle }]}>
            <Input maxLength={100} placeholder={TEXT.enterChapterTitle} />
          </Form.Item>
          <Form.Item name="description" label={TEXT.chapterDesc}>
            <Input.TextArea rows={4} placeholder={TEXT.enterChapterDesc} />
          </Form.Item>
          <Form.Item name="sort" label={TEXT.sort} rules={[{ required: true, message: TEXT.enterSort }]}>
            <InputNumber min={1} precision={0} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default ChapterManagement;