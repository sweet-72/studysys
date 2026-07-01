import { getCourseId, renderStatusTag } from '@/views/course/utils';
import { listCourses, resolvePageResult } from '@/api/course-admin';
import type { CourseItem, CourseListQuery } from '@/api/course-admin';
import { getTopCategoriesUsingGet } from '@/services/backend/courseCategoryController';
import { history } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import { Alert, Button, Card, Col, Form, Input, Row, Select, Space, Table, message } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import React, { useEffect, useState } from 'react';

type CategoryOption = {
  label: string;
  value: number;
};

const difficultyOptions = [
  { label: '\u5165\u95e8', value: '\u5165\u95e8' },
  { label: '\u521d\u7ea7', value: '\u521d\u7ea7' },
  { label: '\u4e2d\u7ea7', value: '\u4e2d\u7ea7' },
  { label: '\u9ad8\u7ea7', value: '\u9ad8\u7ea7' },
];

const CourseListPage: React.FC = () => {
  const [form] = Form.useForm<CourseListQuery>();
  const [loading, setLoading] = useState(false);
  const [categoryLoading, setCategoryLoading] = useState(false);
  const [categoryEmpty, setCategoryEmpty] = useState(false);
  const [categoryOptions, setCategoryOptions] = useState<CategoryOption[]>([]);
  const [dataSource, setDataSource] = useState<CourseItem[]>([]);
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
  });

  const loadCategories = async () => {
    setCategoryLoading(true);
    try {
      const response = await getTopCategoriesUsingGet();
      const list = Array.isArray(response.data) ? response.data : [];
      setCategoryEmpty(list.length === 0);
      setCategoryOptions(
        list
          .filter((item) => item?.id !== undefined && item?.id !== null)
          .map((item) => ({
            label: item.name || `\u5206\u7c7b-${item.id}`,
            value: Number(item.id),
          })),
      );
    } catch (error: any) {
      message.error(error?.message || '\u52a0\u8f7d\u8bfe\u7a0b\u5206\u7c7b\u5931\u8d25');
      setCategoryEmpty(false);
      setCategoryOptions([]);
    } finally {
      setCategoryLoading(false);
    }
  };

  const loadList = async (page = 1, pageSize = 10) => {
    setLoading(true);
    try {
      const response = await listCourses({
        ...form.getFieldsValue(),
        current: page,
        pageSize,
      });
      const pageData = resolvePageResult(response);
      setDataSource(pageData.records);
      setPagination((prev) => ({
        ...prev,
        current: page,
        pageSize,
        total: pageData.total,
      }));
    } catch (error: any) {
      message.error(error?.message || '\u52a0\u8f7d\u8bfe\u7a0b\u5217\u8868\u5931\u8d25');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
    loadList(1, pagination.pageSize || 10);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const renderCategory = (record: CourseItem) =>
    record.categoryName ||
    record.category ||
    categoryOptions.find((item) => item.value === Number(record.categoryId))?.label ||
    record.categoryId ||
    '-';

  const columns: ColumnsType<CourseItem> = [
    {
      title: '\u8bfe\u7a0bID',
      key: 'courseId',
      width: 100,
      render: (_, record) => getCourseId(record) || '-',
    },
    {
      title: '\u6807\u9898',
      dataIndex: 'title',
      render: (value) => value || '-',
    },
    {
      title: '\u5206\u7c7b',
      key: 'categoryId',
      width: 140,
      render: (_, record) => renderCategory(record),
    },
    {
      title: '\u96be\u5ea6',
      dataIndex: 'difficulty',
      width: 120,
      render: (value) => value || '-',
    },
    {
      title: '\u72b6\u6001',
      dataIndex: 'status',
      width: 120,
      render: (_, record) => renderStatusTag(record.status),
    },
    {
      title: '\u8bb2\u5e08',
      key: 'teacher',
      width: 160,
      render: (_, record) => record.teacherName || '-',
    },
    {
      title: '\u521b\u5efa\u65f6\u95f4',
      dataIndex: 'createTime',
      width: 180,
      render: (value) => value || '-',
    },
    {
      title: '\u64cd\u4f5c',
      key: 'action',
      width: 180,
      render: (_, record) => {
        const courseId = getCourseId(record);
        return (
          <Space>
            <Button
              type="link"
              onClick={() => {
                if (!courseId) {
                  message.error('\u8bfe\u7a0bID\u4e0d\u5b58\u5728');
                  return;
                }
                history.push(`/course/detail/${courseId}`);
              }}
            >
              {'\u67e5\u770b\u8be6\u60c5'}
            </Button>
            <Button
              type="link"
              onClick={() => {
                if (!courseId) {
                  message.error('\u8bfe\u7a0bID\u4e0d\u5b58\u5728');
                  return;
                }
                history.push(`/course/learn/${courseId}`);
              }}
            >
              {'\u53bb\u5b66\u4e60'}
            </Button>
          </Space>
        );
      },
    },
  ];

  return (
    <PageContainer>
      <Card>
        <Form form={form} layout="vertical" onFinish={() => loadList(1, pagination.pageSize || 10)}>
          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item label={'\u8bfe\u7a0b\u6807\u9898'} name="title">
                <Input placeholder={'\u8bf7\u8f93\u5165\u8bfe\u7a0b\u6807\u9898'} allowClear />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item label={'\u5206\u7c7b'} name="categoryId">
                <Select
                  showSearch
                  optionFilterProp="label"
                  options={categoryOptions}
                  loading={categoryLoading}
                  allowClear
                  placeholder={'\u8bf7\u9009\u62e9\u5206\u7c7b'}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item label={'\u96be\u5ea6'} name="difficulty">
                <Select options={difficultyOptions} allowClear placeholder={'\u8bf7\u9009\u62e9\u96be\u5ea6'} />
              </Form.Item>
            </Col>
          </Row>

          {categoryEmpty ? (
            <Alert
              type="warning"
              showIcon
              message="暂无课程分类，请先到课程分类管理中新增分类"
              style={{ marginBottom: 16 }}
            />
          ) : null}

          <Space style={{ marginBottom: 16 }}>
            <Button type="primary" htmlType="submit" loading={loading}>
              {'\u67e5\u8be2'}
            </Button>
            <Button
              onClick={() => {
                form.resetFields();
                loadList(1, pagination.pageSize || 10);
              }}
            >
              {'\u91cd\u7f6e'}
            </Button>
          </Space>
        </Form>

        <Table<CourseItem>
          rowKey={(record) => String(getCourseId(record) ?? `${record.title}-${record.createTime}`)}
          columns={columns}
          dataSource={dataSource}
          loading={loading}
          pagination={pagination}
          onChange={(nextPagination) => {
            loadList(nextPagination.current || 1, nextPagination.pageSize || 10);
          }}
        />
      </Card>
    </PageContainer>
  );
};

export default CourseListPage;
