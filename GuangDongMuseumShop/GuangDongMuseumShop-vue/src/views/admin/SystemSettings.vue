<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <!-- 基本设置 -->
      <el-tab-pane label="基本设置" name="basic">
        <el-form
          ref="basicFormRef"
          :model="basicForm"
          :rules="basicRules"
          label-width="120px"
        >
          <el-card>
            <template #header>
              <div class="card-header">
                <span>网站信息</span>
              </div>
            </template>
            
            <el-form-item label="网站名称" prop="siteName">
              <el-input v-model="basicForm.siteName" />
            </el-form-item>
            
            <el-form-item label="网站Logo">
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="beforeLogoUpload"
                :on-success="handleLogoSuccess"
              >
                <img v-if="basicForm.logo" :src="basicForm.logo" class="avatar">
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="basicForm.phone" />
            </el-form-item>
            
            <el-form-item label="客服邮箱" prop="email">
              <el-input v-model="basicForm.email" />
            </el-form-item>
          </el-card>

          <el-card style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>订单设置</span>
              </div>
            </template>
            
            <el-form-item label="自动取消时间" prop="autoCancelTime">
              <el-input-number 
                v-model="basicForm.autoCancelTime"
                :min="1"
                :max="72"
              />
              <span class="unit">小时</span>
            </el-form-item>
            
            <el-form-item label="自动确认收货" prop="autoConfirmTime">
              <el-input-number 
                v-model="basicForm.autoConfirmTime"
                :min="1"
                :max="30"
              />
              <span class="unit">天</span>
            </el-form-item>
            
            <el-form-item label="运费设置" prop="shippingFee">
              <el-input-number 
                v-model="basicForm.shippingFee"
                :min="0"
                :precision="2"
              />
              <span class="unit">元</span>
            </el-form-item>
            
            <el-form-item label="免运费金额" prop="freeShippingAmount">
              <el-input-number 
                v-model="basicForm.freeShippingAmount"
                :min="0"
                :precision="2"
              />
              <span class="unit">元</span>
            </el-form-item>
          </el-card>

          <div class="form-footer">
            <el-button type="primary" @click="handleBasicSubmit">保存设置</el-button>
          </div>
        </el-form>
      </el-tab-pane>

      <!-- 权限管理 -->
      <el-tab-pane label="权限管理" name="permission">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>角色列表</span>
              <el-button type="primary" icon="Plus" @click="handleAddRole">新增角色</el-button>
            </div>
          </template>

          <el-table :data="roleList" style="width: 100%">
            <el-table-column label="角色名称" prop="name" />
            <el-table-column label="角色描述" prop="description" show-overflow-tooltip />
            <el-table-column label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button 
                  link 
                  type="primary" 
                  @click="handleEditPermissions(row)"
                >
                  权限设置
                </el-button>
                <el-button 
                  link 
                  type="primary" 
                  @click="handleEditRole(row)"
                >
                  编辑
                </el-button>
                <el-button 
                  link 
                  type="danger" 
                  @click="handleDeleteRole(row)"
                  :disabled="row.isSystem"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 角色表单对话框 -->
    <el-dialog
      :title="roleDialogTitle"
      v-model="roleDialogVisible"
      width="500px"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-width="80px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            placeholder="请输入角色描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleRoleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 权限设置对话框 -->
    <el-dialog
      title="权限设置"
      v-model="permissionDialogVisible"
      width="600px"
    >
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'name' }"
        :default-checked-keys="selectedPermissions"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handlePermissionSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 数据定义
const activeTab = ref('basic')
const basicFormRef = ref()
const roleFormRef = ref()
const permissionTreeRef = ref()
const roleDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const roleDialogType = ref('add')
const currentRoleId = ref(null)
const roleList = ref([])
const permissionTree = ref([])
const selectedPermissions = ref([])

// 基本设置表单
const basicForm = reactive({
  siteName: '',
  logo: '',
  phone: '',
  email: '',
  autoCancelTime: 24,
  autoConfirmTime: 7,
  shippingFee: 0,
  freeShippingAmount: 0
})

