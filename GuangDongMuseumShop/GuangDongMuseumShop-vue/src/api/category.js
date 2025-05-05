import request from '@/utils/request'

export function getCategories() {
  return request({
    url: '/api/admin/categories/all',  // 修改这里，使用正确的API端点
    method: 'get'
  })
} 