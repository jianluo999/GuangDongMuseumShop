<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="filter-container">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-radio-group v-model="queryParams.status" class="status-group">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="PENDING">待付款</el-radio-button>
            <el-radio-button label="PAID">待发货</el-radio-button>
            <el-radio-button label="SHIPPED">已发货</el-radio-button>
            <el-radio-button label="COMPLETED">已完成</el-radio-button>
            <el-radio-button label="CANCELLED">已取消</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="dateShortcuts"
            :clearable="true"
            @change="handleDateRangeChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <el-card class="table-container">
      <el-table v-loading="loading" :data="orderList" border stripe>
        <el-table-column label="订单号" prop="orderNo" min-width="180" show-overflow-tooltip />
        <el-table-column label="用户名" prop="username" width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-avatar 
                :size="32" 
                :src="getAvatarUrl(row.username)"
                style="margin-right: 8px;"
              />
              {{ row.username }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="商品信息" min-width="300">
          <template #default="{ row }">
            <div v-for="item in row.items" :key="item.id" class="order-item">
              <el-image
                v-if="item.productImage"
                :src="item.productImage"
                style="width: 50px; height: 50px; margin-right: 10px;"
                fit="cover"
              />
              <div class="item-info">
                <div class="product-name">{{ item.productName }}</div>
                <div class="item-detail">
                  <span class="price">{{ formatPrice(item.price) }}</span>
                  <span class="quantity">× {{ item.quantity }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="总金额" width="120" align="right">
          <template #default="{ row }">
            <span class="total-amount">{{ formatPrice(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">
              <el-icon><Document /></el-icon>详情
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'"
              link 
              type="danger" 
              @click="handleCancel(row)"
            >
              <el-icon><Close /></el-icon>取消
            </el-button>
            <el-button
              v-if="row.status === 'PAID'"
              link
              type="success"
              @click="handleShip(row)"
            >
              <el-icon><Van /></el-icon>发货
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
          :layout="'total, sizes, prev, pager, next, jumper'"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
          <template #total>
            共 {{ total }} 条
          </template>
          <template #sizes="{ size }">
            <span style="margin-right: 4px">每页</span>
            <el-select
              v-model="queryParams.pageSize"
              :options="[10, 20, 50, 100].map(item => ({ value: item, label: item + ' 条' }))"
              @change="handleSizeChange"
            />
          </template>
          <template #jumper>
            前往 <el-input v-model="queryParams.pageNum" /> 页
          </template>
        </el-pagination>
      </div>
    </el-card>

    <!-- 订单详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="订单详情"
      size="60%"
      destroy-on-close
    >
      <template #default>
        <div class="detail-container" v-loading="detailLoading">
          <!-- 基本信息 -->
          <div class="detail-section">
            <div class="section-title">基本信息</div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="订单号">{{ currentOrder?.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="用户信息">
                <div style="display: flex; align-items: center;">
                  <el-avatar 
                    :size="32" 
                    :src="getAvatarUrl(currentOrder?.username)"
                    style="margin-right: 8px;"
                  />
                  {{ currentOrder?.username }}
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="下单时间">{{ formatDateTime(currentOrder?.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="订单状态">
                <el-tag :type="getStatusType(currentOrder?.status)">
                  {{ getStatusText(currentOrder?.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="支付时间">
                {{ currentOrder?.paymentTime ? formatDateTime(currentOrder.paymentTime) : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="实付金额" :span="2">
                <span class="price">¥{{ Number(currentOrder?.totalAmount || 0).toFixed(2) }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 收货信息 -->
          <div class="detail-section">
            <div class="section-title">收货信息</div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="收货人">{{ currentOrder?.recipientName || currentOrder?.shippingName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ currentOrder?.recipientPhone || currentOrder?.shippingPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="收货地址" :span="2">
                {{ currentOrder?.recipientAddress || currentOrder?.shippingAddress || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 商品信息 -->
          <div class="detail-section">
            <div class="section-title">商品信息</div>
            <el-table :data="currentOrder?.orderItems || []" border stripe>
              <el-table-column label="商品图片" width="100">
                <template #default="{ row }">
                  <el-image 
                    :src="getImageUrl(row.productImage || row.image)" 
                    :preview-src-list="[getImageUrl(row.productImage || row.image)]"
                    fit="cover"
                    style="width: 50px; height: 50px"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </template>
              </el-table-column>
              <el-table-column label="商品名称" prop="productName" min-width="200" show-overflow-tooltip />
              <el-table-column label="单价" width="120" align="right">
                <template #default="{ row }">¥{{ Number(row.productPrice || row.price || 0).toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="数量" prop="quantity" width="100" align="center" />
              <el-table-column label="小计" width="120" align="right">
                <template #default="{ row }">
                  ¥{{ (Number(row.productPrice || row.price || 0) * (row.quantity || 0)).toFixed(2) }}
                </template>
              </el-table-column>
            </el-table>

            <!-- 金额信息 -->
            <div class="amount-info">
              <div class="amount-item total">
                <span>实付金额：</span>
                <span class="price">¥{{ Number(currentOrder?.totalAmount || 0).toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <!-- 物流信息 -->
          <div class="detail-section" v-if="currentOrder?.status !== 'PENDING' && currentOrder?.status !== 'CANCELLED'">
            <div class="section-title">物流信息</div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="快递公司">
                {{ getShippingCompany(currentOrder?.shippingCompany) || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="快递单号">
                {{ currentOrder?.trackingNumber || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="发货时间" :span="2">
                {{ formatDateTime(currentOrder?.shipTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </template>
    </el-drawer>

    <!-- 发货对话框 -->
    <el-dialog
      title="订单发货"
      v-model="shipDialogVisible"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="shipFormRef"
        :model="shipForm"
        :rules="shipRules"
        label-width="100px"
      >
        <el-form-item label="快递公司" prop="company">
          <el-select v-model="shipForm.company" placeholder="请选择快递公司" style="width: 100%">
            <el-option
              v-for="item in shippingCompanies"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="trackingNo">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmitShip">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Close, Van, Picture, Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
dayjs.locale('zh-cn')

// 数据定义
const loading = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const detailDrawerVisible = ref(false)
const shipDialogVisible = ref(false)
const orderList = ref([])
const total = ref(0)
const currentOrder = ref(null)
const shipFormRef = ref()
const dateRange = ref([])

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  username: '',
  status: '',
  startTime: '',
  endTime: ''
})

// 发货表单
const shipForm = reactive({
  company: '',
  trackingNo: ''
})

// 发货表单校验规则
const shipRules = {
  company: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
  trackingNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }]
}

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 禁用未来日期
const disabledDate = (time) => {
  return time.getTime() > Date.now()
}

// 状态映射
const statusMap = {
  PENDING: { type: 'warning', text: '待付款' },
  PAID: { type: 'info', text: '待发货' },
  SHIPPED: { type: 'primary', text: '已发货' },
  COMPLETED: { type: 'success', text: '已完成' },
  CANCELLED: { type: 'danger', text: '已取消' }
}

// 支付方式映射
const paymentMethods = {
  ALIPAY: '支付宝',
  WECHAT: '微信支付',
  UNIONPAY: '银联支付'
}

// 快递公司选项
const shippingCompanies = [
  { label: '顺丰速运', value: 'SF' },
  { label: '中通快递', value: 'ZTO' },
  { label: '圆通速递', value: 'YTO' },
  { label: '韵达快递', value: 'YD' },
  { label: '申通快递', value: 'STO' }
]

// 获取状态类型
const getStatusType = (status) => {
  return statusMap[status]?.type || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  return statusMap[status]?.text || status
}

// 获取支付方式文本
const getPaymentMethod = (method) => {
  return paymentMethods[method] || '-'
}

// 获取快递公司名称
const getShippingCompany = (code) => {
  return shippingCompanies.find(item => item.value === code)?.label || code || '-'
}

// 格式化日期
const formatDateTime = (date) => {
  if (!date) return '-'
  try {
    const formattedDate = dayjs(date).format('YYYY-MM-DD HH:mm:ss')
    return formattedDate === 'Invalid Date' ? '-' : formattedDate
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '-'
  }
}

// 获取图片URL
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/')) return `${import.meta.env.VITE_API_URL}${url}`
  return `${import.meta.env.VITE_API_URL}/${url}`
}

// 获取默认头像URL
const getAvatarUrl = (username) => {
  // 使用 Dicebear API 生成头像
  return `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(username)}&backgroundColor=2196f3`;
}

// 处理日期范围变化
const handleDateRangeChange = (val) => {
  console.log('日期范围变化:', val)
  if (val && val.length === 2) {
    const startTime = dayjs(val[0]).format('YYYY-MM-DD 00:00:00')
    const endTime = dayjs(val[1]).format('YYYY-MM-DD 23:59:59')
    console.log('格式化后的日期:', { startTime, endTime })
    queryParams.startTime = startTime
    queryParams.endTime = endTime
  } else {
    queryParams.startTime = ''
    queryParams.endTime = ''
  }
  handleQuery()
}

// 获取订单列表
const getList = async () => {
  loading.value = true
  try {
    console.log('查询参数:', queryParams)
    // 处理日期范围
    let params = {
      orderNo: queryParams.orderNo || undefined,
      username: queryParams.username || undefined,
      status: queryParams.status || undefined,
      startTime: queryParams.startTime || undefined,
      endTime: queryParams.endTime || undefined,
      page: queryParams.pageNum - 1,
      size: queryParams.pageSize
    }

    console.log('发送请求参数:', params)
    const response = await request.get('/api/admin/orders', { params })
    console.log('订单列表响应:', response)
    
    if (response.data.code === 200) {
      orderList.value = response.data.data.content.map(order => ({
        ...order,
        orderNo: order.orderNumber || order.orderNo,
        username: order.user?.username || order.username || '未知用户',
        items: (order.orderItems || []).map(item => ({
          id: item.id,
          productName: item.productName,
          productImage: item.productImage,
          price: Number(item.productPrice || 0),
          quantity: item.quantity || 0,
          subtotal: Number(item.subtotal || 0)
        })),
        totalAmount: Number(order.totalAmount || 0),
        status: order.status || 'PENDING',
        createdAt: order.createdAt || order.createTime || order.createAt
      }))
      total.value = response.data.data.totalElements || 0
    } else {
      ElMessage.error(response.data.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 查看订单详情
const handleDetail = async (row) => {
  detailLoading.value = true
  try {
    const { data } = await request.get(`/api/admin/orders/${row.id}`)
    console.log('获取到的订单详情原始数据:', data.data)
    
    if (data.code === 200) {
      // 处理订单详情数据
      const orderData = data.data;
      console.log('开始处理物流信息:', {
        shippingCompany: orderData.shippingCompany,
        expressCompany: orderData.expressCompany,
        trackingNumber: orderData.trackingNumber,
        trackingNo: orderData.trackingNo,
        expressNo: orderData.expressNo,
        shipTime: orderData.shipTime,
        shippingTime: orderData.shippingTime,
        deliveryTime: orderData.deliveryTime
      });
      
      currentOrder.value = {
        ...orderData,
        id: orderData.orderNumber || orderData.id,
        orderNo: orderData.orderNumber || orderData.orderNo,
        recipientName: orderData.recipientName || orderData.shippingName,
        recipientPhone: orderData.recipientPhone || orderData.shippingPhone,
        recipientAddress: orderData.recipientAddress || orderData.shippingAddress,
        orderItems: (orderData.orderItems || []).map(item => ({
          ...item,
          productImage: getImageUrl(item.productImage || item.image),
          productPrice: item.productPrice || item.price,
          productName: item.productName || item.name,
          quantity: item.quantity || 0
        })),
        // 物流信息字段处理
        shippingCompany: getShippingCompany(orderData.shippingCompany || orderData.expressCompany),
        trackingNumber: orderData.trackingNumber || orderData.trackingNo || orderData.expressNo,
        shipTime: orderData.shipTime || orderData.shippingTime || orderData.deliveryTime,
        status: orderData.status || 'PENDING',
        totalAmount: Number(orderData.totalAmount || 0),
        paymentTime: orderData.paymentTime || orderData.payTime
      }
      console.log('处理后的订单详情:', currentOrder.value)
      detailDrawerVisible.value = true
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 发货处理
const handleShip = (row) => {
  currentOrder.value = row
  shipForm.company = ''
  shipForm.trackingNo = ''
  shipDialogVisible.value = true
}

// 提交发货
const handleSubmitShip = async () => {
  if (!currentOrder.value) return;
  
  try {
    submitting.value = true;
    await request.put(`/api/admin/orders/${currentOrder.value.id}/ship`, {
      shippingCompany: shipForm.company,
      trackingNo: shipForm.trackingNo
    });
    
    ElMessage.success('发货成功');
    shipDialogVisible.value = false;
    getList(); // 刷新列表
  } catch (error) {
    console.error('发货失败:', error);
    ElMessage.error('发货失败: ' + error.message);
  } finally {
    submitting.value = false;
  }
};

// 取消订单
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确认取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const { data } = await request.put(`/api/admin/orders/${row.id}/cancel`)
    if (data.code === 200) {
      ElMessage.success('订单取消成功')
      getList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  dateRange.value = []
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    orderNo: '',
    username: '',
    status: '',
    startTime: '',
    endTime: ''
  })
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

// 格式化金额
const formatPrice = (amount) => {
  return `¥${Number(amount || 0).toFixed(2)}`
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;

  .filter-container,
  .table-container {
    margin-bottom: 20px;
  }

  .status-group {
    .el-radio-button__inner {
      padding: 8px 15px;
    }
  }

  .order-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    
    &:not(:last-child) {
      border-bottom: 1px solid #f0f0f0;
    }

    .item-info {
      flex: 1;
      
      .product-name {
        font-size: 14px;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .item-detail {
        display: flex;
        align-items: center;
        font-size: 13px;
        
        .price {
          color: #ff4949;
          margin-right: 8px;
        }
        
        .quantity {
          color: #909399;
        }
      }
    }
  }

  .total-amount {
    color: #ff4949;
    font-weight: bold;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;

    :deep(.el-pagination) {
      .el-select .el-input {
        width: 100px;
      }

      .el-pagination__total {
        margin-right: 16px;
      }

      .el-pagination__sizes {
        margin-right: 16px;
      }

      .el-pagination__jump {
        margin-left: 16px;
        .el-pagination__editor {
          margin: 0 4px;
        }
        .el-input__inner {
          text-align: center;
        }
      }
    }
  }

  .detail-container {
    padding: 0 20px;

    .detail-section {
      margin-bottom: 24px;

      .section-title {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 16px;
        padding-left: 10px;
        border-left: 4px solid var(--el-color-primary);
      }
    }

    .amount-info {
      margin-top: 20px;
      padding: 16px;
      background-color: #f8f9fa;
      border-radius: 4px;

      .amount-item {
        display: flex;
        justify-content: flex-end;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }

        span:first-child {
          margin-right: 20px;
          color: #606266;
        }

        &.total {
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px dashed #dcdfe6;

          .price {
            color: #ff4949;
            font-size: 18px;
            font-weight: bold;
          }
        }
      }
    }
  }

  .image-error {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 50px;
    height: 50px;
    background-color: #f0f0f0;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
  }
}
</style> 