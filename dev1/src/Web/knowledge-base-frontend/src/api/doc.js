import request from './request'

let docContents = {
  1: { id: 1, title: 'README', content: '# 欢迎使用文档系统\n\n这是一个示例文档，您可以在这里编写内容。', version: 1, updatedAt: new Date().toISOString() },
  2: { id: 2, title: '开发规范', content: '# 开发规范\n\n## 代码规范\n- 使用 ESLint\n- 遵循 Vue 3 风格指南', version: 1, updatedAt: new Date().toISOString() },
  3: { id: 3, title: 'API文档', content: '# API 文档\n\n## 接口列表\n- 用户登录\n- 文档管理', version: 1, updatedAt: new Date().toISOString() },
  4: { id: 4, title: '学习笔记', content: '# 学习笔记\n\n记录日常学习内容。', version: 1, updatedAt: new Date().toISOString() }
}

let docVersions = {
  1: [{ version: 1, content: '# 欢迎使用文档系统\n\n这是一个示例文档，您可以在这里编写内容。', createdAt: new Date().toISOString(), note: '初始版本' }],
  2: [{ version: 1, content: '# 开发规范\n\n## 代码规范\n- 使用 ESLint\n- 遵循 Vue 3 风格指南', createdAt: new Date().toISOString(), note: '初始版本' }],
  3: [{ version: 1, content: '# API 文档\n\n## 接口列表\n- 用户登录\n- 文档管理', createdAt: new Date().toISOString(), note: '初始版本' }],
  4: [{ version: 1, content: '# 学习笔记\n\n记录日常学习内容。', createdAt: new Date().toISOString(), note: '初始版本' }]
}

export async function createDoc(data) {
  await new Promise(resolve => setTimeout(resolve, 300))

  const newId = Math.max(...Object.keys(docContents).map(Number), 0) + 1
  const newDoc = {
    id: newId,
    title: data.title,
    content: data.content || '',
    version: 1,
    updatedAt: new Date().toISOString()
  }
  docContents[newId] = newDoc
  docVersions[newId] = [{
    version: 1,
    content: newDoc.content,
    createdAt: new Date().toISOString(),
    note: '初始版本'
  }]

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

  const oldContent = doc.content
  doc.title = data.title || doc.title
  doc.content = data.content || doc.content
  doc.version = (doc.version || 0) + 1
  doc.updatedAt = new Date().toISOString()

  // 内容变化时创建新版本
  if (data.content && oldContent !== data.content) {
    if (!docVersions[id]) docVersions[id] = []
    docVersions[id].unshift({
      version: doc.version,
      content: data.content,
      createdAt: new Date().toISOString(),
      note: data.versionNote || '更新文档'
    })
    // 只保留最近10个版本
    if (docVersions[id].length > 10) docVersions[id].pop()
  }

  return { success: true, version: doc.version }
}

export async function deleteDoc(id) {
  await new Promise(resolve => setTimeout(resolve, 300))

  delete docContents[id]
  delete docVersions[id]
  return { success: true }
}

export async function getDocumentVersions(docId) {
  await new Promise(resolve => setTimeout(resolve, 200))

  return docVersions[docId] || []
}

export async function rollbackVersion(docId, versionNum) {
  await new Promise(resolve => setTimeout(resolve, 500))

  const versions = docVersions[docId]
  const targetVersion = versions.find(v => v.version === versionNum)
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
