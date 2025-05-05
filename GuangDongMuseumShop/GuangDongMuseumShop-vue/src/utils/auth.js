// Token 的 key 名称
const TOKEN_KEY = 'token'

// 获取 token
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

// 设置 token
export function setToken(token) {
  if (!token) {
    console.warn('尝试设置空token')
    return
  }
  localStorage.setItem(TOKEN_KEY, token)
}

// 移除 token
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
} 