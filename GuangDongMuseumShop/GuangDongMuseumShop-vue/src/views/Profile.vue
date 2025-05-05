<template>
  <div class="profile-container">
    <h2>个人中心</h2>
    <div class="profile-content">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const form = reactive({
  username: userStore.username,
  email: userStore.userProfile?.email || '',
  phone: userStore.userProfile?.phone || ''
})

onMounted(async () => {
  if (!userStore.userProfile) {
    try {
      await userStore.fetchUserProfile()
      form.email = userStore.userProfile?.email || ''
      form.phone = userStore.userProfile?.phone || ''
    } catch (error) {
      ElMessage.error('获取用户信息失败')
    }
  }
})

const handleSubmit = async () => {
  try {
    await userStore.updateProfile(form)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-content {
  margin-top: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  max-width: 500px;
}
</style>