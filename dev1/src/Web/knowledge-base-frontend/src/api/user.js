import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getCurrentUser() {
  // 简单实现：从 token 解析，或调用一个获取用户信息的接口
  return Promise.resolve({ id: 1, username: 'admin', nickname: '管理员' })
}