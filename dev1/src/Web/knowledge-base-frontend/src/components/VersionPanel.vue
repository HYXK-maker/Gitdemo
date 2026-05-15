<template>
  <div class="version-panel" :class="{ collapsed: isCollapsed }">
    <div class="panel-header" @click="toggle">
      <span v-show="!isCollapsed">版本历史</span>
      <span class="toggle-icon">{{ isCollapsed ? '◀' : '▶' }}</span>
    </div>
    <div v-show="!isCollapsed" class="panel-body">
      <div v-if="versions.length === 0" class="empty">暂无版本</div>
      <div v-else class="version-list">
        <div
          v-for="(ver, index) in versions"
          :key="ver.id"
          class="version-item"
          @click="previewVersion(ver)"
        >

          <div class="version-num">v{{ versions.length - index }}</div>
          <div class="version-time">{{ formatTime(ver.createTime) }}</div>
          <div class="version-note" v-if="ver.versionNote">{{ ver.versionNote }}</div>
        </div>
      </div>
    </div>


    <el-dialog v-model="previewVisible" title="版本详情" width="700px" top="5vh">
      <div class="preview-header">
        <!-- 预览弹窗中也用索引计算版本号 -->
        <span>版本 v{{ getCurrentPreviewVersion() }}</span>
        <span class="preview-time">{{ formatTime(currentPreview?.createTime) }}</span>
      </div>
      <div class="preview-content" v-html="currentPreview?.content || ''"></div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="restoreVersion(currentPreview)">恢复此版本</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDocVersions } from '@/api/doc'

const props = defineProps({ docId: [Number, String] })
const emit = defineEmits(['restore'])
const versions = ref([])
const previewVisible = ref(false)
const currentPreview = ref(null)
const isCollapsed = ref(false)

const loadVersions = async () => {
  if (!props.docId) return
  try {
    const res = await getDocVersions(props.docId)
    let list = Array.isArray(res) ? res : (res.data || [])

    // 按创建时间升序排列（最旧的在前，最新的在后）
    list.sort((a, b) => {
      const timeA = new Date(a.createTime || a.create_time || 0).getTime()
      const timeB = new Date(b.createTime || b.create_time || 0).getTime()
      return timeA - timeB
    })

    versions.value = list
  } catch (e) {
    console.error('加载版本列表失败:', e)
    versions.value = []
  }
}

defineExpose({ loadVersions })

watch(() => props.docId, () => {
  loadVersions()
}, { immediate: true })

function toggle() {
  isCollapsed.value = !isCollapsed.value
}

function previewVersion(ver) {
  currentPreview.value = ver
  previewVisible.value = true
}

// 获取当前预览版本的版本号
function getCurrentPreviewVersion() {
  if (!currentPreview.value) return '?'
  const index = versions.value.findIndex(v => v.id === currentPreview.value.id)
  if (index === -1) return '?'
  // 版本号 = 总数量 - 当前索引
  return versions.value.length - index
}

function restoreVersion(ver) {
  if (!ver) return
  const versionNum = getCurrentPreviewVersion()
  ElMessageBox.confirm(`确定恢复到版本 v${versionNum}？当前内容将被覆盖。`, '警告', {
    type: 'warning',
    confirmButtonText: '确定恢复',
    cancelButtonText: '取消'
  })
    .then(() => {
      emit('restore', ver.content || '')
      previewVisible.value = false
      ElMessage.success(`已恢复到版本 v${versionNum}，请保存`)
    })
    .catch(() => {})
}

function formatTime(time) {
  if (!time) return ''
  try {
    const d = new Date(time)
    if (isNaN(d.getTime())) return time
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  } catch {
    return time
  }
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
  transition: width 0.3s;
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
}
.toggle-icon { font-size: 12px; color: #909399; }
.panel-body { flex: 1; overflow-y: auto; }
.version-list { padding: 8px; }
.version-item {
  padding: 10px 8px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
}
.version-item:hover { background: #f5f7fa; }
.version-num { font-size: 14px; font-weight: 600; color: #409eff; }
.version-time { font-size: 12px; color: #909399; margin-top: 4px; }
.version-note { font-size: 12px; color: #606266; margin-top: 2px; }
.empty { padding: 20px; text-align: center; color: #909399; }

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
  font-weight: 600;
}
.preview-time { font-size: 13px; color: #909399; font-weight: normal; }
.preview-content {
  max-height: 500px;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
  line-height: 1.8;
}
.preview-content :deep(img) { max-width: 100%; }
</style>
