import request from '@/utils/request'

// 获取用户消息列表
export function getMessages(params) {
  return request({
    url: '/api/messages',
    method: 'get',
    params
  })
}

// 获取未读消息数量
export function getUnreadCount() {
  return request({
    url: '/api/messages/unread-count',
    method: 'get'
  })
}

// 标记所有消息为已读
export function markAllAsRead() {
  return request({
    url: '/api/messages/mark-all-read',
    method: 'post'
  })
}

// 标记单条消息为已读
export function markAsRead(messageId) {
  return request({
    url: `/api/messages/${messageId}/mark-read`,
    method: 'post'
  })
}

// 管理员接口：获取所有消息
export function getAdminMessages(params) {
  return request({
    url: '/api/admin/messages',
    method: 'get',
    params
  })
}

// 管理员接口：发送系统消息
export function sendSystemMessage(data) {
  return request({
    url: '/api/admin/messages/send',
    method: 'post',
    data
  })
}

// 管理员接口：删除消息
export function deleteMessage(id) {
  return request({
    url: `/api/admin/messages/${id}`,
    method: 'delete'
  })
}