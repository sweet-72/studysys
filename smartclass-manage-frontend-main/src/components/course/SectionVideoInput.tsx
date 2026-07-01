import { InboxOutlined, UploadOutlined } from '@ant-design/icons';
import { Button, Form, Input, Space, Upload, message } from 'antd';
import type { UploadProps } from 'antd';
import type { FormInstance } from 'antd/es/form';
import { resolveVideoUploadPath, uploadCourseVideo } from '@/api/course-admin';

export type FormFieldPath = Array<string | number>;

interface SectionVideoInputProps {
  form: FormInstance;
  videoUrlName: FormFieldPath;
  localVideoPathName: FormFieldPath;
  disabled?: boolean;
  courseId?: number;
  chapterId?: number;
  sectionId?: number;
}

const SectionVideoInput: React.FC<SectionVideoInputProps> = ({
  form,
  videoUrlName,
  localVideoPathName,
  disabled,
  courseId,
  chapterId,
  sectionId,
}) => {
  const localVideoPath = Form.useWatch(localVideoPathName, form) as string | undefined;

  const uploadProps: UploadProps = {
    accept: 'video/*',
    maxCount: 1,
    showUploadList: false,
    disabled,
    customRequest: async (options) => {
      try {
        const file = options.file as File;
        const response = await uploadCourseVideo({
          file,
          courseId,
          chapterId,
          sectionId,
        });
        const uploadedPath = resolveVideoUploadPath(response);
        if (!uploadedPath) {
          throw new Error(response.message || '上传成功但未返回视频路径');
        }
        form.setFieldValue(localVideoPathName, uploadedPath);
        message.success('视频上传成功，已回填本地视频路径');
        options.onSuccess?.(response, new XMLHttpRequest());
      } catch (error: any) {
        message.error(error?.message || '视频上传失败');
        options.onError?.(error);
      }
    },
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Form.Item
        label="视频 URL"
        name={videoUrlName}
        extra="可填第三方视频地址，例如对象存储/CDN URL"
        rules={[
          {
            validator: async (_, value: string) => {
              if (!value) {
                return;
              }
              const urlRegex = /^https?:\/\/.+/;
              if (!urlRegex.test(value)) {
                throw new Error('视频 URL 必须以 http:// 或 https:// 开头');
              }
            },
          },
        ]}
      >
        <Input placeholder="请输入视频 URL" disabled={disabled} allowClear />
      </Form.Item>

      <Form.Item label="本地视频路径" name={localVideoPathName}>
        <Input placeholder="上传后自动回填" disabled allowClear />
      </Form.Item>

      <Upload.Dragger {...uploadProps}>
        <p className="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p className="ant-upload-text">点击或拖拽视频到此区域上传</p>
        <p className="ant-upload-hint">支持 mp4 / mov / webm，上传成功后将自动回填 localVideoPath</p>
      </Upload.Dragger>

      {localVideoPath ? (
        <Button
          icon={<UploadOutlined />}
          onClick={() => {
            navigator.clipboard
              ?.writeText(localVideoPath)
              .then(() => message.success('本地视频路径已复制'))
              .catch(() => message.warning('复制失败，请手动复制'));
          }}
          disabled={disabled}
        >
          复制 localVideoPath
        </Button>
      ) : null}
    </Space>
  );
};

export default SectionVideoInput;
