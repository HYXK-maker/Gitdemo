<!--
  文件：src/components/DocEditor.vue
  功能：富文本编辑，支持加粗、斜体、列表等，保存 HTML 内容
-->

<template>
  <div class="doc-editor">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="8" animated />
    </div>
    <template v-else-if="doc">
      <div class="toolbar">
        <h2 class="doc-title" v-if="!editingTitle">
          <span>{{ doc.title || '未命名文档' }}</span>
          <el-button text size="small" @click="startEditTitle">✏️</el-button>
        </h2>
        <el-input
          v-else
          v-model="titleDraft"
          class="title-input"
          placeholder="输入文档标题"
          @blur="saveTitle"
          @keyup.enter="saveTitle"
          ref="titleInputRef"
        />
        <div class="actions">
          <el-button type="primary" @click="handleSave" :loading="saving">
            <el-icon><Check /></el-icon>保存
          </el-button>
        </div>
      </div>

      <!-- ====== 替换为 Quill 富文本编辑器 ====== -->
      <div class="editor-body">
        <QuillEditor
          v-model:content="content"
          content-type="html"
          theme="snow"
          :toolbar="toolbarOptions"
          style="height: calc(100vh - 200px)"
        />
      </div>
    </template>
    <div v-else class="error">
      <el-result icon="error" title="文档加载失败" sub-title="请检查文档是否存在或重试" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { QuillEditor } from '@vueup/vue-quill'
import 'quill/dist/quill.snow.css'

/* ====== 【替换API】根据你项目中的实际路径调整 ====== */
import { getDocDetail, updateDoc, createDocVersion } from '@/api/doc'

const props = defineProps({
  docId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['doc-updated'])

const doc = ref(null)
const content = ref('')
const titleDraft = ref('')
const editingTitle = ref(false)
const titleInputRef = ref(null)
const loading = ref(false)
const saving = ref(false)

// Quill 工具栏选项
const toolbarOptions = [
  ['bold', 'italic', 'underline', 'strike'],
  [{ 'header': 1 }, { 'header': 2 }],
  [{ 'list': 'ordered' }, { 'list': 'bullet' }],
  [{ 'color': [] }, { 'background': [] }],
  ['blockquote', 'code-block'],
  ['clean']
]

async function loadDoc() {
  if (!props.docId) return
  loading.value = true
  try {
    /* 【替换API】获取文档详情 */
    const res = await getDocDetail(props.docId)
    doc.value = res.data || res
    content.value = doc.value.content || ''
    titleDraft.value = doc.value.title || ''
  } catch (err) {
    ElMessage.error('加载文档失败')
    doc.value = null
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!doc.value) return
  if (!content.value.replace(/<[^>]*>?/gm, '').trim()) {
    ElMessage.warning('内容不能为空')
    return
  }
  saving.value = true
  try {
    /* 【替换API】更新文档内容（content 为 HTML 字符串） */
    await updateDoc(props.docId, {
      title: titleDraft.value || doc.value.title,
      content: content.value
    })
    // 尝试创建版本（若后端未实现版本功能，失败时静默忽略）
    try {
      /* 【替换API】创建文档版本（可选） */
      await createDocVersion(props.docId, content.value, `用户保存于 ${new Date().toLocaleString()}`)
    } catch (versionErr) { /* 静默 */ }
    ElMessage.success('保存成功')
    emit('doc-updated')
  } catch (err) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

function startEditTitle() {
  titleDraft.value = doc.value.title || ''
  editingTitle.value = true
  nextTick(() => {
    titleInputRef.value?.focus()
  })
}

async function saveTitle() {
  editingTitle.value = false
  doc.value.title = titleDraft.value
  try {
    /* 【替换API】更新标题 */
    await updateDoc(props.docId, {
      title: titleDraft.value,
      content: content.value
    })
    ElMessage.success('标题已保存')
    emit('doc-updated')
  } catch {
    ElMessage.error('标题保存失败')
  }
}

watch(() => props.docId, (newId) => {
  if (newId) loadDoc()
  else doc.value = null
}, { immediate: true })
</script>

<style scoped>
.doc-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.loading {
  padding: 32px;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
  gap: 16px;
}
.doc-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.title-input {
  max-width: 400px;
}
.actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.editor-body {
  flex: 1;
  overflow: auto;
}
.error {
  padding: 40px;
}
</style>