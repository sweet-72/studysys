import React, { useMemo, useRef, useState } from 'react';
import { Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Typography, message } from 'antd';
import { PageContainer } from '@ant-design/pro-components';
import ProTable, { ActionType, ProColumns } from '@ant-design/pro-table';
import moment from 'moment';
import {
  addCategoryUsingPost,
  deleteCategoryUsingPost,
  getSubCategoriesUsingGet,
  getTopCategoriesUsingGet,
  updateCategoryUsingPost,
} from '@/services/backend/courseCategoryController';

const TEXT = {
  topCategory: '\u9876\u7ea7\u5206\u7c7b',
  categoryFallback: '\u5206\u7c7b',
  categoryUpdated: '\u5206\u7c7b\u66f4\u65b0\u6210\u529f',
  categoryCreated: '\u5206\u7c7b\u65b0\u589e\u6210\u529f',
  categorySaveFailed: '\u4fdd\u5b58\u8bfe\u7a0b\u5206\u7c7b\u5931\u8d25',
  categoryDeleted: '\u5206\u7c7b\u5df2\u5220\u9664',
  categoryDeleteFailed: '\u5220\u9664\u8bfe\u7a0b\u5206\u7c7b\u5931\u8d25',
  categoryId: '\u5206\u7c7bID',
  name: '\u5206\u7c7b\u540d\u79f0',
  icon: '\u56fe\u6807',
  description: '\u63cf\u8ff0',
  sort: '\u6392\u5e8f',
  parentId: '\u7236\u5206\u7c7b',
  createTime: '\u521b\u5efa\u65f6\u95f4',
  actions: '\u64cd\u4f5c',
  edit: '\u7f16\u8f91',
  remove: '\u5220\u9664',
  removeConfirm: '\u5220\u9664\u540e\u7528\u6237\u7aef\u8bfe\u7a0b\u5206\u7c7b\u5c06\u4e0d\u518d\u663e\u793a\uff0c\u5173\u8054\u8bfe\u7a0b\u53ef\u80fd\u53d8\u4e3a\u672a\u5206\u7c7b',
  confirmRemove: '\u786e\u8ba4\u5220\u9664',
  cancel: '\u53d6\u6d88',
  pageTitle: '\u8bfe\u7a0b\u5206\u7c7b\u7ba1\u7406',
  create: '\u65b0\u589e\u5206\u7c7b',
  editTitle: '\u7f16\u8f91\u5206\u7c7b',
  confirmCreate: '\u786e\u8ba4\u65b0\u589e',
  confirmUpdate: '\u4fdd\u5b58\u4fee\u6539',
  enterName: '\u8bf7\u8f93\u5165\u5206\u7c7b\u540d\u79f0',
  enterIcon: '\u8bf7\u8f93\u5165\u56fe\u6807\u6807\u8bc6\u3001Emoji \u6216\u56fe\u7247 URL',
  enterDescription: '\u8bf7\u8f93\u5165\u5206\u7c7b\u63cf\u8ff0',
  enterSort: '\u8bf7\u8f93\u5165\u6392\u5e8f\u503c',
  chooseParent: '\u8bf7\u9009\u62e9\u7236\u5206\u7c7b',
  parentHelp: '\u7236\u5206\u7c7b\u9ed8\u8ba4\u4e3a 0\uff0c\u8868\u793a\u9876\u7ea7\u5206\u7c7b\u3002',
} as const;

type CategoryFormValues = Pick<API.CourseCategory, 'name' | 'icon' | 'description' | 'sort' | 'parentId'>;

const normalizeCategories = (responseData: any): API.CourseCategory[] => {
  if (Array.isArray(responseData)) {
    return responseData;
  }
  const records = responseData?.records || responseData?.list || responseData?.data;
  return Array.isArray(records) ? records : [];
};

const loadAllCategories = async () => {
  const topRes = await getTopCategoriesUsingGet();
  const topCategories = normalizeCategories(topRes.data);
  const subResults = await Promise.allSettled(
    topCategories
      .filter((item) => item?.id !== undefined && item?.id !== null)
      .map((item) => getSubCategoriesUsingGet({ parentId: Number(item.id) })),
  );
  const subCategories = subResults.flatMap((result) =>
    result.status === 'fulfilled' ? normalizeCategories(result.value.data) : [],
  );
  return [...topCategories, ...subCategories].sort((a, b) => Number(b.sort || 0) - Number(a.sort || 0));
};

