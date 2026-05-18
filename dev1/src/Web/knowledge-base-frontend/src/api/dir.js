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

export function addDocumentToTree(data) {

  return Promise.resolve()
}
export function moveDir(id, targetParentId) {

  return Promise.resolve({ success: true })
}
export function moveDirectory(data) {
  return request.post('/dir/move', data)
}
