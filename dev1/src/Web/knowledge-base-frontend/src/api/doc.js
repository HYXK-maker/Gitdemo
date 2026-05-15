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

export function renameDoc(id, title) {
  return request.post('/doc/rename', {id, title})
}

export function deleteDoc(id) {
  return request.post('/doc/delete', {id})
}

export function getDocVersions(docId) {
  return request.get(`/doc/${docId}/versions`)
}

export function getVersionContent(versionId) {
  return request.get(`/doc/versions/${versionId}`)
}

export function createDocVersion(docId, data) {
  return request.post(`/doc/${docId}/version`, {
    content: data.content,
    versionNote: data.versionNote || ''
  })
}

export function updateDoc(id, data) {
  return request.post('/doc/update', {id, ...data})
}
