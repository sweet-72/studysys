import React, { useRef, useState } from 'react';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { Button, Card, message, Popconfirm, Space, Tag, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import {
  deleteTeacherUsingPost,
  listTeacherVoByPageUsingPost,
} from '@/services/backend/teacherController';
import CreateModal from './components/CreateModal';
import UpdateModal from './components/UpdateModal';

const { Title } = Typography;

const MAX_PAGE_SIZE = 20;

const resolveSortOrder = (order?: 'ascend' | 'descend') => {
  if (order === 'ascend') {
    return 'asc';
  }
  if (order === 'descend') {
    return 'desc';
  }
  return undefined;
};

const TeacherAdminPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [createVisible, setCreateVisible] = useState(false);
  const [updateVisible, setUpdateVisible] = useState(false);
  const [currentRow, setCurrentRow] = useState<API.TeacherVO | undefined>();

  const handleDelete = async (row: API.TeacherVO) => {
    if (!row?.id) {
      message.error('缺少讲师 ID，无法删除');
      return false;
    }
    const hide = message.loading('正在删除');
    try {
      await deleteTeacherUsingPost({ id: row.id });
      hide();
      message.success('删除成功');
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      hide();
      message.error(error?.message || '删除失败');
      return false;
    }
  };

  const columns: ProColumns<API.TeacherVO>[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
      hideInForm: true,
      search: {
        transform: (value) => ({ id: value }),
      },
    },
    {
      title: '讲师姓名',
      dataIndex: 'name',
      valueType: 'text',
      width: 160,
    },
    {
      title: '职称',
      dataIndex: 'title',
      valueType: 'text',
      width: 160,
    },
    {
      title: '擅长领域',
      dataIndex: 'expertise',
      valueType: 'text',
      width: 220,
      ellipsis: true,
    },
    {
      title: '头像',
      dataIndex: 'avatar',
      hideInSearch: true,
      width: 120,
      render: (value) => (value ? <img src={value} alt="avatar" style={{ width: 40, height: 40, borderRadius: 4 }} /> : '-'),
    },
    {
      title: '简介',
      dataIndex: 'introduction',
      hideInSearch: true,
      ellipsis: true,
      width: 260,
    },
    {
      title: '课程数',
      dataIndex: 'courseCount',
      hideInSearch: true,
      width: 100,
      render: (value) => value ?? 0,
    },
    {
      title: '学员数',
      dataIndex: 'studentCount',
      hideInSearch: true,
      width: 100,
      render: (value) => value ?? 0,
    },
    {
      title: '评分',
      dataIndex: 'averageRating',
      hideInSearch: true,
      width: 100,
      render: (value) => (value !== undefined && value !== null ? <Tag color="blue">{Number(value).toFixed(1)}</Tag> : '-'),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 180,
    },
    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      width: 140,
      fixed: 'right',
      render: (_, record) => (
        <Space direction="vertical" size="small" style={{ width: '100%' }}>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => {
              setCurrentRow(record);
              setUpdateVisible(true);
            }}
            style={{ padding: 0 }}
          >
            编辑
          </Button>
          <Popconfirm
            title="确认删除该讲师吗？"
            onConfirm={() => handleDelete(record)}
            okText="确认"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />} style={{ padding: 0 }}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer
      header={{
        title: '',
        ghost: true,
      }}
    >
      <Card>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>讲师管理</Title>
        </div>
        <ProTable<API.TeacherVO>
          headerTitle="讲师列表"
          actionRef={actionRef}
          rowKey="id"
          search={{
            labelWidth: 'auto',
            defaultCollapsed: false,
            layout: 'vertical',
            span: 6,
          }}
          toolBarRender={() => [
            <Button
              type="primary"
              key="create"
              onClick={() => setCreateVisible(true)}
            >
              <PlusOutlined /> 新增讲师
            </Button>,
          ]}
          request={async (params, sort) => {
            const sortField = Object.keys(sort || {})[0];
            const sortOrder = sortField ? resolveSortOrder(sort?.[sortField]) : undefined;
            const safePageSize = Math.min(Number(params.pageSize || 10), MAX_PAGE_SIZE);
            try {
              const { data, code } = await listTeacherVoByPageUsingPost({
                current: params.current,
                pageSize: safePageSize,
                id: params.id,
                name: params.name,
                title: params.title,
                expertise: params.expertise,
                sortField,
                sortOrder,
              } as API.TeacherQueryRequest);

              return {
                success: code === 0,
                data: data?.records || [],
                total: Number(data?.total) || 0,
              };
            } catch (error: any) {
              message.error(error?.message || '加载讲师列表失败');
              return {
                success: false,
                data: [],
                total: 0,
              };
            }
          }}
          columns={columns}
          pagination={{
            showQuickJumper: true,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20'],
            showTotal: (total) => `共 ${total} 条`,
          }}
          scroll={{ x: 1400 }}
        />
      </Card>

      <CreateModal
        visible={createVisible}
        onSubmit={() => {
          setCreateVisible(false);
          actionRef.current?.reload();
        }}
        onCancel={() => setCreateVisible(false)}
      />

      <UpdateModal
        visible={updateVisible}
        oldData={currentRow}
        onSubmit={() => {
          setUpdateVisible(false);
          setCurrentRow(undefined);
          actionRef.current?.reload();
        }}
        onCancel={() => {
          setUpdateVisible(false);
          setCurrentRow(undefined);
        }}
      />
    </PageContainer>
  );
};

export default TeacherAdminPage;
