import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Submission, PageResponse } from '@/types/course'
import { getSubmissionList, getSubmissionDetail, reviewSubmission } from '@/api/submission'

export const useSubmissionStore = defineStore('submission', () => {
  const submissionList = ref<Submission[]>([])
  const total = ref(0)
  const loading = ref(false)
  const currentSubmission = ref<Submission | null>(null)

  /**
   * 获取作业提交列表
 */
  async function fetchSubmissionList(params: {
    page: number
    size: number
    courseId?: number
    status?: 'PENDING' | 'REVIEWED'
  }) {
    loading.value = true
    try {
      const res = await getSubmissionList(params)
      submissionList.value = res.records
      total.value = res.total
      return res
    } catch (error) {
      console.error('获取作业列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取作业详情
 */
  async function fetchSubmissionDetail(id: number) {
    loading.value = true
    try {
      const res = await getSubmissionDetail(id)
      currentSubmission.value = res
      return res
    } catch (error) {
      console.error('获取作业详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 批改作业
 */
  async function submitReview(data: {
    submissionId: number
    score: number
    comment: string
  }) {
    loading.value = true
    try {
      const res = await reviewSubmission(data)
      // 刷新列表
      await fetchSubmissionList({ page: 1, size: 10 })
      // 清空当前
      currentSubmission.value = null
      return res
    } catch (error) {
      console.error('批改作业失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置当前作业
 */
  function resetCurrentSubmission() {
    currentSubmission.value = null
  }

  return {
    submissionList,
    total,
    loading,
    currentSubmission,
    fetchSubmissionList,
    fetchSubmissionDetail,
    submitReview,
    resetCurrentSubmission
  }
})
