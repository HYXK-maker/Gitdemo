import request from './request'

export function createDoc(data) {
  return request.post('/doc/create', {
    title: data.title,
    content: data.content || '',
    folderId: data.folderId || 0
  })
}

export function getDocDetail(id) {
  return request.get(`/doc/${id}`)
}

export function updateDoc(id, data) {
  return request.put(`/doc/${id}`, {
    title: data.title,
    content: data.content,
    versionNote: data.versionNote
  })
}

export function deleteDoc(id) {
  return request.delete(`/doc/${id}`)
}

export function getDocumentVersions(docId) {
  return request.get(`/doc/${docId}/versions`)
}

export function rollbackVersion(docId, versionNum) {
  return request.post(`/doc/${docId}/rollback`, { versionNum })
}