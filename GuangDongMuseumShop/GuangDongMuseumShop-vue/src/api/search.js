import request from '@/utils/request'

// 获取热门搜索
export function getHotSearches() {
  return request({
    url: '/api/search/hot',
    method: 'get'
  })
}

// 保存搜索记录
export function saveSearchHistory(keyword) {
  return request({
    url: '/api/search/history',
    method: 'post',
    data: { keyword }
  })
}

// 获取搜索历史
export function getSearchHistory() {
  return request({
    url: '/api/search/history',
    method: 'get'
  })
}

// 清除搜索历史
export function clearSearchHistory() {
  return request({
    url: '/api/search/history',
    method: 'delete'
  })
} 