<template>
  <div class="version-panel" :class="{ collapsed: isCollapsed }">
    <div class="panel-header" @click="toggle">
      <span v-show="!isCollapsed">版本历史</span>
      <span class="toggle-icon">{{ isCollapsed ? '◀' : '▶' }}</span>
    </div>
    <div v-show="!isCollapsed" class="panel-body">
      <div v-if="versions.length === 0" class="empty">暂无版本</div>
      <div v-else class="version-list">
        <div v-for="ver in versions" :key="ver.id" class="version-item" @click="previewVersion(ver)">
          <div class="version-num">v{{ ver.versionNum || ver.versionNumber }}</div>
          <div class="version-time">{{ formatTime(ver.createTime) }}</div>
          <div class="version-actions" v-if="previewId === ver.id">
            <el-button size="small" @click.stop="restoreVersion(ver)">恢复</el-button>
          </div>
          <div v-if="previewId === ver.id" class="version-preview-text">
            {{ truncate(ver.content, 80) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDocVersions } from '@/api/doc'

const props = defineProps({ docId: [Number, String] })
const emit = defineEmits(['restore'])
const versions = ref([])
const previewId = ref(null)
const isCollapsed = ref(false)

const loadVersions = async () => {
  if (!props.docId) return
  try {
    const res = await getDocVersions(props.docId)
    versions.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    versions.value = []
  }
}

defineExpose({ loadVersions })

watch(() => props.docId, () => {
  loadVersions()
  previewId.value = null
}, { immediate: true })

function toggle() {
  isCollapsed.value = !isCollapsed.value
}

function previewVersion(ver) {
  previewId.value = previewId.value === ver.id ? null : ver.id
}

function restoreVersion(ver) {
  const versionNum = ver.versionNum || ver.versionNumber || '?'
  ElMessageBox.confirm(`恢复到版本 v${versionNum}？`, '提示', { type: 'warning' })
    .then(() => {
      emit('restore', ver.content)
      ElMessage.success('版本已恢复')
    })
    .catch(() => {})
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

function truncate(text, len) {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}
</script>

<style scoped>
.version-panel {
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
  transition: width 0.3s, background 0.3s;
  height: 100%;
  width: 260px;
}
.version-panel.collapsed { width: 36px; }
.panel-header {
  font-weight: bold;
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  white-space: nowrap;
  flex-shrink: 0;
  transition: border-color 0.3s;
}
.toggle-icon { font-size: 12px; color: #909399; }
.panel-body { flex: 1; overflow-y: auto; }
.version-list { padding: 8px; }
.version-item {
  padding: 8px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: border-color 0.3s;
}
.version-num { font-size: 13px; font-weight: 500; }
.version-time { font-size: 12px; color: #909399; }
.version-actions { margin-top: 4px; }
.version-preview-text {
  margin-top: 4px;
  font-size: 12px;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-all;
}
.empty { padding: 20px; text-align: center; color: #909399; }
</style>
