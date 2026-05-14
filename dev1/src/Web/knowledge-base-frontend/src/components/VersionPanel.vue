<template>
  <div class="version-panel">
    <div class="panel-header">版本历史</div>
    <div v-if="versions.length === 0" class="empty">暂无版本</div>
    <div v-else class="version-list">
      <div
        v-for="ver in versions"
        :key="ver.id"
        class="version-item"
        @click="previewVersion(ver)"
      >
        <div class="version-num">v{{ ver.versionNumber }}</div>
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
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDocVersions } from '@/api/doc'

const props = defineProps({ docId: Number })
const emit = defineEmits(['restore'])
const versions = ref([])
const previewId = ref(null)

const loadVersions = async () => {
  if (!props.docId) return
  try {
    const res = await getDocVersions(props.docId)
    versions.value = res
  } catch (e) {
    versions.value = []
  }
}

watch(() => props.docId, () => {
  loadVersions()
  previewId.value = null
}, { immediate: true })

function previewVersion(ver) {
  previewId.value = previewId.value === ver.id ? null : ver.id
}

function restoreVersion(ver) {
  ElMessageBox.confirm(`恢复到版本 v${ver.versionNumber}？`, '提示', { type: 'warning' })
    .then(() => {
      emit('restore', ver.content)
      ElMessage.success('版本已恢复')
    })
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString()
}
function truncate(text, len) {
  return text?.length > len ? text.substring(0, len) + '...' : text
}
</script>

<style scoped>
.version-panel {
  padding: 12px;
}
.panel-header {
  font-weight: bold;
  margin-bottom: 8px;
}
.version-item {
  padding: 8px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
}
.version-num { font-size: 13px; font-weight: 500; }
.version-time { font-size: 12px; color: #909399; }
.version-actions { margin-top: 4px; }
.version-preview-text {
  margin-top: 4px;
  font-size: 12px;
  color: #606266;
  white-space: pre-wrap;
}
</style>