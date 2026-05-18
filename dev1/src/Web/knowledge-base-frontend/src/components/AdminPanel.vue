<template>
  <div class="admin-panel">
    <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 16px;">
      <el-button @click="goBack" size="small" icon="ArrowLeft">返回</el-button>
      <h3 style="margin: 0;">用户管理</h3>
    </div>
    <el-table :data="users" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="role" label="角色" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'info'">
            {{ scope.row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          {{ scope.row.status === 1 ? '禁用' : '正常' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button size="small" @click="toggleRole(scope.row)">
            {{ scope.row.role === 'admin' ? '降为普通' : '提升为管理' }}
          </el-button>
          <el-button size="small" @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteUser(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const users = ref([])

function goBack() {
  router.push('/main')
}

async function fetchUsers() {
  try {
    const res = await request.get('/admin/users')
    if (res.code === 200) {
      users.value = res.data
    } else {
      ElMessage.error(res.msg || '获取用户失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

async function toggleRole(user) {
  const newRole = user.role === 'admin' ? 'user' : 'admin'
  try {
    await request.post('/admin/changeRole', { id: user.id, role: newRole })
    ElMessage.success('角色已更新')
    fetchUsers()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function toggleStatus(user) {
  const newStatus = user.status === 1 ? 0 : 1
  try {
    await request.post('/admin/toggleStatus', { id: user.id, status: newStatus })
    ElMessage.success('状态已更新')
    fetchUsers()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function deleteUser(user) {
  try {
    await ElMessageBox.confirm(`确定删除用户 ${user.username}？`, '警告', { type: 'warning' })
    await request.post('/admin/deleteUser', { id: user.id })
    ElMessage.success('已删除')
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.admin-panel {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}
</style>
