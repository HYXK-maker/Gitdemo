import request from './request'

export function getDirTree() {
  return request.get('/dir/tree')
}

export function createDir(data) {
  return request.post('/dir/create', data)
}

export function renameDir(id, name) {
  return request.post('/dir/rename', { id, name })
}

export function deleteDir(id) {
  return request.post('/dir/delete', { id })
}

// 以下两个函数保留空实现，因为后端未提供移动功能，可以根据需要扩展
export function addDocumentToTree(data) {
  // 该操作在后端创建文档时已自动处理（目录树是实时查询的）
  return Promise.resolve()
}

export function moveDir(id, targetParentId) {
  // 暂未实现
  return Promise.resolve({ success: true })
}