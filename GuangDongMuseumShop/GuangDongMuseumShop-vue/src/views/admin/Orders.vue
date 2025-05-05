<template>
  <div class="orders">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索订单号/收货人"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="status" placeholder="订单状态" clearable>
        <el-option
          v-for="option in statusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 订单列表 -->
    <el-table
      v-loading="loading"
      :data="orders"
      border
      style="width: 100%"
    >
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="receiver" label="收货人" width="120" />
      <el-table-column prop="phone" label="联系电话" width="120" />
      <el-table-column prop="address" label="收货地址" show-overflow-tooltip />
      <el-table-column prop="totalAmount" label="订单金额" width="120">
        <template #default="{ row }">
          ¥{{ row.totalAmount.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="订单状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button
              v-if="row.status === 'PAID'"
              type="primary"
              @click="handleShip(row)"
            >
              发货
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </el-button-group>
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
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const status = ref('')
const dateRange = ref([])

// 订单状态选项
const statusOptions = [
  { label: '全部', value: '' },
  { label: '待付款', value: 'PENDING' },
  { label: '待发货', value: 'PAID' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

// 订单状态映射
const statusMap = {
  PENDING: { label: '待付款', type: 'warning' },
  PAID: { label: '待发货', type: 'info' },
  SHIPPED: { label: '已发货', type: 'primary' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'danger' }
}

// 获取状态标签
const getStatusLabel = (status) => {
  return statusMap[status]?.label || status
}

// 获取状态类型
const getStatusType = (status) => {
  return statusMap[status]?.type || ''
}

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
    const { data } = await request.get('/api/admin/orders', { params })
    orders.value = data.content
    total.value = data.totalElements
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 发货处理
const handleShip = async (order) => {
  try {
    await request.put(`/api/admin/orders/${order.id}/ship`, {
      shippingName: order.shippingName,
      shippingPhone: order.shippingPhone,
      shippingAddress: order.shippingAddress
    })
    ElMessage.success('发货成功')
    fetchOrders()
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.error('发货失败')
  }
}

// 取消订单
const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      type: 'warning'
    })
    await request.post(`/api/admin/orders/${order.id}/cancel`)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 监听页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchOrders()
}

// 监听每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchOrders()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

// 重置搜索
const resetSearch = () => {
  searchQuery.value = ''
  status.value = ''
  dateRange.value = []
  currentPage.value = 1
  fetchOrders()
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.el-input {
  width: 200px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 