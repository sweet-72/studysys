<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">AI 赋能教育系统</h2>
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 快捷测试按钮 -->
      <div class="quick-login">
        <p style="margin-bottom: 10px; font-size: 13px; color: #999;">快速测试：</p>
        <el-button size="small" @click="quickLogin('admin')">管理员</el-button>
        <el-button size="small" @click="quickLogin('instructor')">讲师</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 登录
async function handleLogin() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true

    try {
      // TODO: 调用真实登录接口
      // const res = await login(loginForm.username, loginForm.password)
      
      // 模拟登录成功
      const mockToken = 'mock-token-' + Date.now()
      const mockUserInfo = {
        id: 1,
        username: loginForm.username,
        role: loginForm.username === 'admin' ? 'ADMIN' : 'INSTRUCTOR' as const
      }

      userStore.login(mockToken, mockUserInfo)
      
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}

// 快速登录
function quickLogin(role: 'admin' | 'instructor') {
  loginForm.username = role === 'admin' ? 'admin' : 'instructor'
  loginForm.password = '123456'
  ElMessage.info(`已填充${role === 'admin' ? '管理员' : '讲师'}账号，请点击登录按钮`)
}
</script>

<style scoped lang="less">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 450px;
  padding: 20px;

  .login-title {
    text-align: center;
    margin-bottom: 30px;
    color: #303133;
    font-size: 24px;
  }

  .quick-login {
    margin-top: 20px;
    text-align: center;

    button {
      margin: 5px;
    }
  }
}
</style>
