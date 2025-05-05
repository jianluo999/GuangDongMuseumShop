import request from '@/utils/request'

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/api/admin/users',
    method: 'get',
    params
  })
}

// 新增用户
export function addUser(data) {
  return request({
    url: '/api/admin/users',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(id, data) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'delete'
  })
}

// 启用用户
export function enableUser(id) {
  return request({
    url: `/api/admin/users/${id}/enable`,
    method: 'put'
  })
}

// 禁用用户
export function disableUser(id) {
  return request({
    url: `/api/admin/users/${id}/disable`,
    method: 'put'
  })
} 