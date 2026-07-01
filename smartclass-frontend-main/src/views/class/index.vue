<!-- 
此文件已屏蔽，因为后端班级功能尚未完善

原内容：src/views/class/index.vue
-->
<template>
  <div class="class-container">
    <van-empty description="功能暂未开放" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ClassControllerService } from '../../services/services/ClassControllerService';
import { useUserStore } from '../../stores/userStore';
import { showFailToast, showSuccessToast } from 'vant';

const router = useRouter();
const userStore = useUserStore();

// 状态管理
const loading = ref(false);
const finished = ref(false);
const page = ref(1);
const pageSize = ref(10);
const classList = ref<Array<any>>([]);
const showCreateDialog = ref(false);
const className = ref('');
const classDescription = ref('');

// 获取班级列表
// const getClassList = async () => {
//   try {
//     console.log('开始获取班级列表，参数:', { current: page.value, pageSize: pageSize.value });
//     
//     const response = await ClassControllerService.getClassList({
//       current: page.value,
//       pageSize: pageSize.value
//     });
//
//     console.log('获取班级列表原始响应:', response);
//     console.log('响应类型:', typeof response);
//     console.log('响应是否为对象:', response !== null && typeof response === 'object');
//
//     // 确保 response 是正确的数据结构
//     const apiResponse = response as any;
//     
//     // 检查响应格式
//     if (apiResponse && apiResponse.code === 0) {
//       const newItems = apiResponse.data?.records || [];
//       console.log('解析后的班级数据:', newItems);
//       classList.value.push(...newItems);
//       
//       // 如果当前页数据少于每页数量，说明已加载完
//       if (newItems.length < pageSize.value) {
//         finished.value = true;
//       }
//     } else {
//       console.error('获取班级列表失败，错误码:', apiResponse?.code, '错误信息:', apiResponse?.message);
//       console.error('完整响应:', apiResponse);
//       showFailToast(apiResponse?.message || '获取班级列表失败');
//     }
//   } catch (error) {
//     console.error('获取班级列表异常:', error);
//     console.error('错误详情:', {
//       name: (error as Error).name,
//       message: (error as Error).message,
//       stack: (error as Error).stack,
//     });
//     const errorMessage = error instanceof Error ? error.message : '未知错误';
//     showFailToast('网络请求失败：' + errorMessage);
//   } finally {
//     loading.value = false;
//   }
// };

// 加载更多
const onLoad = () => {
  // 异步更新数据
  setTimeout(() => {
    // getClassList();
    page.value++;
  }, 1000);
};

// 跳转到班级详情
const goToClassDetail = (classId: number) => {
  router.push(`/class/${classId}`);
};

// 创建班级
const handleCreateClass = async () => {
  if (!className.value.trim()) {
    showFailToast('请填写班级名称');
    return false; // 阻止对话框关闭
  }

  try {
    const response = await ClassControllerService.createClass({
      className: className.value.trim(),
      classDescription: classDescription.value.trim() || undefined
    });

    if (response.code === 0) {
      showSuccessToast('创建成功');
      // 重置表单
      className.value = '';
      classDescription.value = '';
      // 刷新列表
      classList.value = [];
      page.value = 1;
      finished.value = false;
      onLoad();
      return true; // 允许对话框关闭
    } else {
      showFailToast(response.message || '创建失败');
      return false; // 阻止对话框关闭
    }
  } catch (error) {
    console.error('创建班级失败:', error);
    showFailToast('网络请求失败');
    return false; // 阻止对话框关闭
  }
};

// 页面初始化
onMounted(() => {
  // 清空列表并重新加载
  classList.value = [];
  page.value = 1;
  finished.value = false;
  // onLoad();
});
</script>

<style scoped>
.class-container {
  padding-bottom: 50px;
}

.create-btn-container {
  padding: 16px;
}

.class-name {
  font-weight: 500;
  font-size: 16px;
}

.class-member-count {
  color: #969799;
  font-size: 12px;
}

.van-list {
  padding: 0 16px;
}

.van-cell {
  margin-bottom: 8px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>