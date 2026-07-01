import React from 'react';
import { Modal, message } from 'antd';
import { ProForm, ProFormText, ProFormTextArea, ProFormDigit } from '@ant-design/pro-components';
import { addTeacherUsingPost } from '@/services/backend/teacherController';

interface Props {
  visible: boolean;
  onSubmit: () => void;
  onCancel: () => void;
}

const CreateModal: React.FC<Props> = ({ visible, onSubmit, onCancel }) => {
  return (
    <Modal
      destroyOnClose
      title="新增讲师"
      open={visible}
      footer={null}
      width={720}
      onCancel={onCancel}
    >
      <ProForm<API.TeacherAddRequest>
        layout="vertical"
        submitter={{
          searchConfig: {
            submitText: '提交',
            resetText: '重置',
          },
        }}
        onFinish={async (values) => {
          try {
            await addTeacherUsingPost(values);
            message.success('创建成功');
            onSubmit();
          } catch (error: any) {
            message.error(error?.message || '创建失败');
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

export default CreateModal;
