<template>
  <div class="dir-tree-container">
    <div class="toolbar-section">
      <div class="title-area">
        <h3>📁 文档目录</h3>
      </div>
      <div class="button-area">
        <el-button type="primary" size="small" plain @click="openCreateDialog(null, 'dir')">
          + 新建目录
        </el-button>
        <el-button type="success" size="small" plain @click="openCreateDialog(null, 'doc')">
          + 新建文档
        </el-button>
        <el-button size="small" plain @click="loadTree">
          🔄 刷新
        </el-button>
      </div>
    </div>

    <div class="search-area">
      <el-input
        v-model="keyword"
        placeholder="搜索文件或文件夹..."
        clearable
        size="small"
        prefix-icon="Search"
      />
    </div>

    <div class="tree-area">
      <TreeNode
        v-for="item in filteredTree"
        :key="item.id"
        :node="item"
        :selected-id="selectedId"
        :expanded-map="expandedMap"
        @toggle="toggleExpand"
        @select="handleSelect"
        @command="handleCommand"
        @contextmenu="showContextMenu"
      />
      <div v-if="filteredTree.length === 0" class="empty-state">
        <p>暂无内容，点击上方按钮创建</p>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-input v-model="dialogName" :placeholder="dialogPlaceholder" @keyup.enter="confirmCreate" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="renameVisible" title="重命名" width="400px">
      <el-input v-model="renameName" placeholder="请输入新名称" @keyup.enter="confirmRename" />
      <template #footer>
        <el-button @click="renameVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRename">确定</el-button>
      </template>
    </el-dialog>

    <div v-if="menuVisible" class="context-menu" :style="{ left: menuX + 'px', top: menuY + 'px' }">
      <div v-if="menuItem?.type === 'dir'" class="menu-item" @click="menuCreateDir">新建子目录</div>
      <div v-if="menuItem?.type === 'dir'" class="menu-item" @click="menuCreateDoc">新建文档</div>
      <div class="menu-divider"></div>
      <div class="menu-item" @click="menuRename">重命名</div>
      <div class="menu-item danger" @click="menuDelete">删除</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDirTree, createDir, renameDir, deleteDir } from '@/api/dir'
import { createDoc } from '@/api/doc'
import TreeNode from './TreeNode.vue'

const emit = defineEmits(['select-doc'])

const treeData = ref([])
const selectedId = ref(null)
const keyword = ref('')
const expandedMap = ref({})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogName = ref('')
const dialogType = ref('')
const dialogParent = ref(null)
const dialogPlaceholder = ref('')

const renameVisible = ref(false)
const renameName = ref('')
const renameItem = ref(null)

const menuVisible = ref(false)
const menuX = ref(0)
const menuY = ref(0)
const menuItem = ref(null)

const filteredTree = computed(() => {
  if (!keyword.value) return treeData.value
  return filterTree(treeData.value, keyword.value.toLowerCase())
})

function filterTree(nodes, kw) {
  return nodes.reduce((acc, node) => {
    const match = node.name.toLowerCase().includes(kw)
    const children = node.children ? filterTree(node.children, kw) : []
    if (match || children.length) {
      acc.push({
        ...node,
        children: children.length ? children : node.children
      })
    }
    return acc
  }, [])
}

async function loadTree() {
  try {
    const data = await getDirTree()
    treeData.value = data
    data.forEach(item => {
      if (item.type === 'dir') {
        expandedMap.value[item.id] = true
      }
    })
  } catch (error) {
    ElMessage.error('加载目录失败')
  }
}

function toggleExpand(id) {
  expandedMap.value[id] = !expandedMap.value[id]

  expandedMap.value = { ...expandedMap.value }
}

function openCreateDialog(parent, type) {
  dialogType.value = type
  dialogParent.value = parent
  dialogTitle.value = type === 'dir' ? '新建目录' : '新建文档'
  dialogName.value = ''
  dialogPlaceholder.value = type === 'dir' ? '请输入目录名称' : '请输入文档标题'
  dialogVisible.value = true
}

async function confirmCreate() {
  if (!dialogName.value.trim()) {
    ElMessage.warning('请输入名称')
    return
  }

  try {
    if (dialogType.value === 'dir') {
      await createDir({
        name: dialogName.value,
        parentId: dialogParent.value ? dialogParent.value.id : 0
      })
      ElMessage.success('目录创建成功')
    } else {
      const res = await createDoc({
        title: dialogName.value,
        content: '# ' + dialogName.value + '\n\n开始编写...',
        folderId: dialogParent.value ? dialogParent.value.id : 0
      })
      ElMessage.success('文档创建成功')
      if (res.id) {
        emit('select-doc', res.id)
      }
    }
    dialogVisible.value = false
    await loadTree()

    if (dialogParent.value) {
      expandedMap.value[dialogParent.value.id] = true
      expandedMap.value = { ...expandedMap.value }
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('创建失败')
  }
}


function handleSelect(node) {
  selectedId.value = node.id
  if (node.type === 'doc') {
    emit('select-doc', node.docId || node.id)
  }
}

function handleCommand(cmd, node) {
  if (cmd === 'createDir') {
    openCreateDialog(node, 'dir')
  } else if (cmd === 'createDoc') {
    openCreateDialog(node, 'doc')
  } else if (cmd === 'rename') {
    renameItem.value = node
    renameName.value = node.name
    renameVisible.value = true
  } else if (cmd === 'delete') {
    deleteNode(node)
  }
}


async function confirmRename() {
  if (!renameName.value.trim()) {
    ElMessage.warning('请输入名称')
    return
  }
  try {
    await renameDir(renameItem.value.id, renameName.value)
    ElMessage.success('重命名成功')
    renameVisible.value = false
    await loadTree()
  } catch (error) {
    ElMessage.error('重命名失败')
  }
}

async function deleteNode(node) {
  try {
    await ElMessageBox.confirm(
      `确定删除 "${node.name}" 吗？${node.type === 'dir' ? '删除目录会删除所有子内容。' : ''}`,
      '警告',
      { type: 'warning' }
    )
    await deleteDir(node.id)
    ElMessage.success('删除成功')
    if (selectedId.value === node.id) {
      selectedId.value = null
      emit('select-doc', null)
    }
    await loadTree()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}


function showContextMenu(event, node) {
  event.preventDefault()
  menuItem.value = node
  menuX.value = event.clientX
  menuY.value = event.clientY
  menuVisible.value = true
}

function menuCreateDir() { handleCommand('createDir', menuItem.value); menuVisible.value = false }
function menuCreateDoc() { handleCommand('createDoc', menuItem.value); menuVisible.value = false }
function menuRename() { handleCommand('rename', menuItem.value); menuVisible.value = false }
function menuDelete() { handleCommand('delete', menuItem.value); menuVisible.value = false }

function closeMenu() { menuVisible.value = false }

onMounted(() => {
  loadTree()
  document.addEventListener('click', closeMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', closeMenu)
})

defineExpose({ loadTree })
</script>

<style scoped>
.dir-tree-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}
.toolbar-section {
  padding: 16px;
  border-bottom: 1px solid #e8eef2;
}
.title-area h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
}
.button-area {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.search-area {
  padding: 12px 16px;
  border-bottom: 1px solid #e8eef2;
}
.tree-area {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}
.context-menu {
  position: fixed;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  min-width: 150px;
  z-index: 2000;
}
.menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 13px;
}
.menu-item:hover {
  background: #f5f7fa;
}
.menu-item.danger {
  color: #f56c6c;
}
.menu-divider {
  height: 1px;
  background: #e8eef2;
  margin: 4px 0;
}
</style>
