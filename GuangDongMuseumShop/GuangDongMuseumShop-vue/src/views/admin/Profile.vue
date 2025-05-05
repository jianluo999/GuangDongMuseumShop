<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" @click="handleUpdateProfile" :loading="updating">保存修改</el-button>
        </div>
      </template>
      <el-form ref="profileForm" :model="profileData" :rules="profileRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="profileData.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileData.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
          <el-button type="primary" @click="handleUpdatePassword" :loading="updatingPwd">确认修改</el-button>
        </div>
      </template>
      <el-form ref="passwordForm" :model="passwordData" :rules="passwordRules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordData.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordData.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordData.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const profileForm = ref(null)
const passwordForm = ref(null)
const updating = ref(false)
const updatingPwd = ref(false)

// 个人信息表单数据
const profileData = ref({
  username: '',
  email: '',
  phone: ''
})

// 密码表单数据
const passwordData = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const profileRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordData.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取个人信息
const getProfile = async () => {
  try {
    const { data } = await request.get('/api/admin/users/profile')
    if (data.code === 200) {
      profileData.value = data.data
    }
  } catch (error) {
    console.error('获取个人信息失败:', error)
    ElMessage.error('获取个人信息失败')
  }
}

// 更新个人信息
const handleUpdateProfile = async () => {
  if (!profileForm.value) return
  
  try {
    await profileForm.value.validate()
    updating.value = true
    
    const { data } = await request.put('/api/admin/users/profile', profileData.value)
    if (data.code === 200) {
      ElMessage.success('个人信息更新成功')
    } else {
      ElMessage.error(data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新失败')
  } finally {
    updating.value = false
  }
}

// 修改密码
const handleUpdatePassword = async () => {
  if (!passwordForm.value) return
  
  try {
    await passwordForm.value.validate()
    updatingPwd.value = true
    
    const { data } = await request.put('/api/admin/users/password', passwordData.value)
    if (data.code === 200) {
      ElMessage.success('密码修改成功')
      passwordForm.value.resetFields()
    } else {
      ElMessage.error(data.message || '修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改失败')
  } finally {
    updatingPwd.value = false
  }
}

onMounted(() => {
  getProfile()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  background-color: #f5f9fc;
  min-height: 100vh;
}

.profile-card,
.password-card {
  max-width: 800px;
  margin: 0 auto 20px;
  box-shadow: 0 2px 12px rgba(0, 150, 225, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-card__header) {
  background: linear-gradient(to right, #e8f4fc, #f5f9fc);
  color: #0096e1;
  font-weight: bold;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #2c3e50;
}

:deep(.el-input__inner) {
  border-color: #e8f4fc;
}

:deep(.el-input__inner:focus) {
  border-color: #0096e1;
}

:deep(.el-button--primary) {
  background-color: #0096e1;
  border-color: #0096e1;
}

:deep(.el-button--primary:hover) {
  background-color: #007bc1;
  border-color: #007bc1;
}
</style> 