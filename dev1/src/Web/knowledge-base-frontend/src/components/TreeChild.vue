<template>
  <div class="tree-child-wrapper">
    <div
      class="tree-item-content"
      :style="{ paddingLeft: (level * 20 + 12) + 'px' }"
      :class="{ active: currentId === node.id }"
      @click="handleClick"
      @contextmenu.prevent="$emit('contextmenu', $event, node)"
    >
      <span
        v-if="node.type === 'dir' && node.children && node.children.length"
        class="expand-icon"
        @click.stop="$emit('toggle', node.id)"
      >
        {{ expandedIds.has(node.id) ? '▼' : '▶' }}
      </span>
      <span v-else class="expand-placeholder"></span>

      <span class="icon">{{ node.type === 'dir' ? '📁' : '📄' }}</span>
      <span class="name">{{ node.name }}</span>

      <el-dropdown trigger="click" @command="(cmd) => $emit('command', cmd, node)" @click.stop>
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

    <div v-if="node.type === 'dir' && expandedIds.has(node.id) && node.children && node.children.length" class="tree-children">
      <TreeChild
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :level="level + 1"
        :current-id="currentId"
        :expanded-ids="expandedIds"
        @select="$emit('select', $event)"
        @toggle="$emit('toggle', $event)"
        @command="$emit('command', $event[0], $event[1])"
        @contextmenu="$emit('contextmenu', $event[0], $event[1])"
      />
    </div>
  </div>
</template>

<script setup>
defineProps({
  node: Object,
  level: Number,
  currentId: [Number, String],
  expandedIds: Set
})

defineEmits(['select', 'toggle', 'command', 'contextmenu'])

function handleClick() {
  emit('select', props.node)
}
</script>

<style scoped>
.tree-child-wrapper {
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
  /* 子节点样式 */
}
</style>
