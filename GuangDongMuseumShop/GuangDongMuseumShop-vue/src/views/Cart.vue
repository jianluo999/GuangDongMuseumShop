<template>
  <div class="cart-container">
    <!-- 购物车标题 -->
    <div class="cart-header">
      <h2>我的购物车</h2>
      <div class="cart-summary">
        已选择 <span class="selected-count">{{ selectedCount }}</span> 件商品
      </div>
    </div>

    <!-- 购物车主体 -->
    <div class="cart-main" v-loading="loading">
      <template v-if="cartStores.length">
        <div v-for="store in cartStores" :key="store.id" class="store-group">
          <div class="store-header">
            <el-checkbox 
              v-model="store.selected" 
              @change="(val) => handleStoreSelect(store.id, val)"
            >
              <div class="store-info">
                <el-icon><Shop /></el-icon>
                <span>{{ store.name }}</span>
              </div>
            </el-checkbox>
          </div>

          <!-- 商品列表 -->
          <div class="cart-items">
            <div v-for="item in store.items" :key="item.itemId" class="cart-item">
              <div class="item-content">
                <el-checkbox 
                  v-model="item.selected"
                  @change="(val) => handleItemSelect(store.id, item.itemId, val)"
                />
                <div class="item-image">
                  <el-image :src="item.productImage" fit="cover" />
                </div>
                <div class="item-info">
                  <h3 class="item-name">{{ item.productName }}</h3>
                  <p class="item-spec">{{ item.specification }}</p>
                  <div class="item-price">
                    <span class="price">¥{{ item.productPrice.toFixed(2) }}</span>
                    <el-input-number 
                      v-model="item.quantity" 
                      :min="1" 
                      :max="item.stock || 1"
                      :controls="true"
                      :precision="0"
                      @change="(val) => handleQuantityChange(item.itemId, val)"
                    />
                    <span class="stock-info" v-if="item.stock">
                      库存: {{ item.stock }}
                    </span>
                  </div>
                  <div class="item-total">
                    小计：<span class="total-price">¥{{ (item.productPrice * item.quantity).toFixed(2) }}</span>
                  </div>
                </div>
                <div class="item-actions">
                  <el-button link @click="handleRemoveItem(item.itemId)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 空购物车提示 -->
      <el-empty 
        v-else-if="!loading" 
        description="购物车还是空的，去挑选心仪的广府文创吧~"
      >
        <el-button type="primary" @click="$router.push('/categories/featured')">
          去逛逛
        </el-button>
      </el-empty>
    </div>

    <!-- 推荐商品区域 -->
    <div class="recommended-products" v-if="!loading">
      <h3 class="section-title">猜你喜欢</h3>
      <div class="product-list">
        <div v-for="product in recommendedProducts" :key="product.id" class="product-card" @click="goToProduct(product.id)">
          <el-image :src="product.mainImage || ''" fit="cover" class="product-image" />
          <div class="product-info">
            <h4 class="product-name">{{ product.name }}</h4>
            <p class="product-price">¥{{ (product.price || 0).toFixed(2) }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 购物车底部 -->
    <div class="cart-footer" v-if="cartStores.length">
      <div class="footer-left">
        <el-checkbox 
          v-model="allSelected"
          @change="handleSelectAll"
        >
          全选
        </el-checkbox>
      </div>
      <div class="footer-right">
        <div class="total-info">
          已选择 <span>{{ selectedCount }}</span> 件商品
          <div class="total-price">
            合计：<span>¥{{ totalPrice.toFixed(2) }}</span>
          </div>
        </div>
        <el-button 
          type="primary" 
          :disabled="selectedCount === 0"
          :loading="checkoutLoading"
          @click="handleCheckout"
        >
          结算
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Shop, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { useCheckoutStore } from '@/stores/checkout'
import { getRecommendedProducts } from '@/api/product'

const router = useRouter()
const loading = ref(false)
const checkoutLoading = ref(false)
const cartStore = useCartStore()
const checkoutStore = useCheckoutStore()
const recommendedProducts = ref([])

// 获取购物车数据
const fetchCartData = async () => {
  try {
    loading.value = true
    await cartStore.fetchCartItems()
  } catch (error) {
    console.error('获取购物车数据失败:', error)
    ElMessage.error(error.message || '获取购物车数据失败')
  } finally {
    loading.value = false
  }
}

// 按商家分组的购物车商品
const cartStores = computed(() => {
  const stores = {}
  cartStore.items.forEach(item => {
    if (!stores[item.storeId]) {
      stores[item.storeId] = {
        id: item.storeId,
        name: item.storeName || '广东省博物馆文创店',
        selected: false,
        items: []
      }
    }
    stores[item.storeId].items.push(item)
  })
  
  // 更新店铺选中状态
  Object.values(stores).forEach(store => {
    store.selected = store.items.every(item => item.selected)
  })
  
  return Object.values(stores)
})

// 计算属性
const allSelected = computed({
  get: () => cartStore.items.length > 0 && cartStore.items.every(item => item.selected),
  set: (value) => handleSelectAll(value)
})

const selectedCount = computed(() => 
  cartStore.items.filter(item => item.selected).length
)

const totalPrice = computed(() => {
  return cartStore.items
    .filter(item => item.selected)
    .reduce((total, item) => total + item.productPrice * item.quantity, 0)
})

// 事件处理方法
const handleStoreSelect = async (storeId, selected) => {
  try {
    const store = cartStores.value.find(s => s.id === storeId)
    if (!store) return
    
    await Promise.all(store.items.map(item => 
      cartStore.updateSelected(item.itemId, selected)
    ))
  } catch (error) {
    console.error('更新店铺商品选中状态失败:', error)
    ElMessage.error('更新选中状态失败')
  }
}

const handleItemSelect = async (storeId, itemId, selected) => {
  try {
    if (!itemId) {
      ElMessage.warning('商品ID无效')
      return
    }
    await cartStore.updateSelected(itemId, selected)
  } catch (error) {
    console.error('更新商品选中状态失败:', error)
    ElMessage.error(error.message || '更新选中状态失败')
  }
}

const handleSelectAll = async (selected) => {
  try {
    await Promise.all(cartStore.items.map(item => 
      cartStore.updateSelected(item.itemId, selected)
    ))
  } catch (error) {
    console.error('更新全选状态失败:', error)
    ElMessage.error('更新选中状态失败')
  }
}

const handleQuantityChange = async (itemId, quantity) => {
  try {
    if (!itemId) {
      ElMessage.warning('商品ID无效')
      return
    }
    
    const item = cartStore.items.find(i => i.itemId === itemId)
    if (!item) {
      ElMessage.warning('商品不存在')
      return
    }

    if (quantity < 1) {
      ElMessage.warning('商品数量不能小于1')
      return
    }

    // 保存原始数量
    const originalQuantity = item.quantity

    // 如果超出库存，自动调整为最大库存数量
    if (item.stock && quantity > item.stock) {
      quantity = item.stock
      ElMessage.warning(`已自动调整为最大库存数量：${item.stock}`)
    }

    try {
      await cartStore.updateQuantity(itemId, quantity)
    } catch (error) {
      // 如果更新失败，回滚到原始数量
      item.quantity = originalQuantity
      throw error
    }
  } catch (error) {
    console.error('更新商品数量失败:', error)
    ElMessage.error(error.message || '更新商品数量失败')
  }
}

const handleRemoveItem = async (itemId) => {
  try {
    if (!itemId) {
      ElMessage.warning('商品ID无效')
      return
    }
    
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      type: 'warning'
    })
    
    await cartStore.removeFromCart(itemId)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
      ElMessage.error(error.message || '删除商品失败')
    }
  }
}

