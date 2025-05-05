<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-box">
        <div class="auth-header">
          <h1 class="site-title">广东省博物馆文创</h1>
          <h2>管理员登录</h2>
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
              placeholder="管理员账号"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="formData.password"
              :prefix-icon="Lock"
              type="password"
              placeholder="密码"
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
              登录系统
            </el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          <router-link to="/login" class="back-link">
            <el-icon><Back /></el-icon>
            返回用户登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Back } from '@element-plus/icons-vue'
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
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 4, max: 50, message: '账号长度必须在4-50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6-100个字符之间', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  try {
    loading.value = true
    await formRef.value.validate()
    
    console.log('开始管理员登录请求:', formData.username)
    const result = await userStore.adminLogin(formData)
    console.log('登录响应:', result)
    
    if (userStore.token) {
      ElMessage.success('登录成功')
      router.push('/admin/dashboard')
    } else {
      throw new Error('登录失败：未获取到token')
    }
  } catch (error) {
    console.error('登录错误详情:', error)
    ElMessage.error(error.message || '登录失败，请检查账号密码是否正确')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
}

.auth-container {
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.auth-box {
  background: #ffffff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);

  .auth-header {
    text-align: center;
    margin-bottom: 40px;

    .site-title {
      font-size: 24px;
      color: #1a237e;
      margin-bottom: 20px;
      font-weight: normal;
    }

    h2 {
      margin: 0;
      font-size: 28px;
      color: #0d47a1;
      font-weight: 500;
    }
  }
}

:deep(.el-input) {
  --el-input-bg-color: #f8f9fa;
  --el-input-border-color: #dee2e6;
  --el-input-hover-border-color: #0d47a1;
  --el-input-focus-border-color: #0d47a1;
  
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
  background-color: #0d47a1;
  border-color: #0d47a1;
  margin-top: 12px;

  &:hover {
    background-color: darken(#0d47a1, 10%);
    border-color: darken(#0d47a1, 10%);
  }
}

.auth-footer {
  text-align: center;
  margin-top: 24px;
  
  .back-link {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: #0d47a1;
    text-decoration: none;
    font-size: 16px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

:deep(.el-input__prefix-inner) {
  color: #6c757d;
}
</style> 