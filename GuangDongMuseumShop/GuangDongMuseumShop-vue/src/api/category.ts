import request from '@/utils/request'

export function getCategories(params: any) {
  return request({
    url: '/api/admin/categories',
    method: 'get',
    params
  })
}

export function toggleCategoryStatus(id: number) {
  return request({
    url: `/api/admin/categories/${id}/status`,
    method: 'put'
  })
} 