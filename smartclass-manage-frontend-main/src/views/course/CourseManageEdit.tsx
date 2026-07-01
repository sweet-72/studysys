import ChapterSectionEditor from '@/components/course/ChapterSectionEditor';
import { createCourse, getCourseDetail, updateCourse } from '@/api/course-admin';
import type { CourseUpsertPayload } from '@/api/course-admin';
import { history, useLocation, useParams } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Col,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Spin,
  Typography,
  message,
} from 'antd';
import React, { useEffect, useMemo, useState } from 'react';
import { DEFAULT_COURSE_FORM, toCourseFormValues, validateCoursePayload } from './utils';

const { Title } = Typography;

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

const CourseManageEdit: React.FC = () => {
  const location = useLocation();
  const params = useParams<{ courseId?: string }>();
  const [form] = Form.useForm<CourseUpsertPayload>();
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const courseId = useMemo(() => {
    if (params.courseId) {
      const value = Number(params.courseId);
      return Number.isNaN(value) ? undefined : value;
    }
    const query = new URLSearchParams(location.search);
    const raw = query.get('courseId');
    if (!raw) {
      return undefined;
    }
    const value = Number(raw);
    return Number.isNaN(value) ? undefined : value;
  }, [location.search, params.courseId]);

  const isEdit = !!courseId;

  useEffect(() => {
    const loadDetail = async () => {
      if (!courseId) {
        form.setFieldsValue(DEFAULT_COURSE_FORM);
        return;
      }

      setLoading(true);
      try {
        const response = await getCourseDetail({ courseId });
        const detail = response.data as Record<string, any> | undefined;
        form.setFieldsValue(toCourseFormValues(detail));
      } catch (error: any) {
        message.error(error?.message || '加载课程详情失败');
      } finally {
        setLoading(false);
      }
    };

    loadDetail();
  }, [courseId, form]);

  const normalizePayload = (values: CourseUpsertPayload): CourseUpsertPayload => {
    return {
      ...values,
      courseId,
      aiKnowleage: values.aiKnowleage?.trim() || '',
      teacherId: Number(values.teacherId),
      status: values.status ?? 0,
      chapters: (values.chapters || []).map((chapter, chapterIndex) => ({
        ...chapter,
        sort: chapter.sort ?? chapterIndex + 1,
        sections: (chapter.sections || []).map((section, sectionIndex) => ({
          ...section,
          sort: section.sort ?? sectionIndex + 1,
          videoUrl: section.videoUrl?.trim(),
          localVideoPath: section.localVideoPath?.trim(),
        })),
      })),
    };
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const payload = normalizePayload(values);
      const validationError = validateCoursePayload(payload);
      if (validationError) {
        message.error(validationError);
        return;
      }

      setSubmitting(true);
      if (isEdit) {
        await updateCourse(payload);
        message.success('课程更新成功');
      } else {
        await createCourse(payload);
        message.success('课程创建成功');
      }
      history.push('/admin/course-manage/list');
    } catch (error: any) {
      if (error?.errorFields) {
        message.error('请先修正表单校验错误后再提交');
      } else {
        message.error(error?.message || '提交失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <PageContainer>
      <Card>
        <Title level={4} style={{ marginTop: 0 }}>
          {isEdit ? '编辑课程' : '创建课程'}
        </Title>

        <Spin spinning={loading}>
          <Form form={form} layout="vertical" initialValues={DEFAULT_COURSE_FORM}>
            <Card type="inner" title="基础信息" style={{ marginBottom: 16 }}>
              <Row gutter={16}>
                <Col xs={24} md={12}>
                  <Form.Item
                    label="课程标题"
                    name="title"
                    rules={[{ required: true, message: '课程标题不能为空' }]}
                  >
                    <Input placeholder="请输入课程标题" />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item
                    label="讲师 ID"
                    name="teacherId"
                    rules={[{ required: true, message: '创建/更新课程时必须有讲师' }]}
                  >
                    <InputNumber min={1} precision={0} style={{ width: '100%' }} placeholder="请输入讲师 ID" />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={16}>
                <Col xs={24} md={8}>
                  <Form.Item label="讲师名称" name="teacherName">
                    <Input placeholder="请输入讲师名称（选填）" />
                  </Form.Item>
                </Col>
                <Col xs={24} md={8}>
                  <Form.Item
                    label="课程分类"
                    name="category"
                    rules={[{ required: true, message: '课程分类不能为空' }]}
                  >
                    <Input placeholder="如：Java、前端、算法" />
                  </Form.Item>
                </Col>
                <Col xs={24} md={8}>
                  <Form.Item
                    label="难度"
                    name="difficulty"
                    rules={[{ required: true, message: '难度不能为空' }]}
                  >
                    <Select options={difficultyOptions} placeholder="请选择难度" />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={16}>
                <Col xs={24} md={8}>
                  <Form.Item label="课程状态" name="status" rules={[{ required: true, message: '状态不能为空' }]}>
                    <Select options={statusOptions} placeholder="请选择状态" />
                  </Form.Item>
                </Col>
                <Col xs={24} md={16}>
                  <Form.Item label="课程简介" name="description">
                    <Input.TextArea rows={3} placeholder="请输入课程简介（选填）" />
                  </Form.Item>
                </Col>
              </Row>
              <Row gutter={16}>
                <Col xs={24}>
                  <Form.Item
                    label="AI 学习助手知识内容"
                    name="aiKnowleage"
                    rules={[{ required: true, message: '请填写 AI 学习助手知识内容' }]}
                  >
                    <Input.TextArea rows={6} placeholder="请填写将提供给学习助手的知识内容（提示词）" />
                  </Form.Item>
                </Col>
              </Row>
            </Card>

            <Card type="inner" title="章节与小节（动态）">
              <ChapterSectionEditor form={form} courseId={courseId} />
            </Card>

            <Space style={{ marginTop: 20 }}>
              <Button type="primary" loading={submitting} onClick={handleSubmit}>
                {isEdit ? '保存更新' : '创建课程'}
              </Button>
              <Button onClick={() => history.push('/admin/course-manage/list')}>返回列表</Button>
            </Space>
          </Form>
        </Spin>
      </Card>
    </PageContainer>
  );
};

export default CourseManageEdit;
