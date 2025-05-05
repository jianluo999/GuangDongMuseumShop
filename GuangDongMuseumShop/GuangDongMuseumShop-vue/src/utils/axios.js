import axios from 'axios'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const token = userStore.userInfo?.token

    // 如果有 token，添加到请求头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    console.log('Request:', {
      url: config.url,
      method: config.method,
      headers: config.headers,
      data: config.data ? {
        ...config.data,
        password: config.data.password ? '******' : undefined
      } : undefined
    })
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  response => {
    console.log('Response raw:', {
      status: response.status,
      statusText: response.statusText,
      headers: response.headers,
      data: response.data
    })
    
    // 检查响应数据格式
    if (!response.data) {
      throw new Error('响应数据为空')
    }
    
    // 如果响应中包含 code 字段，检查其值
    if ('code' in response.data && response.data.code !== 200) {
      throw new Error(response.data.message || '操作失败')
    }
    
    return response.data
  },
  error => {
    console.error('Response error full:', error)
    console.error('Response error details:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      message: error.message,
      config: error.config
    })
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          ElMessage.error(data?.message || '请求参数错误')
          break
        case 401:
          ElMessage.error(data?.message || '未授权，请重新登录')
          if (router.currentRoute.value.path !== '/login') {
            const userStore = useUserStore()
            userStore.clearUserInfo()
            router.push('/login')
          }
          break
        case 403:
          ElMessage.error(data?.message || '没有权限执行此操作')
          break
        case 404:
          ElMessage.error(data?.message || '请求的资源不存在')
          break
        case 500:
          ElMessage.error(data?.message || '服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || '操作失败，请重试')
      }
    } else if (error.request) {
      ElMessage.error('网络请求失败，请检查网络连接')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default instance