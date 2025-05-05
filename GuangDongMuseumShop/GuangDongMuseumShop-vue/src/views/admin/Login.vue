<template>
  <div class="admin-login-container">
    <div class="admin-login-box">
      <h2>广东省博物馆文创</h2>
      <h3>管理员登录</h3>
      <el-form :model="form" :rules="rules" ref="loginForm">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="back-to-home">
        <router-link to="/">返回用户登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '@/utils/axios'

const router = useRouter()
const loading = ref(false)
const loginForm = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    loading.value = true
    const response = await axios.post('/api/admin/login', {
      username: form.username,
      password: form.password
    })
    
    const { code, message, data } = response.data
    
    if (code === 200 && data) {
      const { token, roles, user } = data
      localStorage.setItem('token', token)
      localStorage.setItem('roles', JSON.stringify(roles))
      localStorage.setItem('user', JSON.stringify(user))
      
      ElMessage.success(message || '登录成功')
      await router.replace('/admin/dashboard')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #fff;
}

.admin-login-box {
  width: 400px;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #409EFF;
  margin-bottom: 10px;
  font-size: 24px;
}

h3 {
  text-align: center;
  color: #606266;
  margin-bottom: 30px;
  font-size: 18px;
}

.login-btn {
  width: 100%;
  background-color: #409EFF;
  border-color: #409EFF;
  height: 40px;
  font-size: 16px;
}

.login-btn:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.el-input {
  height: 40px;
}

.back-to-home {
  text-align: center;
  margin-top: 20px;
}

.back-to-home a {
  color: #409EFF;
  text-decoration: none;
  font-size: 14px;
}

.back-to-home a:hover {
  color: #66b1ff;
}

:deep(.el-input__wrapper) {
  background-color: #f5f7fa;
}

:deep(.el-input__inner) {
  height: 40px;
}
</style> 