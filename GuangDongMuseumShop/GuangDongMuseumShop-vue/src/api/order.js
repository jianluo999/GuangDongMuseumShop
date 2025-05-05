import request from '@/utils/request'

export const getOrders = (params) => {
  return request({
    url: '/api/orders',
    method: 'get',
    params
  }).then(response => {
    console.log('获取订单响应:', response)
    return response.data
  }).catch(error => {
    console.error('获取订单列表失败:', error)
    throw error
  })
}

export const createOrder = (data) => {
  console.log('=== 发起创建订单请求 ===')
  console.log('订单数据:', data)
  return request({
    url: '/api/orders',
    method: 'post',
    data
  })
  .then(response => {
    console.log('创建订单响应:', response)
    return response
  })
  .catch(error => {
    console.error('创建订单失败:', error)
    throw error
  })
}

// 创建订单支付
export function createOrderPayment(orderId) {
  console.log('发起支付请求，订单ID:', orderId)
  return request({
    url: `/api/payments/orders/${orderId}/pay`,
    method: 'post'
  }).then(response => {
    console.log('支付响应:', response)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '支付失败')
    }
    return response.data
  })
}

// 获取订单支付二维码
export function getPaymentQRCode(orderId, paymentMethod) {
  return request({
    url: `/api/payments/orders/${orderId}/qrcode`,
    method: 'post',
    data: { paymentMethod }
  })
}

// 查询支付状态
export function checkPaymentStatus(orderId) {
  return request({
    url: `/api/payments/orders/${orderId}/status`,
    method: 'get'
  })
}

// 确认支付
export function confirmPayment(orderId) {
  return request({
    url: `/api/payments/orders/${orderId}/confirm`,
    method: 'post'
  })
}

// 取消支付
export function cancelPayment(orderId) {
  console.log('发起取消支付请求，订单ID:', orderId)
  return request({
    url: `/api/payments/orders/${orderId}/cancel`,
    method: 'post'
  }).then(response => {
    console.log('取消支付响应:', response)
    return response
  }).catch(error => {
    console.error('取消支付失败:', error)
    throw error
  })
}

export function cancelOrder(orderId) {
  return request({
    url: `/api/orders/${orderId}/cancel`,
    method: 'post'
  })
} 