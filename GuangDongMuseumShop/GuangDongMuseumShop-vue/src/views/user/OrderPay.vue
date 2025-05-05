<template>
  <div class="payment-page">
    <el-card class="payment-card">
      <template #header>
        <div class="card-header">
          <span>订单支付</span>
        </div>
      </template>

      <div class="payment-info">
        <div class="amount-section">
          <span class="label">支付金额</span>
          <span class="amount">¥{{ orderAmount }}</span>
        </div>

        <div class="payment-method-section">
          <span class="label">选择支付方式</span>
          <div class="payment-methods">
            <el-radio-group v-model="selectedPaymentMethod">
              <el-radio label="ALIPAY">
                <el-icon><Money /></el-icon>
                支付宝支付
              </el-radio>
              <el-radio label="WECHAT">
                <el-icon><ChatDotRound /></el-icon>
                微信支付
              </el-radio>
            </el-radio-group>
          </div>
        </div>

        <div v-if="qrCode" class="qr-code-section">
          <div class="qr-code-wrapper">
            <div class="mock-qr-code">
              <el-icon :size="50"><Grid /></el-icon>
              <p>模拟二维码</p>
            </div>
            <p class="scan-tip">请使用{{ selectedPaymentMethod === 'ALIPAY' ? '支付宝' : '微信' }}扫码支付</p>
            <p class="time-limit">支付剩余时间：{{ formatTime(remainingTime) }}</p>
          </div>
        </div>

        <div class="action-buttons">
          <el-button type="primary" @click="handlePayment" :loading="loading" :disabled="!selectedPaymentMethod">
            确认支付
          </el-button>
          <el-button @click="handleCancelButtonClick">取消支付</el-button>
        </div>
      </div>
    </el-card>

    <!-- 支付确认对话框 -->
    <el-dialog
      v-model="showConfirmDialog"
      title="支付确认"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="confirm-content">
        <p>请在手机上完成支付后，点击"已完成支付"按钮。</p>
        <p class="warning">请确保完成支付后再点击！</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handlePaymentCancel">取消支付</el-button>
          <el-button type="primary" @click="handlePaymentConfirm" :loading="confirmLoading">
            已完成支付
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Money, ChatDotRound, Grid } from '@element-plus/icons-vue'
import { getPaymentQRCode, checkPaymentStatus, confirmPayment, cancelPayment } from '@/api/order'

const route = useRoute()
const router = useRouter()

const orderId = ref(null)
const orderAmount = ref(0)
const selectedPaymentMethod = ref('')
const qrCode = ref('')
const loading = ref(false)
const confirmLoading = ref(false)
const showConfirmDialog = ref(false)
const remainingTime = ref(1800) // 30分钟倒计时
let timer = null
let statusCheckTimer = null

// 格式化剩余时间
const formatTime = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 处理支付
const handlePayment = async () => {
  if (!selectedPaymentMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  try {
    loading.value = true
    const response = await getPaymentQRCode(orderId.value, selectedPaymentMethod.value)
    if (response.data.code === 200) {
      qrCode.value = response.data.data.qrCodeUrl
      showConfirmDialog.value = true
      startPollingPaymentStatus()
    }
  } catch (error) {
    ElMessage.error(error.message || '获取支付二维码失败')
  } finally {
    loading.value = false
  }
}

// 开始轮询支付状态
const startPollingPaymentStatus = () => {
  statusCheckTimer = setInterval(async () => {
    try {
      const response = await checkPaymentStatus(orderId.value)
      if (response.code === 200) {
        const status = response.data.status
        if (status === 'PAID') {
          clearInterval(statusCheckTimer)
          ElMessage.success('支付成功')
          router.push('/user/orders')
        } else if (status === 'FAILED' || status === 'CANCELLED') {
          clearInterval(statusCheckTimer)
          ElMessage.error('支付失败')
          showConfirmDialog.value = false
        }
      }
    } catch (error) {
      console.error('检查支付状态失败:', error)
    }
  }, 3000) // 每3秒检查一次
}

// 确认支付
const handlePaymentConfirm = async () => {
  try {
    confirmLoading.value = true
    await confirmPayment(orderId.value)
    ElMessage.success('支付成功')
    router.push('/user/orders')
  } catch (error) {
    ElMessage.error(error.message || '确认支付失败')
  } finally {
    confirmLoading.value = false
    showConfirmDialog.value = false
  }
}

// 取消支付
const handlePaymentCancel = async () => {
  try {
    loading.value = true
    const response = await cancelPayment(orderId.value)
    if (response.data.code === 200) {
      ElMessage.info('已取消支付')
      router.push('/user/orders')
    } else {
      throw new Error(response.data.message || '取消支付失败')
    }
  } catch (error) {
    console.error('取消支付失败:', error)
    ElMessage.error(error.message || '取消支付失败')
  } finally {
    loading.value = false
    showConfirmDialog.value = false
  }
}

// 点击取消支付按钮
const handleCancelButtonClick = async () => {
  try {
    await handlePaymentCancel()
  } catch (error) {
    console.error('取消支付失败:', error)
    ElMessage.error(error.message || '取消支付失败')
  }
}

// 倒计时
const startCountdown = () => {
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      clearInterval(timer)
      ElMessage.warning('支付超时，请重新下单')
      router.push('/user/orders')
    }
  }, 1000)
}

onMounted(() => {
  orderId.value = route.params.orderId
  orderAmount.value = route.query.amount || 0
  startCountdown()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (statusCheckTimer) clearInterval(statusCheckTimer)
})
</script>

<style lang="scss" scoped>
.payment-page {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;

  .payment-card {
    .card-header {
      font-size: 20px;
      color: #333;
    }

    .payment-info {
      padding: 20px;

      .amount-section {
        text-align: center;
        margin-bottom: 30px;

        .label {
          font-size: 16px;
          color: #666;
        }

        .amount {
          font-size: 32px;
          color: #862D2D;
          font-weight: bold;
          margin-left: 10px;
        }
      }

      .payment-method-section {
        margin-bottom: 30px;

        .label {
          display: block;
          margin-bottom: 15px;
          font-size: 16px;
          color: #333;
        }

        .payment-methods {
          display: flex;
          justify-content: center;
          gap: 30px;

          .el-radio {
            display: flex;
            align-items: center;
            gap: 5px;
            
            .el-icon {
              margin-right: 5px;
              font-size: 20px;
            }
          }
        }
      }

      .qr-code-section {
        text-align: center;
        margin-bottom: 30px;

        .qr-code-wrapper {
          display: inline-block;
          padding: 20px;
          border: 1px solid #eee;
          border-radius: 8px;
          background: #f8f8f8;

          .mock-qr-code {
            width: 200px;
            height: 200px;
            margin: 0 auto 15px;
            border: 1px solid #ddd;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background: #fff;

            .el-icon {
              margin-bottom: 10px;
              color: #862D2D;
            }

            p {
              color: #666;
              font-size: 14px;
            }
          }

          .scan-tip {
            color: #666;
            margin-bottom: 10px;
          }

          .time-limit {
            color: #862D2D;
            font-weight: bold;
          }
        }
      }

      .action-buttons {
        text-align: center;
        margin-top: 20px;

        .el-button {
          min-width: 120px;
          margin: 0 10px;
        }
      }
    }
  }

  .confirm-content {
    text-align: center;
    padding: 20px 0;

    .warning {
      color: #862D2D;
      font-weight: bold;
      margin-top: 10px;
    }
  }

  .dialog-footer {
    text-align: center;
  }
}
</style> 