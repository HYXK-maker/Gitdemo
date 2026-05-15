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
            保存
          </el-button>
        </div>
      </div>

      <div class="editor-body">
        <QuillEditor
          v-model:content="content"
          content-type="html"
          theme="snow"
          :toolbar="toolbarOptions"
          style="height: 100%"
        />
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
  saving.value = true
  try {
    const updateRes = await updateDoc(props.docId, {
      title: titleDraft.value || doc.value.title,
      content: content.value
    })
    console.log('updateDoc 成功:', updateRes)

    try {
      const versionRes = await createDocVersion(props.docId, content.value, '保存于 ' + new Date().toLocaleString())
      console.log('createDocVersion 成功:', versionRes)
    } catch (e) {
      console.error('createDocVersion 失败 - 状态码:', e.response?.status)
      console.error('createDocVersion 失败 - 返回数据:', e.response?.data)
      console.error('createDocVersion 失败 - 完整错误:', e)
    }

    ElMessage.success('保存成功')
    emit('doc-updated')
  } catch (err) {
    console.error('updateDoc 失败 - 状态码:', err.response?.status)
    console.error('updateDoc 失败 - 返回数据:', err.response?.data)
    console.error('updateDoc 失败 - 完整错误:', err)
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
  transition: background 0.3s;
}
.loading { padding: 32px; }
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
  transition: border-color 0.3s;
}
.doc-title { font-size: 18px; font-weight: 600; margin: 0; }
.title-input { max-width: 300px; }
.editor-body {
  flex: 1;
  overflow: hidden;
  min-height: 0;
}
.error { padding: 40px; }
</style>
