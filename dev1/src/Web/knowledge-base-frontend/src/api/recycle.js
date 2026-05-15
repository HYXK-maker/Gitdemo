import request from './request'

// 获取回收站列表
export function getRecycleList() {
  return request.get('/recycle/list')
}

// 恢复
export function restoreFromRecycle(id) {
  return request.post('/recycle/restore', { id })
}

// 永久删除
export function permanentDeleteFromRecycle(id) {
  return request.post('/recycle/permanent-delete', { id })
}
