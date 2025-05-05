<template>
  <div class="order-detail" :class="{ 'dark-theme': isDarkTheme }">
    <!-- 顶部导航栏 -->
    <div class="nav-bar">
      <div class="left" @click="goBack">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="title">订单详情</div>
      <div class="right">
        <i class="el-icon-service" @click="contactService"></i>
      </div>
    </div>

    <!-- 订单状态卡片 -->
    <div class="status-card">
      <div class="status-info">
        <div class="status-icon" :class="order.status">
          <i :class="getStatusIcon(order.status)"></i>
        </div>
        <div class="status-text">
          <h3>{{ getStatusText(order.status) }}</h3>
          <p>{{ getStatusDescription(order.status) }}</p>
        </div>
      </div>
      <!-- 物流信息 -->
      <div 
        class="logistics-info" 
        v-if="order.logistics"
        @click="viewLogistics(order.id)"
      >
        <div class="logistics-icon">
          <i class="el-icon-truck"></i>
        </div>
        <div class="logistics-text">
          <p>{{ order.logistics.latestStatus }}</p>
          <span>{{ order.logistics.updateTime }}</span>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>
    </div>

    <!-- 收货地址 -->
    <div class="address-card">
      <div class="address-icon">
        <i class="el-icon-location"></i>
      </div>
      <div class="address-info">
        <div class="contact">
          <span class="name">{{ order.address.name }}</span>
          <span class="phone">{{ order.address.phone }}</span>
        </div>
        <div class="address">{{ order.address.fullAddress }}</div>
      </div>
    </div>

    <!-- 店铺商品信息 -->
    <div class="store-card">
      <div class="store-header">
        <div class="store-info">
          <i class="el-icon-shop"></i>
          <span>{{ order.storeName }}</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>
      <div class="product-list">
        <div 
          v-for="item in order.items" 
          :key="item.id"
          class="product-item"
          @click="viewProduct(item.id)"
        >
          <div class="product-image">
            <img :src="item.image" :alt="item.name">
          </div>
          <div class="product-info">
            <h4>{{ item.name }}</h4>
            <p class="specs">{{ item.specs }}</p>
            <div class="price-row">
              <span class="price">¥{{ item.price }}</span>
              <span class="quantity">x{{ item.quantity }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 订单信息 -->
    <div class="order-info-card">
      <div class="info-group">
        <div class="info-item">
          <span class="label">商品总价</span>
          <span class="value">¥{{ getProductTotal() }}</span>
        </div>
        <div class="info-item">
          <span class="label">运费</span>
          <span class="value">¥{{ order.deliveryFee }}</span>
        </div>
        <div class="info-item discount" v-if="order.discount">
          <span class="label">优惠</span>
          <span class="value">-¥{{ order.discount }}</span>
        </div>
        <div class="info-item total">
          <span class="label">实付款</span>
          <span class="value">¥{{ getTotalPrice() }}</span>
        </div>
      </div>
      <div class="divider"></div>
      <div class="info-group">
        <div class="info-item">
          <span class="label">订单编号</span>
          <div class="value-group">
            <span>{{ order.id }}</span>
            <el-button 
              type="text" 
              size="small"
              @click="copyOrderId"
            >
              复制
            </el-button>
          </div>
        </div>
        <div class="info-item">
          <span class="label">创建时间</span>
          <span class="value">{{ order.createTime }}</span>
        </div>
        <div class="info-item" v-if="order.payTime">
          <span class="label">付款时间</span>
          <span class="value">{{ order.payTime }}</span>
        </div>
        <div class="info-item" v-if="order.deliveryTime">
          <span class="label">发货时间</span>
          <span class="value">{{ order.deliveryTime }}</span>
        </div>
        <div class="info-item" v-if="order.completeTime">
          <span class="label">完成时间</span>
          <span class="value">{{ order.completeTime }}</span>
        </div>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="action-bar" v-if="showActionBar">
      <template v-if="order.status === 'pending'">
        <el-button size="small" @click="cancelOrder">取消订单</el-button>
        <el-button 
          type="primary" 
          size="small"
          @click="payOrder"
        >
          立即付款
        </el-button>
      </template>
      <template v-else-if="order.status === 'processing'">
        <el-button size="small" @click="viewLogistics(order.id)">查看物流</el-button>
        <el-button size="small" @click="urgeDelivery">催发货</el-button>
      </template>
      <template v-else-if="order.status === 'shipping'">
        <el-button size="small" @click="viewLogistics(order.id)">查看物流</el-button>
        <el-button 
          type="primary" 
          size="small"
          @click="confirmReceipt"
        >
          确认收货
        </el-button>
      </template>
      <template v-else-if="order.status === 'completed'">
        <el-button size="small" @click="buyAgain">再次购买</el-button>
        <el-button 
          type="primary" 
          size="small"
          @click="writeReview"
          v-if="!order.hasReview"
        >
          评价晒单
        </el-button>
      </template>
      <template v-else-if="order.status === 'after-sale'">
        <el-button size="small" @click="viewAfterSale">查看进度</el-button>
      </template>
    </div>

    <!-- 确认弹窗 -->
    <el-dialog
      v-model="confirmDialogVisible"
      :title="confirmDialogTitle"
      width="90%"
      custom-class="confirm-dialog"
    >
      <p>{{ confirmDialogContent }}</p>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="confirmDialogVisible = false">取消</el-button>
          <el-button 
            type="primary"
            @click="handleConfirmAction"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const isDarkTheme = ref(false)

// 确认弹窗
const confirmDialogVisible = ref(false)
const confirmDialogTitle = ref('')
const confirmDialogContent = ref('')
const confirmAction = ref(null)

// 订单数据
const order = ref({
  id: 'GD2023112001',
  storeName: '故宫文创官方店',
  status: 'shipping',
  items: [
    {
      id: 1,
      name: '青花瓷茶具套装',
      specs: '青花瓷 4件套',
      price: '399.00',
      quantity: 1,
      image: 'https://source.unsplash.com/800x800/?chinese,pottery,tea'
    }
  ],
  address: {
    name: '张三',
    phone: '138****8888',
    fullAddress: '广东省广州市天河区天河路100号'
  },
  logistics: {
    latestStatus: '快件已到达广州市天河区配送中心',
    updateTime: '2023-11-21 10:30:00'
  },
  deliveryFee: '12.00',
  discount: '50.00',
  createTime: '2023-11-20 15:30:00',
  payTime: '2023-11-20 15:35:00',
  deliveryTime: '2023-11-21 09:00:00'
})

// 是否显示底部操作栏
const showActionBar = computed(() => {
  return ['pending', 'processing', 'shipping', 'completed', 'after-sale'].includes(order.value.status)
})

// 获取状态图标
const getStatusIcon = (status) => {
  const iconMap = {
    pending: 'el-icon-wallet',
    processing: 'el-icon-box',
    shipping: 'el-icon-truck',
    completed: 'el-icon-circle-check',
    'after-sale': 'el-icon-warning'
  }
  return iconMap[status] || 'el-icon-info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待付款',
    processing: '待发货',
    shipping: '待收货',
    completed: '已完成',
    'after-sale': '售后中'
  }
  return statusMap[status] || status
}

