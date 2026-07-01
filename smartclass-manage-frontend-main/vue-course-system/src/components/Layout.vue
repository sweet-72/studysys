<template>
  <el-container class="layout-container">
    <!-- 顶部导航 -->
    <el-header class="layout-header">
      <div class="header-content">
        <h2 class="logo">AI 赋能教育系统</h2>
        <div class="header-right">
          <el-menu mode="horizontal" :ellipsis="false" style="flex: 1; border-bottom: none">
            <el-menu-item index="/courses" @click="$router.push('/courses')">
              <el-icon><Reading /></el-icon>
              课程管理
            </el-menu-item>
            <el-menu-item
              index="/submissions"
              @click="$router.push('/submissions')"
              v-if="isAdmin || isInstructor"
            >
              <el-icon><DocumentChecked /></el-icon>
              作业批改
            </el-menu-item>
          </el-menu>

          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="36" :icon="UserFilled" />
              <span style="margin-left: 10px">{{ userStore.userInfo?.username }}</span>
              <el-tag size="small" style="margin-left: 5px">
                {{ userStore.isAdmin ? '管理员' : '讲师' }}
              </el-tag>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="layout-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Reading, DocumentChecked, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isAdmin = computed(() => userStore.isAdmin)
const isInstructor = computed(() => userStore.isInstructor)

// 处理命令
async function handleCommand(command: string) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="less">
.layout-container {
  height: 100vh;
}

.layout-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;

    .logo {
      margin: 0;
      color: #409eff;
      font-size: 20px;
      font-weight: bold;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 20px;

      .user-info {
        display: flex;
        align-items: center;
        cursor: pointer;
      }
    }
  }
}

.layout-main {
  background-color: #f0f2f5;
  padding: 0;
}
</style>
