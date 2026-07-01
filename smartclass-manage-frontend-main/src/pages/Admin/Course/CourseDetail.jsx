import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, Empty, Space, Spin, Tabs, Tag, Typography, message } from 'antd';
import { PageContainer } from '@ant-design/pro-components';
import { history, useParams } from '@umijs/max';
import moment from 'moment';
import { getCourseVoByIdUsingGet, updateCourseStatusUsingPost } from '@/services/backend/courseController';
import { getTopCategoriesUsingGet } from '@/services/backend/courseCategoryController';
import SectionManager from './components/SectionManager';
import HomeworkManager from './components/HomeworkManager';

const { Paragraph, Text, Title } = Typography;

const statusMap = {
  0: <Tag>{'\u8349\u7a3f'}</Tag>,
  1: <Tag color="success">{'\u5df2\u53d1\u5e03'}</Tag>,
  2: <Tag color="warning">{'\u5df2\u4e0b\u67b6'}</Tag>,
};

const difficultyMap = {
  1: '\u5165\u95e8',
  2: '\u521d\u7ea7',
  3: '\u4e2d\u7ea7',
  4: '\u9ad8\u7ea7',
  5: '\u4e13\u5bb6',
};

const infoGridStyle = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
  gap: 16,
};

const infoItemStyle = {
  padding: '12px 14px',
  border: '1px solid #f0f0f0',
  borderRadius: 6,
  background: '#fafafa',
};

const InfoItem = ({ label, children }) => (
  <div style={infoItemStyle}>
    <Text type="secondary">{label}</Text>
    <div style={{ marginTop: 6 }}>{children || '-'}</div>
  </div>
);

const CourseDetail = () => {
  const { id } = useParams();
  const courseId = Number(id);
  const [loading, setLoading] = useState(false);
  const [course, setCourse] = useState(null);
  const [categoryOptions, setCategoryOptions] = useState([]);
  const [aiKnowledgeExpanded, setAiKnowledgeExpanded] = useState(false);

  const validCourseId = useMemo(() => Number.isFinite(courseId) && courseId > 0, [courseId]);

  const loadCategories = async () => {
    try {
      const res = await getTopCategoriesUsingGet();
      const list = Array.isArray(res.data) ? res.data : [];
      setCategoryOptions(
        list
          .filter((item) => item?.id !== undefined && item?.id !== null)
          .map((item) => ({ label: item.name || `\u5206\u7c7b-${item.id}`, value: Number(item.id) })),
      );
    } catch (error) {
      setCategoryOptions([]);
    }
  };

  const loadDetail = async () => {
    if (!validCourseId) {
      return;
    }
    setLoading(true);
    try {
      const res = await getCourseVoByIdUsingGet({ id: courseId });
      setCourse(res.data || null);
    } catch (error) {
      message.error(error?.message || '\u52a0\u8f7d\u8bfe\u7a0b\u8be6\u60c5\u5931\u8d25');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
    loadDetail();
  }, [courseId]);

  const toggleCourseStatus = async () => {
    if (!course?.id) {
      message.error('\u8bfe\u7a0bID\u4e0d\u5b58\u5728');
      return;
    }

    const nextStatus = course.status === 2 ? 1 : 2;
    try {
      await updateCourseStatusUsingPost({ id: course.id, status: nextStatus });
      message.success(nextStatus === 1 ? '\u8bfe\u7a0b\u5df2\u4e0a\u67b6' : '\u8bfe\u7a0b\u5df2\u4e0b\u67b6');
      await loadDetail();
    } catch (error) {
      message.error(error?.message || '\u72b6\u6001\u66f4\u65b0\u5931\u8d25');
    }
  };

  const categoryText =
    course?.categoryName ||
    categoryOptions.find((item) => item.value === Number(course?.categoryId))?.label ||
    (course?.category && !/^\d+$/.test(String(course.category)) ? course.category : '') ||
    '未分类';

  if (!validCourseId) {
    return (
      <PageContainer>
        <Empty description={'\u8bfe\u7a0b ID \u65e0\u6548'} />
      </PageContainer>
    );
  }

  return (
    <PageContainer>
      <Spin spinning={loading}>
        <Card style={{ marginBottom: 16 }}>
          <Space direction="vertical" size={12} style={{ width: '100%' }}>
            <Space style={{ width: '100%', justifyContent: 'space-between' }} wrap>
              <Title level={4} style={{ margin: 0 }}>
                {course?.title || '\u8bfe\u7a0b\u8be6\u60c5'}
              </Title>
              <Space wrap>
                <Button onClick={() => history.push('/admin/courseManagement')}>{'\u8fd4\u56de\u5217\u8868'}</Button>
                <Button onClick={() => history.push(`/admin/courseManagement/review?courseId=${courseId}`)}>
                  {'\u4f5c\u4e1a\u6279\u6539'}
                </Button>
                <Button type="primary" onClick={() => history.push(`/admin/courseManagement?editId=${courseId}`)}>
                  {'\u7f16\u8f91\u8bfe\u7a0b'}
                </Button>
                <Button onClick={toggleCourseStatus} disabled={!course?.id}>
                  {course?.status === 2 ? '\u4e0a\u67b6' : '\u4e0b\u67b6'}
                </Button>
              </Space>
            </Space>

            {!course ? (
              <Empty description={'\u6682\u65e0\u8bfe\u7a0b\u8be6\u60c5\u6570\u636e'} />
            ) : (
              <>
                <div style={infoGridStyle}>
                  <InfoItem label="分类">{categoryText}</InfoItem>
                  <InfoItem label="难度">{difficultyMap[course.difficulty || 0] || course.difficulty || '-'}</InfoItem>
                  <InfoItem label="讲师">{course.teacherName || course.teacherId || '-'}</InfoItem>
                  <InfoItem label="状态">{statusMap[course.status || 0] || '-'}</InfoItem>
                  <InfoItem label="创建时间">
                    {course.createTime ? moment(course.createTime).format('YYYY-MM-DD HH:mm') : '-'}
                  </InfoItem>
                  <InfoItem label="课程ID">{course.id}</InfoItem>
                </div>
                <Card size="small" title="课程简介" style={{ marginTop: 16 }}>
                  <Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>{course.description || '-'}</Paragraph>
                </Card>
                <Card size="small" title="AI 学习助手知识" style={{ marginTop: 16 }}>
                  <Paragraph
                    style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}
                    ellipsis={
                      course.aiKnowleage && !aiKnowledgeExpanded
                        ? {
                            rows: 4,
                          }
                        : false
                    }
                  >
                    {course.aiKnowleage || '暂无 AI 学习助手知识内容'}
                  </Paragraph>
                  {course.aiKnowleage ? (
                    <Button type="link" size="small" style={{ padding: 0 }} onClick={() => setAiKnowledgeExpanded((value) => !value)}>
                      {aiKnowledgeExpanded ? '收起' : '展开'}
                    </Button>
                  ) : null}
                </Card>
              </>
            )}
          </Space>
        </Card>

        <Tabs
          items={[
            {
              key: 'section',
              label: '\u7ae0\u8282 / \u5c0f\u8282\u7ba1\u7406',
              children: <SectionManager courseId={courseId} />,
            },
            {
              key: 'homework',
              label: '\u4f5c\u4e1a\u7ba1\u7406',
              children: <HomeworkManager courseId={courseId} />,
            },
          ]}
        />
      </Spin>
    </PageContainer>
  );
};

export default CourseDetail;
