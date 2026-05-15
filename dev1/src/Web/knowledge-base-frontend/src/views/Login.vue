<template>
  <div class="login-wrapper">
    <el-card class="login-card">
      <h2>团队知识库登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width:100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="tip">
        没有账号？<router-link to="/register">立即注册</router-link>
      </div>
      <div class="demo-tip">
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  console.log('handleLogin 被调用')

  // 表单验证
  try {
    await formRef.value.validate()
  } catch (error) {
    console.log('表单验证失败:', error)
    return
  }

  loading.value = true

  try {
    console.log('调用 loginAction, 用户名:', form.username)
    await userStore.loginAction(form)
    console.log('loginAction 完成, 准备跳转')
    ElMessage.success('登录成功')
    router.push('/main')
  } catch (err) {
    console.error('登录失败:', err)
    // 错误已经在 store 中处理了，这里不需要重复提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f0f2f5;
}
.login-card {
  width: 400px;
}
.tip {
  text-align: right;
  margin-top: 10px;
}
.tip a {
  color: #409eff;
  text-decoration: none;
}
.demo-tip {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: #909399;
}
</style>
