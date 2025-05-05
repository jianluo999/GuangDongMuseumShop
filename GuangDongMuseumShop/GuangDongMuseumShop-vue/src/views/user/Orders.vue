<template>
  <div class="orders-page">
    <h2 class="page-title">我的订单</h2>

    <!-- 订单状态标签 -->
    <div class="order-tabs">
      <div 
        v-for="tab in orderTabs" 
        :key="tab.value"
        :class="['tab-item', { active: activeStatus === tab.value }]"
        @click="handleTabChange(tab.value)"
      >
        {{ tab.label }}
      </div>
    </div>

    <div class="order-list" v-loading="loading">
      <!-- 单个订单 -->
      <div v-for="order in orders" :key="order.id" class="order-card">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-info">
            <span class="order-number">订单号：{{ order.orderNumber }}</span>
            <span class="order-time">{{ formatDate(order.createdAt) }}</span>
          </div>
          <div class="order-status-tag" :class="order.status.toLowerCase()">
            {{ getStatusText(order.status) }}
          </div>
        </div>

        <!-- 订单内容 -->
        <div class="order-content">
          <!-- 商品列表 -->
          <div class="order-items">
            <div v-for="item in order.orderItems" :key="item.id" class="order-item">
              <div class="item-main">
                <div class="product-info">
                  <h4 class="product-name">{{ item.productName }}</h4>
                  <div class="product-meta">
                    <span class="product-price">¥{{ item.productPrice }}</span>
                    <span class="quantity">× {{ item.quantity }}</span>
                    <span class="subtotal">小计：¥{{ (item.productPrice * item.quantity).toFixed(2) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 收货信息 -->
          <div class="shipping-info">
            <div class="info-item">
              <span class="label">收货人：</span>
              <span class="value">{{ order.shippingName }}</span>
            </div>
            <div class="info-item">
              <span class="label">电话：</span>
              <span class="value">{{ order.shippingPhone }}</span>
            </div>
            <div class="info-item">
              <span class="label">地址：</span>
              <span class="value">{{ order.shippingAddress }}</span>
            </div>
          </div>

          <div class="order-actions">
            <el-button
              v-if="order.status === 'COMPLETED'"
              type="warning"
              size="large"
              class="review-button"
              @click="handleReview(order)"
            >
              去评价
            </el-button>
          </div>
        </div>

        <!-- 订单底部 -->
        <div class="order-footer">
          <div class="order-total">
            <span class="label">订单总计：</span>
            <span class="price">¥{{ order.totalAmount }}</span>
          </div>
          <div class="order-actions">
            <el-button
              v-if="order.status === 'PENDING'"
              type="primary"
              size="large"
              class="pay-button"
              @click="handlePayment(order)"
            >
              立即支付
            </el-button>
            <el-button
              v-if="order.status === 'SHIPPED'"
              type="success"
              size="large"
              class="receive-button"
              @click="handleConfirmReceive(order.id)"
            >
              确认收货
            </el-button>
            <el-button
              v-if="order.status === 'PENDING'"
              link
              type="danger"
              size="small"
              class="cancel-button"
              @click="handleCancelOrder(order.id)"
            >
              取消订单
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { getOrders, cancelOrder } from '@/api/order'

const router = useRouter()
const loading = ref(false)
const activeStatus = ref('ALL')
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const orderTabs = [
  { label: '全部', value: 'ALL' },
  { label: '待付款', value: 'PENDING' },
  { label: '待发货', value: 'PAID' },
  { label: '待收货', value: 'SHIPPED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

// 处理标签切换
const handleTabChange = async (status) => {
  activeStatus.value = status
  await fetchOrders()
}

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      status: activeStatus.value === 'ALL' ? undefined : activeStatus.value
    }
    
    const { data } = await request.get('/api/orders/list', { params })
    
    if (data.code === 200) {
      orders.value = data.data.map(order => ({
        ...order,
        items: order.orderItems || order.items || []
      }))
      console.log('处理后的订单数据:', orders.value)
    } else {
      ElMessage.error(data.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待付款',
    'PAID': '待发货',
    'SHIPPED': '待收货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 处理状态切换
const handleStatusChange = () => {
  currentPage.value = 1
  fetchOrders()
}

// 处理分页
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchOrders()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchOrders()
}

// 立即支付
const handlePayment = (order) => {
  router.push({
    name: 'OrderPay',
    params: { orderId: order.id },
    query: { amount: order.totalAmount }
  })
}

const handleConfirmReceive = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认收到商品了吗？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const { data } = await request.post(`/api/orders/${orderId}/receive`)
    if (data.code === 200) {
      ElMessage.success('确认收货成功')
      fetchOrders()
    } else {
      ElMessage.error(data.message || '确认收货失败')
    }
  } catch (error) {
    if (error?.message !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '确认收货失败')
    }
  }
}

const handleReview = (order) => {
  // 获取订单中的第一个商品
  const firstItem = order.items[0]
  if (!firstItem) {
    ElMessage.error('未找到商品信息')
    return
  }

  // 跳转到商品详情页面并显示评价表单
  router.push({
    path: `/products/${firstItem.productId}`,
    query: {
      orderId: order.id,
      showReview: 'true'
    }
  })
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确认取消订单？', '取消订单')
    const { data } = await request.post(`/api/orders/${order.id}/cancel`)
    if (data.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrders()
    } else {
      ElMessage.error(data.message || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

const handleViewDetail = (order) => {
  router.push(`/orders/${order.id}`)
}

const handleBack = () => {
  router.back()
}

const handleCancelOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消该订单吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await cancelOrder(orderId)
    ElMessage.success('订单取消成功')
    // 刷新订单列表
    await fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消订单失败')
    }
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
.orders-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f7fa;
}

.page-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
  font-weight: bold;
}

.order-tabs {
  display: flex;
  gap: 20px;
  margin: 20px 0;
  background: #fff;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.tab-item {
  cursor: pointer;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  transition: all 0.3s ease;
  
  &:hover {
    background: #f0f2f5;
  }
}

.tab-item.active {
  color: #fff;
  font-weight: bold;
  background: #409eff;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  overflow: hidden;

  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: #f8f9fa;
    border-bottom: 1px solid #ebeef5;

    .order-info {
      .order-number {
        font-size: 14px;
        color: #606266;
        margin-right: 20px;
      }

      .order-time {
        font-size: 14px;
        color: #909399;
      }
    }

    .order-status-tag {
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 13px;
      
      &.pending {
        background: #fff0f0;
        color: #f56c6c;
      }
      
      &.paid {
        background: #f0f9eb;
        color: #67c23a;
      }
      
      &.shipped {
        background: #ecf5ff;
        color: #409eff;
      }

      &.completed {
        background: #f4f4f5;
        color: #909399;
      }

      &.cancelled {
        background: #f4f4f5;
        color: #909399;
      }
    }
  }

  .order-content {
    padding: 20px 24px;

    .order-items {
      .order-item {
        padding: 16px;
        border-bottom: 1px solid #ebeef5;
        transition: all 0.3s ease;

        &:last-child {
          border-bottom: none;
        }

        &:hover {
          background: #f5f7fa;
        }

        .item-main {
          .product-info {
            .product-name {
              font-size: 15px;
              color: #303133;
              margin-bottom: 8px;
              font-weight: 500;
            }

            .product-meta {
              display: flex;
              align-items: center;
              gap: 16px;

              .product-price {
                color: #f56c6c;
                font-size: 15px;
                font-weight: bold;
              }

              .quantity {
                color: #606266;
                background: #f0f2f5;
                padding: 2px 8px;
                border-radius: 12px;
              }

              .subtotal {
                color: #f56c6c;
                font-weight: bold;
                margin-left: auto;
              }
            }
          }
        }
      }
    }

    .shipping-info {
      margin-top: 20px;
      padding: 16px;
      background: #f8f9fa;
      border-radius: 8px;

      .info-item {
        line-height: 24px;
        display: flex;
        margin-bottom: 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .label {
          color: #909399;
          margin-right: 8px;
          min-width: 60px;
        }
        
        .value {
          color: #606266;
          flex: 1;
        }
      }
    }

    .order-actions {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }
  }

  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: #fff;
    border-top: 1px solid #ebeef5;

    .order-total {
      .label {
        font-size: 14px;
        color: #606266;
      }

      .price {
        font-size: 20px;
        color: #f56c6c;
        font-weight: bold;
        margin-left: 8px;
      }
    }

    .order-actions {
      display: flex;
      gap: 12px;
      align-items: center;

      .el-button {
        border-radius: 20px;
        padding: 10px 24px;
      }

      .pay-button {
        background: #409eff;
        border-color: #409eff;
        
        &:hover {
          background: #66b1ff;
          border-color: #66b1ff;
        }
      }

      .receive-button {
        background: #67c23a;
        border-color: #67c23a;
        
        &:hover {
          background: #85ce61;
          border-color: #85ce61;
        }
      }

      .review-button {
        background: #e6a23c;
        border-color: #e6a23c;
        
        &:hover {
          background: #ebb563;
          border-color: #ebb563;
        }
      }

      .cancel-button {
        color: #f56c6c;
        
        &:hover {
          color: #f78989;
        }
      }
    }
  }
}
</style>