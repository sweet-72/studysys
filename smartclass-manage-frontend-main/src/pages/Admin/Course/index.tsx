import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Alert, Button, Form, Input, Modal, Popconfirm, Select, Space, Tag, Typography, message } from 'antd';
import { PageContainer } from '@ant-design/pro-components';
import ProTable, { ActionType, ProColumns } from '@ant-design/pro-table';
import { history, useLocation } from '@umijs/max';
import moment from 'moment';
import {
  addCourseUsingPost,
  deleteCourseUsingPost,
  getCourseVoByIdUsingGet,
  listCourseVoByPageUsingGet,
  updateCourseStatusUsingPost,
  updateCourseUsingPost,
} from '@/services/backend/courseController';
import { getTopCategoriesUsingGet } from '@/services/backend/courseCategoryController';
import { listTeacherVoByPageUsingPost } from '@/services/backend/teacherController';

const TEXT = {
  statusDraft: '\u8349\u7a3f',
  statusPublished: '\u5df2\u53d1\u5e03',
  statusOffline: '\u5df2\u4e0b\u67b6',
  difficultyBeginner: '\u5165\u95e8',
  difficultyPrimary: '\u521d\u7ea7',
  difficultyIntermediate: '\u4e2d\u7ea7',
  difficultyAdvanced: '\u9ad8\u7ea7',
  difficultyExpert: '\u4e13\u5bb6',
  categoryFallback: '\u5206\u7c7b',
  teacherFallback: '\u8bb2\u5e08',
  categoryLoadFailed: '\u5206\u7c7b\u6570\u636e\u52a0\u8f7d\u5931\u8d25',
  uncategorized: '\u672a\u5206\u7c7b',
  teacherLoadFailed: '\u8bb2\u5e08\u6570\u636e\u52a0\u8f7d\u5931\u8d25',
  courseUpdated: '\u8bfe\u7a0b\u66f4\u65b0\u6210\u529f',
  courseCreated: '\u8bfe\u7a0b\u521b\u5efa\u6210\u529f',
  courseSaveFailed: '\u4fdd\u5b58\u8bfe\u7a0b\u5931\u8d25',
  courseDeleted: '\u8bfe\u7a0b\u5220\u9664\u6210\u529f',
  courseDeleteFailed: '\u8bfe\u7a0b\u5220\u9664\u5931\u8d25',
  coursePutOn: '\u8bfe\u7a0b\u5df2\u4e0a\u67b6',
  coursePutOff: '\u8bfe\u7a0b\u5df2\u4e0b\u67b6',
  courseStatusFailed: '\u72b6\u6001\u66f4\u65b0\u5931\u8d25',
  pageTitle: '\u8bfe\u7a0b\u7ba1\u7406',
  createCourse: '\u65b0\u5efa\u8bfe\u7a0b',
  editCourse: '\u7f16\u8f91\u8bfe\u7a0b',
  courseId: '\u8bfe\u7a0bID',
  courseTitle: '\u8bfe\u7a0b\u6807\u9898',
  courseDesc: '\u8bfe\u7a0b\u7b80\u4ecb',
  aiKnowledge: 'AI 学习助手知识内容',
  coverImage: '\u5c01\u9762\u5730\u5740',
  category: '\u5206\u7c7b',
  difficulty: '\u96be\u5ea6',
  teacher: '\u8bb2\u5e08',
  status: '\u72b6\u6001',
  sort: '\u6392\u5e8f',
  createTime: '\u521b\u5efa\u65f6\u95f4',
  actions: '\u64cd\u4f5c',
  viewDetail: '\u67e5\u770b\u8be6\u60c5',
  edit: '\u7f16\u8f91',
  putOn: '\u4e0a\u67b6',
  putOff: '\u4e0b\u67b6',
  remove: '\u5220\u9664',
  removeConfirm: '\u786e\u8ba4\u5220\u9664\u8be5\u8bfe\u7a0b\u5417\uff1f',
  enterCourseTitle: '\u8bf7\u8f93\u5165\u8bfe\u7a0b\u6807\u9898',
  enterCourseDesc: '\u8bf7\u8f93\u5165\u8bfe\u7a0b\u7b80\u4ecb',
  enterAiKnowledge: '请输入课程级 AI 学习助手知识内容',
  enterCoverImage: '\u8bf7\u8f93\u5165\u5c01\u9762\u56fe\u7247 URL',
  chooseCategory: '\u8bf7\u9009\u62e9\u5206\u7c7b',
  chooseDifficulty: '\u8bf7\u9009\u62e9\u96be\u5ea6',
  chooseTeacher: '\u8bf7\u9009\u62e9\u8bb2\u5e08',
  chooseStatus: '\u8bf7\u9009\u62e9\u72b6\u6001',
  confirmCreate: '\u786e\u8ba4\u521b\u5efa',
  confirmUpdate: '\u4fdd\u5b58\u4fee\u6539',
  cancel: '\u53d6\u6d88',
  basicInfo: '\u57fa\u7840\u4fe1\u606f',
  categoryInfo: '\u5206\u7c7b\u4fe1\u606f',
  statusInfo: '\u72b6\u6001\u4fe1\u606f',
  categoryEmpty: '暂无课程分类，请先到课程分类管理中新增分类',
  enterSort: '\u8bf7\u8f93\u5165\u6392\u5e8f\u503c',
} as const;

