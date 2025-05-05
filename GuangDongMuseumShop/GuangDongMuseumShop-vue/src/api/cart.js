import request from '@/utils/request'

const BASE_URL = '/api/cart'

// 获取购物车列表
export function getCartList() {
  return request({
    url: BASE_URL,
    method: 'get'
  })
}

// 更新购物车商品数量
export function updateCartItemQuantity(itemId, quantity) {
  return request({
    url: `${BASE_URL}/items/quantity`,
    method: 'put',
    data: { itemId, quantity }
  })
}

// 删除购物车商品
export function removeCartItem(productId) {
  return request({
    url: `${BASE_URL}/${productId}`,
    method: 'delete'
  })
}

// 更新商品选中状态
export function updateCartItemSelected(productId, selected) {
  return request({
    url: `${BASE_URL}/${productId}/selected`,
    method: 'put',
    params: { selected }
  })
}

// 全选/取消全选
export function updateAllSelected(selected) {
  return request({
    url: `${BASE_URL}/selected/all`,
    method: 'put',
    data: { selected }
  })
}

// 清空购物车
export function clearCart() {
  return request({
    url: BASE_URL,
    method: 'delete'
  })
} 