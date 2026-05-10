
let fileTree = [
  {
    id: 1,
    name: '我的文档',
    type: 'dir',
    parentId: 0,
    children: [
      { id: 101, name: '示例文档', type: 'doc', parentId: 1, docId: 1 }
    ]
  },
  {
    id: 2,
    name: '工作文档',
    type: 'dir',
    parentId: 0,
    children: []
  },
  {
    id: 3,
    name: '个人笔记',
    type: 'dir',
    parentId: 0,
    children: []
  }
]

let nextId = 100


export async function getDirTree() {
  await new Promise(resolve => setTimeout(resolve, 100))
  return JSON.parse(JSON.stringify(fileTree))
}

function checkDuplicate(parentId, name, type) {
  // 找到父节点
  let parentNode = null
  if (parentId === 0) {
    parentNode = { children: fileTree }
  } else {
    const findParent = (items) => {
      for (let item of items) {
        if (item.id === parentId) {
          parentNode = item
          return true
        }
        if (item.children && findParent(item.children)) return true
      }
      return false
    }
    findParent(fileTree)
  }
  if (!parentNode) return false
  const siblings = parentNode.children || []
  return siblings.some(sibling => sibling.name === name && sibling.type === type)
}


export async function createDir(data) {
  await new Promise(resolve => setTimeout(resolve, 200))

  if (checkDuplicate(data.parentId, data.name, 'dir')) {
    throw new Error('同级目录下已存在同名文件夹')
  }

  const newId = nextId++
  const newDir = {
    id: newId,
    name: data.name,
    type: 'dir',
    parentId: data.parentId || 0,
    children: []
  }

  if (data.parentId === 0) {
    fileTree.push(newDir)
  } else {
    const addToParent = (items) => {
      for (let item of items) {
        if (item.id === data.parentId && item.type === 'dir') {
          if (!item.children) item.children = []
          item.children.push(newDir)
          return true
        }
        if (item.children && addToParent(item.children)) return true
      }
      return false
    }
    addToParent(fileTree)
  }

  console.log('目录创建成功:', newDir)
  return { id: newId }
}

export async function addDocumentToTree(data) {
  await new Promise(resolve => setTimeout(resolve, 200))

  if (checkDuplicate(data.parentId, data.title, 'doc')) {
    throw new Error('同级目录下已存在同名文档')
  }

  const newId = nextId++
  const newDoc = {
    id: newId,
    name: data.title,
    type: 'doc',
    parentId: data.parentId || 0,
    docId: data.docId || newId
  }

  if (data.parentId === 0) {
    fileTree.push(newDoc)
  } else {
    const addToParent = (items) => {
      for (let item of items) {
        if (item.id === data.parentId && item.type === 'dir') {
          if (!item.children) item.children = []
          item.children.push(newDoc)
          return true
        }
        if (item.children && addToParent(item.children)) return true
      }
      return false
    }
    addToParent(fileTree)
  }

  console.log('文档已添加到目录树:', newDoc)
  return { id: newDoc.docId }
}

export async function renameDir(id, name) {
  await new Promise(resolve => setTimeout(resolve, 200))


  let targetNode = null
  let parentId = null
  const findNode = (items, parent = null) => {
    for (let item of items) {
      if (item.id === id) {
        targetNode = item
        parentId = parent ? parent.id : 0
        return true
      }
      if (item.children && findNode(item.children, item)) return true
    }
    return false
  }
  findNode(fileTree)

  if (!targetNode) throw new Error('节点不存在')

  const duplicate = checkDuplicate(parentId, name, targetNode.type)
  if (duplicate) {
    throw new Error(`同级已存在同名${targetNode.type === 'dir' ? '文件夹' : '文档'}`)
  }

  targetNode.name = name
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
  deleteItem(fileTree)

  return { success: true }
}


export async function moveDir(id, targetParentId) {
  await new Promise(resolve => setTimeout(resolve, 200))

  return { success: true }
}
