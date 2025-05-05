import request from '@/utils/request'

// 获取销售趋势数据
export function getSalesTrend(days) {
  return request({
    url: '/api/admin/stats/sales/trend',
    method: 'get',
    params: { days }
  })
} 