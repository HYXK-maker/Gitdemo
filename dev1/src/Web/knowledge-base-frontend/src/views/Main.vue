<template>
  <div class="main-layout" :class="{ dark: themeStore.isDark }">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <div class="logo" @click="toggleSidebar">
          <span class="logo-icon">📚</span>
          <span v-show="!isCollapsed" class="logo-text">知识库</span>
        </div>
        <el-button v-show="!isCollapsed" circle size="small" class="toggle-btn" @click="toggleSidebar">
          ◀
        </el-button>
      </div>
      <div v-show="!isCollapsed" class="sidebar-content">
        <div class="sidebar-actions">
          <el-button type="primary" size="small" @click="dirTreeRef?.openCreateDialog?.(null, 'dir')">
            <el-icon><FolderAdd /></el-icon> 新建目录
          </el-button>
          <el-button type="success" size="small" @click="dirTreeRef?.openCreateDialog?.(null, 'file')">
            <el-icon><DocumentAdd /></el-icon> 新建文档
          </el-button>
        </div>
        <DirTree ref="dirTreeRef" @select-doc="handleSelectDoc" @doc-created="handleDocCreated" />
        <div class="sidebar-menu">
          <div class="menu-item" :class="{ active: currentView === 'recycle' }" @click="showRecycleBin">
            <el-icon><Delete /></el-icon>
            <span>回收站</span>
          </div>
        </div>
      </div>
      <div v-show="isCollapsed" class="collapsed-expand" @click="toggleSidebar">
        ▶
      </div>
    </div>

    <!-- 主区域 -->
    <div class="main-container">
      <div class="header">
        <div class="header-left">
          <span class="current-path">{{ currentPath }}</span>
        </div>
        <div class="header-right">
          <el-button class="theme-btn" @click="themeStore.toggle" circle>
            {{ themeStore.isDark ? '☀️' : '🌙' }}
          </el-button>
          <el-dropdown trigger="click">
            <div class="user-avatar">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="user-name">{{ userName }}</span>
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
        <div v-if="currentView === 'recycle'" class="page-view">
          <RecycleBin ref="recycleBinRef" />
        </div>
        <div v-else class="doc-workspace">
          <div v-if="currentDocId" class="workspace-inner">
            <div class="editor-pane">
              <DocEditor :key="currentDocId" :docId="currentDocId" @doc-updated="handleDocUpdated" />
            </div>
            <VersionPanel ref="versionPanelRef" :docId="currentDocId" @restore="handleRestoreVersion" />
          </div>
          <div v-else class="empty-state">
            <div class="empty-content">
              <span class="empty-icon">📄</span>
              <p class="empty-text">请在左侧选择或创建文档</p>
            </div>
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
import RecycleBin from '@/components/RecycleBin.vue'
import { UserFilled, ArrowDown, Delete, FolderAdd, DocumentAdd } from '@element-plus/icons-vue'
import { getDocDetail } from '@/api/doc'
import { ElMessage } from 'element-plus'
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
const dirTreeRef = ref()
const versionPanelRef = ref()
const recycleBinRef = ref()
const currentDocId = ref(null)
const isCollapsed = ref(false)
const currentView = ref('doc')

const userName = computed(() => {
  return userStore.currentUser?.nickname || userStore.currentUser?.username || '用户'
})
const currentPath = ref('知识库')

async function handleSelectDoc(docId) {
  if (!docId) { currentDocId.value = null; return }
  currentView.value = 'doc'
  try {
    await getDocDetail(docId)
    currentDocId.value = docId
    currentPath.value = `知识库 > 文档 ${docId}`
  } catch (error) {
    console.error('文档加载失败:', error)
    // 不吞错误，让目录树重新加载
    if (dirTreeRef.value) dirTreeRef.value.loadTree()
  }
}

async function handleDocCreated(docId) {
  if (dirTreeRef.value) await dirTreeRef.value.loadTree()
  if (docId) await handleSelectDoc(docId)
}

