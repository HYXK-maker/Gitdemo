import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 把 /api 开头的请求转发到后端 http://localhost:8080
      '/api': {
        target: 'http://localhost:8080', // 后端基础地址
        changeOrigin: true, // 必须开启，解决跨域问题
        rewrite: (path) => path.replace(/^\/api/, '')
        // 效果：前端请求 /api/user/register → 后端收到 /user/register
      }
    }
  }
})
