import request from './request'

// Mock 数据
let mockData = [
  { id: 1, name: '我的文档', type: 'dir', parentId: 0, children: [
      { id: 101, name: '示例文档', type: 'doc', parentId: 1, docId: 1 }
    ] },
  { id: 2, name: '工作文档', type: 'dir', parentId: 0, children: [] }
]

export async function getDirTree() {
  await new Promise(resolve => setTimeout(resolve, 200))
  return mockData
}

export async function createDir(data) {
  await new Promise(resolve => setTimeout(resolve, 200))
  const newId = Date.now()
  const newDir = {
    id: newId,
    name: data.name,
    type: 'dir',
    parentId: data.parentId || 0,
    children: []
  }

  if (data.parentId === 0) {
    mockData.push(newDir)
  } else {
    const addToParent = (items) => {
      for (let item of items) {
        if (item.id === data.parentId) {
          if (!item.children) item.children = []
          item.children.push(newDir)
          return true
        }
        if (item.children && addToParent(item.children)) return true
      }
      return false
    }
    addToParent(mockData)
  }

  return { id: newId }
}

export async function renameDir(id, name) {
  await new Promise(resolve => setTimeout(resolve, 200))

  const renameItem = (items) => {
    for (let item of items) {
      if (item.id === id) {
        item.name = name
        return true
      }
      if (item.children && renameItem(item.children)) return true
    }
    return false
  }
  renameItem(mockData)

  return { success: true }
}

export async function deleteDir(id) {
  await new Promise(resolve => setTimeout(resolve, 200))

  const deleteItem = (items) => {
    for (let i = 0; i < items.length; i++) {
      if (items[i].id === id) {
        items.splice(i, 1)
        return true
      }
      if (items[i].children && deleteItem(items[i].children)) return true
    }
    return false
  }
  deleteItem(mockData)

  return { success: true }
}

export async function moveDir(id, targetParentId) {
  await new Promise(resolve => setTimeout(resolve, 200))
  return { success: true }
}
