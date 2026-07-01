import CreateModal from '@/pages/Admin/AiAvatar/components/CreateModal';
import UpdateModal from '@/pages/Admin/AiAvatar/components/UpdateModal';
import {
  deleteAiAvatarUsingDelete,
  getAiAvatarByIdUsingGet,
  listAiAvatarByPageAdminUsingGet,
} from '@/services/backend/aiAvatarController';
import { DeleteOutlined, EditOutlined, EyeOutlined, PlusOutlined, RobotOutlined, TagOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { Avatar, Badge, Button, Card, Descriptions, message, Modal, Popconfirm, Rate, Space, Spin, Tag, Typography } from 'antd';
import React, { useRef, useState } from 'react';
import './index.less';

const { Title } = Typography;

const parseTags = (raw?: string): string[] => {
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw);
    if (Array.isArray(parsed)) {
      return parsed.map((item) => String(item).trim()).filter(Boolean);
    }
  } catch (error) {
    // fallback for historical comma separated data
  }
  return raw
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
};

const renderPublicStatus = (isPublic?: number) =>
  isPublic === 1 ? <Badge status="success" text="公开" /> : <Badge status="default" text="私密" />;

const renderStatus = (status?: number) => {
  if (status === 1) return <Badge status="success" text="启用" />;
  if (status === 0) return <Badge status="default" text="禁用" />;
  return <Badge status="processing" text="未知" />;
};

