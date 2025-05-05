<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="filter-container">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="消息类型" prop="type">
          <el-select v-model="queryParams.type" clearable placeholder="请选择类型">
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="订单通知" value="ORDER" />
            <el-option label="活动通知" value="PROMOTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="发送状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
            <el-option label="待发送" value="PENDING" />
            <el-option label="已发送" value="SENT" />
            <el-option label="发送失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="发送时间" prop="dateRange">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 消息列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">消息列表</span>
          <div class="right-buttons">
            <el-button type="primary" icon="Plus" @click="handleAdd">新建消息</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="messageList">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="消息类型" width="120">
          <template #default="{ row }">
            <el-tag :type="messageTypeMap[row.type].type">
              {{ messageTypeMap[row.type].text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标题" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="接收对象" width="150">
          <template #default="{ row }">
            {{ row.targetType === 'ALL' ? '全部用户' : row.targetUserCount + '个用户' }}
          </template>
        </el-table-column>
        <el-table-column label="发送状态" width="100">
          <template #default="{ row }">
            <el-tag :type="messageStatusMap[row.status].type">
              {{ messageStatusMap[row.status].text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="计划发送时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.scheduledTime) }}
          </template>
        </el-table-column>
        <el-table-column label="实际发送时间" width="180">
          <template #default="{ row }">
            {{ row.sentTime ? formatDateTime(row.sentTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.status === 'PENDING'"
              link 
              type="primary" 
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'"
              link 
              type="danger" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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
        />
      </div>
    </el-card>

    <!-- 消息表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="700px"
    >
      <el-form
        ref="messageFormRef"
        :model="messageForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="消息类型" prop="type">
          <el-select v-model="messageForm.type" placeholder="请选择消息类型">
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="订单通知" value="ORDER" />
            <el-option label="活动通知" value="PROMOTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="消息标题" prop="title">
          <el-input v-model="messageForm.title" placeholder="请输入消息标题" />
        </el-form-item>
        <el-form-item label="消息内容" prop="content">
          <el-input
            v-model="messageForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入消息内容"
          />
        </el-form-item>
        <el-form-item label="接收对象" prop="targetType">
          <el-radio-group v-model="messageForm.targetType">
            <el-radio label="ALL">全部用户</el-radio>
            <el-radio label="SELECTED">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          v-if="messageForm.targetType === 'SELECTED'"
          label="选择用户"
          prop="targetUserIds"
        >
          <el-select
            v-model="messageForm.targetUserIds"
            multiple
            filterable
            remote
            :remote-method="searchUsers"
            placeholder="请输入用户名搜索"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发送时间" prop="scheduledTime">
          <el-date-picker
            v-model="messageForm.scheduledTime"
            type="datetime"
            placeholder="请选择发送时间"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 消息详情对话框 -->
    <el-dialog
      title="消息详情"
      v-model="detailDialogVisible"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="消息类型">
          <el-tag :type="messageTypeMap[currentMessage?.type]?.type">
            {{ messageTypeMap[currentMessage?.type]?.text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="消息标题">{{ currentMessage?.title }}</el-descriptions-item>
        <el-descriptions-item label="消息内容">{{ currentMessage?.content }}</el-descriptions-item>
        <el-descriptions-item label="接收对象">
          {{ currentMessage?.targetType === 'ALL' ? '全部用户' : '指定用户' }}
        </el-descriptions-item>
        <el-descriptions-item label="发送状态">
          <el-tag :type="messageStatusMap[currentMessage?.status]?.type">
            {{ messageStatusMap[currentMessage?.status]?.text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="计划发送时间">
          {{ formatDateTime(currentMessage?.scheduledTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="实际发送时间">
          {{ currentMessage?.sentTime ? formatDateTime(currentMessage.sentTime) : '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 数据定义
const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogType = ref('add')
const messageList = ref([])
const total = ref(0)
const userOptions = ref([])
const messageFormRef = ref()
const currentMessage = ref(null)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  type: '',
  status: '',
  dateRange: []
})

// 消息表单
const messageForm = reactive({
  id: undefined,
  type: 'SYSTEM',
  title: '',
  content: '',
  targetType: 'ALL',
  targetUserIds: [],
  scheduledTime: null
})

// 表单校验规则
const rules = {
  type: [{ required: true, message: '请选择消息类型', trigger: 'change' }],
  title: [
    { required: true, message: '请输入消息标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入消息内容', trigger: 'blur' },
    { min: 2, max: 500, message: '长度在 2 到 500 个字符', trigger: 'blur' }
  ],
  scheduledTime: [{ required: true, message: '请选择发送时间', trigger: 'change' }]
}

// 状态映射
const messageTypeMap = {
  SYSTEM: { text: '系统通知', type: 'info' },
  ORDER: { text: '订单通知', type: 'success' },
  PROMOTION: { text: '活动通知', type: 'warning' }
}

const messageStatusMap = {
  PENDING: { text: '待发送', type: '' },
  SENT: { text: '已发送', type: 'success' },
  FAILED: { text: '发送失败', type: 'danger' }
}

// 计算属性
const dialogTitle = computed(() => {
  return dialogType.value === 'add' ? '新建消息' : '编辑消息'
})

// 获取消息列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await request.get('/api/admin/messages', {
      params: {
        page: queryParams.pageNum - 1,
        size: queryParams.pageSize,
        type: queryParams.type || undefined,
        status: queryParams.status || undefined,
        startTime: queryParams.dateRange?.[0],
        endTime: queryParams.dateRange?.[1]
      }
    })
    messageList.value = data.content
    total.value = data.totalElements
  } catch (error) {
    console.error('获取消息列表失败:', error)
    ElMessage.error('获取消息列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索用户
const searchUsers = async (query) => {
  if (query) {
    try {
      const { data } = await request.get('/api/admin/users/search', {
        params: { keyword: query }
      })
      userOptions.value = data
    } catch (error) {
      console.error('搜索用户失败:', error)
    }
  } else {
    userOptions.value = []
  }
}

// 新建消息
const handleAdd = () => {
  dialogType.value = 'add'
  messageForm.id = undefined
  messageForm.type = 'SYSTEM'
  messageForm.title = ''
  messageForm.content = ''
  messageForm.targetType = 'ALL'
  messageForm.targetUserIds = []
  messageForm.scheduledTime = null
  dialogVisible.value = true
}

// 编辑消息
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(messageForm, row)
  dialogVisible.value = true
}

// 查看消息
const handleView = (row) => {
  currentMessage.value = row
  detailDialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!messageFormRef.value) return
  
  try {
    await messageFormRef.value.validate()
    
    if (dialogType.value === 'add') {
      await request.post('/api/admin/messages', messageForm)
      ElMessage.success('新建成功')
    } else {
      await request.put(`/api/admin/messages/${messageForm.id}`, messageForm)
      ElMessage.success('修改成功')
    }
    
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('保存消息失败:', error)
    ElMessage.error('保存失败')
  }
}

// 删除消息
const handleDelete = (row) => {
  ElMessageBox.confirm('确认要删除该消息吗？', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/api/admin/messages/${row.id}`)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 查询和重置
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.type = ''
  queryParams.status = ''
  queryParams.dateRange = []
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

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  
  .filter-container {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .card-title {
      font-size: 16px;
      font-weight: bold;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style> 