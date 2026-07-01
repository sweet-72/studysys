import { ProColumns, ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { Modal } from 'antd';
import React from 'react';

interface Props {
  visible: boolean;
  oldData?: any;
  columns: ProColumns<any>[];
  onSubmit: (values: any) => void;
  onCancel: () => void;
}

const ChapterUpdateModal: React.FC<Props> = ({ visible, oldData, columns, onSubmit, onCancel }) => {
  return (
    <Modal destroyOnClose title="更新章节" open={visible} footer={null} onCancel={() => onCancel?.()} width={800}>
      <ProTable
        type="form"
        columns={columns}
        form={{
          initialValues: oldData,
          layout: 'vertical',
        }}
        onSubmit={onSubmit}
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

export default ChapterUpdateModal;