// 获取状态描述
const getStatusDescription = (status) => {
  const descMap = {
    pending: '请在24小时内完成支付',
    processing: '商家正在处理您的订单',
    shipping: '商品正在配送中',
    completed: '交易已完成',
    'after-sale': '售后处理中'
  }
  return descMap[status] || ''
}

// 计算商品总价
const getProductTotal = () => {
  return order.value.items.reduce((sum, item) => 
    sum + item.price * item.quantity, 0
  ).toFixed(2)
}

// 计算订单总价
const getTotalPrice = () => {
  const productTotal = Number(getProductTotal())
  const deliveryFee = Number(order.value.deliveryFee)
  const discount = Number(order.value.discount || 0)
  return (productTotal + deliveryFee - discount).toFixed(2)
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 联系客服
const contactService = () => {
  router.push('/service')
}

// 查看物流
const viewLogistics = (id) => {
  router.push(`/logistics/${id}`)
}

// 查看商品
const viewProduct = (id) => {
  router.push(`/products/${id}`)
}

// 复制订单号
const copyOrderId = () => {
  navigator.clipboard.writeText(order.value.id)
  ElMessage.success('订单号已复制')
}

// 显示确认弹窗
const showConfirmDialog = (title, content, action) => {
  confirmDialogTitle.value = title
  confirmDialogContent.value = content
  confirmAction.value = action
  confirmDialogVisible.value = true
}

// 处理确认操作
const handleConfirmAction = () => {
  if (confirmAction.value) {
    confirmAction.value()
  }
  confirmDialogVisible.value = false
}

// 取消订单
const cancelOrder = () => {
  showConfirmDialog(
    '取消订单',
    '确定要取消该订单吗？',
    () => {
      ElMessage.success('订单已取消')
      // 实际场景中这里需要调用取消订单的API
    }
  )
}

// 支付订单
const payOrder = () => {
  router.push(`/payment/${order.value.id}`)
}

// 催发货
const urgeDelivery = () => {
  ElMessage.success('已通知商家尽快发货')
}

// 确认收货
const confirmReceipt = () => {
  showConfirmDialog(
    '确认收货',
    '确认已收到商品吗？',
    () => {
      ElMessage.success('确认收货成功')
      // 实际场景中这里需要调用确认收货的API
    }
  )
}

// 再次购买
const buyAgain = () => {
  // 将商品添加到购物车
  ElMessage.success('已添加到购物车')
  router.push('/cart')
}

// 评价晒单
const writeReview = () => {
  router.push(`/review/${order.value.id}`)
}

// 查看售后进度
const viewAfterSale = () => {
  router.push(`/after-sale/${order.value.id}`)
}
</script>

<style scoped lang="scss">
.order-detail {
  min-height: 100vh;
  background-color: #F5F0E6;
  padding-bottom: 70px;

  &.dark-theme {
    background-color: #1C1C1E;
    color: #fff;
  }

  .nav-bar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 44px;
    background: rgba(255, 255, 255, 0.98);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 15px;
    z-index: 100;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

    .left, .right {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;

      i {
        font-size: 20px;
        color: #333;
      }
    }

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .status-card {
    margin-top: 44px;
    background: linear-gradient(135deg, #B24F30 0%, #D4956B 100%);
    padding: 20px 15px;
    color: #fff;

    .status-info {
      display: flex;
      align-items: center;
      gap: 15px;
      margin-bottom: 20px;

      .status-icon {
        width: 48px;
        height: 48px;
        border-radius: 24px;
        background: rgba(255, 255, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;

        i {
          font-size: 24px;
        }
      }

      .status-text {
        h3 {
          font-size: 18px;
          margin-bottom: 4px;
        }

        p {
          font-size: 12px;
          opacity: 0.8;
        }
      }
    }

    .logistics-info {
      display: flex;
      align-items: center;
      gap: 12px;
      background: rgba(255, 255, 255, 0.1);
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;

      .logistics-icon {
        width: 36px;
        height: 36px;
        border-radius: 18px;
        background: rgba(255, 255, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;

        i {
          font-size: 20px;
        }
      }

      .logistics-text {
        flex: 1;

        p {
          font-size: 14px;
          margin-bottom: 4px;
        }

        span {
          font-size: 12px;
          opacity: 0.8;
        }
      }

      .el-icon-arrow-right {
        font-size: 16px;
        opacity: 0.8;
      }
    }
  }

  .address-card {
    margin: 15px;
    background: #fff;
    border-radius: 12px;
    padding: 15px;
    display: flex;
    gap: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .address-icon {
      width: 24px;
      height: 24px;
      background: #F5F0E6;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;

      i {
        font-size: 16px;
        color: #B24F30;
      }
    }

    .address-info {
      flex: 1;

      .contact {
        margin-bottom: 8px;

        .name {
          font-size: 16px;
          font-weight: 500;
          color: #333;
          margin-right: 12px;
        }

        .phone {
          font-size: 14px;
          color: #666;
        }
      }

      .address {
        font-size: 14px;
        color: #666;
        line-height: 1.4;
      }
    }
  }

  .store-card {
    margin: 15px;
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .store-header {
      padding: 12px 15px;
      border-bottom: 1px solid #eee;

      .store-info {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        color: #333;

        i {
          font-size: 16px;

          &.el-icon-arrow-right {
            font-size: 12px;
            color: #999;
          }
        }
      }
    }

    .product-list {
      .product-item {
        display: flex;
        padding: 15px;
        gap: 12px;
        cursor: pointer;

        &:not(:last-child) {
          border-bottom: 1px solid #eee;
        }

        .product-image {
          width: 80px;
          height: 80px;
          border-radius: 8px;
          overflow: hidden;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .product-info {
          flex: 1;

          h4 {
            font-size: 14px;
            color: #333;
            margin-bottom: 6px;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
          }

          .specs {
            font-size: 12px;
            color: #999;
            margin-bottom: 8px;
          }

          .price-row {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .price {
              font-size: 16px;
              color: #B24F30;
              font-weight: bold;
            }

            .quantity {
              font-size: 12px;
              color: #999;
            }
          }
        }
      }
    }
  }

  .order-info-card {
    margin: 15px;
    background: #fff;
    border-radius: 12px;
    padding: 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .info-group {
      .info-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          font-size: 14px;
          color: #666;
        }

        .value {
          font-size: 14px;
          color: #333;
        }

        .value-group {
          display: flex;
          align-items: center;
          gap: 8px;
        }

        &.discount {
          .value {
            color: #F56C6C;
          }
        }

        &.total {
          .label, .value {
            font-size: 16px;
            font-weight: 500;
            color: #333;
          }

          .value {
            color: #B24F30;
          }
        }
      }
    }

    .divider {
      height: 1px;
      background: #eee;
      margin: 15px 0;
    }
  }

  .action-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 50px;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 0 15px;
    gap: 10px;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  }
}

// 深色主题样式
.dark-theme {
  .nav-bar {
    background: rgba(44, 44, 46, 0.98);

    .left, .right {
      i {
        color: #fff;
      }
    }

    .title {
      color: #fff;
    }
  }

  .address-card,
  .store-card,
  .order-info-card,
  .action-bar {
    background: #2C2C2E;

    .store-header {
      border-color: #3A3A3C;

      .store-info {
        color: #fff;
      }
    }

    .product-item {
      border-color: #3A3A3C !important;

      .product-info {
        h4 {
          color: #fff;
        }
      }
    }

    .info-item {
      .label {
        color: #999;
      }

      .value {
        color: #fff;
      }

      &.total {
        .label {
          color: #fff;
        }
      }
    }

    .divider {
      background: #3A3A3C;
    }
  }
}
</style>