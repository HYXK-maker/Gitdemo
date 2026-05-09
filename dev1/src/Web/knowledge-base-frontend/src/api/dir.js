import request from './request'

// Mock 数据 - 支持多级目录
let mockData = [
  {
    id: 1,
    name: '我的文档',
    type: 'dir',
    parentId: 0,
    children: [
      {
        id: 101,
        name: '技术笔记',
        type: 'dir',
        parentId: 1,
        children: [
          { id: 1001, name: 'Vue学习笔记', type: 'doc', parentId: 101, docId: 1 },
          { id: 1002, name: 'React学习笔记', type: 'doc', parentId: 101, docId: 2 }
        ]
      },
      {
        id: 102,
        name: '工作文档',
        type: 'dir',
        parentId: 1,
        children: [
          { id: 1003, name: '周报', type: 'doc', parentId: 102, docId: 3 }
        ]
      },
      { id: 103, name: '欢迎文档', type: 'doc', parentId: 1, docId: 4 }
    ]
  },
  {
    id: 2,
    name: '工作文档',
    type: 'dir',
    parentId: 0,
    children: [
      { id: 201, name: '项目计划', type: 'doc', parentId: 2, docId: 5 },
      { id: 202, name: '会议纪要', type: 'doc', parentId: 2, docId: 6 }
    ]
  },
  { id: 3, name: '个人笔记', type: 'dir', parentId: 0, children: [] }
]

export async function getDirTree() {
  await new Promise(resolve => setTimeout(resolve, 200))
  return JSON.parse(JSON.stringify(mockData))
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

  let movingItem = null

  // 找到要移动的项
  const findItem = (items) => {
    for (let i = 0; i < items.length; i++) {
      if (items[i].id === id) {
        movingItem = items.splice(i, 1)[0]
        return true
      }
      if (items[i].children && findItem(items[i].children)) return true
    }
    return false
  }
  findItem(mockData)

  if (movingItem) {
    if (targetParentId === 0) {
      mockData.push(movingItem)
    } else {
      const addToParent = (items) => {
        for (let item of items) {
          if (item.id === targetParentId) {
            if (!item.children) item.children = []
            item.children.push(movingItem)
            return true
          }
          if (item.children && addToParent(item.children)) return true
        }
        return false
      }
      addToParent(mockData)
    }
  }

  return { success: true }
}
