import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => {
    // 从 localStorage 恢复状态
    const token = localStorage.getItem('token')
    let userInfo = {}
    try {
      const storedInfo = localStorage.getItem('userInfo')
      if (storedInfo) {
        userInfo = JSON.parse(storedInfo)
      }
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
    
    return {
      token: token || '',
      userInfo: userInfo,
      isLoggedIn: !!token && !!userInfo.username
    }
  },

  actions: {
    async login(credentials) {
      try {
        const response = await request.post('/auth/login', credentials)
        console.log('用户登录响应:', response)
        
        // 检查响应数据
        const responseData = response.data
        if (!responseData?.data?.token) {
          console.error('登录响应数据格式:', responseData)
          throw new Error('登录失败：未获取到token')
        }

        // 从响应中获取 token 和用户信息
        const token = responseData.data.token
        const userInfo = {
          username: credentials.username,
          role: 'ROLE_USER',
          roles: ['ROLE_USER']
        }
        
        // 保存登录状态
        this.setUserState(token, userInfo)
        return responseData
      } catch (error) {
        console.error('用户登录失败:', error)
        throw new Error(error.response?.data?.message || '登录失败，请稍后重试')
      }
    },

    async adminLogin(credentials) {
      try {
        const response = await request.post('/api/admin/login', credentials)
        console.log('管理员登录响应:', response)
        
        if (!response?.data?.token) {
          throw new Error('登录失败：未获取到token')
        }

        const token = response.data.token
        const userInfo = {
          username: credentials.username,
          role: 'ROLE_ADMIN',
          roles: ['ROLE_ADMIN', 'ROLE_USER']
        }
        
        this.setUserState(token, userInfo)
        return response.data
      } catch (error) {
        console.error('管理员登录失败:', error)
        throw error
      }
    },

    setUserState(token, userInfo) {
      this.token = token
      this.userInfo = userInfo
      this.isLoggedIn = true
      
      // 保存到 localStorage
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      
      console.log('用户状态已更新:', { token, userInfo })
    },

    logout() {
      this.clearUserInfo()
    },

    clearUserInfo() {
      this.token = ''
      this.userInfo = {}
      this.isLoggedIn = false
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      console.log('用户状态已清除')
    },

    setLoginState(status, username = '', token = '') {
      this.isLoggedIn = status
      this.userInfo.username = username
      this.token = token
      if (status) {
        localStorage.setItem('token', token)
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      } else {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      }
    },

    checkLoginState() {
      const token = localStorage.getItem('token')
      let userInfo = null
      
      try {
        const storedInfo = localStorage.getItem('userInfo')
        if (storedInfo) {
          userInfo = JSON.parse(storedInfo)
        }
      } catch (e) {
        console.error('解析存储的用户信息失败:', e)
      }
      
      if (token && userInfo?.username) {
        this.setUserState(token, userInfo)
        return true
      }
      
      this.clearUserInfo()
      return false
    },

    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    }
  },

  getters: {
    isAuthenticated: (state) => state.isLoggedIn && !!state.token,
    username: (state) => state.userInfo?.username || '',
    roles: (state) => state.userInfo?.roles || []
  }
})