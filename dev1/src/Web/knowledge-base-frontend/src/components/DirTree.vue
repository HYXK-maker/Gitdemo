<template>
  <div class="dir-tree-container">
    <div class="tree-area">
      <TreeNode
        v-for="item in treeData"
        :key="item.id"
        :node="item"
        :selected-id="selectedId"
        :expanded-map="expandedMap"
        @toggle="toggleExpand"
        @select="handleSelect"
        @command="handleCommand"
        @contextmenu="showContextMenu"
      />
      <div v-if="treeData.length === 0" class="empty-state">
        <span class="empty-icon">📂</span>
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


    <el-dialog v-model="moveDialogVisible" title="移动到..." width="400px">
      <el-tree
        :data="moveTreeData"
        node-key="id"
        :props="{ label: 'name' }"
        highlight-current
        default-expand-all
        @node-click="onMoveTargetClick"
      />
      <template #footer>
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMoveDir" :disabled="!moveTargetId">确定</el-button>
      </template>
    </el-dialog>

    <div v-if="menuVisible" class="context-menu" :style="{ left: menuX + 'px', top: menuY + 'px' }">
      <div v-if="menuItem?.type === 'dir'" class="menu-item" @click="menuCreateDir">📁 新建子目录</div>
      <div v-if="menuItem?.type === 'dir'" class="menu-item" @click="menuCreateDoc">📄 新建文档</div>
      <div class="menu-divider"></div>
      <div class="menu-item" @click="menuRename">✏️ 重命名</div>
      <div v-if="menuItem?.type === 'dir'" class="menu-item" @click="menuMove">📦 移动到...</div>
      <div class="menu-divider"></div>
      <div class="menu-item danger" @click="menuDelete">🗑️ 删除</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDirTree, createDir, renameDir, deleteDir, moveDirectory } from '@/api/dir'
import { createDoc, deleteDoc, renameDoc } from '@/api/doc'
import TreeNode from './TreeNode.vue'

const emit = defineEmits(['select-doc', 'doc-created'])

const treeData = ref([])
const selectedId = ref(null)
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

const moveDialogVisible = ref(false)
const moveNode = ref(null)
const moveTargetId = ref(null)

const menuVisible = ref(false)
const menuX = ref(0)
const menuY = ref(0)
const menuItem = ref(null)

async function loadTree() {
  try {
    const data = await getDirTree()
    treeData.value = Array.isArray(data) ? data : (data.data || [])
    treeData.value.forEach(item => {
      if (item.type === 'dir') expandedMap.value[item.id] = true
    })
  } catch (error) {
    console.error('加载目录失败:', error)
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
      await createDir({ name: dialogName.value, parentId: dialogParent.value?.id || 0 })
      ElMessage.success('目录创建成功')
    } else {
      const res = await createDoc({ title: dialogName.value, content: '', folderId: dialogParent.value?.id || 0 })
      ElMessage.success('文档创建成功')
      const docId = res.id || res.data?.id
      if (docId) emit('doc-created', docId)
    }
    dialogVisible.value = false
    if (dialogParent.value) {
      expandedMap.value[dialogParent.value.id] = true
    }
    await loadTree()

    if (dialogParent.value) {
      expandedMap.value[dialogParent.value.id] = true
      expandedMap.value = { ...expandedMap.value }
    }
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败')
  }
}

function handleSelect(node) {
  selectedId.value = node.id
  if (node.type === 'doc') emit('select-doc', node.docId || node.id)
}

function handleCommand(cmd, node) {
  if (cmd === 'createDir') openCreateDialog(node, 'dir')
  else if (cmd === 'createDoc') openCreateDialog(node, 'doc')
  else if (cmd === 'rename') { renameItem.value = node; renameName.value = node.name; renameVisible.value = true }
  else if (cmd === 'delete') deleteNode(node)
}

async function confirmRename() {
  if (!renameName.value.trim()) return ElMessage.warning('请输入名称')
  try {
    if (renameItem.value.type === 'doc') await renameDoc(renameItem.value.id, renameName.value)
    else await renameDir(renameItem.value.id, renameName.value)
    ElMessage.success('重命名成功')
    renameVisible.value = false
    await loadTree()
  } catch (error) { ElMessage.error('重命名失败') }
}

async function deleteNode(node) {
  try {
    await ElMessageBox.confirm(`确定删除 "${node.name}"？`, '警告', { type: 'warning' })
    if (node.type === 'doc') await deleteDoc(node.id)
    else await deleteDir(node.id)
    ElMessage.success('删除成功')
    if (selectedId.value === node.id) { selectedId.value = null; emit('select-doc', null) }
    await loadTree()
  } catch (error) { if (error !== 'cancel') ElMessage.error('删除失败') }
}

const moveTreeData = computed(() => {
  function filterDirs(nodes, excludeId) {
    return nodes.reduce((acc, node) => {
      if (node.type === 'dir' && node.id !== excludeId) {
        acc.push({ id: node.id, name: node.name, children: filterDirs(node.children || [], excludeId) })
      }
      return acc
    }, [])
  }
  return filterDirs(treeData.value, moveNode.value?.id || null)
})

function onMoveTargetClick(target) { moveTargetId.value = target.id }

async function confirmMoveDir() {
  if (!moveTargetId.value || !moveNode.value) return
  try {
    await moveDirectory({ id: moveNode.value.id, newParentId: moveTargetId.value })
    ElMessage.success('移动成功')
    moveDialogVisible.value = false
    await loadTree()
  } catch (error) { ElMessage.error('移动失败') }
}

function showContextMenu(event, node) {
  event.preventDefault()
  menuItem.value = node
  menuX.value = event.clientX
  menuY.value = event.clientY
  menuVisible.value = true
}

function menuCreateDir() { openCreateDialog(menuItem.value, 'dir'); menuVisible.value = false }
function menuCreateDoc() { openCreateDialog(menuItem.value, 'doc'); menuVisible.value = false }
function menuRename() { handleCommand('rename', menuItem.value); menuVisible.value = false }
function menuDelete() { handleCommand('delete', menuItem.value); menuVisible.value = false }
function menuMove() { moveNode.value = menuItem.value; moveTargetId.value = null; moveDialogVisible.value = true; menuVisible.value = false }

function closeMenu() { menuVisible.value = false }

onMounted(() => { loadTree(); document.addEventListener('click', closeMenu) })
onUnmounted(() => { document.removeEventListener('click', closeMenu) })

defineExpose({ loadTree, openCreateDialog })
</script>

<style scoped>
.dir-tree-container { height: 100%; display: flex; flex-direction: column; background: transparent; }
.tree-area { flex: 1; overflow-y: auto; padding: 4px 8px; }
.empty-state { text-align: center; padding: 40px 16px; color: #999; }
.empty-icon { font-size: 40px; display: block; margin-bottom: 8px; }
.context-menu {
  position: fixed; background: #fff; border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12); padding: 6px 0; min-width: 160px; z-index: 2000;
}
.menu-item { padding: 9px 16px; cursor: pointer; font-size: 13px; }
.menu-item:hover { background: #f0f0f0; }
.menu-item.danger { color: #f56c6c; }
.menu-divider { height: 1px; background: #eee; margin: 4px 0; }
</style>
