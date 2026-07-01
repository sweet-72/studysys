import { getCourseDetail, listCourses, resolvePageResult, reviewCourse } from '@/api/course-admin';
import type { CourseChapterItem, CourseListQuery, CourseSectionItem } from '@/api/course-admin';
import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import React, { useEffect, useState } from 'react';
import { getCourseId } from './utils';

const { Text } = Typography;

interface ReviewRow {
  key: string;
  assignmentId?: number;
  courseId: number;
  courseTitle: string;
  chapterId?: number;
  chapterTitle?: string;
  sectionId?: number;
  sectionTitle?: string;
  assignmentTitle?: string;
  assignmentStatus?: string;
}

interface ReviewDraft {
  score?: number;
  comment?: string;
}

const isPendingAssignment = (status?: string) => {
  if (!status) {
    return true;
  }
  const value = status.toUpperCase();
  return value === 'PENDING' || value === 'WAIT_REVIEW' || value === 'UNREVIEWED';
};

const buildRowsFromDetail = (
  courseId: number,
  courseTitle: string,
  chapters?: CourseChapterItem[],
): ReviewRow[] => {
  if (!Array.isArray(chapters) || chapters.length === 0) {
    return [
      {
        key: `course-${courseId}`,
        courseId,
        courseTitle,
        assignmentTitle: '课程级作业',
      },
    ];
  }

  const rows: ReviewRow[] = [];
  chapters.forEach((chapter, chapterIndex) => {
    const chapterId = Number(chapter.chapterId ?? 0) || undefined;
    const chapterTitle = chapter.title || `章节${chapterIndex + 1}`;
    const sections = Array.isArray(chapter.sections) ? chapter.sections : [];

    sections.forEach((section: CourseSectionItem, sectionIndex) => {
      if (!isPendingAssignment(section.assignmentStatus)) {
        return;
      }
      const sectionId = Number(section.sectionId ?? 0) || undefined;
      rows.push({
        key: `course-${courseId}-chapter-${chapterId || chapterIndex}-section-${sectionId || sectionIndex}`,
        assignmentId: section.assignmentId,
        courseId,
        courseTitle,
        chapterId,
        chapterTitle,
        sectionId,
        sectionTitle: section.title || `小节${sectionIndex + 1}`,
        assignmentTitle: section.assignmentTitle || section.title || '小节作业',
        assignmentStatus: section.assignmentStatus,
      });
    });
  });

  if (rows.length === 0) {
    rows.push({
      key: `course-${courseId}-fallback`,
      courseId,
      courseTitle,
      assignmentTitle: '课程级作业',
      assignmentStatus: 'PENDING',
    });
  }

  return rows;
};

