import request from '@/utils/request'

// 获取评论列表
export function getReviews(params) {
  return request({
    url: '/api/admin/reviews',
    method: 'get',
    params
  })
}

// 切换评论可见性
export function toggleReviewVisible(id, visible) {
  return request({
    url: `/api/admin/reviews/${id}/visible`,
    method: 'put',
    data: { visible }
  })
}

// 删除评论
export function deleteReview(id) {
  return request({
    url: `/api/admin/reviews/${id}`,
    method: 'delete'
  })
}

export function checkReviewStatus(productId) {
  return request({
    url: `/api/products/${productId}/review-status`,
    method: 'get'
  })
}

export function submitReview(productId, reviewData) {
  return request({
    url: `/api/products/${productId}/reviews`,
    method: 'post',
    data: reviewData
  })
} 