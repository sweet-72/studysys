import { addCourseUsingPost } from '@/services/backend/courseController';
import { ProColumns, ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { message, Modal } from 'antd';
import React from 'react';

interface Props {
  visible: boolean;
  columns: ProColumns<API.CourseAddRequest>[];
  onSubmit: (values: API.CourseAddRequest) => void;
  onCancel: () => void;
}

/**
 * 创建弹窗
 * @param props
 * @constructor
 */
const CreateModal: React.FC<Props> = (props) => {
  const { visible, columns, onSubmit, onCancel } = props;

  return (
    <Modal
      destroyOnClose
      title={'创建课程'}
      open={visible}
      footer={null}
      onCancel={() => {
        onCancel?.();
      }}
      width={800}
    >
      <ProTable
        type="form"
        columns={columns}
        onSubmit={onSubmit}
        form={{
          layout: 'vertical',
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
export default CreateModal;
