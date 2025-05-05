<template>
  <div class="payment-container">
    <el-dialog
      title="订单支付"
      v-model="paymentDialogVisible"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="payment-content">
        <p>确认支付订单金额：¥{{ amount }}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="paymentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePayment" :loading="loading">
            确认支付
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrderPayment } from '@/api/order'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const paymentDialogVisible = ref(true)
const amount = ref(route.query.amount || 0)
const orderId = route.query.orderId

const handlePayment = async () => {
  if (!orderId) {
    ElMessage.error('订单信息不完整')
    return
  }

  loading.value = true
  try {
    console.log('开始处理支付，订单ID:', orderId)
    const result = await createOrderPayment(orderId)
    console.log('支付结果:', result)
    
    ElMessage.success('支付成功')
    router.push('/user/orders')
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.message || '支付失败，请稍后重试')
  } finally {
    loading.value = false
    paymentDialogVisible.value = false
  }
}
</script>

<style scoped>
.payment-content {
  text-align: center;
  padding: 20px 0;
}
</style>