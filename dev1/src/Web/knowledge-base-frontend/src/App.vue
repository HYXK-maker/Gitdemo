<template>
  <div :class="{ 'dark-mode': themeStore.isDark }" class="app-shell">
    <router-view />
  </div>
</template>

<script setup>
import { useThemeStore } from '@/stores/theme'
const themeStore = useThemeStore()
</script>

<style>
/* 全局暗色变量可通过 data-theme 实现，这里沿用 Element Plus 暗黑模式 */
:root {
  --bg: #ffffff;
  --text: #303133;
}
[data-theme='dark'] {
  --bg: #1a1a2e;
  --text: #e0e0e0;
}
html, body {
  background: var(--bg);
  color: var(--text);
}
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body, #app {
  height: 100%;
}
/* 深色模式 - 编辑器文字柔和不刺眼 */
.dark .doc-editor,
.dark .ql-toolbar.ql-snow,
.dark .ql-container.ql-snow {
  background: #1e1e1e !important;
  border-color: #333 !important;
}
.dark .ql-editor {
  color: #d4d4d4 !important;
}
.dark .ql-snow .ql-stroke {
  stroke: #c0c0c0 !important;
}
.dark .ql-snow .ql-fill {
  fill: #c0c0c0 !important;
}
.dark .ql-snow .ql-picker-label {
  color: #c0c0c0 !important;
}
.dark .toolbar {
  background: #252526 !important;
  border-color: #333 !important;
}
.dark .version-panel {
  background: #1e1e1e !important;
}
.dark .version-item {
  border-color: #333 !important;
}
.dark .panel-header {
  border-color: #333 !important;
}
</style>

