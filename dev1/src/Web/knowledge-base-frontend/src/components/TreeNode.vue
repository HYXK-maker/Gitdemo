<template>
  <div class="tree-node">

    <div
      class="tree-node-content"
      :class="{ active: selectedId === node.id }"
      @click="handleClick"
      @contextmenu.prevent="handleContextMenu"
    >

      <span
        v-if="node.type === 'dir' "
        class="expand-icon"
        @click.stop="toggleExpand"
      >
        {{ expanded ? '▼' : '▶' }}
      </span>
      <span v-else class="expand-placeholder"></span>

      <span class="node-icon">{{ node.type === 'dir' ? '📁' : '📄' }}</span>
      <span class="node-name">{{ node.name }}</span>


      <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd)" @click.stop>
        <el-button link size="small">⋯</el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <template v-if="node.type === 'dir'">
              <el-dropdown-item command="createDir">新建子目录</el-dropdown-item>
              <el-dropdown-item command="createDoc">新建文档</el-dropdown-item>
              <el-dropdown-item command="rename" divided>重命名</el-dropdown-item>
              <el-dropdown-item command="delete">删除</el-dropdown-item>
            </template>
            <template v-else>
              <el-dropdown-item command="rename">重命名</el-dropdown-item>
              <el-dropdown-item command="delete">删除</el-dropdown-item>
            </template>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div v-if="node.type === 'dir' && expanded && node.children && node.children.length" class="tree-children">
      <TreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :selected-id="selectedId"
        :expanded-map="expandedMap"
        @toggle="handleToggle"
        @select="handleSelect"
        @command="handleChildCommand"
        @contextmenu="handleContextMenu"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  node: Object,
  selectedId: [Number, String],
  expandedMap: Object
})

const emit = defineEmits(['toggle', 'select', 'command', 'contextmenu'])


const expanded = computed(() => props.expandedMap[props.node.id] || false)


function toggleExpand() {
  emit('toggle', props.node.id)
}


function handleClick() {
  emit('select', props.node)
}


function handleContextMenu(event) {
  emit('contextmenu', event, props.node)
}


function handleCommand(cmd) {
  emit('command', cmd, props.node)
}

function handleToggle(id) {
  emit('toggle', id)
}

// 子节点触发的 select（向上传递）
function handleSelect(node) {
  emit('select', node)
}

function handleChildCommand(cmd, node) {
  emit('command', cmd, node)
}
</script>

<style scoped>
.tree-node {
  margin-bottom: 2px;
}
.tree-node-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.tree-node-content:hover {
  background: #f5f7fa;
}
.tree-node-content.active {
  background: #ecf5ff;
  color: #409eff;
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
.node-icon {
  font-size: 16px;
  flex-shrink: 0;
}
.node-name {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tree-children {
  margin-left: 24px;
}
</style>
