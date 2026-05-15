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
    historyApiFallback: true,
    proxy: {
      '/api/dir': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/api/doc': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
      // 删除 /api 通用规则，或者也指向 8080
    }
  }
})
