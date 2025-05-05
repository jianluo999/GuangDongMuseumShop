<template>
  <div class="order-detail">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回订单列表
      </el-button>
      <h2>订单详情</h2>
    </div>

    <div class="detail-content" v-loading="loading">
      <!-- 订单基本信息 -->
      <el-card class="detail-section">
        <template #header>
          <div class="card-header">
            <span>订单信息</span>
            <span class="order-status">{{ getStatusText(order.status) }}</span>
          </div>
        </template>
        <div class="order-info">
          <p>订单编号：{{ order.orderNo }}</p>
          <p>创建时间：{{ formatDate(order.createdAt) }}</p>
          <p>支付方式：{{ order.paymentMethod || '未支付' }}</p>
        </div>
      </el-card>

      <!-- 收货信息 -->
      <el-card class="detail-section">
        <template #header>
          <div class="card-header">收货信息</div>
        </template>
        <div class="shipping-info">
          <p>收货人：{{ order.receiver }}</p>
          <p>联系电话：{{ order.phone }}</p>
          <p>收货地址：{{ order.address }}</p>
        </div>
      </el-card>

      <!-- 商品信息 -->
      <el-card class="detail-section">
        <template #header>
          <div class="card-header">商品信息</div>
        </template>
        <div class="product-list">
          <div v-for="item in order.items" :key="item.id" class="product-item">
            <img :src="item.productImage" :alt="item.productName">
            <div class="product-info">
              <h4>{{ item.productName }}</h4>
              <p class="price">¥{{ item.price }} × {{ item.quantity }}</p>
            </div>
            <div class="item-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
        </div>
        <div class="order-total">
          <span>订单总计：</span>
          <span class="total-price">¥{{ order.totalAmount }}</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getOrderDetail } from '@/api/order'
import { formatDate, getStatusText } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref({})

const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const { data } = await getOrderDetail(route.params.orderNo)
    order.value = data
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* ... 添加其他样式 ... */
</style> 