const AiAvatarManagement: React.FC = () => {
  const [createModalVisible, setCreateModalVisible] = useState<boolean>(false);
  const [updateModalVisible, setUpdateModalVisible] = useState<boolean>(false);
  const [detailVisible, setDetailVisible] = useState<boolean>(false);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);
  const [currentRow, setCurrentRow] = useState<API.AiAvatar>();
  const [detailData, setDetailData] = useState<API.AiAvatarVO>();
  const actionRef = useRef<ActionType>();

  const handleDelete = async (row: API.AiAvatar) => {
    const hide = message.loading('正在删除');
    try {
      await deleteAiAvatarUsingDelete({ id: Number(row.id) });
      hide();
      message.success('删除成功');
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      hide();
      message.error(`删除失败：${error?.message || '请稍后重试'}`);
      return false;
    }
  };

  const handleViewDetail = async (id?: number) => {
    if (!id) {
      message.error('智能体 ID 无效');
      return;
    }
    setDetailVisible(true);
    setDetailLoading(true);
    try {
      const res = await getAiAvatarByIdUsingGet({ id });
      setDetailData(res.data);
    } catch (error: any) {
      setDetailVisible(false);
      message.error(error?.message || '加载详情失败');
    } finally {
      setDetailLoading(false);
    }
  };

  const columns: ProColumns<API.AiAvatar>[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
      valueType: 'text',
      search: { transform: (value) => ({ id: value }) },
    },
    {
      title: '名称',
      dataIndex: 'name',
      width: 180,
      render: (_, record) => (
        <Space>
          <Avatar src={record.avatarImgUrl} icon={<RobotOutlined />} size="small" />
          <span style={{ fontWeight: 600 }}>{record.name || '-'}</span>
        </Space>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      width: 220,
      hideInSearch: true,
      render: (_, record) => (
        <Typography.Paragraph ellipsis={{ rows: 2 }} style={{ marginBottom: 0, minWidth: 150 }}>
          {record.description || '-'}
        </Typography.Paragraph>
      ),
    },
    {
      title: '能力',
      dataIndex: 'abilities',
      width: 220,
      hideInSearch: true,
      render: (_, record) => (
        <Typography.Paragraph ellipsis={{ rows: 2 }} style={{ marginBottom: 0, minWidth: 150 }}>
          {record.abilities || '-'}
        </Typography.Paragraph>
      ),
    },
    {
      title: '人格',
      dataIndex: 'personality',
      width: 220,
      hideInSearch: true,
      render: (_, record) => (
        <Typography.Paragraph ellipsis={{ rows: 2 }} style={{ marginBottom: 0, minWidth: 150 }}>
          {record.personality || '-'}
        </Typography.Paragraph>
      ),
    },
    {
      title: '标签',
      dataIndex: 'tags',
      width: 220,
      render: (_, record) => {
        const tags = parseTags(record.tags);
        if (!tags.length) return '-';
        return (
          <Space wrap>
            {tags.map((tag) => (
              <Tag key={tag} color="blue">
                <TagOutlined /> {tag}
              </Tag>
            ))}
          </Space>
        );
      },
    },
    {
      title: '公开',
      dataIndex: 'isPublic',
      valueType: 'select',
      valueEnum: { 1: { text: '公开' }, 0: { text: '私密' } },
      width: 90,
      render: (_, record) => renderPublicStatus(record.isPublic),
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 1: { text: '启用' }, 0: { text: '禁用' } },
      width: 90,
      render: (_, record) => renderStatus(record.status),
    },
    {
      title: '评分',
      dataIndex: 'rating',
      hideInSearch: true,
      width: 180,
      render: (_, record) => (
        <Space>
          <Rate disabled allowHalf value={record.rating || 0} />
          <span>({record.ratingCount || 0})</span>
        </Space>
      ),
    },
    { title: '使用次数', dataIndex: 'usageCount', width: 100, sorter: true, search: false },
    { title: '排序', dataIndex: 'sort', width: 80, sorter: true, search: false },
    { title: '创建者', dataIndex: 'creatorId', width: 100, hideInSearch: true },
    { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime', width: 170, sorter: true },
    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      width: 140,
      fixed: 'right',
      render: (_, record) => (
        <Space direction="vertical" size="small" style={{ width: '100%' }}>
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleViewDetail(record.id)} style={{ padding: 0 }}>
            详情
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            style={{ padding: 0 }}
            onClick={() => {
              setCurrentRow(record);
              setUpdateModalVisible(true);
            }}
          >
            编辑
          </Button>
          <Popconfirm title="确认删除该智能体吗？" onConfirm={() => handleDelete(record)} okText="确认" cancelText="取消">
            <Button type="link" danger icon={<DeleteOutlined />} style={{ padding: 0 }}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer>
      <div className="ai-avatar-management">
        <Card>
          <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 }}>
            <RobotOutlined style={{ fontSize: 24, marginRight: 8, color: '#1890ff' }} />
            <Title level={4} style={{ margin: 0 }}>
              智能体管理
            </Title>
          </div>

          <ProTable<API.AiAvatar>
            headerTitle="智能体列表"
            actionRef={actionRef}
            rowKey="id"
            columns={columns}
            search={{ labelWidth: 'auto', defaultCollapsed: false, layout: 'vertical', span: 6 }}
            toolBarRender={() => [
              <Button key="create" type="primary" onClick={() => setCreateModalVisible(true)}>
                <PlusOutlined /> 新建智能体
              </Button>,
            ]}
            request={async (params, sort) => {
              const sortField = Object.keys(sort || {})?.[0];
              const sortOrder = sortField ? (sort as any)[sortField] : undefined;
              const { data, code } = await listAiAvatarByPageAdminUsingGet({
                ...params,
                current: params.current,
                pageSize: params.pageSize,
                sortField,
                sortOrder,
              });
              return {
                success: code === 0,
                data: data?.records || [],
                total: data?.total || 0,
              };
            }}
            pagination={{
              defaultPageSize: 10,
              showQuickJumper: true,
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '50'],
              showTotal: (total) => `共 ${total} 条记录`,
            }}
            scroll={{ x: 1800 }}
            tableLayout="fixed"
          />

          <CreateModal
            visible={createModalVisible}
            columns={columns}
            onSubmit={() => {
              setCreateModalVisible(false);
              actionRef.current?.reload();
            }}
            onCancel={() => setCreateModalVisible(false)}
          />

          <UpdateModal
            visible={updateModalVisible}
            oldData={currentRow}
            columns={columns}
            onSubmit={() => {
              setUpdateModalVisible(false);
              setCurrentRow(undefined);
              actionRef.current?.reload();
            }}
            onCancel={() => {
              setUpdateModalVisible(false);
              setCurrentRow(undefined);
            }}
          />
        </Card>

        <Modal
          title="智能体详情"
          open={detailVisible}
          footer={null}
          width={900}
          onCancel={() => {
            setDetailVisible(false);
            setDetailData(undefined);
          }}
        >
          <Spin spinning={detailLoading}>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="ID">{detailData?.id ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="名称">{detailData?.name ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="Base URL">{detailData?.baseUrl ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="创建者">{detailData?.creatorId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="是否公开">{detailData?.isPublic === 1 ? '公开' : '私密'}</Descriptions.Item>
              <Descriptions.Item label="状态">{detailData?.status === 1 ? '启用' : '禁用'}</Descriptions.Item>
              <Descriptions.Item label="使用次数">{detailData?.usageCount ?? 0}</Descriptions.Item>
              <Descriptions.Item label="评分">
                {detailData?.rating ?? 0}（{detailData?.ratingCount ?? 0}）
              </Descriptions.Item>
              <Descriptions.Item label="排序">{detailData?.sort ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{detailData?.createTime ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="更新时间" span={2}>
                {detailData?.updateTime ?? '-'}
              </Descriptions.Item>
              <Descriptions.Item label="标签" span={2}>
                <Space wrap>
                  {parseTags(detailData?.tags).map((tag) => (
                    <Tag key={tag} color="blue">
                      {tag}
                    </Tag>
                  ))}
                  {!parseTags(detailData?.tags).length ? '-' : null}
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="描述" span={2}>
                <Typography.Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
                  {detailData?.description || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label="人格" span={2}>
                <Typography.Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
                  {detailData?.personality || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label="能力" span={2}>
                <Typography.Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
                  {detailData?.abilities || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
            </Descriptions>
          </Spin>
        </Modal>
      </div>
    </PageContainer>
  );
};

export default AiAvatarManagement;
