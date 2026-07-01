import request from '@/utils/request'
import type { Course, CourseQueryParams, PageResponse } from '@/types/course'

/**
 * 获取课程列表
 */
export function getCourseList(params: CourseQueryParams) {
  return request<PageResponse<Course>>({
    url: '/course/list',
    method: 'get',
    params
  })
}

/**
 * 获取课程详情
 */
export function getCourseDetail(id: number) {
  return request<Course>({
    url: `/course/${id}`,
    method: 'get'
  })
}

/**
 * 创建课程
 */
export function createCourse(data: Course) {
  return request({
    url: '/course',
    method: 'post',
    data
  })
}

/**
 * 更新课程
 */
export function updateCourse(id: number, data: Course) {
  return request({
    url: `/course/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除课程
 */
export function deleteCourse(id: number) {
  return request({
    url: `/course/${id}`,
    method: 'delete'
  })
}

/**
 * 发布课程
 */
export function publishCourse(id: number) {
  return request({
    url: `/course/${id}/publish`,
    method: 'post'
  })
}

/**
 * 下架课程
 */
export function unlistCourse(id: number) {
  return request({
    url: `/course/${id}/unlist`,
    method: 'post'
  })
}
