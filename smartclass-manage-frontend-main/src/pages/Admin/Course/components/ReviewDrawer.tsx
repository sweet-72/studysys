import {
  getCourseHomeworkSubmissionDetail,
  reviewCourseHomeworkSubmission,
  resolveCourseHomeworkReviewStatus,
} from '@/api/courseHomeworkSubmission';
import {
  Button,
  Descriptions,
  Divider,
  Drawer,
  Empty,
  Form,
  Input,
  InputNumber,
  Space,
  Spin,
  Tag,
  Typography,
  message,
} from 'antd';
import React, { useEffect, useState } from 'react';

export interface ReviewDrawerProps {
  open: boolean;
  submissionId?: number;
  onClose: () => void;
  onReviewed?: () => void;
}

const ReviewDrawer: React.FC<ReviewDrawerProps> = ({ open, submissionId, onClose, onReviewed }) => {
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [detail, setDetail] = useState<any>();
  const [form] = Form.useForm();

  const resetState = () => {
    setDetail(undefined);
    form.resetFields();
  };

  const loadDetail = async () => {
    if (!submissionId) {
      resetState();
      return;
    }

    setLoading(true);
    resetState();
    try {
      const res = await getCourseHomeworkSubmissionDetail(submissionId);
      const nextDetail = res.data;
      setDetail(nextDetail);
      form.setFieldsValue({
        score: nextDetail?.reviewScore ?? nextDetail?.score,
        comment: nextDetail?.reviewComment ?? nextDetail?.comment,
      });
    } catch (error: any) {
      resetState();
      message.error(error?.message || '\u52a0\u8f7d\u63d0\u4ea4\u8be6\u60c5\u5931\u8d25');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open) {
      loadDetail();
    } else {
      resetState();
    }
  }, [open, submissionId]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (!submissionId) {
        return;
      }

      setSubmitting(true);
      await reviewCourseHomeworkSubmission({
        submissionId,
        score: Number(values.score),
        comment: values.comment,
      });
      message.success('\u6279\u6539\u7ed3\u679c\u63d0\u4ea4\u6210\u529f');
      await loadDetail();
      onReviewed?.();
    } catch (error: any) {
      if (error?.errorFields) {
        return;
      }
      message.error(error?.message || '\u63d0\u4ea4\u6279\u6539\u5931\u8d25');
    } finally {
      setSubmitting(false);
    }
  };

  const reviewStatus = resolveCourseHomeworkReviewStatus(detail?.reviewStatus);
  const reviewScore = detail?.reviewScore ?? detail?.score;
  const reviewComment = detail?.reviewComment ?? detail?.comment;

  return (
    <Drawer
      title={detail?.homeworkTitle ? `\u4f5c\u4e1a\u63d0\u4ea4 - ${detail.homeworkTitle}` : '\u4f5c\u4e1a\u63d0\u4ea4\u8be6\u60c5'}
      width={720}
      open={open}
      onClose={onClose}
      destroyOnClose
      forceRender
    >
      <Spin spinning={loading}>
        {!detail ? (
          <Empty description={'\u6682\u65e0\u63d0\u4ea4\u8be6\u60c5'} />
        ) : (
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label={'\u63d0\u4ea4 ID'}>{detail.id || '-'}</Descriptions.Item>
              <Descriptions.Item label={'\u72b6\u6001'}>
                <Tag color={reviewStatus.color}>{reviewStatus.label}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label={'\u5b66\u751f'}>{detail.studentName || detail.studentId || '-'}</Descriptions.Item>
              <Descriptions.Item label={'\u63d0\u4ea4\u65f6\u95f4'}>{detail.submitTime || '-'}</Descriptions.Item>
              <Descriptions.Item label={'\u4f5c\u4e1a\u6807\u9898'}>{detail.homeworkTitle || '-'}</Descriptions.Item>
              <Descriptions.Item label={'\u5206\u6570'}>{reviewScore ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={'\u9644\u4ef6'} span={2}>
                {detail.answerAttachmentUrl ? (
                  <Typography.Link href={detail.answerAttachmentUrl} target="_blank" rel="noreferrer">
                    {'\u67e5\u770b\u9644\u4ef6'}
                  </Typography.Link>
                ) : (
                  '-'
                )}
              </Descriptions.Item>
              <Descriptions.Item label={'\u5b66\u751f\u7b54\u6848'} span={2}>
                <Typography.Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
                  {detail.answerContent || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label={'\u8bc4\u8bed'} span={2}>
                <Typography.Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
                  {reviewComment || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label={'\u6279\u6539\u4eba'}>{detail.reviewerName || detail.reviewerId || '-'}</Descriptions.Item>
              <Descriptions.Item label={'\u6279\u6539\u65f6\u95f4'}>{detail.reviewTime || '-'}</Descriptions.Item>
            </Descriptions>

            <Divider style={{ margin: 0 }} />

            <Form form={form} layout="vertical">
              <Form.Item
                name="score"
                label={'\u8bc4\u5206'}
                rules={[
                  { required: true, message: '\u8bf7\u8f93\u5165\u8bc4\u5206' },
                  { type: 'number', min: 0, max: 100, message: '\u8bc4\u5206\u8303\u56f4\u9700\u5728 0-100 \u4e4b\u95f4' },
                ]}
              >
                <InputNumber min={0} max={100} precision={0} style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item
                name="comment"
                label={'\u8bc4\u8bed'}
                rules={[{ required: true, message: '\u8bf7\u8f93\u5165\u8bc4\u8bed' }]}
              >
                <Input.TextArea rows={4} placeholder={'\u8bf7\u8f93\u5165\u6279\u6539\u8bc4\u8bed'} />
              </Form.Item>
              <Space>
                <Button type="primary" onClick={handleSubmit} loading={submitting}>
                  {'\u63d0\u4ea4\u6279\u6539'}
                </Button>
                <Button onClick={onClose}>{'\u5173\u95ed'}</Button>
              </Space>
            </Form>
          </Space>
        )}
      </Spin>
    </Drawer>
  );
};

export default ReviewDrawer;