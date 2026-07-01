import { history } from '@umijs/max';
import { Button, Result } from 'antd';
import React from 'react';

const ForbiddenPage: React.FC = () => {
  return (
    <Result
      status="403"
      title="403"
      subTitle="抱歉，你没有权限访问该页面。"
      extra={
        <Button type="primary" onClick={() => history.push('/welcome')}>
          返回首页
        </Button>
      }
    />
  );
};

export default ForbiddenPage;
