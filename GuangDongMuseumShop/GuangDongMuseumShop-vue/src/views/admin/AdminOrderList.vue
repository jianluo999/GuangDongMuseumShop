<template>
  <div class="orders-manage">
    <!-- 搜索和筛选 -->
    <div class="action-bar">
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="订单号/用户名"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="status" placeholder="订单状态" clearable>
          <el-option label="待付款" value="PENDING" />
          <el-option label="待发货" value="PAID" />
          <el-option label="待收货" value="SHIPPED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
        <el-button type="primary" @click="handleSearch">
          搜索
        </el-button>
      </div>
      <div class="action-buttons">
        <el-button @click="handleExport">
          导出订单
        </el-button>
      </div>
    </div>

    <!-- 订单列表 -->
    <el-table
      v-loading="loading"
      :data="orders"
      stripe
      border
      style="width: 100%"
    >
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column label="商品信息" min-width="300">
        <template #default="{ row }">
          <div class="product-list">
            <div 
              v-for="item in row.items" 
              :key="item.id"
              class="product-item"
            >
              <el-image
                :src="item.productImage"
                :preview-src-list="[item.productImage]"
                fit="cover"
                class="product-image"
              />
              <div class="product-info">
                <div class="name">{{ item.productName }}</div>
                <div class="price-qty">
                  ¥{{ item.price }} x {{ item.quantity }}
                </div>
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总金额" width="120">
        <template #default="{ row }">
          ¥{{ row.totalAmount.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleViewDetail(row)">
            查看
          </el-button>
          <el-button 
            v-if="row.status === 'PAID'"
            type="success" 
            link
            @click="handleShip(row)"
          >
            发货
          </el-button>
          <el-button 
            v-if="row.status === 'PENDING'"
            type="danger" 
            link
            @click="handleCancel(row)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 发货对话框 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="订单发货"
      width="500px"
    >
      <el-form
        ref="shipFormRef"
        :model="shipForm"
        :rules="shipRules"
        label-width="100px"
      >
        <el-form-item label="物流公司" prop="company">
          <el-select v-model="shipForm.company" placeholder="请选择物流公司">
            <el-option label="顺丰速运" value="SF" />
            <el-option label="中通快递" value="ZTO" />
            <el-option label="圆通速递" value="YTO" />
            <el-option label="韵达快递" value="YD" />
            <el-option label="申通快递" value="STO" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号" prop="trackingNo">
          <el-input v-model="shipForm.trackingNo" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting"
          @click="handleSubmitShip"
        >
          确定发货
        </el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="800px"
    >
      <div v-if="currentOrder" class="order-detail">
        <div class="detail-section">
          <h3>订单信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">订单编号：</span>
              <span>{{ currentOrder.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单状态：</span>
              <el-tag :type="getStatusType(currentOrder.status)">
                {{ getStatusText(currentOrder.status) }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span>{{ formatDate(currentOrder.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付时间：</span>
              <span>{{ currentOrder.payTime ? formatDate(currentOrder.payTime) : '未支付' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3>收货信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">收货人：</span>
              <span>{{ currentOrder.receiver }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系电话：</span>
              <span>{{ currentOrder.phone }}</span>
            </div>
            <div class="info-item full">
              <span class="label">收货地址：</span>
              <span>{{ currentOrder.address }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3>商品信息</h3>
          <el-table :data="currentOrder.items" border>
            <el-table-column label="商品图片" width="100">
              <template #default="{ row }">
                <el-image
                  :src="row.productImage"
                  :preview-src-list="[row.productImage]"
                  fit="cover"
                  class="product-image"
                />
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="商品名称" min-width="200" />
            <el-table-column prop="price" label="单价" width="120">
              <template #default="{ row }">
                ¥{{ row.price.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column label="小计" width="120">
              <template #default="{ row }">
                ¥{{ (row.price * row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="amount-info">
            <div class="amount-item">
              <span>商品总额：</span>
              <span>¥{{ currentOrder.totalAmount.toFixed(2) }}</span>
            </div>
            <div class="amount-item">
              <span>运费：</span>
              <span>¥{{ currentOrder.shippingFee.toFixed(2) }}</span>
            </div>
            <div class="amount-item total">
              <span>实付金额：</span>
              <span class="price">¥{{ (currentOrder.totalAmount + currentOrder.shippingFee).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div v-if="currentOrder.remark" class="detail-section">
          <h3>订单备注</h3>
          <p class="remark">{{ currentOrder.remark }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import axios from '@/utils/axios'

// 状态
const loading = ref(false)
const submitting = ref(false)
const shipDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentOrder = ref(null)
const shipFormRef = ref(null)

// 搜索条件
const searchQuery = ref('')
const status = ref('')
const dateRange = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 数据
const orders = ref([])

// 发货表单
const shipForm = ref({
  company: '',
  trackingNo: ''
})

const shipRules = {
  company: [
    { required: true, message: '请选择物流公司', trigger: 'change' }
  ],
  trackingNo: [
    { required: true, message: '请输入物流单号', trigger: 'blur' }
  ]
}

// 状态映射
const statusMap = {
  PENDING: { text: '待付款', type: 'warning' },
  PAID: { text: '待发货', type: 'info' },
  SHIPPED: { text: '待收货', type: 'primary' },
  COMPLETED: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'danger' }
}

const getStatusText = (status) => statusMap[status]?.text || status
const getStatusType = (status) => statusMap[status]?.type || ''

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchQuery.value || undefined,
      status: status.value || undefined,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    }
    const { data } = await axios.get('/api/admin/orders', { params })
    orders.value = data.content
    total.value = data.totalElements
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

// 分页处理
const handleSizeChange = () => {
  currentPage.value = 1
  fetchOrders()
}

const handleCurrentChange = () => {
  fetchOrders()
}

// 查看详情
const handleViewDetail = (order) => {
  currentOrder.value = order
  detailDialogVisible.value = true
}

// 发货处理
const handleShip = (order) => {
  currentOrder.value = order
  shipForm.value = {
    company: '',
    trackingNo: ''
  }
  shipDialogVisible.value = true
}

const handleSubmitShip = async () => {
  if (!shipFormRef.value) return
  
  try {
    await shipFormRef.value.validate()
    submitting.value = true
    
    await axios.put(`/api/admin/orders/${currentOrder.value.id}/ship`, shipForm.value)
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    fetchOrders()
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.error('发货失败')
  } finally {
    submitting.value = false
  }
}

// 取消订单
const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      type: 'warning'
    })
    
    await axios.post(`/api/admin/orders/${order.id}/cancel`)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 导出订单
const handleExport = () => {
  const params = {
    keyword: searchQuery.value || undefined,
    status: status.value || undefined,
    startDate: dateRange.value?.[0],
    endDate: dateRange.value?.[1]
  }
  
  window.open(`/api/admin/orders/export?${new URLSearchParams(params)}`)
}

// 格式化日期
const formatDate = (date) => {
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
.orders-manage {
  .action-bar {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .search-bar {
      display: flex;
      gap: 10px;

      .el-input {
        width: 200px;
      }
    }
  }

  .product-list {
    .product-item {
      display: flex;
      align-items: center;
      padding: 5px 0;

      &:not(:last-child) {
        border-bottom: 1px solid #eee;
      }

      .product-image {
        width: 50px;
        height: 50px;
        border-radius: 4px;
        margin-right: 10px;
      }

      .product-info {
        .name {
          font-size: 14px;
          margin-bottom: 5px;
        }

        .price-qty {
          color: #666;
          font-size: 12px;
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

.order-detail {
  .detail-section {
    margin-bottom: 30px;

    h3 {
      font-size: 16px;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 1px solid #eee;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 15px;

      .info-item {
        &.full {
          grid-column: span 2;
        }

        .label {
          color: #666;
          margin-right: 10px;
        }
      }
    }

    .amount-info {
      margin-top: 20px;
      text-align: right;

      .amount-item {
        margin-bottom: 10px;

        &.total {
          margin-top: 10px;
          padding-top: 10px;
          border-top: 1px solid #eee;
          font-size: 16px;

          .price {
            color: var(--primary-color);
            font-size: 20px;
            font-weight: bold;
          }
        }
      }
    }

    .remark {
      color: #666;
      line-height: 1.6;
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .orders-manage {
    .action-bar {
      flex-direction: column;
      gap: 10px;

      .search-bar {
        flex-wrap: wrap;

        .el-input,
        .el-select,
        .el-date-picker {
          width: 100%;
        }
      }
    }
  }

  .order-detail {
    .info-grid {
      grid-template-columns: 1fr !important;

      .info-item.full {
        grid-column: auto !important;
      }
    }
  }
}
</style> 