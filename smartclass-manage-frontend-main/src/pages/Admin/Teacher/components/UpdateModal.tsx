import React from 'react';
import { Modal, message } from 'antd';
import { ProForm, ProFormText, ProFormTextArea, ProFormDigit } from '@ant-design/pro-components';
import { updateTeacherUsingPost } from '@/services/backend/teacherController';

interface Props {
  visible: boolean;
  oldData?: API.TeacherVO;
  onSubmit: () => void;
  onCancel: () => void;
}

const UpdateModal: React.FC<Props> = ({ visible, oldData, onSubmit, onCancel }) => {
  if (!oldData) {
    return null;
  }

  return (
    <Modal
      destroyOnClose
      title="编辑讲师"
      open={visible}
      footer={null}
      width={720}
      onCancel={onCancel}
    >
      <ProForm<API.TeacherUpdateRequest>
        layout="vertical"
        initialValues={oldData}
        submitter={{
          searchConfig: {
            submitText: '保存',
            resetText: '重置',
          },
        }}
        onFinish={async (values) => {
          try {
            await updateTeacherUsingPost({
              ...values,
              id: oldData.id,
            });
            message.success('更新成功');
            onSubmit();
          } catch (error: any) {
            message.error(error?.message || '更新失败');
          }
        }}
      >
        <ProFormText
          name="name"
          label="讲师姓名"
          rules={[{ required: true, message: '请输入讲师姓名' }]}
        />
        <ProFormText name="title" label="职称" />
        <ProFormText name="expertise" label="擅长领域" />
        <ProFormTextArea name="introduction" label="简介" fieldProps={{ rows: 3 }} />
        <ProFormText name="avatar" label="头像 URL" />
        <ProFormDigit
          name="userId"
          label="绑定用户ID"
          min={1}
          fieldProps={{ precision: 0 }}
        />
      </ProForm>
    </Modal>
  );
};

export default UpdateModal;
