<template>
  <div class="tree-node-wrapper">
    <div
      class="tree-item-content"
      :style="{ paddingLeft: (level * 20 + 12) + 'px' }"
      :class="{ active: currentId === node.id }"
      @click="handleClick"
      @contextmenu.prevent="handleContextMenu"
    >
      <!-- 展开/折叠按钮 -->
      <span
        v-if="hasChildren"
        class="expand-icon"
        @click.stop="toggleExpand"
      >
        {{ expanded ? '▼' : '▶' }}
      </span>
      <span v-else class="expand-placeholder"></span>

      <span class="icon">{{ node.type === 'dir' ? '📁' : '📄' }}</span>
      <span class="name">{{ node.name }}</span>

      <el-dropdown
        trigger="click"
        @command="(cmd) => handleCommand(cmd)"
        class="item-menu"
        @click.stop
      >
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

    <!-- 子节点（递归） -->
    <div v-if="hasChildren && expanded" class="tree-children">
      <TreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :level="level + 1"
        :current-id="currentId"
        @select="(val) => $emit('select', val)"
        @contextmenu="(event, val) => $emit('contextmenu', event, val)"
        @command="(cmd, val) => $emit('command', cmd, val)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  node: {
    type: Object,
    required: true
  },
  level: {
    type: Number,
    default: 0
  },
  currentId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['select', 'contextmenu', 'command'])

const expanded = ref(true)

const hasChildren = computed(() => {
  return props.node.type === 'dir' && props.node.children && props.node.children.length > 0
})

function toggleExpand() {
  expanded.value = !expanded.value
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
</script>

<style scoped>
.tree-node-wrapper {
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
  position: relative;
  min-height: 36px;
}

.tree-item-content:hover {
  background: #f5f7fa;
}

.tree-item-content.active {
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

.tree-item-content .item-menu {
  opacity: 0;
  transition: opacity 0.2s;
  flex-shrink: 0;
}

.tree-item-content:hover .item-menu {
  opacity: 1;
}

.tree-children {
  /* 子节点样式 */
}
</style>
