import request from './request'


let directories = [
  { id: 1, name: '我的文档', type: 'dir', parentId: 0, level: 0 },
  { id: 2, name: '工作文档', type: 'dir', parentId: 0, level: 0 },
  { id: 3, name: '项目资料', type: 'dir', parentId: 2, level: 1 },
  { id: 4, name: '个人笔记', type: 'dir', parentId: 1, level: 1 }
]

let documents = [
  { id: 101, name: 'README.md', type: 'doc', parentId: 1, docId: 1 },
  { id: 102, name: '开发规范.md', type: 'doc', parentId: 2, docId: 2 },
  { id: 103, name: 'API文档.md', type: 'doc', parentId: 3, docId: 3 },
  { id: 104, name: '学习笔记.md', type: 'doc', parentId: 4, docId: 4 }
]

let nextId = 5
let nextDocId = 5


function buildTree(parentId = 0) {
  // 获取子目录
  const dirs = directories
    .filter(d => d.parentId === parentId)
    .map(d => ({
      ...d,
      children: buildTree(d.id)
    }))


  const docs = documents
    .filter(d => d.parentId === parentId)
    .map(d => ({
      ...d,
      children: []
    }))

  return [...dirs, ...docs]
}

export async function getDirTree() {
  await new Promise(resolve => setTimeout(resolve, 300))
  const tree = buildTree(0)
  return tree
}

export async function createDir(data) {
  await new Promise(resolve => setTimeout(resolve, 300))

  const newDir = {
    id: nextId++,
    name: data.name,
    type: 'dir',
    parentId: data.parentId || 0,
    level: 0
  }
  directories.push(newDir)
  return { id: newDir.id }
}

export async function renameDir(id, name) {
  await new Promise(resolve => setTimeout(resolve, 200))

  const dir = directories.find(d => d.id === id)
  if (dir) {
    dir.name = name
  }
  const doc = documents.find(d => d.id === id)
  if (doc) {
    doc.name = name
  }
  return { success: true }
}

export async function deleteDir(id) {
  await new Promise(resolve => setTimeout(resolve, 300))


  const idsToDelete = [id]
  const findChildren = (parentId) => {
    directories.filter(d => d.parentId === parentId).forEach(child => {
      idsToDelete.push(child.id)
      findChildren(child.id)
    })
    documents.filter(d => d.parentId === parentId).forEach(doc => {
      idsToDelete.push(doc.id)
    })
  }
  findChildren(id)

  directories = directories.filter(d => !idsToDelete.includes(d.id))
  documents = documents.filter(d => !idsToDelete.includes(d.id))

  return { success: true }
}
