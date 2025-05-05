<template>
  <div class="search-page">
    <div class="search-header">
      <h1>搜索结果: {{ searchKeyword }}</h1>
      <p v-if="total > 0">共找到 {{ total }} 个相关商品</p>
      <p v-else>未找到相关商品</p>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else>
      <div v-if="products.length" class="product-grid">
        <el-card v-for="product in products" 
          :key="product.id" 
          class="product-card"
          @click="handleProductClick(product.id)"
        >
          <img :src="product.coverImage" class="product-image" />
          <div class="product-info">
            <h3 class="product-title">{{ product.name }}</h3>
            <p class="product-price">¥{{ product.price }}</p>
          </div>
        </el-card>
      </div>

      <div v-else class="no-results">
        <el-empty description="暂无搜索结果" />
      </div>

      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchProducts } from '@/api/product'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const searchKeyword = ref('')

const fetchSearchResults = async () => {
  if (!searchKeyword.value) return
  
  loading.value = true
  try {
    const response = await searchProducts({
      keyword: searchKeyword.value,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    if (response.data.code === 200) {
      products.value = response.data.data.content || []
      total.value = response.data.data.totalElements || 0
    } else {
      ElMessage.error(response.data.message || '搜索失败')
    }
  } catch (error) {
    console.error('搜索商品失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 监听路由参数变化
watch(
  () => route.query.q,
  (newQuery) => {
    if (newQuery) {
      searchKeyword.value = newQuery
      currentPage.value = 1
      fetchSearchResults()
    }
  },
  { immediate: true }
)

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchSearchResults()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchSearchResults()
}

// 处理商品点击
const handleProductClick = (productId) => {
  router.push(`/products/${productId}`)
}

onMounted(() => {
  if (route.query.q) {
    searchKeyword.value = route.query.q
    fetchSearchResults()
  }
})
</script>

<style scoped>
.search-page {
  padding: 20px;
}

.search-header {
  margin-bottom: 24px;
}

.search-header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.search-header p {
  color: #666;
  font-size: 14px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.product-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  padding: 12px 0;
}

.product-title {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  color: #862D2D;
  font-size: 18px;
  font-weight: 600;
}

.loading-container {
  padding: 20px;
}

.no-results {
  text-align: center;
  padding: 40px 0;
}

.pagination-container {
  text-align: center;
  margin-top: 32px;
}

:deep(.el-pagination) {
  justify-content: center;
}
</style> 