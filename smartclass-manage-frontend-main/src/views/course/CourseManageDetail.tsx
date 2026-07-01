import { getCourseDetail } from '@/api/course-admin';
import type { CourseChapterItem, CourseSectionItem } from '@/api/course-admin';
import { history, useParams } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Collapse,
  Descriptions,
  Empty,
  List,
  Space,
  Spin,
  Tag,
  Typography,
  message,
} from 'antd';
import React, { useEffect, useMemo, useState } from 'react';
import { renderStatusTag } from './utils';

const { Paragraph, Text, Link } = Typography;

interface CourseDetailState {
  courseId?: number;
  title?: string;
  aiKnowleage?: string;
  category?: string;
  difficulty?: string;
  status?: number | string;
  teacherId?: number;
  teacherName?: string;
  description?: string;
  createTime?: string;
  updateTime?: string;
  chapters?: CourseChapterItem[];
}

const renderSection = (section: CourseSectionItem) => (
  <List.Item key={`${section.sectionId || section.title}`}>
    <Space direction="vertical" style={{ width: '100%' }}>
      <Text strong>{section.title || '-'}</Text>
      {section.description ? <Text type="secondary">{section.description}</Text> : null}
      <Space wrap>
        {section.videoUrl ? (
          <Tag color="processing">
            视频URL：
            <Link href={section.videoUrl} target="_blank" rel="noreferrer">
              打开链接
            </Link>
          </Tag>
        ) : null}
        {section.localVideoPath ? <Tag color="success">localVideoPath：{section.localVideoPath}</Tag> : null}
        {!section.videoUrl && !section.localVideoPath ? <Tag color="error">未配置视频资源</Tag> : null}
      </Space>
    </Space>
  </List.Item>
);

const CourseManageDetail: React.FC = () => {
  const params = useParams<{ courseId: string }>();
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<CourseDetailState>();

  const courseId = useMemo(() => {
    const value = Number(params.courseId);
    return Number.isNaN(value) ? undefined : value;
  }, [params.courseId]);

  useEffect(() => {
    if (!courseId) {
      message.error('courseId 缺失，无法查看详情');
      return;
    }

    const loadDetail = async () => {
      setLoading(true);
      try {
        const response = await getCourseDetail({ courseId });
        const data = (response.data || {}) as Record<string, any>;
        setDetail({
          ...data,
          courseId: Number(data.courseId ?? data.id ?? courseId),
          chapters: Array.isArray(data.chapters) ? data.chapters : [],
        });
      } catch (error: any) {
        message.error(error?.message || '加载课程详情失败');
      } finally {
        setLoading(false);
      }
    };

    loadDetail();
  }, [courseId]);

  const chapterItems = (detail?.chapters || []).map((chapter, index) => ({
    key: String(chapter.chapterId || index),
    label: `第 ${index + 1} 章：${chapter.title || '-'}`,
    children: Array.isArray(chapter.sections) && chapter.sections.length > 0 ? (
      <List
        dataSource={chapter.sections}
        renderItem={(section) => renderSection(section)}
        locale={{ emptyText: '该章节暂无小节' }}
      />
    ) : (
      <Empty description="该章节暂无小节" image={Empty.PRESENTED_IMAGE_SIMPLE} />
    ),
  }));

  return (
    <PageContainer>
      <Card>
        <Spin spinning={loading}>
          <Space direction="vertical" style={{ width: '100%' }} size={16}>
            <Space>
              <Button onClick={() => history.push('/admin/course-manage/list')}>返回列表</Button>
              {courseId ? (
                <Button type="primary" onClick={() => history.push(`/admin/course-manage/edit/${courseId}`)}>
                  编辑课程
                </Button>
              ) : null}
            </Space>

            <Descriptions title="课程信息" bordered column={2}>
              <Descriptions.Item label="课程ID">{detail?.courseId || '-'}</Descriptions.Item>
              <Descriptions.Item label="课程标题">{detail?.title || '-'}</Descriptions.Item>
              <Descriptions.Item label="分类">{detail?.category || '-'}</Descriptions.Item>
              <Descriptions.Item label="难度">{detail?.difficulty || '-'}</Descriptions.Item>
              <Descriptions.Item label="状态">{renderStatusTag(detail?.status)}</Descriptions.Item>
              <Descriptions.Item label="讲师">
                {detail?.teacherName || '-'}（ID: {detail?.teacherId ?? '-'}）
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">{detail?.createTime || '-'}</Descriptions.Item>
              <Descriptions.Item label="更新时间">{detail?.updateTime || '-'}</Descriptions.Item>
              <Descriptions.Item label="课程简介" span={2}>
                <Paragraph style={{ marginBottom: 0 }}>{detail?.description || '暂无简介'}</Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label="AI 学习助手知识内容" span={2}>
                <Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
                  {detail?.aiKnowleage || '-'}
                </Paragraph>
              </Descriptions.Item>
            </Descriptions>

            <Card type="inner" title="章节结构与视频资源">
              {chapterItems.length > 0 ? (
                <Collapse items={chapterItems} defaultActiveKey={[chapterItems[0].key]} />
              ) : (
                <Empty description="暂无章节数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </Card>
          </Space>
        </Spin>
      </Card>
    </PageContainer>
  );
};

export default CourseManageDetail;
