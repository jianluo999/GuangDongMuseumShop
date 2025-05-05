<template>
  <div class="orders-page">
    <!-- 顶部导航栏 -->
    <div class="nav-bar">
      <div class="left" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="title">我的订单</div>
      <div class="right">
        <el-icon><Search /></el-icon>
      </div>
    </div>

    <!-- 订单状态标签页 -->
    <div class="order-tabs">
      <div 
        v-for="tab in orderTabs" 
        :key="tab.id"
        class="tab-item"
        :class="{ active: currentTab === tab.id }"
        @click="switchTab(tab.id)"
      >
        {{ tab.name }}
        <span class="count" v-if="tab.count">({{ tab.count }})</span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-if="orders.length">
      <div 
        v-for="order in orders" 
        :key="order.id"
        class="order-card"
      >
        <!-- 订单内容 -->
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <el-empty description="暂无相关订单">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Search } from '@element-plus/icons-vue'
import { getOrders } from '@/api/order'

const router = useRouter()
const route = useRoute()

// 基础数据
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentTab = ref('all')

// 获取订单列表
const fetchOrders = async () => {
  try {
    const response = await getOrders({
      page: currentPage.value,
      size: pageSize.value,
      status: currentTab.value === 'all' ? undefined : currentTab.value
    })
    orders.value = response.data || []
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
.orders-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 20px;

  .nav-bar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 44px;
    background: #fff;
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
    }

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .order-tabs {
    margin-top: 44px;
    background: #fff;
    display: flex;
    padding: 10px 0;

    .tab-item {
      flex: 1;
      text-align: center;
      font-size: 14px;
      color: #666;
      cursor: pointer;

      &.active {
        color: #409EFF;
      }
    }
  }

  .order-list {
    padding: 10px 15px;
  }
}

.item-info {
  h4 {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}
</style>