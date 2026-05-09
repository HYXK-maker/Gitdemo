<template>
  <div class="doc-editor">
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <div v-else class="editor-container">
      <div class="editor-header">
        <div class="title-area">
          <el-input
            v-model="title"
            placeholder="文档标题"
            class="title-input"
            @blur="saveTitle"
          />
        </div>
        <div class="actions-area">
          <el-tag v-if="saving" type="info" size="small">
            保存中...
          </el-tag>
          <el-tag v-else-if="saved" type="success" size="small">
            已保存
          </el-tag>
          <el-button size="small" @click="openHistoryDialog">历史版本</el-button>
        </div>
      </div>

      <div class="editor-body">
        <textarea
          v-model="content"
          class="content-textarea"
          placeholder="请输入内容..."
          @input="onContentChange"
        ></textarea>
      </div>
    </div>

    <!-- 历史版本弹窗 -->
    <el-dialog v-model="showHistory" title="历史版本" width="700px" @close="closeHistory">
      <el-timeline>
        <el-timeline-item
          v-for="(version, index) in versions"
          :key="version.version"
          :timestamp="formatTime(version.createdAt)"
          placement="top"
          :type="index === 0 ? 'primary' : 'info'"
        >
          <el-card shadow="hover">
            <div class="version-item">
              <div class="version-header">
                <span class="version-num">
                  <el-tag :type="index === 0 ? 'danger' : ''" size="small">
                    v{{ version.version }}
                  </el-tag>
                  <span v-if="index === 0" class="current-badge">当前版本</span>
                </span>
                <span class="version-note">{{ version.note || '无说明' }}</span>
                <el-button
                  v-if="index !== 0"
                  type="primary"
                  link
                  size="small"
                  @click="rollbackToVersion(version.version)"
                >
                  恢复此版本
                </el-button>
              </div>
              <div class="version-preview">
                {{ getPreview(version.content) }}
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <div v-if="versions.length === 0" class="empty-versions">暂无历史版本</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { getDocDetail, updateDoc, getDocumentVersions, rollbackVersion } from '@/api/doc'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({ docId: Number })

const loading = ref(false)
const title = ref('')
const content = ref('')
const saving = ref(false)
const saved = ref(false)
const showHistory = ref(false)
const versions = ref([])
const currentVersion = ref(1)

let saveTimer = null
let autoSaveTimer = null

async function loadDoc() {
  if (!props.docId) return
  loading.value = true
  try {
    const data = await getDocDetail(props.docId)
    title.value = data.title || ''
    content.value = data.content || ''
    currentVersion.value = data.version || 1
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载文档失败')
  } finally {
    loading.value = false
  }
}

async function saveDoc(versionNote = '') {
  if (!props.docId) return
  saving.value = true
  saved.value = false
  try {
    await updateDoc(props.docId, {
      title: title.value,
      content: content.value,
      versionNote: versionNote
    })
    saved.value = true
    setTimeout(() => { saved.value = false }, 2000)
    // 保存后重新加载以更新版本号
    await loadDoc()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function autoSave() {
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
  autoSaveTimer = setTimeout(() => {
    if (content.value !== '') {
      saveDoc('自动保存')
    }
  }, 30000)
}

function onContentChange() {
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    saveDoc()
  }, 2000)
  autoSave()
}

async function saveTitle() {
  if (!props.docId) return
  try {
    await updateDoc(props.docId, { title: title.value, content: content.value })
    ElMessage.success('标题已保存')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

async function openHistoryDialog() {
  showHistory.value = true
  await loadVersions()
}

async function loadVersions() {
  if (!props.docId) return
  try {
    const data = await getDocumentVersions(props.docId)
    versions.value = data || []
  } catch (error) {
    console.error('加载版本失败:', error)
    ElMessage.error('加载版本历史失败')
  }
}

async function rollbackToVersion(versionNum) {
  try {
    await ElMessageBox.confirm(
      `确定恢复到 v${versionNum} 吗？当前未保存的内容将会丢失。`,
      '确认恢复',
      {
        type: 'warning',
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消'
      }
    )
    await rollbackVersion(props.docId, versionNum)
    ElMessage.success('已恢复到版本 v' + versionNum)
    showHistory.value = false
    await loadDoc()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('恢复失败')
    }
  }
}

function closeHistory() {
  showHistory.value = false
}

function getPreview(content) {
  if (!content) return '空内容'
  // 移除 Markdown 标记，只显示纯文本预览
  const plainText = content
    .replace(/#{1,6}\s/g, '')
    .replace(/\*\*/g, '')
    .replace(/\*/g, '')
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
    .replace(/`{1,3}[^`]*`{1,3}/g, '')
    .replace(/\n/g, ' ')
    .substring(0, 100)
  return plainText + (plainText.length >= 100 ? '...' : '')
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

watch(() => props.docId, () => {
  loadDoc()
}, { immediate: true })

onBeforeUnmount(() => {
  if (saveTimer) clearTimeout(saveTimer)
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
})
</script>

<style scoped>
.doc-editor {
  height: 100%;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  height: 100%;
  color: #909399;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e4e7ed;
  border-top-color: #409eff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.editor-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.editor-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
}

.title-input {
  font-size: 20px;
  font-weight: 600;
}

.title-input :deep(.el-input__wrapper) {
  box-shadow: none;
  padding: 0;
  background: transparent;
}

.title-input :deep(.el-input__inner) {
  font-size: 20px;
  font-weight: 600;
  height: 40px;
}

.actions-area {
  display: flex;
  gap: 12px;
  align-items: center;
}

.editor-body {
  flex: 1;
  padding: 20px;
}

.content-textarea {
  width: 100%;
  height: 100%;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  outline: none;
  background: #fafafa;
}

.content-textarea:focus {
  border-color: #409eff;
  background: #fff;
}

.version-item {
  padding: 4px 0;
}

.version-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.version-num {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-badge {
  font-size: 12px;
  color: #f56c6c;
  margin-left: 8px;
}

.version-note {
  flex: 1;
  font-size: 13px;
  color: #606266;
}

.version-preview {
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 6px;
  line-height: 1.5;
}

.empty-versions {
  text-align: center;
  padding: 40px;
  color: #909399;
}
</style>
