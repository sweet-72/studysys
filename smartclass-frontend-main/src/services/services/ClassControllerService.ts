// ClassControllerService.ts
// 修正后的班级管理API服务文件

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

/**
 * 班级管理 API 服务
 */
export class ClassControllerService {
  /**
   * 创建班级
   * POST /api/class/create
   */
  static createClass(body: { className: string; classDescription?: string }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/class/create',
      body: body,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * 获取班级列表
   * GET /api/class/list/page
   */
  static getClassList(params?: { current?: number; pageSize?: number }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/class/list/page',
      query: params,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * 获取班级详情
   * GET /api/class/get/id
   */
  static getClassDetail(id: number): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: `/api/class/get/id`,
      query: { id },
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * 邀请/添加成员
   * POST /api/class/member/add
   */
  static addMember(body: { classId: number; userId: number; role: string }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/class/member/add',
      body: body,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * 移除班级成员
   * DELETE /api/class/member/delete
   */
  static removeMember(params: { classId: number; userId: number }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/class/member/delete',
      query: params,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * 修改成员角色
   * PUT /api/class/member/update/role
   */
  static updateMemberRole(body: { classId: number; userId: number; newRole: string }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/class/member/update/role',
      body: body,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * 退出班级
   * POST /api/class/leave
   */
  static leaveClass(body: { classId: number }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/class/leave',
      body: body,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * 解散班级
   * POST /api/class/delete
   */
  static disbandClass(body: { id: number }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/class/delete',
      body: body,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }
}