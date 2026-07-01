import { ProColumns, ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { Modal } from 'antd';
import React from 'react';

interface Props {
  visible: boolean;
  courseId?: number;
  chapterId?: number;
  columns: ProColumns<any>[];
  onSubmit: (values: any) => void;
  onCancel: () => void;
}

const SectionCreateModal: React.FC<Props> = ({ visible, columns, onSubmit, onCancel }) => {
  return (
    <Modal destroyOnClose title="创建小节" open={visible} footer={null} onCancel={() => onCancel?.()} width={800}>
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

export default SectionCreateModal;