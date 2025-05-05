import request from '@/utils/request'

// 获取商品评价列表
export function getProductReviews(productId: number, params: any) {
  return request({
    url: `/api/products/${productId}/reviews`,
    method: 'get',
    params
  })
}

// 获取评价统计
export function getReviewStats(productId: number) {
  return request({
    url: `/api/products/${productId}/review-stats`,
    method: 'get'
  })
}

// 检查评价状态
export function checkReviewStatus(productId: number) {
  return request({
    url: `/api/products/${productId}/review-status`,
    method: 'get'
  })
}

// 提交评价
export function submitReview(productId: number, data: any) {
  return request({
    url: `/api/products/${productId}/reviews`,
    method: 'post',
    data
  })
} 