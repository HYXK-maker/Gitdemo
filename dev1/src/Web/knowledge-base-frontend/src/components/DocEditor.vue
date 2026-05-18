<template>
  <div class="doc-editor">
    <div class="editor-toolbar">
      <input
        v-model="title"
        class="doc-title-input"
        placeholder="文档标题"
        @blur="saveTitle"
        @keyup.enter="saveTitle"
      />
      <div class="toolbar-right">
        <el-button size="small" @click="saveVersion" :loading="savingVersion">
          保存版本
        </el-button>
        <span class="save-status">{{ saveStatus }}</span>
      </div>
    </div>
    <div class="editor-wrapper">
      <QuillEditor
        v-model:content="content"
        theme="snow"
        contentType="html"
        toolbar="full"
        @update:content="onContentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import 'quill/dist/quill.snow.css'
import { getDocDetail, saveDoc, renameDoc, createDocVersion } from '@/api/doc'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  docId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['doc-updated'])

const title = ref('')
const content = ref('')
const saveStatus = ref('已保存')
const savingVersion = ref(false)
let saveTimer = null

watch(() => props.docId, async (newId) => {
  if (!newId) {
    title.value = ''
    content.value = ''
    saveStatus.value = ''
    return
  }
  try {
    const doc = await getDocDetail(newId)
    title.value = doc.title || ''
    content.value = doc.content || ''
    saveStatus.value = '已加载'
  } catch (e) {
    ElMessage.error('加载文档失败')
  }
}, { immediate: true })

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
      ElMessage.error('自动保存失败，请重试')
    }
  }, 2000)
}

async function saveTitle() {
  if (!props.docId || !title.value.trim()) return
  try {
    await renameDoc(props.docId, title.value)
    emit('doc-updated')
    ElMessage.success('标题已保存')
  } catch (e) {
    ElMessage.error('标题保存失败')
  }
}

async function saveVersion() {
  if (!props.docId) return
  try {
    const { value: versionNote } = await ElMessageBox.prompt('请输入版本说明（可选）', '保存版本', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：完成初稿'
    })
    savingVersion.value = true
    // 先确保最新内容已保存
    await saveDoc({ id: props.docId, content: content.value })
    // 创建版本快照
    await createDocVersion(props.docId, {
      content: content.value,
      versionNote: versionNote || ''
    })
    emit('doc-updated')  // 刷新版本面板
    ElMessage.success('版本已保存')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('保存版本失败')
    }
  } finally {
    savingVersion.value = false
  }
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
  background: #fff;
}
.editor-toolbar {
  padding: 12px 16px;
  border-bottom: 1px solid #e8eef2;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.doc-title-input {
  flex: 1;
  font-size: 18px;
  font-weight: bold;
  border: none;
  outline: none;
  background: transparent;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.save-status {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}
.editor-wrapper {
  flex: 1;
  overflow: hidden;
}
</style>