const CourseReview: React.FC = () => {
  const [searchForm] = Form.useForm<CourseListQuery>();
  const [loading, setLoading] = useState(false);
  const [submittingKey, setSubmittingKey] = useState<string>('');
  const [rows, setRows] = useState<ReviewRow[]>([]);
  const [drafts, setDrafts] = useState<Record<string, ReviewDraft>>({});
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 5,
    total: 0,
    showSizeChanger: true,
  });

  const loadData = async (page = 1, pageSize = 5) => {
    setLoading(true);
    try {
      const listResponse = await listCourses({
        ...searchForm.getFieldsValue(),
        current: page,
        pageSize,
      });
      const pageResult = resolvePageResult(listResponse);
      const courseRows = pageResult.records;

      const detailResults = await Promise.allSettled(
        courseRows.map(async (item) => {
          const courseId = getCourseId(item);
          if (!courseId) {
            return [] as ReviewRow[];
          }
          const detailResponse = await getCourseDetail({ courseId });
          const detailData = (detailResponse.data || {}) as Record<string, any>;
          return buildRowsFromDetail(courseId, String(item.title || detailData.title || '-'), detailData.chapters);
        }),
      );

      const flattenedRows = detailResults.flatMap((result, idx) => {
        if (result.status === 'fulfilled') {
          return result.value;
        }

        // 某个详情失败时，回退为课程级待批条目，保证页面可用
        const fallbackCourseId = getCourseId(courseRows[idx]);
        if (!fallbackCourseId) {
          return [];
        }
        return [
          {
            key: `fallback-${fallbackCourseId}`,
            courseId: fallbackCourseId,
            courseTitle: String(courseRows[idx]?.title || '-'),
            assignmentTitle: '课程级作业',
          },
        ];
      });

      setRows(flattenedRows);
      setPagination((prev) => ({
        ...prev,
        current: page,
        pageSize,
        total: pageResult.total,
      }));
    } catch (error: any) {
      message.error(error?.message || '加载待批作业失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData(1, pagination.pageSize || 5);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const updateDraft = (key: string, patch: ReviewDraft) => {
    setDrafts((prev) => ({
      ...prev,
      [key]: {
        ...prev[key],
        ...patch,
      },
    }));
  };

  const handleSubmitReview = async (row: ReviewRow) => {
    const draft = drafts[row.key] || {};
    if (draft.score === undefined || draft.score === null) {
      message.error('请填写打分后再提交');
      return;
    }
    if (draft.score < 0 || draft.score > 100) {
      message.error('分数范围必须在 0-100 之间');
      return;
    }
    if (!draft.comment || !draft.comment.trim()) {
      message.error('请填写评语后再提交');
      return;
    }

    setSubmittingKey(row.key);
    try {
      await reviewCourse({
        reviewId: row.assignmentId,
        assignmentId: row.assignmentId,
        courseId: row.courseId,
        chapterId: row.chapterId,
        sectionId: row.sectionId,
        score: draft.score,
        comment: draft.comment.trim(),
      });
      message.success('作业批改提交成功');
      setRows((prev) => prev.filter((item) => item.key !== row.key));
      setDrafts((prev) => {
        const next = { ...prev };
        delete next[row.key];
        return next;
      });
    } catch (error: any) {
      message.error(error?.message || '作业批改提交失败');
    } finally {
      setSubmittingKey('');
    }
  };

  const columns: ColumnsType<ReviewRow> = [
    {
      title: '课程/章节/小节',
      key: 'path',
      width: 260,
      render: (_, row) => (
        <Space direction="vertical" size={0}>
          <Text strong>{row.courseTitle}</Text>
          <Text type="secondary">{row.chapterTitle || '-'}</Text>
          <Text type="secondary">{row.sectionTitle || '-'}</Text>
        </Space>
      ),
    },
    {
      title: '作业标题',
      dataIndex: 'assignmentTitle',
      width: 220,
      render: (value, row) => value || `作业-${row.courseId}`,
    },
    {
      title: '状态',
      dataIndex: 'assignmentStatus',
      width: 120,
      render: (value) => <Tag color={isPendingAssignment(value) ? 'processing' : 'success'}>{value || 'PENDING'}</Tag>,
    },
    {
      title: '打分(0-100)',
      key: 'score',
      width: 160,
      render: (_, row) => (
        <InputNumber
          min={0}
          max={100}
          precision={0}
          style={{ width: '100%' }}
          value={drafts[row.key]?.score}
          onChange={(value) => updateDraft(row.key, { score: value ?? undefined })}
          placeholder="请输入分数"
        />
      ),
    },
    {
      title: '评语',
      key: 'comment',
      render: (_, row) => (
        <Input.TextArea
          rows={2}
          value={drafts[row.key]?.comment}
          onChange={(event) => updateDraft(row.key, { comment: event.target.value })}
          placeholder="请输入评语"
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, row) => (
        <Button type="primary" loading={submittingKey === row.key} onClick={() => handleSubmitReview(row)}>
          提交批改
        </Button>
      ),
    },
  ];

  return (
    <PageContainer>
      <Card>
        <Form form={searchForm} layout="inline" onFinish={() => loadData(1, pagination.pageSize || 5)}>
          <Form.Item name="title" label="课程标题">
            <Input allowClear placeholder="输入课程标题" style={{ width: 220 }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading}>
                查询
              </Button>
              <Button
                onClick={() => {
                  searchForm.resetFields();
                  loadData(1, pagination.pageSize || 5);
                }}
              >
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>

        <Table<ReviewRow>
          style={{ marginTop: 16 }}
          rowKey="key"
          dataSource={rows}
          columns={columns}
          loading={loading}
          pagination={pagination}
          onChange={(nextPagination) => {
            loadData(nextPagination.current || 1, nextPagination.pageSize || 5);
          }}
          locale={{ emptyText: '暂无待批作业' }}
          scroll={{ x: 1200 }}
        />
      </Card>
    </PageContainer>
  );
};

export default CourseReview;
