import request from './request'

// Mock 用户数据
let mockUsers = [
  { id: 1, username: 'admin', password: '123456', nickname: '管理员', email: 'admin@test.com' },
  { id: 2, username: 'user', password: '123456', nickname: '普通用户', email: 'user@test.com' }
]

// 登录
export async function login(data) {
  console.log('登录请求:', data)

  // 模拟网络延迟
  await new Promise(resolve => setTimeout(resolve, 500))

  const user = mockUsers.find(u => u.username === data.username && u.password === data.password)

  if (user) {
    const result = {
      code: 200,
      token: 'mock-token-' + Date.now(),
      user: { id: user.id, username: user.username, nickname: user.nickname }
    }
    console.log('登录成功:', result)
    return result
  }

  console.log('登录失败: 用户名或密码错误')
  throw new Error('用户名或密码错误')
}

// 注册
export async function register(data) {
  await new Promise(resolve => setTimeout(resolve, 500))

  const existUser = mockUsers.find(u => u.username === data.username)
  if (existUser) {
    throw new Error('用户名已存在')
  }

  const newUser = {
    id: mockUsers.length + 1,
    username: data.username,
    password: data.password,
    nickname: data.username,
    email: ''
  }
  mockUsers.push(newUser)
  return { code: 200, message: '注册成功' }
}

// 获取当前用户
export function getCurrentUser() {
  const token = localStorage.getItem('token')
  if (token) {
    return { id: 1, username: 'admin', nickname: '管理员' }
  }
  throw new Error('未登录')
}
