import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000,
    historyApiFallback: true,   // 解决 SPA 路由 404
    proxy: {
      '/api': {
        target: 'http://localhost:8081',  // 改为你的后端实际端口
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  }
})
