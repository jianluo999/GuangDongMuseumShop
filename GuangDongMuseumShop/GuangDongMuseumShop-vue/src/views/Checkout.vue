<template>
  <div class="checkout-page">
    <h2 class="page-title">确认订单</h2>
    
    <!-- 商品列表 -->
    <el-card class="order-items">
      <template #header>
        <div class="card-header">
          <span>商品清单</span>
        </div>
      </template>
      <div class="items-list">
        <div v-for="item in checkoutStore.checkoutItems" :key="item.id" class="item">
          <el-image :src="item.productImage" :alt="item.productName" class="product-image" />
          <div class="item-info">
            <h3 class="product-name">{{ item.productName }}</h3>
            <p class="specs" v-if="item.specs">规格：{{ item.specs }}</p>
            <p class="product-price">¥{{ item.productPrice }}</p>
            <p class="quantity">数量：{{ item.quantity }}</p>
          </div>
          <div class="item-total">
            <p>小计：¥{{ (item.productPrice * item.quantity).toFixed(2) }}</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 收货信息 -->
    <el-card class="shipping-info">
      <template #header>
        <div class="card-header">
          <span>收货信息</span>
        </div>
      </template>
      <el-form :model="shippingForm" :rules="rules" ref="shippingFormRef" label-width="100px">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="shippingForm.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="shippingForm.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input type="textarea" v-model="shippingForm.address" placeholder="请输入详细收货地址" :rows="3" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单摘要 -->
    <div class="order-summary">
      <div class="summary-row">
        <span>商品总价</span>
        <span>¥{{ checkoutStore.totalAmount.toFixed(2) }}</span>
      </div>
      <div class="summary-row">
        <span>运费</span>
        <span>¥10.00</span>
      </div>
      <div class="summary-row total">
        <span>总计</span>
        <span>¥{{ (checkoutStore.totalAmount + 10).toFixed(2) }}</span>
      </div>
      <el-button type="primary" class="submit-btn" :loading="submitLoading" @click="handleSubmitOrder">
        提交订单
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCheckoutStore } from '@/stores/checkout'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const checkoutStore = useCheckoutStore()
const cartStore = useCartStore()
const submitLoading = ref(false)
const shippingFormRef = ref(null)

// 收货信息表单
const shippingForm = ref({
  name: '',
  phone: '',
  address: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' },
    { min: 5, max: 100, message: '长度在 5 到 100 个字符', trigger: 'blur' }
  ]
}

onMounted(() => {
  // 检查URL参数中是否有立即购买的商品信息
  const orderItem = router.currentRoute.value.query.orderItem
  if (orderItem) {
    try {
      const decodedItem = JSON.parse(decodeURIComponent(orderItem))
      checkoutStore.setCheckoutItems([decodedItem])
    } catch (error) {
      console.error('解析立即购买商品信息失败:', error)
      ElMessage.error('商品信息无效')
      router.push('/cart')
      return
    }
  }
  
  // 如果没有结算商品，返回购物车页面
  if (checkoutStore.checkoutItems.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    router.push('/cart')
  }
})

// 提交订单
const handleSubmitOrder = async () => {
  if (!shippingFormRef.value) return
  
  try {
    // 验证表单
    await shippingFormRef.value.validate()
    
    submitLoading.value = true
    const response = await request.post('/api/orders', {
      items: checkoutStore.checkoutItems,
      totalAmount: checkoutStore.totalAmount + 10, // 加上运费
      deliveryFee: 10,
      shipping: {
        name: shippingForm.value.name,
        phone: shippingForm.value.phone,
        address: shippingForm.value.address
      }
    })
    
    if (response.data.code === 200) {
      try {
        // 先清除结算数据
        checkoutStore.clearCheckoutData()
        // 如果不是立即购买，则清空购物车
        if (!router.currentRoute.value.query.orderItem) {
          await cartStore.clearCart()
        }
        
        ElMessage.success('订单提交成功')
        router.push('/user/orders') // 跳转到订单列表页面
      } catch (error) {
        console.error('清空购物车失败:', error)
        ElMessage.warning('订单已提交，但清空购物车失败，请手动清空购物车')
        router.push('/user/orders')
      }
    } else {
      throw new Error(response.data.message || '提交订单失败')
    }
  } catch (error) {
    console.error('提交订单失败:', error)
    ElMessage.error(error.message || '提交订单失败')
  } finally {
    submitLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.checkout-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;

  .page-title {
    font-size: 24px;
    color: #333;
    margin-bottom: 20px;
  }

  .order-items {
    margin-bottom: 20px;

    .card-header {
      font-size: 18px;
      color: #333;
    }

    .items-list {
      .item {
        display: flex;
        align-items: center;
        padding: 20px 0;
        border-bottom: 1px solid #eee;

        &:last-child {
          border-bottom: none;
        }

        .product-image {
          width: 100px;
          height: 100px;
          object-fit: cover;
          margin-right: 20px;
        }

        .item-info {
          flex: 1;

          .product-name {
            font-size: 16px;
            color: #333;
            margin: 0 0 10px;
          }

          .specs {
            color: #666;
          }

          .product-price {
            color: #862D2D;
            font-size: 16px;
            margin: 0 0 5px;
          }

          .quantity {
            color: #666;
          }
        }

        .item-total {
          font-size: 16px;
          color: #862D2D;
          font-weight: bold;
        }
      }
    }
  }

  .shipping-info {
    margin-bottom: 20px;

    .card-header {
      font-size: 18px;
      color: #333;
    }

    :deep(.el-form) {
      padding: 20px;
      
      .el-form-item {
        margin-bottom: 20px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
      
      .el-input,
      .el-textarea {
        width: 100%;
        max-width: 400px;
      }
    }
  }

  .order-summary {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);

    .summary-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
      color: #666;

      &.total {
        color: #862D2D;
        font-size: 18px;
        font-weight: bold;
        margin-top: 20px;
        padding-top: 20px;
        border-top: 1px solid #eee;
      }
    }

    .submit-btn {
      width: 100%;
      margin-top: 20px;
      background-color: #862D2D;
      border-color: #862D2D;

      &:hover {
        background-color: darken(#862D2D, 10%);
        border-color: darken(#862D2D, 10%);
      }
    }
  }
}
</style> 