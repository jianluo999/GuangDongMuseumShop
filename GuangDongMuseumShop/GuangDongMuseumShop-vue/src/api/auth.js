import request from '@/utils/request'

// 登录接口
export const login = (data) => {
  console.log('=== 发起登录请求 ===')
  console.log('登录数据:', data)
  return request({
    url: '/auth/login',
    method: 'post',
    data
  }).then(response => {
    // 确保返回完整的响应数据
    return response
  })
}

// 注册接口
export const register = (data) => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 退出登录
export const logout = () => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

// 获取用户信息
export const getUserProfile = () => {
  return request({
    url: '/api/users/profile',
    method: 'get'
  })
}
