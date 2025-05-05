<template>
  <div class="app-container">
    <!-- 时间范围选择 -->
    <el-card class="filter-container">
      <el-form :inline="true">
        <el-form-item label="统计时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleDateChange"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据概览卡片 -->
    <el-row :gutter="20" class="data-overview">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>销售额</span>
              <el-tag type="success">{{ formatPrice(overview.totalSales) }}</el-tag>
            </div>
          </template>
          <div class="card-body">
            <div class="amount">{{ formatPrice(overview.todaySales) }}</div>
            <div class="label">今日销售额</div>
            <div class="compare">
              较昨日
              <span :class="overview.salesGrowth >= 0 ? 'up' : 'down'">
                {{ Math.abs(overview.salesGrowth) }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>订单数</span>
              <el-tag type="warning">{{ overview.totalOrders }}</el-tag>
            </div>
          </template>
          <div class="card-body">
            <div class="amount">{{ overview.todayOrders }}</div>
            <div class="label">今日订单数</div>
            <div class="compare">
              较昨日
              <span :class="overview.orderGrowth >= 0 ? 'up' : 'down'">
                {{ Math.abs(overview.orderGrowth) }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户数</span>
              <el-tag type="info">{{ overview.totalUsers }}</el-tag>
            </div>
          </template>
          <div class="card-body">
            <div class="amount">{{ overview.newUsers }}</div>
            <div class="label">今日新增</div>
            <div class="compare">
              较昨日
              <span :class="overview.userGrowth >= 0 ? 'up' : 'down'">
                {{ Math.abs(overview.userGrowth) }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>转化率</span>
              <el-tag type="primary">{{ overview.conversionRate }}%</el-tag>
            </div>
          </template>
          <div class="card-body">
            <div class="amount">{{ overview.todayConversionRate }}%</div>
            <div class="label">今日转化率</div>
            <div class="compare">
              较昨日
              <span :class="overview.conversionGrowth >= 0 ? 'up' : 'down'">
                {{ Math.abs(overview.conversionGrowth) }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 销售趋势图 -->
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>销��趋势</span>
              <el-radio-group v-model="salesTimeRange" size="small" @change="getSalesData">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="salesChartOption" autoresize />
          </div>
        </el-card>
      </el-col>

      <!-- 商品销量排行 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热销商品</span>
              <el-radio-group v-model="productTimeRange" size="small" @change="getProductData">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="top-products">
            <div 
              v-for="(item, index) in topProducts" 
              :key="item.id" 
              class="product-item"
            >
              <div :class="['rank', index < 3 ? 'top3' : '']">{{ index + 1 }}</div>
              <el-image :src="item.image" fit="cover" />
              <div class="info">
                <div class="name">{{ item.name }}</div>
                <div class="sales">销量: {{ item.sales }}</div>
              </div>
              <div class="amount">¥{{ item.amount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类统计 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>分类销售占比</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="categoryChartOption" autoresize />
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户地域分布</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="regionChartOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import request from '@/utils/request'

// 注册 ECharts 必须的组件
use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
])

// 数据定义
const dateRange = ref([])
const salesTimeRange = ref('week')
const productTimeRange = ref('week')
const overview = ref({})
const topProducts = ref([])

// 销售趋势图配置
const salesChartOption = reactive({
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['销售额', '订单数']
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: [
    {
      type: 'value',
      name: '销售额',
      axisLabel: {
        formatter: '{value} 元'
      }
    },
    {
      type: 'value',
      name: '订单数',
      axisLabel: {
        formatter: '{value}'
      }
    }
  ],
  series: [
    {
      name: '销售额',
      type: 'line',
      data: []
    },
    {
      name: '订单数',
      type: 'line',
      yAxisIndex: 1,
      data: []
    }
  ]
})

// 分类销售占比图配置
const categoryChartOption = reactive({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      type: 'pie',
      radius: '50%',
      data: [],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
})

// 地域分布图配置
const regionChartOption = reactive({
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
    type: 'value'
  },
  yAxis: {
    type: 'category',
    data: []
  },
  series: [
    {
      type: 'bar',
      data: []
    }
  ]
})

// 获取概览数据
const getOverview = async () => {
  try {
    const { data } = await request.get('/api/admin/statistics/overview')
    overview.value = data
  } catch (error) {
    console.error('获取概览数据失败:', error)
  }
}

// 获取销售趋势数据
const getSalesData = async () => {
  try {
    const { data } = await request.get('/api/admin/statistics/sales', {
      params: {
        type: salesTimeRange.value
      }
    })
    salesChartOption.xAxis.data = data.dates
    salesChartOption.series[0].data = data.sales
    salesChartOption.series[1].data = data.orders
  } catch (error) {
    console.error('获取销售趋势数据失败:', error)
  }
}

// 获取热销商品数据
const getProductData = async () => {
  try {
    const { data } = await request.get('/api/admin/statistics/products', {
      params: {
        type: productTimeRange.value
      }
    })
    topProducts.value = data
  } catch (error) {
    console.error('获取热销商品数据失败:', error)
  }
}

// 获取分类统计数据
const getCategoryData = async () => {
  try {
    const { data } = await request.get('/api/admin/statistics/categories')
    categoryChartOption.series[0].data = data.map(item => ({
      name: item.name,
      value: item.sales
    }))
  } catch (error) {
    console.error('获取分类统计数据失败:', error)
  }
}

// 获取地域分布数据
const getRegionData = async () => {
  try {
    const { data } = await request.get('/api/admin/statistics/regions')
    regionChartOption.yAxis.data = data.map(item => item.region)
    regionChartOption.series[0].data = data.map(item => item.count)
  } catch (error) {
    console.error('获取地域分布数据失败:', error)
  }
}

// 日期变化处理
const handleDateChange = () => {
  getSalesData()
  getProductData()
  getCategoryData()
  getRegionData()
}

// 格式化金额
const formatPrice = (price) => {
  return `¥${price?.toFixed(2)}`
}

onMounted(() => {
  getOverview()
  getSalesData()
  getProductData()
  getCategoryData()
  getRegionData()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  
  .filter-container {
    margin-bottom: 20px;
  }
  
  .data-overview {
    margin-bottom: 20px;
    
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
      
      .label {
        color: #666;
        font-size: 14px;
        margin-bottom: 5px;
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
    }
  }
  
  .chart-row {
    margin-bottom: 20px;
    
    .chart-card {
      height: 400px;
      
      .chart-container {
        height: 350px;
      }
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }
  
  .top-products {
    .product-item {
      display: flex;
      align-items: center;
      padding: 10px;
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
        margin-right: 10px;
        
        &.top3 {
          color: white;
          background: #f56c6c;
        }
      }
      
      .el-image {
        width: 50px;
        height: 50px;
        margin-right: 10px;
      }
      
      .info {
        flex: 1;
        
        .name {
          font-size: 14px;
          margin-bottom: 5px;
        }
        
        .sales {
          color: #666;
          font-size: 12px;
        }
      }
      
      .amount {
        color: #f56c6c;
        font-weight: bold;
      }
    }
  }
}
</style> 