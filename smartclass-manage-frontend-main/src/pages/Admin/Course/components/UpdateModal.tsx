import { updateCourseUsingPost } from '@/services/backend/courseController';
import { ProColumns, ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { message, Modal } from 'antd';
import React from 'react';

interface Props {
  oldData?: API.CourseVO;
  visible: boolean;
  columns: ProColumns<API.CourseUpdateRequest>[];
  onSubmit: (values: API.CourseUpdateRequest) => void;
  onCancel: () => void;
}

const handleUpdate = async (fields: API.CourseUpdateRequest) => {
  const hide = message.loading('正在更新');
  try {
    const processedFields = {
      ...fields,
      tags: Array.isArray(fields.tags) ? JSON.stringify(fields.tags) : fields.tags,
    };
    await updateCourseUsingPost(processedFields);
    hide();
    message.success('更新成功');
    return true;
  } catch (error: any) {
    hide();
    message.error(`更新失败：${error.message}`);
    return false;
  }
};

const UpdateModal: React.FC<Props> = ({ oldData, visible, columns, onSubmit, onCancel }) => {
  if (!oldData) {
    return <></>;
  }

  return (
    <Modal destroyOnClose title="更新课程" open={visible} footer={null} onCancel={() => onCancel?.()} width={800}>
      <ProTable
        type="form"
        columns={columns}
        form={{
          initialValues: oldData,
          layout: 'vertical',
        }}
        onSubmit={async (values: API.CourseUpdateRequest) => {
          const processedValues = {
            ...values,
            id: oldData.id,
          };
          const success = await handleUpdate(processedValues as API.CourseUpdateRequest);
          if (success) {
            onSubmit?.(processedValues as API.CourseUpdateRequest);
          }
        }}
        submitter={{
          searchConfig: {
            submitText: '提交',
            resetText: '重置',
          },
        }}
      />
    </Modal>
  );
};

export default UpdateModal;