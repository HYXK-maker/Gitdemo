import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin } from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const currentUser = ref(null)

  async function loginAction(form) {
    try {
      console.log('loginAction 开始:', form)
      const data = await apiLogin(form)
      console.log('loginAction 返回数据:', data)

      if (data && data.token) {
        token.value = data.token
        localStorage.setItem('token', data.token)
        currentUser.value = data.user
        console.log('登录成功, token已保存')
        return true
      } else {
        throw new Error('登录失败：无token返回')
      }
    } catch (error) {
      console.error('loginAction 错误:', error)
      ElMessage.error(error.message || '登录失败')
      throw error
    }
  }

  function logout() {
    token.value = ''
    localStorage.removeItem('token')
    currentUser.value = null
  }

  return { token, currentUser, loginAction, logout }
})
