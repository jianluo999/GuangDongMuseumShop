import request from '@/utils/request'

// 获取购物车列表
export function getCartList(params) {
  return request({
    url: '/api/admin/carts',
    method: 'get',
    params
  })
}

// 删除购物车
export function deleteCart(id) {
  return request({
    url: `/api/admin/carts/${id}`,
    method: 'delete'
  })
}

// 批量删除购物车
export function batchDeleteCarts(ids) {
  return request({
    url: '/api/admin/carts/batch',
    method: 'delete',
    data: { ids }
  })
} 