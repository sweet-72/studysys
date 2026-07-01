import { getCourseDetail } from '@/api/course-admin';
import type { CourseChapterItem, CourseSectionItem } from '@/api/course-admin';
import { history, useParams } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import { Button, Card, Col, Empty, Input, List, Modal, Row, Space, Spin, Tabs, Tag, Typography, message } from 'antd';
import React, { useEffect, useMemo, useState } from 'react';

const { Paragraph, Text, Title, Link } = Typography;

interface CourseLearnState {
  courseId?: number;
  title?: string;
  description?: string;
  chapters?: CourseChapterItem[];
}

const sectionHasVideo = (section: CourseSectionItem) => !!section.videoUrl || !!section.localVideoPath;

const resolveSectionAssistantKnowledge = (section?: CourseSectionItem) =>
  section?.assistantKnowledge ||
  section?.aiKnowledgeContent ||
  section?.aiKnowledge ||
  section?.aiContent ||
  section?.knowledgeContent ||
  '';

const CourseLearnPage: React.FC = () => {
  const params = useParams<{ courseId: string }>();
  const [loading, setLoading] = useState(false);
  const [course, setCourse] = useState<CourseLearnState>();
  const [assistantOpen, setAssistantOpen] = useState(false);
  const [currentSection, setCurrentSection] = useState<CourseSectionItem>();
  const [question, setQuestion] = useState('');

  const courseId = useMemo(() => {
    const value = Number(params.courseId);
    return Number.isNaN(value) ? undefined : value;
  }, [params.courseId]);

  useEffect(() => {
    if (!courseId) {
      message.error('\u8bfe\u7a0bID\u65e0\u6548\uff0c\u65e0\u6cd5\u8fdb\u5165\u5b66\u4e60\u9875\u9762');
      return;
    }

    const loadDetail = async () => {
      setLoading(true);
      try {
        const response = await getCourseDetail({ courseId });
        const data = (response.data || {}) as Record<string, any>;
        setCourse({
          courseId,
          title: data.title,
          description: data.description,
          chapters: Array.isArray(data.chapters) ? data.chapters : [],
        });
      } catch (error: any) {
        message.error(error?.message || '\u52a0\u8f7d\u5b66\u4e60\u5185\u5bb9\u5931\u8d25');
      } finally {
        setLoading(false);
      }
    };

    loadDetail();
  }, [courseId]);

  const openAssistant = (section: CourseSectionItem) => {
    setCurrentSection(section);
    setQuestion('');
    setAssistantOpen(true);
  };

  const assistantKnowledge = resolveSectionAssistantKnowledge(currentSection);

  const tabItems = (course?.chapters || []).map((chapter, chapterIndex) => ({
    key: String(chapter.chapterId || chapterIndex),
    label: chapter.title || `\u7ae0\u8282 ${chapterIndex + 1}`,
    children: Array.isArray(chapter.sections) && chapter.sections.length > 0 ? (
      <List
        dataSource={chapter.sections}
        renderItem={(section: CourseSectionItem, sectionIndex) => (
          <List.Item key={`${section.sectionId || sectionIndex}`}>
            <Card size="small" style={{ width: '100%' }}>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Space style={{ width: '100%', justifyContent: 'space-between' }} wrap>
                  <Text strong>
                    {chapterIndex + 1}.{sectionIndex + 1} {section.title || '\u672a\u547d\u540d\u5c0f\u8282'}
                  </Text>
                  <Button size="small" onClick={() => openAssistant(section)}>
                    {'\u5b66\u4e60\u52a9\u624b'}
                  </Button>
                </Space>
                {section.description ? <Paragraph style={{ marginBottom: 0 }}>{section.description}</Paragraph> : null}
                <Space wrap>
                  {section.videoUrl ? (
                    <Tag color="processing">
                      {'\u89c6\u9891\u5730\u5740\uff1a'}
                      <Link href={section.videoUrl} target="_blank" rel="noreferrer">
                        {'\u64ad\u653e'}
                      </Link>
                    </Tag>
                  ) : null}
                  {section.localVideoPath ? <Tag color="success">{`\u672c\u5730\u89c6\u9891\uff1a${section.localVideoPath}`}</Tag> : null}
                  {!sectionHasVideo(section) ? <Tag color="error">{'\u8be5\u5c0f\u8282\u6682\u65e0\u89c6\u9891'}</Tag> : null}
                </Space>
              </Space>
            </Card>
          </List.Item>
        )}
        locale={{ emptyText: '\u8be5\u7ae0\u8282\u6682\u65e0\u5c0f\u8282' }}
      />
    ) : (
      <Empty description={'\u8be5\u7ae0\u8282\u6682\u65e0\u5c0f\u8282'} image={Empty.PRESENTED_IMAGE_SIMPLE} />
    ),
  }));

  return (
    <PageContainer>
      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          <Col xs={24} lg={8}>
            <Card title={'\u5b66\u4e60\u4fe1\u606f'} extra={<Button onClick={() => history.push('/course/list')}>{'\u8fd4\u56de\u8bfe\u7a0b\u5217\u8868'}</Button>}>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Title level={5} style={{ marginBottom: 0 }}>
                  {course?.title || '\u8bfe\u7a0b\u5b66\u4e60'}
                </Title>
                <Paragraph style={{ marginBottom: 0 }}>{course?.description || '\u6682\u65e0\u8bfe\u7a0b\u7b80\u4ecb'}</Paragraph>
                <Text type="secondary">{`\u8bfe\u7a0bID\uff1a${course?.courseId || '-'}`}</Text>
              </Space>
            </Card>
          </Col>
          <Col xs={24} lg={16}>
            <Card title={'\u7ae0\u8282\u5b66\u4e60'}>
              {tabItems.length > 0 ? <Tabs items={tabItems} /> : <Empty description={'\u6682\u65e0\u5b66\u4e60\u5185\u5bb9'} />}
            </Card>
          </Col>
        </Row>
      </Spin>

      <Modal
        open={assistantOpen}
        title={`${'\u5b66\u4e60\u52a9\u624b'}${currentSection?.title ? ` - ${currentSection.title}` : ''}`}
        onCancel={() => setAssistantOpen(false)}
        footer={null}
        destroyOnClose
      >
        <Space direction="vertical" style={{ width: '100%' }} size={12}>
          <Input.TextArea
            rows={4}
            value={question}
            onChange={(event) => setQuestion(event.target.value)}
            placeholder={'\u8bf7\u8f93\u5165\u4f60\u60f3\u95ee\u7684\u95ee\u9898'}
          />
          <Card size="small" title={'\u5f53\u524d\u5c0f\u8282\u95ee\u7b54\u4e0a\u4e0b\u6587'}>
            <Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
              {assistantKnowledge || '\u6682\u672a\u914d\u7f6e\u672c\u5c0f\u8282\u7684 AI \u5b66\u4e60\u52a9\u624b\u77e5\u8bc6\u5185\u5bb9'}
            </Paragraph>
          </Card>
        </Space>
      </Modal>
    </PageContainer>
  );
};

export default CourseLearnPage;
