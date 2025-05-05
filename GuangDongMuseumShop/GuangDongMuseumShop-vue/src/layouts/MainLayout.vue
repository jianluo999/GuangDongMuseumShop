<template>
  <div class="main-layout">
    <header class="header">
      <div class="header-content">
        <div class="left-section">
          <router-link to="/" class="logo">
            <el-icon class="logo-icon"><Grid /></el-icon>
            <span class="title">广东省博物馆文创</span>
          </router-link>
          <div class="search-box">
            <div class="search-container"
              @mouseenter="isSearchHovered = true"
              @mouseleave="isSearchHovered = false"
            >
              <el-input
                v-model="searchKeyword"
                placeholder="搜索"
                class="search-input"
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <el-icon class="search-icon"><Search /></el-icon>
                </template>
              </el-input>
              <transition name="fade">
                <div class="hot-search" v-if="hotSearches.length && isSearchHovered">
                  <span class="label">热门搜索：</span>
                  <div class="tags-wrapper">
                    <el-tag
                      v-for="keyword in hotSearches"
                      :key="keyword"
                      class="hot-tag"
                      effect="plain"
                      size="small"
                      @click="handleHotSearch(keyword)"
                    >
                      {{ keyword }}
                    </el-tag>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>
        <div class="user-section">
          <router-link to="/cart" class="cart-link">
            <el-badge :value="cartCount" :max="99" class="cart-badge">
              <el-icon><ShoppingCart /></el-icon>
            </el-badge>
          </router-link>
          <router-link to="/user/messages" class="message-link">
            <el-badge :value="unreadMessageCount" :max="99" :hidden="!unreadMessageCount" class="message-badge">
              <el-icon><Bell /></el-icon>
            </el-badge>
          </router-link>
          <template v-if="isLoggedIn">
            <el-dropdown>
              <span class="user-info">
                <el-avatar 
                  :size="32"
                  :src="`https://api.dicebear.com/7.x/initials/svg?seed=${userStore.username}`"
                />
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/user/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/user/orders')">我的订单</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button link @click="$router.push('/login')">登录</el-button>
            <el-button link @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>

      <nav class="nav-menu">
        <div class="nav-content">
          <ul class="nav-list">
            <li class="nav-item">
              <router-link to="/" class="nav-link" :class="{ active: currentPath === '/' }">首页</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/categories/featured" class="nav-link" :class="{ active: currentPath.includes('/categories/featured') }">精选文创</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/categories/intangible" class="nav-link" :class="{ active: currentPath.includes('/categories/intangible') }">非遗文创</router-link>
            </li>
          </ul>
        </div>
      </nav>
    </header>

    <main class="main-content">
      <router-view></router-view>
    </main>

    <footer class="footer">
      <div class="footer-content">
        <p>基于SpringBoot的广东省博物馆文创销售系统的设计与实现 - 毕业设计2025</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { Search, Grid, ShoppingCart, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getHotSearches } from '@/api/product'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const hotSearches = ref([])
const currentPath = computed(() => route.path)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const cartCount = computed(() => cartStore.totalCount)
const isSearchHovered = ref(false)
const unreadMessageCount = ref(0)

// 获取热门搜索
const fetchHotSearches = async () => {
  try {
    const response = await getHotSearches()
    if (response.data.code === 200) {
      hotSearches.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取热门搜索失败:', error)
  }
}

// 处理热门搜索标签点击
const handleHotSearch = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

// 在 onMounted 之前添加
const handleSearchHover = (hovered) => {
  isSearchHovered.value = hovered
}

// 获取未读消息数量
const getUnreadMessageCount = async () => {
  try {
    const res = await request({
      url: '/api/messages/unread-count',
      method: 'get'
    })
    if (res.data.code === 200) {
      unreadMessageCount.value = res.data.data || 0
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
    unreadMessageCount.value = 0
  }
}

onMounted(() => {
  console.log('Login status:', userStore.isLoggedIn)
  console.log('Username:', userStore.username)
  fetchHotSearches()
  if (isLoggedIn.value) {
    getUnreadMessageCount()
    cartStore.fetchCartItems()
  }
})

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/search',
      query: { q: searchKeyword.value.trim() }
    })
    searchKeyword.value = ''  // 清空搜索框
  }
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #F7EFE5;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.logo-icon {
  font-size: 24px;
  color: #862D2D;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #862D2D;
}

.search-box {
  width: 400px;
  position: relative;
}

.search-container {
  position: relative;
}

.search-input :deep(.el-input__wrapper) {
  background-color: #F7EFE5;
  border-radius: 20px;
  box-shadow: none;
  border: 1px solid #E8E3D7;
  padding: 0 15px;
  height: 40px;
}

.search-input :deep(.el-input__wrapper):hover,
.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #862D2D;
}

.search-icon {
  font-size: 16px;
  color: #862D2D;
}

.hot-search {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 10px 15px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 10;
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.hot-search .label {
  color: #666;
  font-size: 13px;
  white-space: nowrap;
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-tag {
  cursor: pointer;
  margin: 0;
  transition: all 0.3s;
  border-color: #E8E3D7;
  background-color: #F7EFE5;
  color: #666;
  padding: 0 12px;
  height: 24px;
  line-height: 22px;
  
  &:hover {
    color: #862D2D;
    border-color: #862D2D;
    background-color: #fff;
  }
}

.user-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.cart-link {
  color: #666;
  text-decoration: none;
  font-size: 24px;
  display: flex;
  align-items: center;
}

.cart-badge :deep(.el-badge__content) {
  background-color: #862D2D;
}

.message-link {
  color: #666;
  text-decoration: none;
  font-size: 24px;
  display: flex;
  align-items: center;
}

.message-badge :deep(.el-badge__content) {
  background-color: #862D2D;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  
  .el-avatar {
    background-color: #B24F30;
  }
}

.username {
  color: #333;
  font-size: 14px;
}

.auth-link {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.auth-link:hover {
  color: #862D2D;
  background-color: #F7EFE5;
}

.nav-menu {
  background-color: #F7EFE5;
  border-bottom: 1px solid #E8E3D7;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.nav-list {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 32px;
}

.nav-item {
  position: relative;
}

.nav-link {
  display: block;
  padding: 12px 0;
  color: #666;
  text-decoration: none;
  font-size: 15px;
  transition: color 0.3s ease;
}

.nav-link:hover,
.nav-link.active {
  color: #862D2D;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #862D2D;
}

.main-content {
  flex: 1;
  margin-top: 120px;
  padding: 20px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  width: 100%;
}

.footer {
  background-color: #fff;
  padding: 20px 0;
  margin-top: auto;
  border-top: 1px solid #E8E3D7;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  color: #666;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 