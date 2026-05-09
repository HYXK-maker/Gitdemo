<template>
  <div class="main-layout">
    <!-- 侧边栏 - 折叠后更窄 -->
    <div class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <div class="logo" @click="toggleSidebar">
          <span class="logo-icon">📚</span>
          <span v-show="!isCollapsed" class="logo-text">知识库</span>
        </div>
        <el-button
          circle
          size="small"
          class="toggle-btn"
          @click="toggleSidebar"
        >
          {{ isCollapsed ? '▶' : '◀' }}
        </el-button>
      </div>
      <div v-show="!isCollapsed" class="sidebar-content">
        <DirTree ref="dirTreeRef" @select-doc="handleSelectDoc" @doc-created="handleDocCreated" />
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <div class="header">
        <div class="header-left">
          <span class="current-path">{{ currentPath }}</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="user-avatar">
              <el-avatar :size="32" :icon="UserFilled" />
              <span>{{ userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="content-area">
        <DocEditor
          v-if="currentDocId"
          :key="currentDocId"
          :docId="currentDocId"
          @doc-updated="handleDocUpdated"
        />
        <div v-else class="empty-state">
          <el-empty description="请在左侧选择或创建文档" :image-size="120" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import DirTree from '@/components/DirTree.vue'
import DocEditor from '@/components/DocEditor.vue'
import { UserFilled, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const dirTreeRef = ref()
const currentDocId = ref(null)
const isCollapsed = ref(false)

const userName = computed(() => {
  return userStore.currentUser?.nickname || userStore.currentUser?.username || '用户'
})

const currentPath = ref('知识库')

function handleSelectDoc(docId) {
  console.log('选中文档:', docId)
  currentDocId.value = docId
}

function handleDocCreated(docId) {
  console.log('文档创建成功:', docId)
  currentDocId.value = docId
}

function handleDocUpdated() {
  // 文档更新后可以刷新目录树
  if (dirTreeRef.value) {
    dirTreeRef.value.refreshTree()
  }
}

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
  }
})
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f5f7fa;
}

/* 侧边栏 - 折叠后宽度 52px */
.sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: 52px;
}

.sidebar-header {
  height: 56px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  overflow: hidden;
  flex: 1;
}

.logo-icon {
  font-size: 22px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
  white-space: nowrap;
}

.toggle-btn {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  padding: 0;
}

.sidebar-content {
  flex: 1;
  overflow: hidden;
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
  flex-shrink: 0;
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
</style>