function handleDocUpdated() {
  if (dirTreeRef.value?.loadTree) dirTreeRef.value.loadTree()
  if (versionPanelRef.value?.loadVersions) versionPanelRef.value.loadVersions()
}

async function handleRestoreVersion(content) {
  await handleSelectDoc(currentDocId.value)
  setTimeout(() => {
    const editor = document.querySelector('.ql-editor')
    if (editor) {
      editor.innerHTML = content
      ElMessage.success('版本内容已加载，请点击保存')
    }
  }, 500)
}

function showRecycleBin() {
  currentView.value = 'recycle'
  currentPath.value = '回收站'
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
* { box-sizing: border-box; margin: 0; padding: 0; }

.main-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #f0f2f5;
  color: #303133;
  transition: background 0.3s, color 0.3s;
}
.main-layout.dark {
  background: #141414;
  color: #e0e0e0;
}

/* 侧边栏 */
.sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.3s, background 0.3s, border-color 0.3s;
  position: relative;
}
.main-layout.dark .sidebar {
  background: #1f1f1f;
  border-color: #333;
}
.sidebar.collapsed { width: 50px; }
.sidebar-header {
  height: 56px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
  overflow: hidden;
  transition: border-color 0.3s;
}
.main-layout.dark .sidebar-header { border-color: #333; }
.logo { display: flex; align-items: center; gap: 8px; cursor: pointer; white-space: nowrap; }
.logo-icon { font-size: 20px; flex-shrink: 0; }
.logo-text { font-size: 16px; font-weight: 600; color: #1677ff; }
.toggle-btn { flex-shrink: 0; }

.sidebar-actions {
  display: flex;
  gap: 8px;
  padding: 12px;
  flex-shrink: 0;
}
.sidebar-actions .el-button {
  flex: 1;
  font-size: 12px;
}
.sidebar-content { flex: 1; overflow: hidden; display: flex; flex-direction: column; }
.sidebar-menu {
  border-top: 1px solid #e8e8e8;
  padding: 8px;
  transition: border-color 0.3s;
}
.main-layout.dark .sidebar-menu { border-color: #333; }
.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}
.menu-item:hover { background: #f5f5f5; }
.main-layout.dark .menu-item:hover { background: #2a2a2a; }
.menu-item.active { background: #e6f4ff; color: #1677ff; }
.main-layout.dark .menu-item.active { background: #1a3a5c; color: #4dabf7; }
.collapsed-expand {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 16px;
  cursor: pointer;
  color: #1677ff;
}

/* 主区域 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}
.header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  flex-shrink: 0;
  transition: background 0.3s, border-color 0.3s;
}
.main-layout.dark .header {
  background: #1f1f1f;
  border-color: #333;
}
.header-right { display: flex; align-items: center; gap: 16px; }
.theme-btn { margin-right: 4px; }
.current-path { font-size: 14px; color: #666; transition: color 0.3s; }
.main-layout.dark .current-path { color: #aaa; }
.user-avatar { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.user-name { transition: color 0.3s; }
.main-layout.dark .user-name { color: #e0e0e0; }

.content-area {
  flex: 1;
  overflow: hidden;
  padding: 16px;
  min-height: 0;
}
.doc-workspace { height: 100%; display: flex; flex-direction: column; }
.workspace-inner {
  display: flex;
  flex: 1;
  gap: 16px;
  min-height: 0;
}
.editor-pane { flex: 1; min-width: 0; overflow: hidden; }

/* 空状态 */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: #fff;
  border-radius: 12px;
  transition: background 0.3s;
}
.main-layout.dark .empty-state { background: #1f1f1f; }
.empty-content { text-align: center; }
.empty-icon { font-size: 64px; display: block; margin-bottom: 16px; }
.empty-text { font-size: 16px; color: #999; }

.page-view {
  height: 100%;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: background 0.3s;
}
.main-layout.dark .page-view { background: #1f1f1f; }
</style>
