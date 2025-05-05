<template>
  <div class="messages-container">
    <el-card class="message-card">
      <template #header>
        <div class="card-header">
          <span>我的消息</span>
          <el-button type="primary" link @click="markAllRead">全部已读</el-button>
        </div>
      </template>
      
      <div v-loading="loading" class="message-list">
        <template v-if="messages.length">
          <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="{ 'is-unread': !message.isRead }"
          >
            <div class="message-content">
              <div class="message-title">
                <el-tag 
                  size="small" 
                  :type="getMessageTagType(message.type)"
                >
                  {{ getMessageTypeLabel(message.type) }}
                </el-tag>
                <span class="unread-dot" v-show="!message.isRead"></span>
              </div>
              <p class="message-text" :class="{ 'unread-text': !message.isRead }">{{ message.content }}</p>
              <div class="message-footer">
                <span class="time">{{ formatDate(message.createdAt) }}</span>
                <div class="actions">
                  <el-button 
                    v-show="!message.isRead"
                    type="primary" 
                    link 
                    size="small"
                    @click="markRead(message.id)"
                  >
                    标记已读
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无消息" />
      </div>
      
      <!-- 分页 -->
      <div class="pagination" v-if="messages.length">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessages, markAllAsRead, markAsRead } from '@/api/message'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const messages = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取消息列表
const getMessageList = async () => {
  try {
    loading.value = true
    console.log('开始获取消息列表, 参数:', {
      page: currentPage.value,
      size: pageSize.value
    })
    
    const res = await getMessages({
      page: currentPage.value - 1,
      size: pageSize.value
    })
    console.log('获取消息列表响应:', res)
    
    if (res.data.code === 200) {
      // 确保每个消息对象都有 isRead 属性，并且正确转换为布尔值
      messages.value = res.data.data.content.map(msg => ({
        ...msg,
        isRead: msg.isRead === true || msg.isRead === 1  // 确保 isRead 是布尔值，处理后端可能返回 1/0 的情况
      }))
      total.value = res.data.data.totalElements
      console.log('更新后的消息列表:', messages.value)
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
    ElMessage.error(error.message || '获取消息列表失败')
  } finally {
    loading.value = false
  }
}

// 标记全部已读
const markAllRead = async () => {
  try {
    const res = await markAllAsRead()
    if (res.data.code === 200) {
      // 直接更新本地消息状态，避免重新请求
      messages.value = messages.value.map(msg => ({
        ...msg,
        isRead: true
      }))
      ElMessage.success('已全部标记为已读')
    } else {
      throw new Error(res.data.message || '操作失败')
    }
  } catch (error) {
    console.error('标记全部已读失败:', error)
    ElMessage.error(error.message || '标记全部已读失败')
  }
}

// 标记单条消息已读
const markRead = async (messageId) => {
  try {
    console.log('开始标记消息已读:', messageId)
    const res = await markAsRead(messageId)
    console.log('标记已读响应:', res)
    
    if (res.data.code === 200) {
      // 直接更新本地消息状态，避免重新请求
      messages.value = messages.value.map(msg => {
        if (msg.id === messageId) {
          return { ...msg, isRead: true }
        }
        return msg
      })
      ElMessage.success('已标记为已读')
    } else {
      throw new Error(res.data.message || '操作失败')
    }
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error(error.message || '标记已读失败')
  }
}

// 获取消息类型标签样式
const getMessageTagType = (type) => {
  switch (type) {
    case 'ORDER':
      return 'danger'
    case 'SYSTEM':
      return 'info'
    case 'ORDER_STATUS':
      return 'danger'
    case 'PROMOTION':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取消息类型标签文本
const getMessageTypeLabel = (type) => {
  switch (type) {
    case 'ORDER':
      return '订单消息'
    case 'SYSTEM':
      return '系统消息'
    case 'ORDER_STATUS':
      return '订单状态'
    case 'PROMOTION':
      return '促销活动'
    default:
      return '其他消息'
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  getMessageList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  getMessageList()
}

// 监听消息列表变化
watch(messages, (newMessages) => {
  console.log('消息列表发生变化:', newMessages)
}, { deep: true })

onMounted(() => {
  getMessageList()
})
</script>

<style scoped lang="scss">
.messages-container {
  padding: 20px;
  
  .message-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .message-list {
      min-height: 200px;
      
      .message-item {
        padding: 15px;
        border-bottom: 1px solid #eee;
        transition: all 0.3s ease;
        background-color: #fff;
        
        &:last-child {
          border-bottom: none;
        }
        
        &:hover {
          background-color: #f5f7fa;
        }
        
        &.is-unread {
          background-color: #fff;
          border-left: 3px solid #f56c6c;
          
          &:hover {
            background-color: #f5f7fa;
          }
        }
        
        .message-content {
          .message-title {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 8px;
            
            .unread-dot {
              width: 8px;
              height: 8px;
              border-radius: 50%;
              background-color: #f56c6c;
              box-shadow: 0 0 0 3px rgba(245, 108, 108, 0.2);
              animation: pulse 2s infinite;
            }
          }
          
          .message-text {
            color: #606266;
            margin: 0;
            line-height: 1.6;
            font-size: 14px;
            
            &.unread-text {
              color: #303133;
              font-weight: 500;
            }
          }
          
          .message-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 12px;
            
            .time {
              color: #909399;
              font-size: 13px;
            }
            
            .actions {
              .el-button {
                padding: 4px 8px;
                
                &:hover {
                  background-color: rgba(245, 108, 108, 0.1);
                }
              }
            }
          }
        }
      }
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(245, 108, 108, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0);
  }
}
</style>