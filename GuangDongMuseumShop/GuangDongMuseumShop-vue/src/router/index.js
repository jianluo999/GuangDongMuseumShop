import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useUserStore } from '@/stores/user'
import Checkout from '@/views/Checkout.vue'
import Home from '@/views/Home.vue'
import ArtCategory from '@/views/categories/ArtCategory.vue'
import GiftsCategory from '@/views/categories/GiftsCategory.vue'
import MuseumCategory from '@/views/categories/MuseumCategory.vue'
import TeaCategory from '@/views/categories/TeaCategory.vue'
import JewelryCategory from '@/views/categories/JewelryCategory.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: Home
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/products/ProductSearch.vue')
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/products/ProductList.vue')
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/products/ProductDetail.vue'),
        meta: {
          title: '商品详情'
        }
      },
      {
        path: 'products/category/:id',
        name: 'ProductCategory',
        component: () => import('@/views/products/ProductCategory.vue')
      },
      {
        path: 'products/search',
        name: 'ProductSearch',
        component: () => import('@/views/products/ProductSearch.vue')
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'user',
        meta: { requiresAuth: true },
        children: [
          {
            path: 'profile',
            name: 'UserProfile',
            component: () => import('@/views/user/Profile.vue')
          },
          {
            path: 'orders',
            name: 'UserOrders',
            component: () => import('@/views/user/Orders.vue')
          },
          {
            path: 'reviews',
            name: 'UserReviews',
            component: () => import('@/views/user/Review.vue')
          },
          {
            path: 'address',
            name: 'UserAddress',
            component: () => import('@/views/user/Address.vue')
          },
          {
            path: 'messages',
            name: 'UserMessages',
            component: () => import('@/views/user/Messages.vue'),
            meta: { title: '我的消息' }
          },
          {
            path: 'orders/:orderId/pay',
            name: 'OrderPay',
            component: () => import('@/views/user/OrderPay.vue'),
            meta: {
              requiresAuth: true,
              title: '订单支付'
            }
          }
        ]
      },
      {
        path: 'about',
        name: 'About',
        component: () => import('@/views/About.vue')
      },
      {
        path: 'categories/intangible',
        name: 'IntangibleCategory',
        component: () => import('@/views/categories/IntangibleCategory.vue'),
        meta: { title: '非遗文创' }
      },
      {
        path: 'categories/featured',
        name: 'FeaturedCategory',
        component: () => import('@/views/categories/Featured.vue'),
        meta: { title: '精选文创' }
      },
      {
        path: 'categories/gifts',
        name: 'GiftsCategory',
        component: GiftsCategory,
        meta: {
          title: '私人定制'
        }
      },
      {
        path: 'categories/tea',
        name: 'TeaCategory',
        component: TeaCategory,
        meta: {
          title: '茶道精品'
        }
      },
      {
        path: 'categories/textile',
        name: 'TextileCategory',
        component: () => import('@/views/categories/TextileCategory.vue'),
        meta: { title: '丝巾织品' }
      },
      {
        path: 'categories/stationery',
        name: 'StationeryCategory',
        component: () => import('@/views/categories/StationeryCategory.vue'),
        meta: { title: '文房四宝' }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/Checkout.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/payment',
        name: 'Payment',
        component: () => import('@/views/Payment.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/categories/art',
        name: 'ArtCategory',
        component: ArtCategory,
        meta: {
          title: '艺术制品'
        }
      },
      {
        path: '/categories/museum',
        name: 'MuseumCategory',
        component: MuseumCategory,
        meta: {
          title: '广馆文创'
        }
      },
      {
        path: '/categories/jewelry',
        name: 'JewelryCategory',
        component: JewelryCategory,
        meta: {
          title: '国风首饰'
        }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue')
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',  // 默认路由
        redirect: 'dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'products',
        name: 'ProductManage',
        component: () => import('@/views/admin/ProductManage.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'orders',
        name: 'OrderManage',
        component: () => import('@/views/admin/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/Categories.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'carts',
        name: 'AdminCarts',
        component: () => import('@/views/admin/CartManage.vue'),
        meta: { title: '购物车管理' }
      },
      {
        path: 'products/add',
        component: () => import('@/views/admin/ProductEdit.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'products/edit/:id',
        component: () => import('@/views/admin/ProductEdit.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'reviews',
        name: 'ReviewManage',
        component: () => import('@/views/admin/ReviewManage.vue'),
        meta: { requiresAuth: true, title: '评论管理' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue'),
        meta: { requiresAuth: true, title: '个人设置' }
      },
      {
        path: 'messages',
        name: 'AdminMessages',
        component: () => import('@/views/admin/MessageCenter.vue'),
        meta: { requiresAuth: true, title: '消息中心' }
      }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLogin.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory('/'),
  routes
})

// 合并两个路由守卫为一个
router.beforeEach(async (to, from, next) => {
  // 设置标题
  document.title = to.meta.title ?
    `${to.meta.title} - 基于SpringBoot的广东省博物馆文创销售系统` :
    '基于SpringBoot的广东省博物馆文创销售系统'

  // 获取用户状态
  const userStore = useUserStore()
  
  // 检查登录状态
  const isLoggedIn = userStore.checkLoginState()
  console.log('当前登录状态:', isLoggedIn)
  
  // 检查是否需要登录
  if (to.meta.requiresAuth && !isLoggedIn) {
    console.log('需要登录，重定向到登录页')
    next({
      path: '/admin/login',
      query: { redirect: to.fullPath }
    })
    return
  }
  
  // 如果已登录且访问登录页，重定向到管理后台
  if (isLoggedIn && to.path === '/admin/login') {
    console.log('已登录，重定向到管理后台')
    next({ path: '/admin/dashboard' })
    return
  }
  
  next()
})

export default router