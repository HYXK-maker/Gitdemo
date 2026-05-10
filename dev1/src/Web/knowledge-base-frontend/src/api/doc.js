import { addDocumentToTree } from './dir'


let docContents = {
  1: { id: 1, title: '示例文档', content: '# 示例文档\n\n这是示例内容。', version: 1, updatedAt: new Date().toISOString() }
}

let docVersions = {
  1: [{ version: 1, content: '# 示例文档\n\n这是示例内容。', createdAt: new Date().toISOString(), note: '初始版本' }]
}

let nextDocId = 2


export async function createDoc(data) {
  await new Promise(resolve => setTimeout(resolve, 300))

  const newId = nextDocId++
  const now = new Date().toISOString()


  docContents[newId] = {
    id: newId,
    title: data.title,
    content: data.content || '# ' + data.title + '\n\n开始编写...',
    version: 1,
    updatedAt: now
  }


  docVersions[newId] = [{
    version: 1,
    content: docContents[newId].content,
    createdAt: now,
    note: '初始版本'
  }]


  await addDocumentToTree({
    title: data.title,
    parentId: data.folderId || 0,
    docId: newId
  })

  console.log('文档创建成功，ID:', newId)
  return { id: newId }
}


export async function getDocDetail(id) {
  await new Promise(resolve => setTimeout(resolve, 200))

  const doc = docContents[id]
  if (!doc) {
    throw new Error('文档不存在')
  }
  return doc
}


export async function updateDoc(id, data) {
  await new Promise(resolve => setTimeout(resolve, 500))

  const doc = docContents[id]
  if (!doc) {
    throw new Error('文档不存在')
  }

  doc.title = data.title || doc.title
  doc.content = data.content || doc.content
  doc.version = (doc.version || 0) + 1
  doc.updatedAt = new Date().toISOString()

  if (data.content) {
    if (!docVersions[id]) docVersions[id] = []
    docVersions[id].unshift({
      version: doc.version,
      content: data.content,
      createdAt: new Date().toISOString(),
      note: data.versionNote || '更新文档'
    })
    if (docVersions[id].length > 10) docVersions[id].pop()
  }

  return { success: true, version: doc.version }
}


export async function deleteDoc(id) {
  await new Promise(resolve => setTimeout(resolve, 300))

  delete docContents[id]
  delete docVersions[id]
  return {success: true}
}
export async function getDocumentVersions(docId) {
  await new Promise(resolve => setTimeout(resolve, 200))
  return docVersions[docId] || []
}

export async function rollbackVersion(docId, versionNum) {
  await new Promise(resolve => setTimeout(resolve, 500))

  const versions = docVersions[docId]
  const targetVersion = versions?.find(v => v.version === versionNum)
  if (!targetVersion) {
    throw new Error('版本不存在')
  }

  const doc = docContents[docId]
  if (doc) {
    doc.content = targetVersion.content
    doc.version = (doc.version || 0) + 1
    doc.updatedAt = new Date().toISOString()

    versions.unshift({
      version: doc.version,
      content: targetVersion.content,
      createdAt: new Date().toISOString(),
      note: `回滚到 v${versionNum}`
    })
  }

  return { success: true }
}