const statusOptions = [
  { label: TEXT.statusDraft, value: 0, color: 'default' },
  { label: TEXT.statusPublished, value: 1, color: 'success' },
  { label: TEXT.statusOffline, value: 2, color: 'warning' },
];

const difficultyOptions = [
  { label: TEXT.difficultyBeginner, value: 1 },
  { label: TEXT.difficultyPrimary, value: 2 },
  { label: TEXT.difficultyIntermediate, value: 3 },
  { label: TEXT.difficultyAdvanced, value: 4 },
  { label: TEXT.difficultyExpert, value: 5 },
];

const TEACHER_PAGE_SIZE_LIMIT = 20;

type OptionItem = {
  label: string;
  value: number;
};

type CourseFormValues = API.CourseAddRequest & {
  sort?: number;
};

type CourseRow = API.CourseVO & {
  category?: string;
  categoryName?: string;
};

const normalizeCategories = (responseData: any): API.CourseCategory[] => {
  if (Array.isArray(responseData)) {
    return responseData;
  }

  const records = responseData?.records || responseData?.list || responseData?.data;
  return Array.isArray(records) ? records : [];
};

const sectionTitleStyle: React.CSSProperties = {
  margin: '8px 0 12px',
  fontWeight: 600,
};

const twoColumnStyle: React.CSSProperties = {
  display: 'grid',
  gridTemplateColumns: '1fr 1fr',
  columnGap: 16,
};

