import request from '@/utils/request'

const BASE_URL = '/api/products'

// 获取分类产品
export const getCategoryProducts = (categoryId, params) => {
  return request({
    url: `${BASE_URL}/category/${categoryId}`,
    method: 'get',
    params: {
      page: params.page || 0,
      size: params.size || 12,
      sort: params.sort || 'createdAt,desc'
    }
  })
}

// 获取热门搜索
export const getHotSearches = () => {
  return request({
    url: `${BASE_URL}/hot-searches`,
    method: 'get'
  })
}

// 搜索产品
export const searchProducts = (params) => {
  return request({
    url: `${BASE_URL}/search`,
    method: 'get',
    params: {
      keyword: params.keyword,
      page: params.page || 0,
      size: params.size || 12
    }
  })
}

// 获取产品详情
export const getProductDetail = (id) => {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'get'
  })
}

// 获取分类列表
export const getCategories = () => {
  return request({
    url: '/api/categories',
    method: 'get'
  })
}

// 统一使用这个方法获取产品列表
export const getProducts = (params) => {
  return request({
    url: BASE_URL,
    method: 'get',
    params
  })
}

// 获取精选商品
export const getFeaturedProducts = (params) => {
  return request({
    url: '/api/products/featured',
    method: 'get',
    params
  })
}

// 获取新品
export const getNewProducts = (params) => {
  return request({
    url: '/api/products/new',
    method: 'get',
    params: {
      ...params,
      sort: 'createdAt,desc'
    }
  })
}

// 获取推荐商品
export const getRecommendedProducts = async () => {
  const response = await request({
    url: `${BASE_URL}/recommended`,
    method: 'get'
  })
  return {
    code: response.data?.code || response.code,
    message: response.data?.message || response.message,
    data: response.data?.data || response
  }
} 