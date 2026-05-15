import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin } from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const currentUser = ref(JSON.parse(localStorage.getItem('currentUser') || 'null'))

  async function loginAction(form) {
    try {
      const data = await apiLogin(form)

      if (data && data.token) {
        token.value = data.token
        localStorage.setItem('token', data.token)

        if (data.userId) {
          userId.value = data.userId
          localStorage.setItem('userId', data.userId.toString())
        }

        const name = data.username || form.username
        username.value = name
        localStorage.setItem('username', name)

        currentUser.value = { username: name }
        localStorage.setItem('currentUser', JSON.stringify(currentUser.value))

        return true
      } else {
        throw new Error('登录失败：无token返回')
      }
    } catch (error) {
      ElMessage.error(error.message || '登录失败')
      throw error
    }
  }

  function logout() {
    token.value = ''
    userId.value = ''
    username.value = ''
    currentUser.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('currentUser')
  }

  return { token, userId, username, currentUser, loginAction, logout }
})
