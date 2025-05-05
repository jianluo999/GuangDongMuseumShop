<template>
  <div class="dashboard">
    <!-- 数据概览卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日订单</span>
              <el-tag type="success">{{ stats.todayOrders }}</el-tag>
            </div>
          </template>
          <div class="card-body">
            <div class="amount">¥{{ stats.todayAmount }}</div>
            <div class="compare">
              较昨日
              <span :class="stats.orderGrowth >= 0 ? 'up' : 'down'">
                {{ Math.abs(stats.orderGrowth) }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日销售额</span>
              <el-tag type="warning">¥{{ stats.todaySales }}</el-tag>
            </div>
          </template>
          <div class="card-body">
            <div class="amount">{{ stats.totalUsers }}</div>
            <div class="compare">
              较昨日
              <span :class="stats.salesGrowth >= 0 ? 'up' : 'down'">
                {{ Math.abs(stats.salesGrowth) }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>商品总数</span>
              <el-tag type="info">{{ stats.totalProducts }}</el-tag>
            </div>
          </template>
          <div class="card-body">
            <div class="amount">{{ stats.activeProducts }}</div>
            <div class="label">在售商品</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户总数</span>
              <el-tag type="primary">{{ stats.totalUsers }}</el-tag>
            </div>
          </template>
          <div class="card-body">
            <div class="amount">{{ stats.newUsers }}</div>
            <div class="label">今日新增</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <div class="card-header">
            <span>销售趋势</span>
            <div class="date-range">
              <el-radio-group v-model="dateRange" @change="fetchSalesTrend">
                <el-radio :label="7">近7天</el-radio>
                <el-radio :label="30">近30天</el-radio>
              </el-radio-group>
            </div>
          </div>
          <div class="charts-row" v-loading="loading">
            <div ref="salesChart" style="width: 100%; height: 300px"></div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热销商品</span>
              <div class="date-range">
                <el-radio-group v-model="productTimeRange" @change="fetchTopProducts">
                  <el-radio :label="7">近7天</el-radio>
                  <el-radio :label="30">近30天</el-radio>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div class="top-products">
            <div v-for="(product, index) in topProducts" :key="product.id" class="product-item">
              <span class="rank" :class="{ top3: index < 3 }">{{ index + 1 }}</span>
              <div class="info">
                <div class="name">{{ product.name }}</div>
                <div class="sales">
                  <span>销量 {{ product.quantity }}</span>
                  <span class="amount">¥{{ product.totalAmount.toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新订单 -->
    <el-card class="order-card">
      <template #header>
        <div class="card-header">
          <span>最新订单</span>
          <el-button link @click="$router.push('/admin/orders')">查看全部</el-button>
        </div>
      </template>
      <el-table 
        :data="recentOrders" 
        stripe 
        style="width: 100%"
        :header-cell-style="{
          background: '#f5f7fa',
          color: '#606266',
          fontWeight: 500
        }"
      >
        <el-table-column 
          label="订单号" 
          prop="orderNumber" 
          width="200" 
          show-overflow-tooltip
        />
        <el-table-column 
          label="用户" 
          prop="username" 
          width="100"
          align="center"
        />
        <el-table-column 
          label="商品" 
          min-width="200"
        >
          <template #default="{ row }">
            <div v-for="item in row.orderItems" :key="item.id" class="order-items">
              <span class="product-name">{{ item.productName }}</span>
              <span class="product-quantity">× {{ item.quantity }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column 
          label="金额" 
          width="100" 
          align="right"
        >
          <template #default="{ row }">
            <span class="amount">¥{{ Number(row.totalAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column 
          label="状态" 
          width="100" 
          align="center"
        >
          <template #default="{ row }">
            <el-tag 
              :type="statusMap[row.status]?.type"
              size="small"
            >
              {{ statusMap[row.status]?.text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          label="下单时间" 
          prop="createdAt" 
          width="180" 
          show-overflow-tooltip
          align="center"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getSalesTrend } from '@/api/admin'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

// 数据统计
const stats = ref({
  todayOrders: 0,
  todayAmount: 0,
  orderGrowth: 0,
  todaySales: 0,
  salesGrowth: 0,
  totalProducts: 0,
  activeProducts: 0,
  totalUsers: 0,
  newUsers: 0
})

const dateRange = ref(7)
const productTimeRange = ref(7)
const loading = ref(false)
const salesChart = ref(null)
let chart = null

const initChart = () => {
  console.log('开始初始化图表')
  try {
    if (chart) {
      console.log('销毁旧的图表实例')
      chart.dispose()
    }
    
    if (!salesChart.value) {
      console.error('图表DOM元素不存在')
      return
    }
    
    chart = echarts.init(salesChart.value)
    console.log('图表初始化成功')
  } catch (error) {
    console.error('图表初始化失败:', error)
  }
}

const renderChart = (data) => {
  console.log('准备渲染图表，数据:', data)
  if (!data || !Array.isArray(data)) {
    console.error('图表数据格式错误:', data)
    return
  }
  
  const xAxisData = data.map(item => item.date)
  const seriesData = data.map(item => item.orderCount)
  
  console.log('处理后的数据:', {
    xAxis: xAxisData,
    series: seriesData
  })

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '订单数',
        type: 'line',
        data: seriesData,
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }
  
  try {
    if (!chart) {
      console.error('图表实例不存在')
      return
    }
    chart.setOption(option)
    console.log('图表渲染成功')
  } catch (error) {
    console.error('图表渲染失败:', error)
  }
}

const fetchSalesTrend = async () => {
  loading.value = true
  console.log('开始获取销售趋势数据, 日期范围:', dateRange.value)
  
  try {
    const response = await getSalesTrend(dateRange.value)
    console.log('获取到的原始数据:', response)
    
    // 确保数据格式正确
    const data = Array.isArray(response) ? response : response.data
    console.log('处理后的数据:', data)
    
    if (!data || !Array.isArray(data)) {
      throw new Error('数据格式错误')
    }
    
    renderChart(data)
  } catch (error) {
    console.error('获取或处理销售趋势数据失败:', error)
    ElMessage.error('获取销售趋势数据失败')
  } finally {
    loading.value = false
  }
}

// 热销商品
const topProducts = ref([])

// 最新订单
const recentOrders = ref([])

// 状态映射
const statusMap = {
  PENDING: { type: 'warning', text: '待付款' },
  PAID: { type: 'info', text: '待发货' },
  SHIPPED: { type: 'primary', text: '已发货' },
  COMPLETED: { type: 'success', text: '已完成' },
  CANCELLED: { type: 'danger', text: '已取消' }
}

// 格式化日期
const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const response = await request.get('/api/admin/stats/overview')
    // 确保数据格式正确
    stats.value = {
      todayOrders: response.data.todayOrders || 0,
      todayAmount: Number(response.data.todayAmount || 0).toFixed(2),
      orderGrowth: Number(response.data.orderGrowth || 0).toFixed(2),
      todaySales: Number(response.data.todaySales || 0).toFixed(2),
      salesGrowth: Number(response.data.salesGrowth || 0).toFixed(2),
      totalProducts: response.data.totalProducts || 0,
      activeProducts: response.data.activeProducts || 0,
      totalUsers: response.data.totalUsers || 0,
      newUsers: response.data.newUsers || 0
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 获取热销商品
const fetchTopProducts = async () => {
  try {
    const { data } = await request.get('/api/admin/stats/products/top', {
      params: { range: productTimeRange.value }
    })
    console.log('获取到的热销商品数据:', data)
    
    // 确保数据是数组并处理数据格式
    topProducts.value = Array.isArray(data) ? data.map(item => ({
      id: item.id,
      name: item.name,
      quantity: item.quantity || 0,
      totalAmount: Number(item.totalAmount) || 0
    })) : []
    
    console.log('处理后的热销商品数据:', topProducts.value)
  } catch (error) {
    console.error('获取热销商品失败:', error)
    ElMessage.error('获取热销商品失败')
  }
}

// 获取最新订单
const fetchRecentOrders = async () => {
  try {
    const { data } = await request.get('/api/admin/orders/latest')
    if (data.code === 200) {
      recentOrders.value = data.data || []
    } else {
      ElMessage.error(data.message || '获取最新订单失败')
    }
  } catch (error) {
    console.error('获取最新订单失败:', error)
    ElMessage.error('获取最新订单失败')
  }
}

onMounted(() => {
  console.log('组件挂载，开始初始化')
  fetchStats()
  initChart()
  fetchSalesTrend()
  fetchTopProducts()
  fetchRecentOrders()
  
  window.addEventListener('resize', () => {
    if (chart) {
      console.log('窗口大小改变，重置图表大小')
      chart.resize()
    }
  })
})

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', chart?.resize)
  chart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 20px;
  
  .el-row {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .card-body {
    text-align: center;
    
    .amount {
      font-size: 24px;
      font-weight: bold;
      margin: 10px 0;
    }
    
    .compare {
      color: #666;
      font-size: 14px;
      
      .up {
        color: #67c23a;
      }
      
      .down {
        color: #f56c6c;
      }
    }
    
    .label {
      color: #666;
      font-size: 14px;
    }
  }
  
  .charts-row {
    .chart-card {
      height: 400px;
      
      .chart-container {
        height: 300px;
      }
    }
  }
  
  .top-products {
    .product-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border-bottom: 1px solid #eee;
      
      &:last-child {
        border-bottom: none;
      }
      
      .rank {
        width: 24px;
        height: 24px;
        line-height: 24px;
        text-align: center;
        background: #f2f2f2;
        border-radius: 4px;
        margin-right: 12px;
        font-size: 14px;
        
        &.top3 {
          color: #fff;
          &:nth-child(1) { background-color: #f56c6c; }
          &:nth-child(2) { background-color: #e6a23c; }
          &:nth-child(3) { background-color: #409eff; }
        }
      }
      
      .info {
        flex: 1;
        
        .name {
          font-size: 14px;
          margin-bottom: 4px;
          color: #303133;
        }
        
        .sales {
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-size: 12px;
          color: #909399;
          
          .amount {
            color: #f56c6c;
            font-weight: bold;
          }
        }
      }
    }
  }
}

.order-card {
  .order-items {
    display: flex;
    align-items: center;
    padding: 4px 0;
    
    &:not(:last-child) {
      border-bottom: 1px dashed #ebeef5;
    }
    
    .product-name {
      flex: 1;
      color: #303133;
      font-size: 14px;
    }
    
    .product-quantity {
      margin-left: 8px;
      color: #909399;
      font-size: 13px;
    }
  }
  
  .amount {
    color: #f56c6c;
    font-weight: 500;
  }
}
</style> 