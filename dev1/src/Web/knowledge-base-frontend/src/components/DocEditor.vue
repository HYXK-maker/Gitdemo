<template>
  <div class="doc-editor">
    <div class="editor-toolbar">
      <span class="doc-title">{{ title || '未命名文档' }}</span>
      <span class="save-status">{{ saveStatus }}</span>
    </div>
    <textarea
      v-model="content"
      class="editor-textarea"
      placeholder="开始编写文档..."
      @input="onContentChange"
    ></textarea>
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { getDocDetail, saveDoc } from '@/api/doc'
import { ElMessage } from 'element-plus'

const props = defineProps({
  docId: {
    type: Number,
    default: null
  }
})

const title = ref('')
const content = ref('')
const saveStatus = ref('')
let saveTimer = null

// 当选中文档时，加载其内容
watch(() => props.docId, async (newId) => {
  if (!newId) {
    title.value = ''
    content.value = ''
    saveStatus.value = ''
    return
  }
  try {
    const doc = await getDocDetail(newId)
    title.value = doc.title
    content.value = doc.content || ''
    saveStatus.value = '已加载'
  } catch (e) {
    ElMessage.error('加载文档失败')
  }
}, { immediate: true })

// 内容变化 2 秒后自动保存
function onContentChange() {
  if (!props.docId) return
  saveStatus.value = '未保存...'
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(async () => {
    try {
      await saveDoc({ id: props.docId, content: content.value })
      saveStatus.value = '已保存'
    } catch (e) {
      saveStatus.value = '保存失败'
      ElMessage.error('自动保存失败')
    }
  }, 2000)
}

onBeforeUnmount(() => {
  if (saveTimer) clearTimeout(saveTimer)
})
</script>

<style scoped>
.doc-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.editor-toolbar {
  padding: 12px 16px;
  border-bottom: 1px solid #e8eef2;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.doc-title {
  font-size: 16px;
  font-weight: bold;
}
.save-status {
  font-size: 12px;
  color: #909399;
}
.editor-textarea {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  padding: 16px;
  font-size: 14px;
  line-height: 1.6;
  font-family: inherit;
}
</style>
