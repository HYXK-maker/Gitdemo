<template>
  <div class="dir-tree">
    <div class="tree-header">
      <el-button type="primary" size="small" @click="openCreateDialog(null, 'dir')">
        + 新建目录
      </el-button>
      <el-button type="success" size="small" @click="openCreateDialog(null, 'doc')">
        + 新建文档
      </el-button>
    </div>

    <div class="tree-content">
      <div v-for="item in treeData" :key="item.id" class="tree-node">
        <div
          class="node-item"
          :class="{ active: selectedId === item.id }"
          @click="handleNodeClick(item)"
        >
          <span class="node-icon">{{ item.type === 'dir' ? '📁' : '📄' }}</span>
          <span class="node-name">{{ item.name }}</span>
          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, item)">
            <span class="node-more">⋯</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="rename">重命名</el-dropdown-item>
                <el-dropdown-item v-if="item.type === 'dir'" command="createDir">新建目录</el-dropdown-item>
                <el-dropdown-item v-if="item.type === 'dir'" command="createDoc">新建文档</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div v-if="item.children && item.children.length" class="node-children">
          <DirTreeNode
            v-for="child in item.children"
            :key="child.id"
            :node="child"
            :selected-id="selectedId"
            @select="handleNodeClick"
            @command="handleCommand"
          />
        </div>
      </div>
      <div v-if="treeData.length === 0" class="empty-tip">
        暂无内容，点击上方按钮创建
      </div>
    </div>


    <el-dialog v-model="dialog.visible" :title="dialog.title" width="350px">
      <el-input v-model="dialog.name" :placeholder="dialog.placeholder" />
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>


    <el-dialog v-model="renameDialog.visible" title="重命名" width="350px">
      <el-input v-model="renameDialog.name" placeholder="新名称" />
      <template #footer>
        <el-button @click="renameDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitRename">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDirTree, createDir, renameDir, deleteDir } from '@/api/dir'
import { createDoc } from '@/api/doc'
import { ElMessage, ElMessageBox } from 'element-plus'

const emit = defineEmits(['select-doc'])
const treeData = ref([])
const selectedId = ref(null)

const dialog = ref({ visible: false, title: '', name: '', type: '', parent: null, placeholder: '' })
const renameDialog = ref({ visible: false, name: '', node: null })

async function loadTree() {
  try {
    treeData.value = await getDirTree()
  } catch (error) {
    ElMessage.error('加载目录失败')
  }
}

function openCreateDialog(parent, type) {
  dialog.value = {
    visible: true,
    title: type === 'dir' ? '新建目录' : '新建文档',
    name: '',
    type: type,
    parent: parent,
    placeholder: type === 'dir' ? '请输入目录名称' : '请输入文档标题'
  }
}

async function submitCreate() {
  if (!dialog.value.name.trim()) {
    ElMessage.warning('请输入名称')
    return
  }

  try {
    if (dialog.value.type === 'dir') {
      await createDir({
        name: dialog.value.name,
        parentId: dialog.value.parent ? dialog.value.parent.id : 0
      })
      ElMessage.success('目录创建成功')
    } else {
      const res = await createDoc({
        title: dialog.value.name,
        content: '# 新文档\n\n开始编写内容...'
      })
      ElMessage.success('文档创建成功')
      if (res.id) {
        emit('select-doc', res.id)
      }
    }
    dialog.value.visible = false
    await loadTree()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

function handleNodeClick(node) {
  selectedId.value = node.id
  if (node.type === 'doc') {
    emit('select-doc', node.docId || node.id)
  }
}

function handleCommand(cmd, node) {
  if (cmd === 'rename') {
    renameDialog.value = { visible: true, name: node.name, node: node }
  } else if (cmd === 'createDir') {
    openCreateDialog(node, 'dir')
  } else if (cmd === 'createDoc') {
    openCreateDialog(node, 'doc')
  } else if (cmd === 'delete') {
    ElMessageBox.confirm(`确定删除 "${node.name}" 吗？`, '警告', { type: 'warning' })
      .then(async () => {
        await deleteDir(node.id)
        ElMessage.success('删除成功')
        await loadTree()
        if (selectedId.value === node.id) {
          emit('select-doc', null)
        }
      })
      .catch(() => {})
  }
}

async function submitRename() {
  if (!renameDialog.value.name.trim()) return
  try {
    await renameDir(renameDialog.value.node.id, renameDialog.value.name)
    ElMessage.success('重命名成功')
    renameDialog.value.visible = false
    await loadTree()
  } catch (error) {
    ElMessage.error('重命名失败')
  }
}

onMounted(() => {
  loadTree()
})
</script>

<style scoped>
.dir-tree {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.tree-header {
  padding: 12px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.tree-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.tree-node {
  margin-left: 0;
}
.node-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.node-item:hover {
  background: #ecf5ff;
}
.node-item.active {
  background: #409eff20;
  color: #409eff;
}
.node-icon {
  font-size: 16px;
}
.node-name {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.node-more {
  opacity: 0;
  font-size: 18px;
  cursor: pointer;
  padding: 0 4px;
}
.node-item:hover .node-more {
  opacity: 1;
}
.node-children {
  margin-left: 20px;
}
.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px;
  font-size: 13px;
}
</style>