// 角色表单
const roleForm = reactive({
  name: '',
  description: ''
})

// 表单校验规则
const basicRules = {
  siteName: [
    { required: true, message: '请输入网站名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const roleRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const roleDialogTitle = computed(() => {
  return roleDialogType.value === 'add' ? '新增角色' : '编辑角色'
})

// 上传相关
const uploadUrl = import.meta.env.VITE_API_URL + '/api/admin/upload'
const uploadHeaders = {
  Authorization: 'Bearer ' + localStorage.getItem('token')
}

const beforeLogoUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleLogoSuccess = (response) => {
  basicForm.logo = response.data.url
}

// 获取基本设置
const getBasicSettings = async () => {
  try {
    const { data } = await request.get('/api/admin/settings')
    Object.assign(basicForm, data)
  } catch (error) {
    console.error('获取基本设置失败:', error)
  }
}

// 保存基本设置
const handleBasicSubmit = async () => {
  if (!basicFormRef.value) return
  
  try {
    await basicFormRef.value.validate()
    await request.put('/api/admin/settings', basicForm)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存基本设置失败:', error)
    ElMessage.error('保存失败')
  }
}

// 获取角色列表
const getRoleList = async () => {
  try {
    const { data } = await request.get('/api/admin/roles')
    roleList.value = data
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// 获取权限树
const getPermissionTree = async () => {
  try {
    const { data } = await request.get('/api/admin/permissions/tree')
    permissionTree.value = data
  } catch (error) {
    console.error('获取权限树失败:', error)
  }
}

// 角色相关操作
const handleAddRole = () => {
  roleDialogType.value = 'add'
  roleForm.name = ''
  roleForm.description = ''
  roleDialogVisible.value = true
}

const handleEditRole = (row) => {
  roleDialogType.value = 'edit'
  Object.assign(roleForm, row)
  roleDialogVisible.value = true
}

const handleRoleSubmit = async () => {
  if (!roleFormRef.value) return
  
  try {
    await roleFormRef.value.validate()
    
    if (roleDialogType.value === 'add') {
      await request.post('/api/admin/roles', roleForm)
      ElMessage.success('新增成功')
    } else {
      await request.put(`/api/admin/roles/${roleForm.id}`, roleForm)
      ElMessage.success('修改成功')
    }
    
    roleDialogVisible.value = false
    getRoleList()
  } catch (error) {
    console.error('保存角色失败:', error)
    ElMessage.error('保存失败')
  }
}

const handleDeleteRole = (row) => {
  if (row.isSystem) {
    ElMessage.warning('系统角色不能删除')
    return
  }
  
  ElMessageBox.confirm('确认要删除该角色吗？', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/api/admin/roles/${row.id}`)
      ElMessage.success('删除成功')
      getRoleList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 权限相关操作
const handleEditPermissions = async (row) => {
  currentRoleId.value = row.id
  try {
    const { data } = await request.get(`/api/admin/roles/${row.id}/permissions`)
    selectedPermissions.value = data
    permissionDialogVisible.value = true
  } catch (error) {
    console.error('获取角色权限失败:', error)
  }
}

const handlePermissionSubmit = async () => {
  if (!permissionTreeRef.value || !currentRoleId.value) return
  
  try {
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    await request.put(`/api/admin/roles/${currentRoleId.value}/permissions`, {
      permissionIds: checkedKeys
    })
    ElMessage.success('权限设置成功')
    permissionDialogVisible.value = false
  } catch (error) {
    console.error('设置权限失败:', error)
    ElMessage.error('设置失败')
  }
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

onMounted(() => {
  getBasicSettings()
  getRoleList()
  getPermissionTree()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    span {
      font-size: 16px;
      font-weight: bold;
    }
  }
  
  .form-footer {
    margin-top: 20px;
    text-align: center;
  }
  
  .unit {
    margin-left: 10px;
    color: #666;
  }
  
  .avatar-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);

      &:hover {
        border-color: var(--el-color-primary);
      }
    }
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    text-align: center;
    line-height: 178px;
  }
  
  .avatar {
    width: 178px;
    height: 178px;
    display: block;
  }
}
</style> 