import request from '@/utils/request'
import type { Submission, ReviewParams, PageResponse } from '@/types/course'

/**
 * 获取作业提交列表
 */
export function getSubmissionList(params: {
  page: number
  size: number
  courseId?: number
  status?: 'PENDING' | 'REVIEWED'
}) {
  return request<PageResponse<Submission>>({
    url: '/submission/list',
    method: 'get',
    params
  })
}

/**
 * 获取作业提交详情
 */
export function getSubmissionDetail(id: number) {
  return request<Submission>({
    url: `/submission/${id}`,
    method: 'get'
  })
}

/**
 * 批改作业
 */
export function reviewSubmission(data: ReviewParams) {
  return request({
    url: '/submission/review',
    method: 'post',
    data
  })
}
