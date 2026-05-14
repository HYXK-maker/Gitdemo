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
</style>