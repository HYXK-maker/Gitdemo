import request from './request'

export function createDoc(data) {
  return request.post('/doc/create', {
    title: data.title,
    content: data.content || '',
    folderId: data.folderId || 0
  })
}

export function getDocDetail(id) {
  return request.get('/doc/' + id)
}

export function saveDoc(data) {
  return request.post('/doc/save', {
    id: data.id,
    content: data.content
  })
}

export function updateDoc(id, data) {
  return request.put(`/doc/${id}`, data)
}

export function renameDoc(id, title) {
  return request.post('/doc/rename', { id, title })
}

export function deleteDoc(id) {
  return request.post('/doc/delete', { id })
}

export function getDocVersions(docId) {
  return request.get(`/doc/${docId}/versions`)
}

export function createDocVersion(docId, data) {
  return request.post(`/doc/${docId}/version`, data)
}

export function deleteVersion(versionId) {
  return request.delete(`/doc/versions/${versionId}`)
}

export function rollbackVersion(docId, versionNum) {
  return request.post(`/doc/${docId}/rollback`, { versionNum })
}
