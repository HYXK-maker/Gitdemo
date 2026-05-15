

<template>
  <div class="recycle-bin">
    <el-card header="回收站">
      <el-table :data="recycleList" v-loading="loading" border>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'directory' ? '' : 'success'">
              {{ row.type === 'directory' ? '目录' : '文档' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deletedAt" label="删除时间" width="180" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="restoreItem(row)">恢复</el-button>
            <el-popconfirm title="确定永久删除吗？" @confirm="permanentDelete(row)">
              <template #reference>
                <el-button type="danger" size="small" text>彻底删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && recycleList.length === 0" class="empty-tip">
        回收站为空
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

import { getRecycleList } from '@/api/recycle'            // 获取回收站列表
import { restoreFromRecycle } from '@/api/recycle'        // 恢复
import { permanentDeleteFromRecycle } from '@/api/recycle' // 永久删除

const recycleList = ref([])
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const res = await getRecycleList()
    recycleList.value = res.data || []
  } catch (e) {
    ElMessage.error('获取回收站列表失败')
  } finally {
    loading.value = false
  }
}

async function restoreItem(row) {
  try {
    await restoreFromRecycle(row.id)
    ElMessage.success('已恢复')
    fetchList()
  } catch (e) {
    ElMessage.error('恢复失败')
  }
}

async function permanentDelete(row) {
  try {
    await permanentDeleteFromRecycle(row.id)
    ElMessage.success('已彻底删除')
    fetchList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchList()
})

defineExpose({ fetchList })
</script>

<style scoped>
.recycle-bin {
  height: 100%;
  padding: 16px;
}
.empty-tip {
  text-align: center;
  color: #999;
  margin-top: 40px;
}
</style>
