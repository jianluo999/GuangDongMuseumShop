<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-box">
        <div class="auth-header">
          <h1 class="site-title">广东省博物馆文创</h1>
          <h2>注册</h2>
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

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="formData.confirmPassword"
              :prefix-icon="Lock"
              type="password"
              placeholder="请确认密码"
              show-password
              size="large"
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model.trim="formData.email"
              :prefix-icon="Message"
              placeholder="请输入邮箱"
              size="large"
            />
          </el-form-item>

          <el-form-item prop="phone">
            <el-input
              v-model.trim="formData.phone"
              :prefix-icon="Phone"
              placeholder="请输入手机号（选填）"
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
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          <router-link to="/login" class="login-link">已有账号？立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'
import { userRegisterService } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== formData.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 50, message: '用户名长度必须在4-50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6-100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const response = await userRegisterService({
      username: formData.username.trim(),
      password: formData.password,
      email: formData.email.trim(),
      phone: formData.phone.trim()
    })
    
    console.log('注册响应:', response)
    
    if (response.success || response.data?.token) {
      ElMessage({
        type: 'success',
        message: '注册成功！正在跳转到登录页面...',
        duration: 2000
      })
      
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } else {
      throw new Error(response.data?.message || '注册失败，请重试')
    }
  } catch (error) {
    console.error('注册错误:', error)
    ElMessage({
      type: 'error',
      message: error.message || '注册失败，请重试',
      duration: 3000
    })
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
  
  .login-link {
    color: #B24F30;
    text-decoration: none;
    font-size: 16px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

:deep(.el-input__prefix-inner) {
  color: #6A5D4D;
}
</style>