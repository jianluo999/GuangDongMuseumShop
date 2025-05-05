<template>
  <div class="profile-container">
    <div class="profile-card">
      <h2>个人信息</h2>
      
      <div class="avatar-section">
        <img :src="userProfile.avatarUrl || '/default-avatar.png'" alt="用户头像" class="avatar">
      </div>

      <el-form :model="userProfile" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userProfile.username" disabled />
        </el-form-item>
        
        <el-form-item label="邮箱">
          <el-input v-model="userProfile.email" />
        </el-form-item>
        
        <el-form-item label="手机号码">
          <el-input v-model="userProfile.phone" />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const userProfile = ref({
  username: '',
  email: '',
  phone: '',
  avatarUrl: ''
})

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    const { data } = await request.get('/api/users/profile')
    if (data.code === 200) {
      userProfile.value = data.data
    } else {
      throw new Error(data.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 更新用户信息
const updateProfile = async (formData) => {
  try {
    const { data } = await request.put('/api/users/profile', formData)
    if (data.code === 200) {
      ElMessage.success('更新成功')
      await fetchUserProfile()
    } else {
      throw new Error(data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    ElMessage.error('更新失败')
  }
}

onMounted(() => {
  fetchUserProfile()
})
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
</style>