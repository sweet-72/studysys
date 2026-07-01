<!-- 
此文件已屏蔽，因为后端班级功能尚未完善

原内容：src/views/class/ClassDetail.vue
-->
<template>
  <div class="class-detail-container">
    <van-empty description="功能暂未开放" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ClassControllerService } from '../../services/services/ClassControllerService';
import { showFailToast, showSuccessToast, showConfirmDialog } from 'vant';
import { formatDate } from '../../utils/dateUtils';

const router = useRouter();
const route = useRoute();

// 获取班级ID
const classId = Number(route.params.id);

// 状态管理
const classInfo = ref({
  id: 0,
  name: '',
  description: '',
  createTime: '',
  memberCount: 0,
  role: '' // 当前用户在班级中的角色
});
const currentRole = ref('');
const loading = ref(false);
const finished = ref(false);
const page = ref(1);
const pageSize = ref(10);
const memberList = ref<Array<any>>([]);
const isLoading = ref(true);
const showInviteDialog = ref(false);
const inviteUserId = ref('');
const showMemberActionSheetFlag = ref(false);
const selectedMember = ref<any>(null);
const memberActions = ref<Array<{ name: string; value: string }>>([]);

// 返回上一页
const goBack = () => {
  router.back();
};

// 获取班级详情
const getClassDetail = async () => {
  try {
    const response = await ClassControllerService.getClassDetail({ id: classId });
    if (response.code === 0) {
      classInfo.value = response.data;
      currentRole.value = response.data.role;
    } else {
      showFailToast(response.message || '获取班级详情失败');
    }
  } catch (error) {
    console.error('获取班级详情失败:', error);
    showFailToast('网络请求失败');
  } finally {
    isLoading.value = false;
  }
};

// 加载成员列表
const onLoadMembers = async () => {
  try {
    const response = await ClassControllerService.getClassDetail(classId);
    
    if (response.code === 0) {
      // 直接从详情接口获取成员列表
      const allMembers = response.data.members || [];
      // 分页显示
      const start = (page.value - 1) * pageSize.value;
      const end = start + pageSize.value;
      const currentPageMembers = allMembers.slice(start, end);
      
      memberList.value.push(...currentPageMembers);
      
      // 如果当前页数据少于每页数量，说明已加载完
      if (currentPageMembers.length < pageSize.value) {
        finished.value = true;
      }
      
      page.value++;
    } else {
      showFailToast(response.message || '获取成员列表失败');
    }
  } catch (error) {
    console.error('获取成员列表失败:', error);
    showFailToast('网络请求失败');
  } finally {
    loading.value = false;
  }
};

// 显示成员操作菜单
const showMemberActionSheet = (member: any) => {
  selectedMember.value = member;
  
  // 根据当前用户角色和目标成员角色生成操作菜单
  const actions = [];
  
  // 只有老师可以操作成员
  if (currentRole.value === 'teacher') {
    // 不能操作自己
    if (member.userId !== Number(localStorage.getItem('userId'))) {
      if (member.role === 'student') {
        actions.push({ name: '设为老师', value: 'promote' });
      } else if (member.role === 'teacher') {
        actions.push({ name: '设为学生', value: 'demote' });
      }
      actions.push({ name: '移除成员', value: 'remove' });
    }
  }
  
  memberActions.value = actions;
  showMemberActionSheetFlag.value = true;
};

// 处理成员操作选择
const onMemberActionSelect = async (action: { name: string; value: string }) => {
  if (!selectedMember.value) return;
  
  try {
    switch (action.value) {
      case 'promote':
        await updateMemberRole(selectedMember.value.userId, 'teacher');
        break;
      case 'demote':
        await updateMemberRole(selectedMember.value.userId, 'student');
        break;
      case 'remove':
        await removeMember(selectedMember.value.userId);
        break;
    }
  } catch (error) {
    console.error('操作成员失败:', error);
  }
};

// 更新成员角色
const updateMemberRole = async (userId: number, role: string) => {
  try {
    const response = await ClassControllerService.updateMemberRole({
      classId,
      userId,
      newRole: role
    });
    
    if (response.code === 0) {
      showSuccessToast(`${role === 'teacher' ? '设为老师' : '设为学生'}成功`);
      // 刷新列表
      refreshData();
    } else {
      showFailToast(response.message || '操作失败');
    }
  } catch (error) {
    console.error('更新成员角色失败:', error);
    showFailToast('网络请求失败');
  }
};

