export const storage = {
  get(key) {
    try {
      const value = localStorage.getItem(key)
      if (!value) return null
      
      // 检查是否是JWT token格式（以eyJ开头的字符串）
      if (value.startsWith('eyJ')) {
        return value // 如果是token，直接返回字符串
      }
      
      return JSON.parse(value)
    } catch (e) {
      console.error(`读取 ${key} 失败:`, e)
      return null
    }
  },

  set(key, value) {
    try {
      // 如果是对象，转换为JSON字符串
      const saveValue = typeof value === 'object' ? 
        JSON.stringify(value) : 
        String(value)
      
      localStorage.setItem(key, saveValue)
    } catch (e) {
      console.error(`存储 ${key} 失败:`, e)
    }
  },

  remove(key) {
    try {
      localStorage.removeItem(key)
    } catch (e) {
      console.error(`删除 ${key} 失败:`, e)
    }
  }
} 