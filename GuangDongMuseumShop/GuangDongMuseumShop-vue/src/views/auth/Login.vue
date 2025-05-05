<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-box">
        <div class="auth-header">
          <h1 class="site-title">广东省博物馆文创</h1>
          <h2>登录</h2>
        </div>
        
        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-position="top"
          @submit.prevent="handleSubmit"
        >
          <el-form-item prop="username">
            <el-input
              v-model.trim="formData.username"
              :prefix-icon="User"
              placeholder="请输入用户名"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="formData.password"
              :prefix-icon="Lock"
              type="password"
              placeholder="请输入密码"
              show-password
              size="large"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="submit-btn"
              :loading="loading"
              @click="handleSubmit"
              size="large"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          <router-link to="/register" class="register-link">没有账号？立即注册</router-link>
          <el-divider direction="vertical" />
          <router-link to="/admin/login" class="admin-link">管理员登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 50, message: '用户名长度必须在4-50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6-100个字符之间', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    loading.value = true
    await formRef.value.validate()
    
    console.log('Login data:', formData)
    
    const result = await userStore.login({
      username: formData.username,
      password: formData.password
    })
    
    console.log('Login result:', result)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F5F0E6;
}

.auth-container {
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.auth-box {
  background-color: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 16px rgba(106, 93, 77, 0.1);

  .auth-header {
    text-align: center;
    margin-bottom: 40px;

    .site-title {
      font-size: 24px;
      color: #6A5D4D;
      margin-bottom: 20px;
      font-weight: normal;
    }

    h2 {
      margin: 0;
      font-size: 28px;
      color: #B24F30;
      font-weight: 500;
    }
  }
}

:deep(.el-input) {
  --el-input-bg-color: #F5F0E6;
  --el-input-border-color: #D4C8B8;
  --el-input-hover-border-color: #B24F30;
  --el-input-focus-border-color: #B24F30;
  
  .el-input__wrapper {
    border-radius: 8px;
    padding: 4px 12px;
  }

  .el-input__inner {
    height: 42px;
    font-size: 16px;
  }
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 18px;
  border-radius: 8px;
  background-color: #B24F30;
  border-color: #B24F30;
  margin-top: 12px;

  &:hover {
    background-color: darken(#B24F30, 10%);
    border-color: darken(#B24F30, 10%);
  }
}

.auth-footer {
  text-align: center;
  margin-top: 24px;
  
  .register-link,
  .admin-link {
    color: #B24F30;
    text-decoration: none;
    font-size: 16px;
    
    &:hover {
      text-decoration: underline;
    }
  }

  .el-divider {
    margin: 0 12px;
  }
}

:deep(.el-input__prefix-inner) {
  color: #6A5D4D;
}
</style>