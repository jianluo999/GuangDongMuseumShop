import service from '@/utils/request'

const BASE_URL = '/api/auth'

// 为了保持兼容性，导出原来的名称
export const userRegisterService = (data) => {
  console.log('尝试注册路径1:', `${BASE_URL}/register`)
  return service({
    url: `${BASE_URL}/register`,
    method: 'post',
    data
  }).catch(error => {
    console.log('尝试注册路径1失败，尝试路径2')
    return service({
      url: '/legacy/auth/register',
      method: 'post',
      data
    }).catch(error => {
      console.log('尝试注册路径2失败，尝试路径3')
      return service({
        url: '/auth/register',
        method: 'post',
        data
      })
    })
  })
}

export const userLoginService = (data) => {
  return service({
    url: `${BASE_URL}/login`,
    method: 'post',
    data: {
      username: data.username.trim(),
      password: data.password,
      clientType: 'web'
    }
  })
}

// 获取用户信息
export const getUserProfileService = () => {
  return service({
    url: '/users/profile',
    method: 'get'
  })
}

// 更新用户信息
export const updateUserProfileService = (data) => {
  return service({
    url: '/users/profile',
    method: 'put',
    data: {
      username: data.username?.trim(),
      email: data.email?.trim(),
      phone: data.phone?.trim()
    }
  })
}

// 修改密码
export const changePasswordService = ({ oldPassword, newPassword }) => {
  return service({
    url: '/users/password',
    method: 'put',
    data: {
      oldPassword,
      newPassword
    }
  })
}

// 更新头像
export const updateAvatarService = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return service({
    url: '/users/avatar',
    method: 'put',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
