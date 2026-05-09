<template>
  <div class="dir-tree-container">
    <div class="toolbar">
      <el-button type="primary" size="small" plain @click="openCreateDirDialog(null)">
        + 新建根目录
      </el-button>
      <el-button type="success" size="small" plain @click="openCreateDocDialog(null)">
        + 新建文档
      </el-button>
    </div>

    <!-- 目录树列表 -->
    <div class="tree-list">
      <div v-for="item in treeData" :key="item.id" class="tree-node">
        <div class="node-label" @click="onNodeClick(item)">
          <span>{{ item.type === 'dir' ? '📁' : '📄' }}</span>
          <span>{{ item.name }}</span>
        </div>
      </div>
      <div v-if="treeData.length === 0" class="empty-text">
        暂无数据，请先创建目录
      </div>
    </div>

    <!-- 新建目录弹窗 -->
    <el-dialog v-model="dirDialog.visible" title="新建目录" width="350px">
      <el-input v-model="dirDialog.name" placeholder="请输入目录名称" />
      <template #footer>
        <el-button @click="dirDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateDir">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新建文档弹窗 -->
    <el-dialog v-model="docDialog.visible" title="新建文档" width="350px">
      <el-input v-model="docDialog.title" placeholder="请输入文档标题" />
      <template #footer>
        <el-button @click="docDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateDoc">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getDirTree, createDir } from '@/api/dir'
import { createDoc } from '@/api/doc'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['select-doc'])
const treeData = ref([])

// 弹窗状态
const dirDialog = reactive({
  visible: false,
  name: '',
  parent: null
})

const docDialog = reactive({
  visible: false,
  title: '',
  parent: null
})

// 加载目录树
async function loadTree() {
  try {
    console.log('开始加载目录树...')
    const res = await getDirTree()
    console.log('目录树数据:', res)
    treeData.value = res || []
  } catch (error) {
    console.error('加载目录失败:', error)
    ElMessage.error('加载目录失败，请检查后端服务')
  }
}

// 打开新建目录弹窗
function openCreateDirDialog(parent) {
  console.log('打开新建目录弹窗, parent:', parent)
  dirDialog.parent = parent
  dirDialog.name = ''
  dirDialog.visible = true
}

// 打开新建文档弹窗
function openCreateDocDialog(parent) {
  console.log('打开新建文档弹窗, parent:', parent)
  docDialog.parent = parent
  docDialog.title = ''
  docDialog.visible = true
}

// 提交创建目录
async function submitCreateDir() {
  if (!dirDialog.name.trim()) {
    ElMessage.warning('请输入目录名称')
    return
  }
  const parentId = dirDialog.parent ? dirDialog.parent.id : 0
  console.log('创建目录参数:', { name: dirDialog.name, parentId })

  try {
    await createDir({ name: dirDialog.name, parentId })
    ElMessage.success('目录创建成功')
    dirDialog.visible = false
    await loadTree()
  } catch (error) {
    console.error('创建目录失败:', error)
    ElMessage.error('创建失败，请检查后端')
  }
}

// 提交创建文档
async function submitCreateDoc() {
  if (!docDialog.title.trim()) {
    ElMessage.warning('请输入文档标题')
    return
  }
  const folderId = docDialog.parent ? docDialog.parent.id : 0
  console.log('创建文档参数:', { title: docDialog.title, folderId })

  try {
    await createDoc({
      title: docDialog.title,
      folderId: folderId,
      doc_type: 1,
      content: ''
    })
    ElMessage.success('文档创建成功')
    docDialog.visible = false
    await loadTree()
  } catch (error) {
    console.error('创建文档失败:', error)
    ElMessage.error('创建失败，请检查后端')
  }
}

// 点击节点
function onNodeClick(item) {
  if (item.type === 'doc') {
    emit('select-doc', item.id)
  } else if (item.type === 'dir') {
    // 可以扩展目录展开功能
    console.log('点击目录:', item.name)
  }
}

onMounted(() => {
  loadTree()
})
</script>

<style scoped>
.dir-tree-container {
  padding: 12px;
  height: 100%;
  overflow-y: auto;
}

.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.tree-list {
  margin-top: 8px;
}

.tree-node {
  padding: 6px 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
}

.tree-node:hover {
  background: #ecf5ff;
}

.node-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 40px 20px;
  font-size: 13px;
}
</style>
