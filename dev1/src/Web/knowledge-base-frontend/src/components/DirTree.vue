<template>
  <div class="dir-tree-container">
    <div class="toolbar-section">
      <div class="title-area">
        <h3>📁 文档目录</h3>
      </div>
      <div class="button-area">
        <el-button type="primary" size="small" plain @click="openCreateDir(null)">
          + 新建目录
        </el-button>
        <el-button type="success" size="small" plain @click="openCreateDoc(null)">
          + 新建文档
        </el-button>
        <el-button size="small" plain @click="refreshTree">
          🔄 刷新
        </el-button>
      </div>
    </div>

    <div class="search-area">
      <el-input
        v-model="keyword"
        placeholder="搜索..."
        clearable
        size="small"
        prefix-icon="Search"
      />
    </div>

    <div class="tree-area">
      <div v-for="item in filteredList" :key="item.id" class="tree-item">
        <div
          class="tree-item-content"
          :class="{ active: currentId === item.id }"
          @click="handleItemClick(item)"
          @contextmenu.prevent="showContextMenu($event, item)"
        >
          <span
            v-if="item.type === 'dir' && item.children && item.children.length"
            class="expand-icon"
            @click.stop="toggleExpand(item.id)"
          >
            {{ expandedIds.has(item.id) ? '▼' : '▶' }}
          </span>
          <span v-else class="expand-placeholder"></span>

          <span class="icon">{{ item.type === 'dir' ? '📁' : '📄' }}</span>
          <span class="name">{{ item.name }}</span>

          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, item)" @click.stop>
            <el-button link size="small">⋯</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="createDir">新建子目录</el-dropdown-item>
                <el-dropdown-item command="createDoc">新建文档</el-dropdown-item>
                <el-dropdown-item command="rename" divided>重命名</el-dropdown-item>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 子内容 -->
        <div v-if="item.type === 'dir' && expandedIds.has(item.id) && item.children && item.children.length" class="tree-children">
          <div v-for="child in item.children" :key="child.id" class="tree-child">
            <div
              class="tree-item-content child-item"
              :style="{ paddingLeft: '32px' }"
              :class="{ active: currentId === child.id }"
              @click="handleItemClick(child)"
              @contextmenu.prevent="showContextMenu($event, child)"
            >
              <span
                v-if="child.type === 'dir' && child.children && child.children.length"
                class="expand-icon"
                @click.stop="toggleExpand(child.id)"
              >
                {{ expandedIds.has(child.id) ? '▼' : '▶' }}
              </span>
              <span v-else class="expand-placeholder"></span>

              <span class="icon">{{ child.type === 'dir' ? '📁' : '📄' }}</span>
              <span class="name">{{ child.name }}</span>

              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, child)" @click.stop>
                <el-button link size="small">⋯</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="createDir">新建子目录</el-dropdown-item>
                    <el-dropdown-item command="createDoc">新建文档</el-dropdown-item>
                    <el-dropdown-item command="rename" divided>重命名</el-dropdown-item>
                    <el-dropdown-item command="delete">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <!-- 三级子内容 -->
            <div v-if="child.type === 'dir' && expandedIds.has(child.id) && child.children && child.children.length" class="tree-children">
              <div v-for="grandChild in child.children" :key="grandChild.id" class="tree-child">
                <div
                  class="tree-item-content child-item"
                  :style="{ paddingLeft: '52px' }"
                  :class="{ active: currentId === grandChild.id }"
                  @click="handleItemClick(grandChild)"
                  @contextmenu.prevent="showContextMenu($event, grandChild)"
                >
                  <span class="icon">{{ grandChild.type === 'dir' ? '📁' : '📄' }}</span>
                  <span class="name">{{ grandChild.name }}</span>

                  <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, grandChild)" @click.stop>
                    <el-button link size="small">⋯</el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="createDir">新建子目录</el-dropdown-item>
                        <el-dropdown-item command="createDoc">新建文档</el-dropdown-item>
                        <el-dropdown-item command="rename" divided>重命名</el-dropdown-item>
                        <el-dropdown-item command="delete">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="filteredList.length === 0" class="empty-state">
        <p>暂无内容，点击上方按钮创建</p>
      </div>
    </div>

    <!-- 新建对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-input v-model="dialogName" :placeholder="dialogPlaceholder" @keyup.enter="confirmCreate" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重命名对话框 -->
    <el-dialog v-model="renameVisible" title="重命名" width="400px">
      <el-input v-model="renameName" placeholder="请输入新名称" @keyup.enter="confirmRename" />
      <template #footer>
        <el-button @click="renameVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRename">确定</el-button>
      </template>
    </el-dialog>

    <!-- 右键菜单 -->
    <div v-if="menuVisible" class="context-menu" :style="{ left: menuX + 'px', top: menuY + 'px' }">
      <div class="menu-item" @click="menuCreateDir">新建子目录</div>
      <div class="menu-item" @click="menuCreateDoc">新建文档</div>
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

