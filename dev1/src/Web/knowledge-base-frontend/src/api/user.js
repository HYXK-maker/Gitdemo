import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getCurrentUser() {
  return Promise.resolve({ id: 1, username: 'admin', nickname: '管理员' })
}