const CourseCategoryManagement: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRow, setEditingRow] = useState<API.CourseCategory | null>(null);
  const [categoryList, setCategoryList] = useState<API.CourseCategory[]>([]);
  const [form] = Form.useForm<CategoryFormValues>();

  const parentOptions = useMemo(
    () => [
      { label: TEXT.topCategory, value: 0 },
      ...categoryList
        .filter((item) => item.id !== editingRow?.id && item.id !== undefined && item.id !== null)
        .map((item) => ({
          label: `${item.name || TEXT.categoryFallback} (ID: ${item.id})`,
          value: Number(item.id),
        })),
    ],
    [categoryList, editingRow?.id],
  );

  const openCreateModal = () => {
    setEditingRow(null);
    form.setFieldsValue({
      name: '',
      icon: '',
      description: '',
      sort: 0,
      parentId: 0,
    });
    setModalOpen(true);
  };

  const openEditModal = (row: API.CourseCategory) => {
    setEditingRow(row);
    form.setFieldsValue({
      name: row.name,
      icon: row.icon,
      description: row.description,
      sort: row.sort ?? 0,
      parentId: row.parentId ?? 0,
    });
    setModalOpen(true);
  };

  const submitCategory = async () => {
    try {
      const values = await form.validateFields();
      const payload: API.CourseCategory = {
        name: values.name,
        icon: values.icon,
        description: values.description,
        sort: Number(values.sort ?? 0),
        parentId: Number(values.parentId ?? 0),
        isDelete: editingRow?.isDelete ?? 0,
      };

      if (editingRow?.id) {
        await updateCategoryUsingPost({ ...payload, id: editingRow.id });
        message.success(TEXT.categoryUpdated);
      } else {
        await addCategoryUsingPost(payload);
        message.success(TEXT.categoryCreated);
      }

      setModalOpen(false);
      actionRef.current?.reload();
    } catch (error: any) {
      if (error?.errorFields) {
        return;
      }
      message.error(error?.message || TEXT.categorySaveFailed);
    }
  };

  const deleteCategory = async (row: API.CourseCategory) => {
    if (!row.id) {
      return;
    }

    try {
      await deleteCategoryUsingPost({ id: row.id });
      message.success(TEXT.categoryDeleted);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error?.message || TEXT.categoryDeleteFailed);
    }
  };

  const columns: ProColumns<API.CourseCategory>[] = [
    {
      title: TEXT.categoryId,
      dataIndex: 'id',
      width: 90,
      hideInSearch: true,
    },
    {
      title: TEXT.name,
      dataIndex: 'name',
      ellipsis: true,
    },
    {
      title: TEXT.icon,
      dataIndex: 'icon',
      width: 120,
      hideInSearch: true,
      render: (_, row) => row.icon || '-',
    },
    {
      title: TEXT.description,
      dataIndex: 'description',
      hideInSearch: true,
      ellipsis: true,
      render: (_, row) => row.description || '-',
    },
    {
      title: TEXT.sort,
      dataIndex: 'sort',
      width: 90,
      hideInSearch: true,
      sorter: (a, b) => Number(a.sort || 0) - Number(b.sort || 0),
    },
    {
      title: TEXT.parentId,
      dataIndex: 'parentId',
      width: 100,
      hideInSearch: true,
      render: (_, row) => row.parentId ?? 0,
    },
    {
      title: TEXT.createTime,
      dataIndex: 'createTime',
      width: 160,
      hideInSearch: true,
      render: (_, row) => (row.createTime ? moment(row.createTime).format('YYYY-MM-DD HH:mm') : '-'),
    },
    {
      title: TEXT.actions,
      valueType: 'option',
      width: 140,
      render: (_, row) => (
        <Space size={4}>
          <Button type="link" size="small" onClick={() => openEditModal(row)}>
            {TEXT.edit}
          </Button>
          <Popconfirm
            title={TEXT.removeConfirm}
            okText={TEXT.confirmRemove}
            cancelText={TEXT.cancel}
            onConfirm={() => deleteCategory(row)}
          >
            <Button type="link" size="small" danger>
              {TEXT.remove}
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer>
      <ProTable<API.CourseCategory>
        rowKey="id"
        actionRef={actionRef}
        headerTitle={TEXT.pageTitle}
        columns={columns}
        search={{ labelWidth: 80 }}
        tableLayout="fixed"
        toolBarRender={() => [
          <Button key="create" type="primary" onClick={openCreateModal}>
            {TEXT.create}
          </Button>,
        ]}
        request={async () => {
          const list = await loadAllCategories();
          setCategoryList(list);
          return {
            data: list,
            success: true,
          };
        }}
      />

      <Modal
        open={modalOpen}
        title={editingRow ? TEXT.editTitle : TEXT.create}
        onCancel={() => setModalOpen(false)}
        onOk={submitCategory}
        okText={editingRow ? TEXT.confirmUpdate : TEXT.confirmCreate}
        cancelText={TEXT.cancel}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label={TEXT.name} rules={[{ required: true, message: TEXT.enterName }]}>
            <Input maxLength={50} placeholder={TEXT.enterName} />
          </Form.Item>
          <Form.Item name="icon" label={TEXT.icon}>
            <Input maxLength={100} placeholder={TEXT.enterIcon} />
          </Form.Item>
          <Form.Item name="description" label={TEXT.description}>
            <Input.TextArea rows={4} maxLength={500} placeholder={TEXT.enterDescription} />
          </Form.Item>
          <Form.Item name="sort" label={TEXT.sort} rules={[{ required: true, message: TEXT.enterSort }]}>
            <InputNumber min={0} precision={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="parentId" label={TEXT.parentId} rules={[{ required: true, message: TEXT.chooseParent }]}>
            <Select options={parentOptions} placeholder={TEXT.chooseParent} />
          </Form.Item>
          <Typography.Text type="secondary">{TEXT.parentHelp}</Typography.Text>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default CourseCategoryManagement;
