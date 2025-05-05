import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

// 设置语言为中文
dayjs.locale('zh-cn')

/**
 * 格式化日期
 * @param {string | Date} date 日期字符串或Date对象
 * @param {string} fmt 格式化模式，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, fmt = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  
  // 如果是字符串日期，转换为Date对象
  if (typeof date === 'string') {
    date = new Date(date)
  }
  
  const o = {
    'M+': date.getMonth() + 1, // 月份
    'D+': date.getDate(), // 日
    'H+': date.getHours(), // 小时
    'm+': date.getMinutes(), // 分
    's+': date.getSeconds(), // 秒
    'q+': Math.floor((date.getMonth() + 3) / 3), // 季度
    'S': date.getMilliseconds() // 毫秒
  }

  if (/(Y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
  }

  for (let k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(
        RegExp.$1,
        RegExp.$1.length === 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length)
      )
    }
  }
  
  return fmt
}

/**
 * 格式化日期时间
 * @param {string | Date} date 日期时间
 * @param {string} format 格式化模式，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return '-'
  try {
    return dayjs(date).format(format)
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '-'
  }
}

/**
 * 格式化金额
 * @param {number} amount 金额
 * @param {number} decimals 小数位数，默认2位
 * @returns {string} 格式化后的金额字符串
 */
export function formatAmount(amount, decimals = 2) {
  if (!amount && amount !== 0) return '0.00'
  try {
    return Number(amount).toFixed(decimals)
  } catch (error) {
    console.error('金额格式化错误:', error)
    return '0.00'
  }
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @returns {string} 格式化后的文件大小
 */
export function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  
  while (bytes >= 1024 && i < units.length - 1) {
    bytes /= 1024
    i++
  }
  
  return `${bytes.toFixed(2)} ${units[i]}`
}

/**
 * 格式化手机号
 * @param {string} phone 手机号
 * @returns {string} 格式化后的手机号（中间4位用*替代）
 */
export function formatPhone(phone) {
  if (!phone) return ''
  try {
    return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
  } catch (error) {
    console.error('手机号格式化错误:', error)
    return phone
  }
} 