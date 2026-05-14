<!--
  文件：src/views/Main.vue (或你的布局文件)
  功能：主布局，包含侧边栏（目录树+回收站入口）和主内容区（编辑器/回收站）
-->

<template>
  <div class="main-layout" :class="{ dark: themeStore.isDark }">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <div class="logo" @click="toggleSidebar">
          <span class="logo-icon">📚</span>
          <span v-show="!isCollapsed" class="logo-text">知识库</span>
        </div>
        <el-button circle size="small" class="toggle-btn" @click="toggleSidebar">
          {{ isCollapsed ? '▶' : '◀' }}
        </el-button>
      </div>
      <div v-show="!isCollapsed" class="sidebar-content">
        <!-- 目录树 -->
        <DirTree ref="dirTreeRef" @select-doc="handleSelectDoc" @doc-created="handleDocCreated" />
        
        <!-- 回收站入口（新增） -->
        <div class="sidebar-menu">
          <div
            class="menu-item recycle-entry"
            :class="{ active: currentView === 'recycle' }"
            @click="showRecycleBin"
          >
            <el-icon><Delete /></el-icon>
            <span>回收站</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <div class="header">
        <div class="header-left">
          <span class="current-path">{{ currentPath }}</span>
        </div>
        <div class="header-right">
          <el-button @click="themeStore.toggle" circle>
            {{ themeStore.isDark ? '☀️' : '🌙' }}
          </el-button>
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
        <!-- 回收站视图 -->
        <div v-if="currentView === 'recycle'" class="page-view">
          <RecycleBin ref="recycleBinRef" />
        </div>
        <!-- 文档编辑视图 -->
        <div v-else class="doc-workspace">
          <div v-if="currentDocId" class="doc-workspace">
            <DocEditor
              :key="currentDocId"
              :docId="currentDocId"
              @doc-updated="handleDocUpdated"
              class="editor-pane"
            />
            <VersionPanel
              ref="versionPanelRef"
              :docId="currentDocId"
              @restore="handleRestoreVersion"
              class="version-pane"
            />
          </div>
          <div v-else class="empty-state">
            <el-empty description="请在左侧选择或创建文档" :image-size="120" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import DirTree from '@/components/DirTree.vue'
import DocEditor from '@/components/DocEditor.vue'
import VersionPanel from '@/components/VersionPanel.vue'
import RecycleBin from '@/components/RecycleBin.vue'   // 新增回收站组件
import { UserFilled, ArrowDown, Delete } from '@element-plus/icons-vue'

/* ====== 【替换API】根据实际路径调整 ====== */
import { getDocDetail } from '@/api/doc'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
const dirTreeRef = ref()
const versionPanelRef = ref()
const recycleBinRef = ref()
const currentDocId = ref(null)
const isCollapsed = ref(false)

// 当前视图：'doc' 或 'recycle'
const currentView = ref('doc')

const userName = computed(() => {
  return userStore.currentUser?.nickname || userStore.currentUser?.username || '用户'
})

const currentPath = ref('知识库')

async function handleSelectDoc(docId) {
  if (!docId) {
    currentDocId.value = null
    return
  }
  currentView.value = 'doc'   // 切回文档视图
  try {
    /* 【替换API】验证文档是否存在 */
    await getDocDetail(docId)
    currentDocId.value = docId
    currentPath.value = `知识库 > 文档 ${docId}`
  } catch (error) {
    if (dirTreeRef.value) dirTreeRef.value.loadTree()
  }
}

async function handleDocCreated(docId) {
  if (dirTreeRef.value) await dirTreeRef.value.loadTree()
  if (docId) await handleSelectDoc(docId)
}

function handleDocUpdated() {
  if (dirTreeRef.value) dirTreeRef.value.loadTree()
  if (versionPanelRef.value) versionPanelRef.value.loadVersions()
}

async function handleRestoreVersion(content) {
  if (currentDocId.value) await handleSelectDoc(currentDocId.value)
}

function showRecycleBin() {
  currentView.value = 'recycle'
  currentPath.value = '回收站'
  // 每次进入回收站自动刷新列表
  if (recycleBinRef.value) recycleBinRef.value.fetchList()
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
  if (!token) router.push('/login')
})
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f5f7fa;
}
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
.logo-icon { font-size: 22px; flex-shrink: 0; }
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
  display: flex;
  flex-direction: column;
}
.sidebar-menu {
  border-top: 1px solid #e4e7ed;
  padding: 8px;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}
.menu-item:hover {
  background: #f5f7fa;
}
.menu-item.active {
  background: #ecf5ff;
  color: #409eff;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
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
.header-left { display: flex; align-items: center; gap: 12px; }
.current-path { font-size: 14px; color: #606266; }
.header-right { display: flex; align-items: center; gap: 20px; }
.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
}
.user-avatar:hover { background: #f5f7fa; }

.content-area {
  flex: 1;
  overflow: hidden;
  padding: 16px;
}
.doc-workspace {
  display: flex;
  height: 100%;
  gap: 16px;
}
.editor-pane { flex: 1; overflow: hidden; }
.version-pane {
  width: 280px;
  background: #fff;
  border-left: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow-y: auto;
}
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: #fff;
  border-radius: 12px;
}
.page-view {
  height: 100%;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}
</style>