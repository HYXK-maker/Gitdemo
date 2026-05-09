<template>
  <div class="doc-editor">
    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else class="editor-wrapper">
      <div class="editor-toolbar">
        <el-input
          v-model="title"
          placeholder="文档标题"
          size="large"
          @blur="saveTitle"
          class="title-input"
        />
        <div class="toolbar-actions">
          <el-tag v-if="saving" type="info" size="small">保存中...</el-tag>
          <el-tag v-else-if="saved" type="success" size="small">已保存</el-tag>
          <el-button size="small" @click="showVersions = true">历史版本</el-button>
        </div>
      </div>

      <div class="editor-content">
        <textarea
          ref="textareaRef"
          v-model="content"
          class="markdown-editor"
          placeholder="请输入内容..."
          @input="onContentChange"
        ></textarea>
      </div>
    </div>


    <el-dialog v-model="showVersions" title="历史版本" width="700px">
      <el-timeline>
        <el-timeline-item
          v-for="version in versions"
          :key="version.version"
          :timestamp="formatTime(version.createdAt)"
          placement="top"
        >
          <el-card>
            <div class="version-info">
              <span class="version-num">v{{ version.version }}</span>
              <span class="version-note">{{ version.note || '无说明' }}</span>
              <el-button link type="primary" @click="rollbackToVersion(version.version)">
                恢复此版本
              </el-button>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { getDocDetail, updateDoc, getDocumentVersions, rollbackVersion } from '@/api/doc'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({ docId: Number })

const loading = ref(false)
const title = ref('')
const content = ref('')
const saving = ref(false)
const saved = ref(false)
const showVersions = ref(false)
const versions = ref([])

let saveTimer = null
let autoSaveTimer = null

async function loadDoc() {
  if (!props.docId) return
  loading.value = true
  try {
    const data = await getDocDetail(props.docId)
    title.value = data.title
    content.value = data.content || ''
  } catch (error) {
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
    await updateDoc(props.docId, { title: title.value, content: content.value, versionNote })
    saved.value = true
    setTimeout(() => { saved.value = false }, 2000)
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
  await updateDoc(props.docId, { title: title.value, content: content.value })
  ElMessage.success('标题已保存')
}

async function loadVersions() {
  if (!props.docId) return
  const data = await getDocumentVersions(props.docId)
  versions.value = data
}

async function rollbackToVersion(versionNum) {
  await rollbackVersion(props.docId, versionNum)
  ElMessage.success('已恢复版本')
  showVersions.value = false
  await loadDoc()
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

watch(() => props.docId, () => {
  loadDoc()
}, { immediate: true })

watch(showVersions, (val) => {
  if (val) loadVersions()
})

onBeforeUnmount(() => {
  if (saveTimer) clearTimeout(saveTimer)
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
})
</script>

<style scoped>
.doc-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  height: 100%;
  color: #909399;
}
.editor-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}
.title-input {
  width: 300px;
}
.toolbar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
.editor-content {
  flex: 1;
  padding: 16px;
}
.markdown-editor {
  width: 100%;
  height: 100%;
  padding: 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  outline: none;
}
.markdown-editor:focus {
  border-color: #409eff;
}
.version-info {
  display: flex;
  align-items: center;
  gap: 20px;
}
.version-num {
  font-weight: bold;
  color: #409eff;
}
.version-note {
  flex: 1;
  color: #606266;
}
</style>
