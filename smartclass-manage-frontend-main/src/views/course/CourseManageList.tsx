import { deleteCourse, listCourses, resolvePageResult } from '@/api/course-admin';
import type { CourseItem, CourseListQuery } from '@/api/course-admin';
import { history } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Col,
  Form,
  Input,
  Modal,
  Row,
  Select,
  Space,
  Table,
  Typography,
  message,
} from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import React, { useEffect, useState } from 'react';
import { getCourseId, renderStatusTag } from './utils';

const { Text } = Typography;

const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '上架', value: 1 },
  { label: '下架', value: 2 },
];

const difficultyOptions = [
  { label: '入门', value: '入门' },
  { label: '初级', value: '初级' },
  { label: '中级', value: '中级' },
  { label: '高级', value: '高级' },
];

const CourseManageList: React.FC = () => {
  const [form] = Form.useForm<CourseListQuery>();
  const [list, setList] = useState<CourseItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
  });

  const fetchList = async (page = 1, pageSize = 10, extraParams: Partial<CourseListQuery> = {}) => {
    setLoading(true);
    try {
      const query = {
        ...form.getFieldsValue(),
        ...extraParams,
        current: page,
        pageSize,
      };
      const response = await listCourses(query);
      const pageResult = resolvePageResult(response);
      setList(pageResult.records);
      setPagination((prev) => ({
        ...prev,
        current: page,
        pageSize,
        total: pageResult.total,
      }));
    } catch (error: any) {
      message.error(error?.message || '加载课程列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList(1, pagination.pageSize || 10);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleDelete = async (record: CourseItem) => {
    const courseId = getCourseId(record);
    if (!courseId) {
      message.error('课程 ID 不存在，无法删除');
      return;
    }

    Modal.confirm({
      title: '确认逻辑删除课程？',
      content: `课程「${record.title || courseId}」将执行逻辑删除。`,
      okText: '确认删除',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteCourse({ courseId });
          message.success('课程删除成功');
          fetchList(pagination.current || 1, pagination.pageSize || 10);
        } catch (error: any) {
          message.error(error?.message || '课程删除失败');
        }
      },
    });
  };

  const columns: ColumnsType<CourseItem> = [
    {
      title: '课程ID',
      key: 'courseId',
      width: 100,
      render: (_, record) => getCourseId(record) || '-',
    },
    {
      title: '标题',
      dataIndex: 'title',
      render: (value) => value || '-',
    },
    {
      title: 'AI 学习助手知识内容',
      dataIndex: 'aiKnowleage',
      width: 280,
      ellipsis: true,
      render: (value) => value || '-',
    },
    {
      title: '分类',
      dataIndex: 'category',
      width: 120,
      render: (value) => value || '-',
    },
    {
      title: '难度',
      dataIndex: 'difficulty',
      width: 120,
      render: (value) => value || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 120,
      render: (_, record) => renderStatusTag(record.status),
    },
    {
      title: '讲师',
      key: 'teacher',
      width: 200,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text>{record.teacherName || '-'}</Text>
          <Text type="secondary">ID: {record.teacherId ?? '-'}</Text>
        </Space>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 180,
      render: (value) => value || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      render: (_, record) => {
        const courseId = getCourseId(record);
        return (
          <Space wrap>
            <Button
              type="link"
              onClick={() => {
                if (!courseId) {
                  message.error('课程 ID 不存在，无法编辑');
                  return;
                }
                history.push(`/admin/course-manage/edit/${courseId}`);
              }}
            >
              编辑
            </Button>
            <Button
              type="link"
              onClick={() => {
                if (!courseId) {
                  message.error('课程 ID 不存在，无法查看');
                  return;
                }
                history.push(`/admin/course-manage/detail/${courseId}`);
              }}
            >
              查看详情
            </Button>
            <Button danger type="link" onClick={() => handleDelete(record)}>
              删除
            </Button>
          </Space>
        );
      },
    },
  ];

  return (
    <PageContainer>
      <Card>
        <Form form={form} layout="vertical" onFinish={() => fetchList(1, pagination.pageSize || 10)}>
          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item label="标题" name="title">
                <Input placeholder="请输入课程标题" allowClear />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item label="分类" name="category">
                <Input placeholder="请输入分类" allowClear />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item label="难度" name="difficulty">
                <Select options={difficultyOptions} allowClear placeholder="请选择难度" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item label="状态" name="status">
                <Select options={statusOptions} allowClear placeholder="请选择状态" />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item label="讲师" name="teacher">
                <Input placeholder="讲师名称或ID" allowClear />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item label=" ">
                <Space>
                  <Button type="primary" htmlType="submit" loading={loading}>
                    查询
                  </Button>
                  <Button
                    onClick={() => {
                      form.resetFields();
                      fetchList(1, pagination.pageSize || 10, {});
                    }}
                  >
                    重置
                  </Button>
                  <Button type="dashed" onClick={() => history.push('/admin/course-manage/create')}>
                    创建课程
                  </Button>
                </Space>
              </Form.Item>
            </Col>
          </Row>
        </Form>

        <Table<CourseItem>
          rowKey={(record) => String(getCourseId(record) ?? `${record.title}-${record.createTime}`)}
          columns={columns}
          dataSource={list}
          loading={loading}
          pagination={pagination}
          onChange={(nextPagination) => {
            fetchList(nextPagination.current || 1, nextPagination.pageSize || 10);
          }}
          scroll={{ x: 1200 }}
        />
      </Card>
    </PageContainer>
  );
};

export default CourseManageList;
