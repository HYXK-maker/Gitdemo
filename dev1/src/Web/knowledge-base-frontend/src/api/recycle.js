import request from './request'

export function getRecycleList() {
  return request.get('/recycle/list')
}

export function restoreFromRecycle(id) {
  return request.post('/recycle/restore', { id })
}

export function permanentDeleteFromRecycle(id) {
  return request.post('/recycle/permanent-delete', { id })
}
