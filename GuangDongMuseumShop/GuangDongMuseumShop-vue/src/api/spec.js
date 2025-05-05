import request from '@/utils/request'

// 获取所有规格模板
export function getAllTemplates() {
  return request({
    url: '/api/specs/templates',
    method: 'get'
  })
}

// 根据分类获取规格模板
export function getTemplatesByCategory(categoryId) {
  return request({
    url: `/api/specs/templates/${categoryId}`,
    method: 'get'
  })
}

// 根据模板代码获取规格模板
export function getTemplateByCode(code) {
  return request({
    url: `/api/specs/templates/code/${code}`,
    method: 'get'
  })
}

// 获取商品规格
export function getProductSpecs(productId) {
  return request({
    url: `/api/products/${productId}/specs`,
    method: 'get'
  })
}

// 保存商品规格
export function saveProductSpecs(productId, specs) {
  return request({
    url: `/api/products/${productId}/specs`,
    method: 'post',
    data: specs
  })
}

// 删除商品规格
export function deleteProductSpecs(productId) {
  return request({
    url: `/api/products/${productId}/specs`,
    method: 'delete'
  })
}

// 根据模板生成规格
export function generateSpecs(templateCode) {
  return request({
    url: '/api/specs/generate',
    method: 'get',
    params: { templateCode }
  })
} 