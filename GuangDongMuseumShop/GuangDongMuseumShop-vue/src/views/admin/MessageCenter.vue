<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="filter-container">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="消息类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="请选择消息类型" clearable>
            <el-option label="订单消息" value="ORDER" />
            <el-option label="系统消息" value="SYSTEM" />
            <el-option label="订单状态" value="ORDER_STATUS" />
            <el-option label="促销消息" value="PROMOTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="queryParams.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item label="已读状态" prop="isRead">
          <el-select v-model="queryParams.isRead" placeholder="请选择状态" clearable>
            <el-option label="已读" :value="true" />
            <el-option label="未读" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="operation-container">
      <el-button type="primary" @click="handleAdd">发送系统消息</el-button>
    </el-card>

    <!-- 消息列表 -->
    <el-card class="table-container">
      <el-table
        v-loading="loading"
        :data="messageList"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户名" width="120">
          <template #default="{ row }">
            {{ row.username || '未知用户' }}
          </template>
        </el-table-column>
        <el-table-column label="消息类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'ORDER' ? 'success' : row.type === 'SYSTEM' ? 'info' : row.type === 'PROMOTION' ? 'warning' : 'primary'">
              {{ row.type === 'ORDER' ? '订单消息' : row.type === 'SYSTEM' ? '系统消息' : row.type === 'PROMOTION' ? '促销消息' : '订单状态' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="消息内容" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'info' : 'danger'">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发送时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 发送系统消息对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="发送系统消息"
      width="500px"
    >
      <el-form
        ref="messageForm"
        :model="messageForm"
        :rules="messageRules"
        label-width="100px"
      >
        <el-form-item label="接收用户" prop="userId">
          <el-select
            v-model="messageForm.userId"
            placeholder="请选择接收用户"
            clearable
            filterable
            :loading="userLoading"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="消息内容" prop="content">
          <el-input
            v-model="messageForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入消息内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSendMessage">发送</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/format'
import request from '@/utils/request'

// 查询参数
const queryParams = ref({
  page: 1,
  size: 10,
  type: '',
  username: '',
  isRead: null
})

const loading = ref(false)
const messageList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const userLoading = ref(false)
const userList = ref([])

// 表单数据
const messageForm = ref({
  userId: '',
  content: ''
})

// 表单校验规则
const messageRules = {
  userId: [{ required: true, message: '请选择接收用户', trigger: 'change' }],
  content: [{ required: true, message: '请输入消息内容', trigger: 'blur' }]
}

// 获取消息列表
const getList = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.value.page - 1,
      size: queryParams.value.size,
      type: queryParams.value.type || undefined,
      username: queryParams.value.username || undefined,
      isRead: queryParams.value.isRead
    }
    
    const res = await request({
      url: '/api/admin/messages',
      method: 'get',
      params
    })
    console.log('消息列表响应:', res.data)
    if (res.data.code === 200) {
      messageList.value = res.data.data.content
      console.log('消息列表数据:', messageList.value)
      total.value = res.data.data.totalElements
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
    ElMessage.error('获取消息列表失败')
  } finally {
    loading.value = false
  }
}

// 获取用户列表
const getUserList = async () => {
  userLoading.value = true
  try {
    const res = await request({
      url: '/api/admin/users/list',
      method: 'get'
    })
    if (res.data.code === 200) {
      userList.value = res.data.data
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    userLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.value.page = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.value = {
    page: 1,
    size: 10,
    type: '',
    username: '',
    isRead: null
  }
  getList()
}

// 分页处理
const handleSizeChange = (val) => {
  queryParams.value.size = val
  queryParams.value.page = 1
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.value.page = val
  getList()
}

// 打开发送消息对话框
const handleAdd = () => {
  messageForm.value = {
    userId: '',
    content: ''
  }
  dialogVisible.value = true
  getUserList()
}

// 发送消息
const handleSendMessage = async () => {
  try {
    const res = await request({
      url: '/api/admin/messages/send',
      method: 'post',
      data: messageForm.value
    })
    if (res.data.code === 200) {
      ElMessage.success('发送成功')
      dialogVisible.value = false
      getList()
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  }
}

// 删除消息
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认要删除该消息吗？', '提示', {
      type: 'warning'
    })
    
    const res = await request({
      url: `/api/admin/messages/${id}`,
      method: 'delete'
    })
    
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      getList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败:', error)
      ElMessage.error('删除消息失败')
    }
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.operation-container {
  margin-bottom: 20px;
}

.table-container {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 