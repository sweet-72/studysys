import React, { useEffect, useMemo, useState } from 'react';
import { Button, Form, InputNumber, Select, Space, Table, Tag, message } from 'antd';
import { PageContainer } from '@ant-design/pro-components';
import { useLocation } from '@umijs/max';
import {
  listCourseHomeworkSubmissionsByPage,
  resolveCourseHomeworkReviewStatus,
} from '@/api/courseHomeworkSubmission';
import ReviewDrawer from './components/ReviewDrawer';

const PAGE_SIZE_LIMIT = 20;

const CourseReview = () => {
  const location = useLocation();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState([]);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [currentSubmissionId, setCurrentSubmissionId] = useState();
  const [drawerOpen, setDrawerOpen] = useState(false);

  const initialCourseId = useMemo(() => {
    const query = new URLSearchParams(location.search);
    const value = Number(query.get('courseId'));
    return Number.isFinite(value) && value > 0 ? value : undefined;
  }, [location.search]);

  const loadData = async (nextPage = page, nextPageSize = pageSize, extraValues) => {
    setLoading(true);
    try {
      const values = {
        ...form.getFieldsValue(),
        ...extraValues,
      };
      const safePageSize = Math.min(Number(nextPageSize || PAGE_SIZE_LIMIT), PAGE_SIZE_LIMIT);
      const res = await listCourseHomeworkSubmissionsByPage({
        courseId: values.courseId,
        sectionId: values.sectionId,
        homeworkId: values.homeworkId,
        reviewStatus: values.reviewStatus,
        current: nextPage,
        pageSize: safePageSize,
      });
      setRows(res.data?.records || []);
      setTotal(res.data?.total || 0);
      setPage(nextPage);
      setPageSize(safePageSize);
    } catch (error) {
      message.error(error?.message || '加载作业提交列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    form.setFieldsValue({
      courseId: initialCourseId,
      reviewStatus: undefined,
    });
    loadData(1, pageSize, {
      courseId: initialCourseId,
    });
  }, [initialCourseId]);

  const openDrawer = (record) => {
    setCurrentSubmissionId(record.id);
    setDrawerOpen(true);
  };

  return (
    <PageContainer>
      <Form
        form={form}
        layout="inline"
        onFinish={() => loadData(1, pageSize)}
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="courseId" label={'课程ID'}>
          <InputNumber min={1} precision={0} placeholder={'课程ID'} style={{ width: 140 }} />
        </Form.Item>
        <Form.Item name="sectionId" label={'小节ID'}>
          <InputNumber min={1} precision={0} placeholder={'小节ID'} style={{ width: 140 }} />
        </Form.Item>
        <Form.Item name="homeworkId" label={'作业ID'}>
          <InputNumber min={1} precision={0} placeholder={'作业ID'} style={{ width: 140 }} />
        </Form.Item>
        <Form.Item name="reviewStatus" label={'批改状态'}>
          <Select
            allowClear
            placeholder={'全部状态'}
            style={{ width: 140 }}
            options={[
              { label: '待批改', value: 0 },
              { label: '已批改', value: 1 },
            ]}
          />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={loading}>
              {'查询'}
            </Button>
            <Button
              onClick={() => {
                form.resetFields();
                form.setFieldsValue({ courseId: initialCourseId });
                loadData(1, pageSize, { courseId: initialCourseId });
              }}
            >
              {'重置'}
            </Button>
          </Space>
        </Form.Item>
      </Form>

      <Table
        rowKey="id"
        loading={loading}
        dataSource={rows}
        columns={[
          { title: '提交ID', dataIndex: 'id', width: 100 },
          { title: '作业标题', dataIndex: 'homeworkTitle', ellipsis: true },
          {
            title: '学生',
            dataIndex: 'studentName',
            width: 160,
            render: (value, record) => value || record.studentId || '-',
          },
          { title: '提交时间', dataIndex: 'submitTime', width: 180, render: (value) => value || '-' },
          {
            title: '批改状态',
            width: 120,
            render: (_, record) => {
              const status = resolveCourseHomeworkReviewStatus(record.reviewStatus);
              return <Tag color={status.color}>{status.label}</Tag>;
            },
          },
          {
            title: '批改分数',
            width: 100,
            render: (_, record) => record.reviewScore ?? record.score ?? '-',
          },
          {
            title: '操作',
            width: 140,
            render: (_, record) => (
              <Button type="link" onClick={() => openDrawer(record)}>
                {'查看 / 批改'}
              </Button>
            ),
          },
        ]}
        pagination={{
          current: page,
          pageSize,
          total,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20'],
          onChange: (nextPage, nextPageSize) => loadData(nextPage, nextPageSize),
        }}
      />

      <ReviewDrawer
        open={drawerOpen}
        submissionId={currentSubmissionId}
        onClose={() => setDrawerOpen(false)}
        onReviewed={() => loadData(page, pageSize)}
      />
    </PageContainer>
  );
};

export default CourseReview;
