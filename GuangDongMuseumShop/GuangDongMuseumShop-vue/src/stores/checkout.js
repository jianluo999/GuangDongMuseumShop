import { defineStore } from 'pinia'

export const useCheckoutStore = defineStore('checkout', {
  state: () => ({
    checkoutItems: [],
    deliveryAddress: null,
    paymentMethod: null
  }),

  getters: {
    totalAmount: (state) => {
      return state.checkoutItems.reduce((sum, item) => {
        return sum + item.productPrice * item.quantity
      }, 0)
    },
    
    totalCount: (state) => {
      return state.checkoutItems.reduce((sum, item) => sum + item.quantity, 0)
    }
  },

  actions: {
    setCheckoutItems(items) {
      this.checkoutItems = items
    },

    setDeliveryAddress(address) {
      this.deliveryAddress = address
    },

    setPaymentMethod(method) {
      this.paymentMethod = method
    },

    clearCheckoutData() {
      this.checkoutItems = []
      this.deliveryAddress = null
      this.paymentMethod = null
    }
  }
}) 