const handleCheckout = async () => {
  try {
    checkoutLoading.value = true
    const selectedItems = cartStore.items.filter(item => item.selected)
    if (selectedItems.length === 0) {
      ElMessage.warning('请选择要结算的商品')
      return
    }
    
    // 将选中的商品保存到 checkout store
    checkoutStore.setCheckoutItems(selectedItems)
    router.push('/checkout')
  } catch (error) {
    console.error('结算失败:', error)
    ElMessage.error('结算失败')
  } finally {
    checkoutLoading.value = false
  }
}

// 获取推荐商品
const fetchRecommendedProducts = async () => {
  try {
    const response = await getRecommendedProducts()
    if (response.code === 200) {
      recommendedProducts.value = response.data || []
    } else {
      throw new Error(response.message || '获取推荐商品失败')
    }
  } catch (error) {
    console.error('获取推荐商品失败:', error)
    recommendedProducts.value = []
    ElMessage.error(error.message || '获取推荐商品失败')
  }
}

// 跳转到商品详情
const goToProduct = (productId) => {
  router.push(`/products/${productId}`)
}

// 生命周期钩子
onMounted(() => {
  fetchCartData()
  fetchRecommendedProducts()
})
</script>

<style lang="scss" scoped>
.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #f8f4eb;
  min-height: calc(100vh - 180px);

  .cart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 0 20px;
    
    h2 {
      font-size: 24px;
      color: #862D2D;
      font-weight: bold;
      position: relative;
      
      &:after {
        content: '';
        position: absolute;
        bottom: -8px;
        left: 0;
        width: 40px;
        height: 3px;
        background: #862D2D;
      }
    }

    .cart-summary {
      color: #666;
      font-size: 14px;
      
      .selected-count {
        color: #b4854d;
        font-weight: bold;
      }
    }
  }

  .cart-main {
    .store-group {
      background: #fff;
      border-radius: 8px;
      margin-bottom: 20px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
      overflow: hidden;

      .store-header {
        padding: 15px 20px;
        background: #862D2D;
        color: #fff;

        .store-info {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .el-icon {
            font-size: 18px;
          }
        }
      }

      .cart-items {
        .cart-item {
          padding: 20px;
          border-bottom: 1px solid #eee;

          &:last-child {
            border-bottom: none;
          }

          .item-content {
            display: flex;
            align-items: center;
            gap: 20px;

            .item-image {
              width: 120px;
              height: 120px;
              border-radius: 4px;
              overflow: hidden;
              border: 1px solid #e0d5c7;
              
              .el-image {
                width: 100%;
                height: 100%;
                transition: transform 0.3s;
                
                &:hover {
                  transform: scale(1.05);
                }
              }
            }

            .item-info {
              flex: 1;

              .item-name {
                font-size: 16px;
                color: #333;
                margin-bottom: 8px;
              }

              .item-spec {
                color: #666;
                font-size: 14px;
                margin-bottom: 12px;
              }

              .item-price {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin-bottom: 8px;

                .price {
                  color: #b4854d;
                  font-size: 18px;
                  font-weight: bold;
                }
              }

              .item-total {
                text-align: right;
                color: #666;
                
                .total-price {
                  color: #b4854d;
                  font-weight: bold;
                }
              }
            }
          }
        }
      }
    }
  }

  .recommended-products {
    margin-top: 40px;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    margin-bottom: 80px;

    .section-title {
      font-size: 20px;
      color: #862D2D;
      margin-bottom: 20px;
      padding-bottom: 10px;
      border-bottom: 2px solid #f0f0f0;
      position: relative;

      &:after {
        content: '';
        position: absolute;
        bottom: -2px;
        left: 0;
        width: 40px;
        height: 2px;
        background: #862D2D;
      }
    }

    .product-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 20px;
      
      .product-card {
        cursor: pointer;
        transition: all 0.3s;
        border-radius: 8px;
        overflow: hidden;
        background: #fff;
        border: 1px solid #e0d5c7;
        
        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
        }

        .product-image {
          width: 100%;
          height: 200px;
          object-fit: cover;
        }

        .product-info {
          padding: 12px;

          .product-name {
            font-size: 14px;
            color: #333;
            margin-bottom: 8px;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
          }

          .product-price {
            color: #b4854d;
            font-size: 16px;
            font-weight: bold;
          }
        }
      }
    }
  }

  .cart-footer {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    padding: 15px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
    z-index: 10;

    .footer-right {
      display: flex;
      align-items: center;
      gap: 20px;

      .total-info {
        text-align: right;
        
        span {
          color: #b4854d;
          font-weight: bold;
        }

        .total-price {
          margin-top: 4px;
          font-size: 18px;
          
          span {
            color: #b4854d;
            font-weight: bold;
          }
        }
      }

      .el-button {
        background: #862D2D;
        border-color: #862D2D;
        padding: 12px 30px;
        font-size: 16px;
        
        &:hover {
          background: #9A3434;
          border-color: #9A3434;
        }
        
        &:disabled {
          background: #ccc;
          border-color: #ccc;
        }
      }
    }
  }

  @media screen and (max-width: 768px) {
    padding: 10px;

    .cart-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 10px;
    }

    .cart-main {
      .store-group {
        .cart-items {
          .cart-item {
            .item-content {
              flex-direction: column;

              .item-image {
                width: 100%;
                height: 200px;
              }

              .item-info {
                width: 100%;
              }
            }
          }
        }
      }
    }
  }
}

.stock-info {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.item-price {
  display: flex;
  align-items: center;
  margin: 8px 0;
}

.price {
  color: #ff4d4f;
  font-size: 16px;
  font-weight: bold;
  margin-right: 16px;
}
</style>