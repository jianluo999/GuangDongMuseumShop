<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="aside">
      <div class="logo">
        <el-icon class="logo-icon"><Shop /></el-icon>
        <span>后台管理系统</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item v-for="menu in adminMenus" :key="menu.path" :index="menu.path">
          <el-icon><component :is="menu.icon" /></el-icon>
          <span>{{ menu.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主要内容区 -->
    <el-container class="main">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="left">
          <el-icon 
            class="collapse-btn"
            @click="isCollapse = !isCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRoute }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="right">
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar 
                :size="32" 
                style="background-color: #409EFF; color: #fff; font-size: 16px;"
              >
                {{ userStore.username.substring(0, 1).toUpperCase() }}
              </el-avatar>
              <span>{{ userStore.username }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/admin/profile')">
                  <el-icon><User /></el-icon>
                  个人设置
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  DataLine,
  Goods,
  List,
  User,
  Menu,
  Fold,
  Expand,
  CaretBottom,
  ShoppingCart,
  Shop,
  ChatLineRound,
  Bell,
  SwitchButton
} from '@element-plus/icons-vue'
import { adminMenus } from '@/config/menu'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

// 当前路由名称
const currentRoute = computed(() => {
  const routeMap = {
    '/admin/dashboard': '数据概览',
    '/admin/products': '商品管理',
    '/admin/orders': '订单管理',
    '/admin/users': '用户管理',
    '/admin/categories': '分类管理',
    '/admin/carts': '购物车管理',
    '/admin/reviews': '评价管理',
    '/admin/messages': '消息中心'
  }
  return routeMap[route.path] || '首页'
})

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning'
    })
    await userStore.logout()
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
    }
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
  display: flex;

  .aside {
    background: #304156;
    height: 100%;
    color: white;
    transition: width 0.3s;
    width: 200px;  // 固定侧边栏宽度

    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      padding: 0 20px;
      background: #2b3649;

      .logo-icon {
        color: #409EFF;  // Element Plus 主题蓝色
        margin-right: 12px;
        font-size: 24px;
      }

      span {
        font-size: 18px;
        font-weight: 500;
        color: #fff;
      }
    }

    .menu {
      border-right: none;
      background: transparent;
      margin-top: 10px;

      :deep(.el-menu-item) {
        height: 56px;
        line-height: 56px;
        color: #bfcbd9;
        
        &:hover {
          color: #fff;
          background: #263445;
        }

        &.is-active {
          color: #fff;
          background: #409EFF;
        }

        .el-icon {
          margin-right: 12px;
          font-size: 18px;
        }

        span {
          font-size: 14px;
        }
      }
    }
  }

  .main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: #f0f2f5;

    .header {
      background: white;
      border-bottom: 1px solid #eee;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 20px;
      height: 60px;

      .left {
        display: flex;
        align-items: center;
        gap: 20px;

        .collapse-btn {
          font-size: 20px;
          cursor: pointer;
          color: #666;

          &:hover {
            color: #409EFF;
          }
        }

        :deep(.el-breadcrumb__item) {
          .el-breadcrumb__inner {
            color: #666;
            font-weight: normal;
            
            &:hover {
              color: #409EFF;
            }
          }
        }
      }

      .right {
        .user-info {
          display: flex;
          align-items: center;
          gap: 12px;
          cursor: pointer;
          padding: 6px 12px;
          border-radius: 4px;
          transition: all 0.3s;

          &:hover {
            background: #f5f7fa;
          }

          span {
            color: #333;
            font-size: 14px;
          }

          .el-icon {
            color: #666;
            font-size: 12px;
          }
        }
      }
    }

    .content {
      padding: 20px;
      overflow-y: auto;
      background: #f0f2f5;
    }
  }
}

// 路由切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 