// 移除成员
const removeMember = async (userId: number) => {
  try {
    const result = await showConfirmDialog({
      title: '确认操作',
      message: '确定要将该成员从班级中移除吗？',
    });
    
    if (result) {
      const response = await ClassControllerService.removeMember({
        classId,
        userId
      });
      
      if (response.code === 0) {
        showSuccessToast('移除成员成功');
        // 刷新列表
        refreshData();
      } else {
        showFailToast(response.message || '移除失败');
      }
    }
  } catch (error) {
    // 用户取消或网络错误
    if (error !== 'cancel') {
      console.error('移除成员失败:', error);
      showFailToast('网络请求失败');
    }
  }
};

// 邀请成员
const handleInviteMember = async () => {
  if (!inviteUserId.value.trim()) {
    showFailToast('请输入用户ID');
    return false;
  }
  
  try {
    const response = await ClassControllerService.addMember({
      classId,
      userId: Number(inviteUserId.value),
      role: 'student'
    });
    
    if (response.code === 0) {
      showSuccessToast('邀请成功');
      // 重置表单
      inviteUserId.value = '';
      // 刷新数据
      refreshData();
      return true;
    } else {
      showFailToast(response.message || '邀请失败');
      return false;
    }
  } catch (error) {
    console.error('邀请成员失败:', error);
    showFailToast('网络请求失败');
    return false;
  }
};

// 解散班级
const handleDisbandClass = async () => {
  try {
    const result = await showConfirmDialog({
      title: '确认解散',
      message: '确定要解散此班级吗？此操作不可逆。',
      theme: 'danger'
    });
    
    if (result) {
      const response = await ClassControllerService.disbandClass({ id: classId });
      
      if (response.code === 0) {
        showSuccessToast('班级已解散');
        // 返回班级列表
        router.push('/class');
      } else {
        showFailToast(response.message || '解散失败');
      }
    }
  } catch (error) {
    // 用户取消或网络错误
    if (error !== 'cancel') {
      console.error('解散班级失败:', error);
      showFailToast('网络请求失败');
    }
  }
};

// 退出班级
const handleLeaveClass = async () => {
  try {
    const result = await showConfirmDialog({
      title: '确认退出',
      message: '确定要退出此班级吗？',
      theme: 'danger'
    });
    
    if (result) {
      const response = await ClassControllerService.leaveClass({ classId });
      
      if (response.code === 0) {
        showSuccessToast('已退出班级');
        // 返回班级列表
        router.push('/class');
      } else {
        showFailToast(response.message || '退出失败');
      }
    }
  } catch (error) {
    // 用户取消或网络错误
    if (error !== 'cancel') {
      console.error('退出班级失败:', error);
      showFailToast('网络请求失败');
    }
  }
};

// 刷新数据
const refreshData = () => {
  // 重置状态
  memberList.value = [];
  page.value = 1;
  finished.value = false;
  
  // 重新获取班级详情和成员列表
  getClassDetail();
  onLoadMembers();
};

// 页面初始化
onMounted(() => {
  if (!classId) {
    showFailToast('无效的班级ID');
    router.back();
    return;
  }
  
  getClassDetail();
  onLoadMembers();
});
</script>

<style scoped>
.class-detail-container {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.class-info-card {
  margin: 16px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.info-row {
  display: flex;
  margin-bottom: 12px;
}

.label {
  flex: 0 0 80px;
  color: #969799;
  font-size: 14px;
}

.value {
  flex: 1;
  color: #323233;
  font-size: 14px;
}

.action-buttons {
  margin: 0 16px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.leave-button-container {
  margin: 0 16px;
  margin-bottom: 16px;
}

.member-list-container {
  margin: 0 16px;
  padding: 16px 0;
  background-color: #fff;
}

.member-list-container h3 {
  margin: 0 0 16px 0;
  padding: 0 16px;
  font-size: 16px;
  font-weight: 500;
  color: #323233;
}

.loading {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>