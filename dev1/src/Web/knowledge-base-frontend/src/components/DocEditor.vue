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
      <div class="editor-body">
        <textarea
          v-model="content"
          class="editor-textarea"
          placeholder="开始编写文档内容..."
          :readonly="saving"
        ></textarea>
      </div>
    </template>
    <div v-else class="error">
      <el-result icon="error" title="文档加载失败" sub-title="请检查文档是否存在或重试" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { getDocDetail, updateDoc, createDocVersion } from '@/api/doc'
import { ElMessage } from 'element-plus'

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
  if (!content.value.trim()) {
    ElMessage.warning('内容不能为空')
    return
  }
  saving.value = true
  try {
    // 更新文档标题和内容
    await updateDoc(props.docId, {
      title: titleDraft.value || doc.value.title,
      content: content.value
    })
    // 尝试创建版本（如果后端支持）
    try {
      await createDocVersion(props.docId, content.value, `用户保存于 ${new Date().toLocaleString()}`)
    } catch (versionErr) {
      // 版本功能暂未启用，忽略错误
    }
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
  // 标题更改时自动保存一次（可考虑加入防抖）
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
  if (newId) {
    loadDoc()
  } else {
    doc.value = null
  }
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
  padding: 24px;
  overflow: auto;
}

.editor-textarea {
  width: 100%;
  height: 100%;
  min-height: 400px;
  border: none;
  outline: none;
  resize: none;
  font-size: 15px;
  line-height: 1.8;
  color: #2c3e50;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  background: transparent;
}

.editor-textarea::placeholder {
  color: #b3b3b3;
}

.error {
  padding: 40px;
}
</style>