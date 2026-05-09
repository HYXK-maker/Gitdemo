<template>
  <div class="app-main">
    <div class="header">
      <div class="logo">
        <span class="logo-icon">📚</span>
        <span class="logo-text">协同文档知识库</span>
      </div>
      <div class="user-info">
        <el-avatar :size="32" :icon="UserFilled" />
        <span>{{ userStore.currentUser?.nickname || userStore.currentUser?.username }}</span>
        <el-button link @click="handleLogout">退出</el-button>
      </div>
    </div>

    <div class="main-content">
      <div class="sidebar">
        <DirTree @select-doc="handleSelectDoc" />
      </div>
      <div class="editor-area">
        <DocEditor v-if="currentDocId" :docId="currentDocId" />
        <div v-else class="empty-placeholder">
          <el-empty description="请在左侧选择或创建文档" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import DirTree from '@/components/DirTree.vue'
import DocEditor from '@/components/DocEditor.vue'
import { UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const currentDocId = ref(null)

function handleSelectDoc(docId) {
  currentDocId.value = docId
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-main {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.header {
  height: 56px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}
.logo-icon {
  font-size: 24px;
}
.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}
.sidebar {
  width: 300px;
  border-right: 1px solid #e4e7ed;
  background: #fafafa;
  overflow-y: auto;
}
.editor-area {
  flex: 1;
  overflow-y: auto;
  background: #fff;
}
.empty-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
</style>
