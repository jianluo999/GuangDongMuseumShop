<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <div class="search-container">
      <el-form :model="queryParams" ref="queryForm" :inline="true" class="search-form">
        <el-form-item>
          <el-input
            v-model="queryParams.username"
            placeholder="请输入用户名"
            clearable
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="queryParams.phone"
            placeholder="请输入手机号"
            clearable
            prefix-icon="Phone"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery" class="search-btn">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户列表 -->
    <div class="main-container">
      <div class="table-header">
        <h2 class="table-title">用户列表</h2>
        <el-button type="primary" :icon="Plus" @click="handleAdd" class="add-btn">新增用户</el-button>
      </div>

      <el-table 
        v-loading="loading" 
        :data="userList"
        class="custom-table"
        :header-cell-style="{
          background: '#f5f7fa',
          color: '#606266',
          fontWeight: 600,
          fontSize: '14px'
        }"
      >
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar 
              :size="40" 
              :class="['user-avatar', row.username.charAt(0).toLowerCase()]"
            >
              {{ row.username.charAt(0).toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="用户名" prop="username" min-width="120" />
        <el-table-column label="手机号" prop="phone" width="140" />
        <el-table-column label="邮箱" prop="email" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button link type="primary" @click="handleEdit(row)" class="action-btn">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button link type="warning" @click="handleResetPwd(row)" class="action-btn">
                <el-icon><Key /></el-icon>
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)" class="action-btn">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      append-to-body
      destroy-on-close
      class="custom-dialog"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="rules"
        label-width="80px"
        class="dialog-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号">
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!userForm.id">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Key, Delete, User, Phone, Message, Lock } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dayjs from 'dayjs'

// 数据定义
const loading = ref(false)
const dialogVisible = ref(false)
const userList = ref([])
const total = ref(0)
const userFormRef = ref()

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  phone: ''
})

// 表单数据
const userForm = reactive({
  id: undefined,
  username: '',
  phone: '',
  email: '',
  password: ''
})

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => userForm.id ? '编辑用户' : '新增用户')

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await request.get('/api/admin/users', {
      params: {
        page: queryParams.pageNum - 1,
        size: queryParams.pageSize,
        username: queryParams.username || undefined,
        phone: queryParams.phone || undefined
      }
    })
    
    if (data.code === 200) {
      userList.value = data.data.content
      total.value = data.data.totalElements
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 新增用户
const handleAdd = () => {
  Object.assign(userForm, {
    id: undefined,
    username: '',
    phone: '',
    email: '',
    password: ''
  })
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    phone: row.phone,
    email: row.email
  })
  dialogVisible.value = true
}

// 重置密码
const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm('确认重置该用户的密码吗？', '提示', {
      type: 'warning'
    })
    const { data } = await request.put(`/api/admin/users/${row.id}/password/reset`)
    if (data.code === 200) {
      ElMessageBox.alert(
        `用户 ${row.username} 的密码已重置为：${data.data.password}`, 
        '重置成功', 
        {
          confirmButtonText: '确定',
          type: 'success',
          callback: () => {
            // 可以在这里执行其他操作
          }
        }
      )
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该用户吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/api/admin/users/${row.id}`)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    
    if (userForm.id) {
      await request.put(`/api/admin/users/${userForm.id}`, userForm)
      ElMessage.success('更新成功')
    } else {
      await request.post('/api/admin/users', userForm)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('保存用户失败:', error)
    ElMessage.error('保存失败')
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.username = ''
  queryParams.phone = ''
  handleQuery()
}

// 分页
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);

  .search-container {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    margin-bottom: 24px;

    .search-form {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;

      .el-form-item {
        margin-bottom: 0;
      }

      .search-btn {
        margin-right: 8px;
      }
    }
  }

  .main-container {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .table-title {
        font-size: 20px;
        font-weight: 600;
        color: #303133;
        margin: 0;
      }

      .add-btn {
        padding: 12px 20px;
      }
    }

    .custom-table {
      margin-bottom: 24px;
      border-radius: 8px;
      overflow: hidden;

      .user-avatar {
        background: linear-gradient(45deg, #36d1dc, #5b86e5);
        color: #fff;
        font-weight: bold;
        
        &.a { background: linear-gradient(45deg, #ff9a9e, #fad0c4); }
        &.b { background: linear-gradient(45deg, #a18cd1, #fbc2eb); }
        &.c { background: linear-gradient(45deg, #84fab0, #8fd3f4); }
        &.j { background: linear-gradient(45deg, #fad0c4, #ffd1ff); }
        &.u { background: linear-gradient(45deg, #d4fc79, #96e6a1); }
      }

      .action-buttons {
        display: flex;
        gap: 8px;
        justify-content: center;

        .action-btn {
          padding: 8px;
          border-radius: 4px;
          transition: all 0.3s;

          &:hover {
            transform: translateY(-2px);
          }
        }
      }
    }
  }

  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 24px;

    :deep(.el-pagination) {
      padding: 0;
      font-weight: normal;

      .el-pagination__total {
        margin-right: 16px;
      }

      .el-pagination__sizes {
        margin-right: 16px;
      }
    }
  }
}

.custom-dialog {
  :deep(.el-dialog__header) {
    margin: 0;
    padding: 20px 24px;
    border-bottom: 1px solid #dcdfe6;
    
    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
    }
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #dcdfe6;
  }

  .dialog-form {
    .el-form-item:last-child {
      margin-bottom: 0;
    }

    :deep(.el-input) {
      .el-input__prefix {
        font-size: 16px;
      }
    }
  }
}
</style> 