const CoursePage: React.FC = () => {
  const location = useLocation();
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRow, setEditingRow] = useState<CourseRow | null>(null);
  const [categoryOptions, setCategoryOptions] = useState<OptionItem[]>([]);
  const [categoryLoading, setCategoryLoading] = useState(false);
  const [categoryLoadError, setCategoryLoadError] = useState('');
  const [teacherOptions, setTeacherOptions] = useState<OptionItem[]>([]);
  const [form] = Form.useForm<CourseFormValues>();

  const loadCategoryOptions = async () => {
    setCategoryLoading(true);
    setCategoryLoadError('');

    try {
      const res = await getTopCategoriesUsingGet();
      const options = normalizeCategories(res.data)
        .filter((item) => item?.id !== undefined && item?.id !== null)
        .map((item) => ({
          label: item.name || `${TEXT.categoryFallback}-${item.id}`,
          value: Number(item.id),
        }));

      setCategoryOptions(options);

      if (options.length === 0) {
        setCategoryLoadError(TEXT.categoryEmpty);
      }

      return options;
    } catch (error: any) {
      const errorMessage = error?.message || TEXT.categoryLoadFailed;
      setCategoryOptions([]);
      setCategoryLoadError(errorMessage);
      message.warning(errorMessage);
      return [];
    } finally {
      setCategoryLoading(false);
    }
  };

  const loadTeacherOptions = async () => {
    let current = 1;
    let allTeachers: API.TeacherVO[] = [];
    let hasNext = true;

    while (hasNext) {
      const res = await listTeacherVoByPageUsingPost({
        current,
        pageSize: TEACHER_PAGE_SIZE_LIMIT,
      });
      const pageData = res.data || {};
      const records = pageData.records || [];
      const total = Number(pageData.total || 0);

      allTeachers = [...allTeachers, ...records];

      if (total > 0) {
        hasNext = allTeachers.length < total;
      } else {
        hasNext = records.length === TEACHER_PAGE_SIZE_LIMIT;
      }

      current += 1;
    }

    return allTeachers.map((item) => ({
      label: item.name || `${TEXT.teacherFallback}-${item.id}`,
      value: Number(item.id),
    }));
  };

  const loadDictionaries = async () => {
    const [categoryResult, teacherResult] = await Promise.allSettled([
      loadCategoryOptions(),
      loadTeacherOptions(),
    ]);

    if (categoryResult.status === 'rejected') {
      setCategoryLoadError(TEXT.categoryLoadFailed);
      message.warning(TEXT.categoryLoadFailed);
    }

    if (teacherResult.status === 'fulfilled') {
      setTeacherOptions(teacherResult.value);
    } else {
      message.warning(TEXT.teacherLoadFailed);
    }
  };

  useEffect(() => {
    loadDictionaries();
  }, []);

  const categoryNameMap = useMemo(() => {
    const map = new Map<number, string>();
    categoryOptions.forEach((item) => {
      map.set(Number(item.value), item.label);
    });
    return map;
  }, [categoryOptions]);

  const resolveCategoryName = (row: CourseRow) => {
    if (row.categoryName) {
      return row.categoryName;
    }
    const mappedName = categoryNameMap.get(Number(row.categoryId));
    if (mappedName) {
      return mappedName;
    }
    if (row.category && !/^\d+$/.test(String(row.category))) {
      return row.category;
    }
    return TEXT.uncategorized;
  };

  const openCreateModal = async () => {
    setEditingRow(null);
    form.resetFields();
    const options = categoryOptions.length > 0 ? categoryOptions : await loadCategoryOptions();
    form.setFieldsValue({
      status: 1,
      difficulty: 1,
      categoryId: options[0]?.value,
      teacherId: teacherOptions[0]?.value,
    });
    setModalOpen(true);
  };

  const openEditModal = (row: CourseRow) => {
    setEditingRow(row);
    form.setFieldsValue({
      title: row.title,
      description: row.description,
      aiKnowleage: row.aiKnowleage,
      coverImage: row.coverImage,
      categoryId: row.categoryId ?? (/^\d+$/.test(String(row.category || '')) ? Number(row.category) : undefined),
      difficulty: row.difficulty,
      teacherId: row.teacherId,
      status: row.status,
    });
    setModalOpen(true);
  };

  useEffect(() => {
    const query = new URLSearchParams(location.search);
    const editId = Number(query.get('editId'));
    if (!Number.isFinite(editId) || editId <= 0) {
      return;
    }

    const loadEditCourse = async () => {
      try {
        const res = await getCourseVoByIdUsingGet({ id: editId });
        if (res.data) {
          openEditModal(res.data);
        }
      } catch (error: any) {
        message.error(error?.message || '\u52a0\u8f7d\u8bfe\u7a0b\u8be6\u60c5\u5931\u8d25');
      } finally {
        history.replace('/admin/courseManagement');
      }
    };

    loadEditCourse();
  }, [location.search]);

  const submitCourse = async () => {
    try {
      const values = await form.validateFields();
      const payload: API.CourseAddRequest = {
        title: values.title,
        description: values.description,
        aiKnowleage: values.aiKnowleage,
        coverImage: values.coverImage,
        categoryId: Number(values.categoryId),
        difficulty: Number(values.difficulty),
        teacherId: Number(values.teacherId),
        status: Number(values.status ?? 1),
      };

      if (editingRow?.id) {
        await updateCourseUsingPost({ id: editingRow.id, ...payload });
        message.success(TEXT.courseUpdated);
      } else {
        await addCourseUsingPost(payload);
        message.success(TEXT.courseCreated);
      }

      setModalOpen(false);
      actionRef.current?.reload();
    } catch (error: any) {
      if (error?.errorFields) {
        return;
      }
      message.error(error?.message || TEXT.courseSaveFailed);
    }
  };

  const handleDelete = async (row: CourseRow) => {
    if (!row.id) {
      return;
    }
    try {
      await deleteCourseUsingPost({ id: row.id });
      message.success(TEXT.courseDeleted);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error?.message || TEXT.courseDeleteFailed);
    }
  };

  const toggleCourseStatus = async (row: CourseRow) => {
    if (!row.id) {
      return;
    }
    const nextStatus = row.status === 2 ? 1 : 2;
    try {
      await updateCourseStatusUsingPost({ id: row.id, status: nextStatus });
      message.success(nextStatus === 1 ? TEXT.coursePutOn : TEXT.coursePutOff);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error?.message || TEXT.courseStatusFailed);
    }
  };

  const columns: ProColumns<CourseRow>[] = useMemo(
    () => [
      { title: TEXT.courseId, dataIndex: 'id', width: 72, hideInSearch: true },
      {
        title: TEXT.courseTitle,
        dataIndex: 'title',
        width: 260,
        render: (_, row) => (
          <Space direction="vertical" size={2} style={{ maxWidth: 240 }}>
            <Typography.Text strong ellipsis={{ tooltip: row.title }}>
              {row.title || '-'}
            </Typography.Text>
            <Typography.Text type="secondary" ellipsis={{ tooltip: row.description }}>
              {row.description || '-'}
            </Typography.Text>
          </Space>
        ),
      },
      {
        title: TEXT.category,
        dataIndex: 'categoryId',
        valueType: 'select',
        fieldProps: {
          options: categoryOptions,
          loading: categoryLoading,
          notFoundContent: categoryLoadError || undefined,
        },
        render: (_, row) => resolveCategoryName(row),
      },
      {
        title: TEXT.difficulty,
        dataIndex: 'difficulty',
        valueType: 'select',
        fieldProps: { options: difficultyOptions },
        render: (_, row) =>
          difficultyOptions.find((item) => item.value === row.difficulty)?.label || row.difficulty || '-',
      },
      {
        title: TEXT.teacher,
        dataIndex: 'teacherId',
        valueType: 'select',
        fieldProps: { options: teacherOptions },
        render: (_, row) =>
          row.teacherName || teacherOptions.find((item) => item.value === Number(row.teacherId))?.label || row.teacherId || '-',
      },
      {
        title: TEXT.status,
        dataIndex: 'status',
        valueType: 'select',
        fieldProps: { options: statusOptions.map((item) => ({ label: item.label, value: item.value })) },
        render: (_, row) => {
          const current = statusOptions.find((item) => item.value === row.status) || statusOptions[0];
          return <Tag color={current.color}>{current.label}</Tag>;
        },
      },
      {
        title: TEXT.createTime,
        dataIndex: 'createTime',
        hideInSearch: true,
        width: 160,
        render: (_, row) => (row.createTime ? moment(row.createTime).format('YYYY-MM-DD HH:mm') : '-'),
      },
      {
        title: TEXT.actions,
        valueType: 'option',
        width: 176,
        render: (_, row) => (
          <Space size={0} wrap>
            <Button type="link" size="small" onClick={() => history.push(`/admin/courseManagement/detail/${row.id}`)}>
              {TEXT.viewDetail}
            </Button>
            <Button type="link" size="small" onClick={() => openEditModal(row)}>
              {TEXT.edit}
            </Button>
            <Button type="link" size="small" onClick={() => toggleCourseStatus(row)}>
              {row.status === 2 ? TEXT.putOn : TEXT.putOff}
            </Button>
            <Popconfirm title={TEXT.removeConfirm} onConfirm={() => handleDelete(row)}>
              <Button type="link" size="small" danger>
                {TEXT.remove}
              </Button>
            </Popconfirm>
          </Space>
        ),
      },
    ],
    [categoryLoadError, categoryLoading, categoryOptions, categoryNameMap, teacherOptions],
  );

  return (
    <PageContainer>
      <ProTable<CourseRow>
        rowKey="id"
        actionRef={actionRef}
        headerTitle={TEXT.pageTitle}
        columns={columns}
        search={{ labelWidth: 80 }}
        tableLayout="fixed"
        toolBarRender={() => [
          <Button key="create" type="primary" onClick={openCreateModal}>
            {TEXT.createCourse}
          </Button>,
        ]}
        request={async (params) => {
          const res = await listCourseVoByPageUsingGet({
            current: params.current,
            pageSize: params.pageSize,
            title: params.title,
            categoryId: params.categoryId,
            difficulty: params.difficulty,
            status: params.status,
            teacherId: params.teacherId,
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
        title={editingRow ? TEXT.editCourse : TEXT.createCourse}
        onCancel={() => setModalOpen(false)}
        onOk={submitCourse}
        width={760}
        okText={editingRow ? TEXT.confirmUpdate : TEXT.confirmCreate}
        cancelText={TEXT.cancel}
        styles={{ body: { maxHeight: '64vh', overflowY: 'auto', paddingRight: 8 } }}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Typography.Title level={5} style={sectionTitleStyle}>
            {TEXT.basicInfo}
          </Typography.Title>
          <Form.Item name="title" label={TEXT.courseTitle} rules={[{ required: true, message: TEXT.enterCourseTitle }]}>
            <Input maxLength={100} placeholder={TEXT.enterCourseTitle} />
          </Form.Item>
          <Form.Item name="description" label={TEXT.courseDesc}>
            <Input.TextArea rows={4} placeholder={TEXT.enterCourseDesc} />
          </Form.Item>
          <Form.Item name="aiKnowleage" label={TEXT.aiKnowledge}>
            <Input.TextArea rows={5} placeholder={TEXT.enterAiKnowledge} />
          </Form.Item>
          <Form.Item name="coverImage" label={TEXT.coverImage}>
            <Input placeholder={TEXT.enterCoverImage} />
          </Form.Item>

          <Typography.Title level={5} style={sectionTitleStyle}>
            {TEXT.categoryInfo}
          </Typography.Title>
          {categoryLoadError ? <Alert type="error" showIcon message={categoryLoadError} style={{ marginBottom: 12 }} /> : null}
          <div style={twoColumnStyle}>
            <Form.Item name="categoryId" label={TEXT.category} rules={[{ required: true, message: TEXT.chooseCategory }]}>
              <Select
                showSearch
                optionFilterProp="label"
                options={categoryOptions}
                loading={categoryLoading}
                placeholder={categoryLoadError || TEXT.chooseCategory}
                notFoundContent={categoryLoadError || TEXT.chooseCategory}
                onDropdownVisibleChange={(open) => {
                  if (open && categoryOptions.length === 0 && !categoryLoading) {
                    void loadCategoryOptions();
                  }
                }}
              />
            </Form.Item>
            <Form.Item name="difficulty" label={TEXT.difficulty} rules={[{ required: true, message: TEXT.chooseDifficulty }]}>
              <Select options={difficultyOptions} placeholder={TEXT.chooseDifficulty} />
            </Form.Item>
            <Form.Item name="teacherId" label={TEXT.teacher} rules={[{ required: true, message: TEXT.chooseTeacher }]}>
              <Select showSearch optionFilterProp="label" options={teacherOptions} placeholder={TEXT.chooseTeacher} />
            </Form.Item>
          </div>

          <Typography.Title level={5} style={sectionTitleStyle}>
            {TEXT.statusInfo}
          </Typography.Title>
          <div style={twoColumnStyle}>
            <Form.Item name="status" label={TEXT.status} rules={[{ required: true, message: TEXT.chooseStatus }]}>
              <Select
                options={statusOptions.map((item) => ({ label: item.label, value: item.value }))}
                placeholder={TEXT.chooseStatus}
              />
            </Form.Item>
            <Form.Item name="sort" label={TEXT.sort}>
              <Input type="number" min={0} placeholder={TEXT.enterSort} />
            </Form.Item>
          </div>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default CoursePage;
