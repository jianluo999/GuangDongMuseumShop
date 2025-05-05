import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false,
    error: null
  }),

  getters: {
    totalCount: (state) => {
      if (!Array.isArray(state.items)) return 0
      return state.items.reduce((total, item) => total + (Number(item?.quantity) || 0), 0)
    },
    
    totalAmount: (state) => {
      if (!Array.isArray(state.items)) return 0
      return state.items.reduce((total, item) => {
        const price = Number(item?.productPrice || 0)
        const quantity = Number(item?.quantity || 0)
        return total + (price * quantity)
      }, 0)
    }
  },

  actions: {
    async fetchCartItems() {
      this.loading = true
      this.error = null
      try {
        const { data } = await request.get('/api/cart')
        if (data && data.code === 200) {
          // 确保 items 是数组
          const items = Array.isArray(data.data) ? data.data : []
          this.items = items.map(item => ({
            ...item,
            itemId: Number(item.itemId || item.id),
            productPrice: Number(item.productPrice || item.price || 0),
            storeId: Number(item.storeId || 1),
            storeName: item.storeName || '广东省博物馆文创店',
            selected: item.selected === undefined ? true : item.selected,
            quantity: Number(item.quantity || 1),
            productImage: item.productImage || item.mainImage || item.image || 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjI0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mH5Yqg6L295LitPC90ZXh0Pjwvc3ZnPg=='
          }))
          return this.items
        }
        throw new Error(data?.message || '获取购物车数据失败')
      } catch (error) {
        console.error('获取购物车数据失败:', error)
        this.error = error.message || '获取购物车数据失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    async addToCart(productId, quantity = 1) {
      try {
        const { data } = await request.post('/api/cart', {
          productId: Number(productId),
          quantity: Number(quantity)
        })
        if (data && data.code === 200) {
          await this.fetchCartItems()
          return true
        }
        throw new Error(data?.message || '添加到购物车失败')
      } catch (error) {
        console.error('添加到购物车失败:', error)
        throw error
      }
    },

    async updateQuantity(itemId, quantity) {
      try {
        const { data } = await request.put('/api/cart/items/quantity', {
          itemId: Number(itemId),
          quantity: Number(quantity)
        })
        if (data && data.code === 200) {
          const index = this.items.findIndex(item => item.itemId === itemId)
          if (index !== -1) {
            this.items[index] = {
              ...this.items[index],
              quantity: Number(quantity)
            }
          }
          return true
        }
        throw new Error(data?.message || '更新商品数量失败')
      } catch (error) {
        console.error('更新商品数量失败:', error)
        throw error
      }
    },

    async updateSelected(itemId, selected) {
      try {
        const { data } = await request.put('/api/cart/items/selected', {
          itemId: Number(itemId),
          selected: Boolean(selected)
        })
        if (data && data.code === 200) {
          const index = this.items.findIndex(item => item.itemId === itemId)
          if (index !== -1) {
            this.items[index] = {
              ...this.items[index],
              selected: Boolean(selected)
            }
          }
          return true
        }
        throw new Error(data?.message || '更新选中状态失败')
      } catch (error) {
        console.error('更新选中状态失败:', error)
        throw error
      }
    },

    async removeFromCart(itemId) {
      try {
        const { data } = await request.delete('/api/cart/items', {
          data: { itemId: Number(itemId) }
        })
        if (data && data.code === 200) {
          this.items = this.items.filter(item => item.itemId !== itemId)
          return true
        }
        throw new Error(data?.message || '删除商品失败')
      } catch (error) {
        console.error('删除商品失败:', error)
        throw error
      }
    },

    async clearCart() {
      try {
        const { data } = await request.delete('/api/cart')
        if (data && data.code === 200) {
          this.items = []
          this.loading = false
          this.error = null
          return true
        }
        throw new Error(data?.message || '清空购物车失败')
      } catch (error) {
        console.error('清空购物车失败:', error)
        throw error
      }
    }
  }
}) 