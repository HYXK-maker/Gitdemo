<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo" @click="toggleSidebar">
          <span class="logo-icon">📚</span>
          <span v-show="!sidebarCollapsed" class="logo-text">文档知识库</span>
        </div>
        <el-button
          v-if="!sidebarCollapsed"
          :icon="Fold"
          circle
          size="small"
          class="collapse-btn"
          @click="sidebarCollapsed = true"
        />
      </div>
      <DirTree ref="dirTreeRef" @select-doc="handleSelectDoc" />
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部栏 -->
      <div class="header">
        <div class="header-left">
          <el-button
            v-if="sidebarCollapsed"
            :icon="Expand"
            circle
            size="small"
            @click="sidebarCollapsed = false"
          />
          <span class="current-path">{{ currentPath }}</span>
        </div>
        <div class="header-right">
          <el-badge :value="3" class="badge-item">
            <el-button :icon="Bell" circle />
          </el-badge>
          <el-dropdown trigger="click">
            <div class="user-avatar">
              <el-avatar :size="32" :icon="UserFilled" />
              <span>{{ userStore.currentUser?.nickname || userStore.currentUser?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="User">个人中心</el-dropdown-item>
                <el-dropdown-item :icon="Setting">设置</el-dropdown-item>
                <el-dropdown-item :icon="SwitchButton" divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 文档编辑区 -->
      <div class="content-area">
        <DocEditor v-if="currentDocId" :docId="currentDocId" />
        <div v-else class="empty-state">
          <el-empty description="请在左侧选择或创建文档" :image-size="120">
            <el-button type="primary" @click="createNewDoc">新建文档</el-button>
          </el-empty>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import DirTree from '@/components/DirTree.vue'
import DocEditor from '@/components/DocEditor.vue'
import {
  Fold, Expand, Bell, UserFilled, ArrowDown, User,
  Setting, SwitchButton, Plus
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const dirTreeRef = ref()
const currentDocId = ref(null)
const sidebarCollapsed = ref(false)

const currentPath = computed(() => {
  return '我的文档'
})

function handleSelectDoc(docId) {
  currentDocId.value = docId
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function createNewDoc() {
  ElMessage.info('请在左侧目录中右键创建文档')
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f5f7fa;
}

/* 侧边栏 */
.sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.02);
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.collapse-btn {
  flex-shrink: 0;
}

/* 主容器 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-path {
  font-size: 14px;
  color: #606266;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-avatar:hover {
  background: #f5f7fa;
}

/* 内容区 */
.content-area {
  flex: 1;
  overflow: hidden;
  padding: 16px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: #fff;
  border-radius: 12px;
}

.badge-item :deep(.el-badge__content) {
  top: 4px;
  right: 4px;
}
</style>