const emit = defineEmits(['select-doc'])

const treeList = ref([])
const keyword = ref('')
const currentId = ref(null)
const expandedIds = ref(new Set())

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

const filteredList = computed(() => {
  if (!keyword.value) return treeList.value
  return filterTree(treeList.value, keyword.value.toLowerCase())
})

function filterTree(list, kw) {
  return list.filter(item => {
    const match = item.name.toLowerCase().includes(kw)
    const childMatch = item.children ? filterTree(item.children, kw).length > 0 : false
    return match || childMatch
  }).map(item => {
    if (item.children) {
      return { ...item, children: filterTree(item.children, kw) }
    }
    return item
  })
}

async function refreshTree() {
  await loadData()
}

async function loadData() {
  try {
    const res = await getDirTree()
    treeList.value = res || []
    treeList.value.forEach(item => {
      if (item.type === 'dir') {
        expandedIds.value.add(item.id)
      }
    })
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载失败')
  }
}

function toggleExpand(id) {
  if (expandedIds.value.has(id)) {
    expandedIds.value.delete(id)
  } else {
    expandedIds.value.add(id)
  }
  expandedIds.value = new Set(expandedIds.value)
}

function openCreateDir(parent) {
  dialogType.value = 'dir'
  dialogParent.value = parent
  dialogTitle.value = '新建目录'
  dialogName.value = ''
  dialogPlaceholder.value = '请输入目录名称'
  dialogVisible.value = true
}

function openCreateDoc(parent) {
  dialogType.value = 'doc'
  dialogParent.value = parent
  dialogTitle.value = '新建文档'
  dialogName.value = ''
  dialogPlaceholder.value = '请输入文档标题'
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
      await loadData()
    } else {
      const res = await createDoc({
        title: dialogName.value,
        content: '# ' + dialogName.value + '\n\n开始编写...'
      })
      ElMessage.success('文档创建成功')
      await loadData()
      if (res.id) {
        emit('select-doc', res.id)
      }
    }
    dialogVisible.value = false
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败')
  }
}

function handleItemClick(item) {
  currentId.value = item.id
  if (item.type === 'doc') {
    emit('select-doc', item.docId || item.id)
  }
}

function handleCommand(cmd, item) {
  if (cmd === 'createDir') {
    openCreateDir(item)
  } else if (cmd === 'createDoc') {
    openCreateDoc(item)
  } else if (cmd === 'rename') {
    renameItem.value = item
    renameName.value = item.name
    renameVisible.value = true
  } else if (cmd === 'delete') {
    deleteItem(item)
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
    await loadData()
  } catch (error) {
    ElMessage.error('重命名失败')
  }
}

async function deleteItem(item) {
  try {
    await ElMessageBox.confirm(
      `确定删除 "${item.name}" 吗？`,
      '警告',
      { type: 'warning' }
    )
    await deleteDir(item.id)
    ElMessage.success('删除成功')
    if (currentId.value === item.id) {
      currentId.value = null
      emit('select-doc', null)
    }
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function showContextMenu(event, item) {
  event.preventDefault()
  menuItem.value = item
  menuX.value = event.clientX
  menuY.value = event.clientY
  menuVisible.value = true
}

function menuCreateDir() { handleCommand('createDir', menuItem.value); menuVisible.value = false }
function menuCreateDoc() { handleCommand('createDoc', menuItem.value); menuVisible.value = false }
function menuRename() { handleCommand('rename', menuItem.value); menuVisible.value = false }
function menuDelete() { handleCommand('delete', menuItem.value); menuVisible.value = false }

function closeMenu() { menuVisible.value = false }

// 暴露方法给父组件
defineExpose({
  refreshTree,
  loadData
})

onMounted(() => {
  loadData()
  document.addEventListener('click', closeMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', closeMenu)
})
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

.tree-item {
  margin-bottom: 2px;
}

.tree-item-content {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  min-height: 36px;
}

.tree-item-content:hover {
  background: #f5f7fa;
}

.tree-item-content.active {
  background: #ecf5ff;
  color: #409eff;
}

.child-item {
  padding: 6px 12px;
}

.expand-icon {
  width: 16px;
  font-size: 10px;
  color: #909399;
  cursor: pointer;
  flex-shrink: 0;
}

.expand-placeholder {
  width: 16px;
  flex-shrink: 0;
}

.tree-item-content .icon {
  font-size: 16px;
  flex-shrink: 0;
}

.tree-item-content .name {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-children {
  margin-left: 0;
}

.tree-child {
  margin-top: 2px;
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
