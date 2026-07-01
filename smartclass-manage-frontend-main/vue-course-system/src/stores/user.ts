import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserRole } from '@/types/course'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<{
    id: number
    username: string
    role: UserRole
    avatar?: string
  } | null>(null)

  /**
   * 是否为管理员
 */
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  /**
   * 是否为讲师
 */
  const isInstructor = computed(() => userInfo.value?.role === 'INSTRUCTOR')

  /**
   * 登录
 */
  function login(newToken: string, info: typeof userInfo.value) {
    token.value = newToken
    userInfo.value = info
    localStorage.setItem('token', newToken)
  }

  /**
   * 登出
 */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isAdmin,
    isInstructor,
    login,
    logout
  }
})
