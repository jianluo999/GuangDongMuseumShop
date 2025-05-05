import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const baseURL = import.meta.env.VITE_API_URL || '/api'

const request = axios.create({
  baseURL,  // 使用环境变量中的baseURL
  timeout: 10000
})

// 请求重试配置
const retryDelay = 1000
const maxRetryAttempts = 3

// 处理图片URL的函数
const processImageUrl = (url) => {
  if (!url) return defaultsConfig.productImage || ''
  if (url.startsWith('http') || url.startsWith('data:') || url.startsWith('blob:')) return url
  return url.startsWith('/') ? `${baseURL}${url}` : `${baseURL}/${url}`
}

// 处理响应数据中的图片URL
const processResponseData = (data) => {
  if (!data) return data
  
  if (Array.isArray(data)) {
    return data.map(item => processResponseData(item))
  }
  
  if (typeof data === 'object') {
    const processed = { ...data }
    
    // 处理主图
    if (processed.mainImage) {
      processed.mainImage = processImageUrl(processed.mainImage)
      console.log('处理后的主图URL:', processed.mainImage)  // 添加日志
    }
    
    // 处理单个图片
    if (processed.image) {
      processed.image = processImageUrl(processed.image)
      console.log('处理后的图片URL:', processed.image)  // 添加日志
    }
    
    // 处理图片数组
    if (processed.images && Array.isArray(processed.images)) {
      processed.images = processed.images
        .filter(url => url)  // 过滤掉空值
        .map(url => {
          const processedUrl = processImageUrl(url)
          console.log('处理后的图片数组URL:', processedUrl)  // 添加日志
          return processedUrl
        })
    }
    
    // 如果没有主图但有图片数组，使用第一张图片作为主图
    if (!processed.mainImage && processed.images && processed.images.length > 0) {
      processed.mainImage = processed.images[0]
      console.log('使用图片数组第一张作为主图:', processed.mainImage)  // 添加日志
    }
    
    return processed
  }
  
  return data
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    // 添加重试配置
    config.retryAttempts = 0
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 处理响应数据中的图片URL
    if (response.data && response.data.data) {
      response.data.data = processResponseData(response.data.data)
    }
    return response
  },
  async error => {
    const config = error.config

    // 如果是Redis连接错误，尝试重试
    if (error.response?.status === 500 && 
        error.response?.data?.message?.includes('Redis') &&
        (!config.retryAttempts || config.retryAttempts < maxRetryAttempts)) {
      
      config.retryAttempts = config.retryAttempts ? config.retryAttempts + 1 : 1
      
      await new Promise(resolve => setTimeout(resolve, retryDelay))
      return request(config)
    }

    // 处理404错误
    if (error.response?.status === 404) {
      ElMessage.error('请求的资源不存在')
      return Promise.reject(error)
    }

    // 处理500错误
    if (error.response?.status === 500) {
      return Promise.reject(error)
    }

    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.clearUserInfo()
      const router = useRouter()
      router.push('/admin/login')
      ElMessage.error('登录已过期，请重新登录')
    }
    return Promise.reject(error)
  }
)

export default request