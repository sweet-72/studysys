import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Course, CourseQueryParams, PageResponse } from '@/types/course'
import { getCourseList, getCourseDetail, createCourse, updateCourse, deleteCourse } from '@/api/course'

export const useCourseStore = defineStore('course', () => {
  const courseList = ref<Course[]>([])
  const total = ref(0)
  const loading = ref(false)
  const currentCourse = ref<Course | null>(null)

  /**
   * 获取课程列表
 */
  async function fetchCourseList(params: CourseQueryParams) {
    loading.value = true
    try {
      const res = await getCourseList(params)
      courseList.value = res.records
      total.value = res.total
      return res
    } catch (error) {
      console.error('获取课程列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取课程详情
 */
  async function fetchCourseDetail(id: number) {
    loading.value = true
    try {
      const res = await getCourseDetail(id)
      currentCourse.value = res
      return res
    } catch (error) {
      console.error('获取课程详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建课程
 */
  async function createNewCourse(course: Course) {
    loading.value = true
    try {
      const res = await createCourse(course)
      await fetchCourseList({ page: 1, size: 10 })
      return res
    } catch (error) {
      console.error('创建课程失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新课程
 */
  async function updateExistingCourse(id: number, course: Course) {
    loading.value = true
    try {
      const res = await updateCourse(id, course)
      if (currentCourse.value?.id === id) {
        currentCourse.value = res
      }
      await fetchCourseList({ page: 1, size: 10 })
      return res
    } catch (error) {
      console.error('更新课程失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除课程
 */
  async function removeCourse(id: number) {
    loading.value = true
    try {
      await deleteCourse(id)
      await fetchCourseList({ page: 1, size: 10 })
    } catch (error) {
      console.error('删除课程失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置当前课程
 */
  function resetCurrentCourse() {
    currentCourse.value = null
  }

  return {
    courseList,
    total,
    loading,
    currentCourse,
    fetchCourseList,
    fetchCourseDetail,
    createNewCourse,
    updateExistingCourse,
    removeCourse,
    resetCurrentCourse
  }
})
