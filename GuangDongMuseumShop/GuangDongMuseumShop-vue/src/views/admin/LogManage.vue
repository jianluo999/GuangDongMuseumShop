<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="filter-container">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="操作类型" prop="type">
          <el-select v-model="queryParams.type" clearable placeholder="请选择类型">
            <el-option value="LOGIN" label="登录日志" />
            <el-option value="OPERATION" label="操作日志" />
            <el-option value="ERROR" label="错误日志" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人" prop="username">
          <el-input v-model="queryParams.username" placeholder="请输入操作人" clearable />
        </el-form-item>
        <el-form-item label="操作时间" prop="dateRange">
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
          <el-button type="danger" icon="Delete" @click="handleClear">清空日志</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card>
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="操作日志" name="operation">
          <el-table v-loading="loading" :data="logList">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="操作人" prop="username" width="120" />
            <el-table-column label="操作类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getOperationTypeTag(row.operationType)">
                  {{ getOperationTypeText(row.operationType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作描述" prop="description" show-overflow-tooltip />
            <el-table-column label="IP地址" prop="ip" width="130" />
            <el-table-column label="操作时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleView(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="登录日志" name="login">
          <el-table v-loading="loading" :data="logList">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="用户名" prop="username" width="120" />
            <el-table-column label="登录状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
                  {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="IP地址" prop="ip" width="130" />
            <el-table-column label="登录地点" prop="location" width="150" />
            <el-table-column label="浏览器" prop="browser" width="150" />
            <el-table-column label="操作系统" prop="os" width="150" />
            <el-table-column label="登录时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="错误日志" name="error">
          <el-table v-loading="loading" :data="logList">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="错误级别" width="100">
              <template #default="{ row }">
                <el-tag :type="getErrorLevelTag(row.level)">
                  {{ row.level }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="错误信息" prop="message" show-overflow-tooltip />
            <el-table-column label="发生位置" prop="location" width="200" show-overflow-tooltip />
            <el-table-column label="发生时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleView(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

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

    <!-- 日志详情对话框 -->
    <el-dialog
      title="日志详情"
      v-model="detailDialogVisible"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item v-for="(value, key) in currentLog" :key="key" :label="key">
          {{ value }}
        </el-descriptions-item>
      </el-descriptions>
      <template v-if="currentLog?.stackTrace">
        <div class="stack-trace">
          <pre>{{ currentLog.stackTrace }}</pre>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 数据定义
const loading = ref(false)
const activeTab = ref('operation')
const detailDialogVisible = ref(false)
const logList = ref([])
const total = ref(0)
const currentLog = ref(null)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  type: '',
  username: '',
  dateRange: []
})

// 获取日志列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await request.get(`/api/admin/logs/${activeTab.value}`, {
      params: {
        page: queryParams.pageNum - 1,
        size: queryParams.pageSize,
        username: queryParams.username || undefined,
        startTime: queryParams.dateRange?.[0],
        endTime: queryParams.dateRange?.[1]
      }
    })
    logList.value = data.content
    total.value = data.totalElements
  } catch (error) {
    console.error('获取日志列表失败:', error)
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

// 操作类型标签
const getOperationTypeTag = (type) => {
  const map = {
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    QUERY: 'info'
  }
  return map[type] || 'info'
}

const getOperationTypeText = (type) => {
  const map = {
    CREATE: '新增',
    UPDATE: '修改',
    DELETE: '删除',
    QUERY: '查询'
  }
  return map[type] || type
}

// 错误级别标签
const getErrorLevelTag = (level) => {
  const map = {
    ERROR: 'danger',
    WARN: 'warning',
    INFO: 'info',
    DEBUG: ''
  }
  return map[level] || ''
}

// Tab切换
const handleTabChange = () => {
  queryParams.pageNum = 1
  getList()
}

// 查看详情
const handleView = (row) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

// 清空日志
const handleClear = () => {
  ElMessageBox.confirm('确认要清空所有日志吗？此操作不可恢复！', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/api/admin/logs/${activeTab.value}/clear`)
      ElMessage.success('清空成功')
      getList()
    } catch (error) {
      ElMessage.error('清空失败')
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
  queryParams.username = ''
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
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
  
  .stack-trace {
    margin-top: 20px;
    padding: 10px;
    background: #f5f7fa;
    border-radius: 4px;
    
    pre {
      margin: 0;
      white-space: pre-wrap;
      word-wrap: break-word;
      font-family: monospace;
      font-size: 12px;
      line-height: 1.5;
    }
  }
}
</style> 