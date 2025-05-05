import request from '@/utils/request'

// 获取评论列表
export function getReviewList(params) {
  return request({
    url: '/api/admin/reviews',
    method: 'get',
    params
  })
}

// 更新评价显示状态
export function updateReviewVisible(id, visible) {
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