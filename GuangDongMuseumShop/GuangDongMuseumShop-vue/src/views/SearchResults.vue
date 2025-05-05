<template>
  <div class="search-results">
    <div class="search-header">
      <h2>搜索结果: {{ $route.query.q }}</h2>
    </div>

    <div class="results-container" v-loading="loading">
      <el-empty v-if="!loading && products.length === 0" description="暂无相关商品" />
      
      <div class="products-grid" v-else>
        <el-card
          v-for="product in products"
          :key="product.id"
          class="product-card"
          @click="goToProduct(product.id)"
        >
          <el-image 
            :src="product.mainImage || (product.images && product.images.length > 0 ? product.images[0] : defaultImage)" 
            :alt="product.name" 
            class="product-image"
            fit="cover"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p class="price">¥{{ product.price }}</p>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
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
import { Picture } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const products = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 添加默认图片
const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjI0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mH5Yqg6L295LitPC90ZXh0Pjwvc3ZnPg=='

// 获取搜索结果
const fetchSearchResults = async () => {
  const keyword = route.query.q
  if (!keyword) return

  loading.value = true
  try {
    const result = await searchProducts({
      keyword,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    if (result.code === 200) {
      console.log('搜索结果原始数据:', result.data)
      products.value = result.data.content || []
      products.value = products.value.map(product => {
        console.log('===== 开始处理商品数据 =====')
        console.log('商品ID:', product.id)
        console.log('商品名称:', product.name)
        console.log('处理前的商品数据:', JSON.stringify(product))
        
        // 确保每个商品都有图片数组
        if (!product.images) {
          console.log('商品没有images数组，初始化为空数组')
          product.images = []
        }
        
        // 处理主图URL
        console.log('处理前的主图:', product.mainImage)
        if (product.mainImage && product.mainImage !== '(Null)') {
          // 移除域名前缀
          if (product.mainImage.includes('localhost:8080')) {
            product.mainImage = product.mainImage.replace(/http:\/\/localhost:8080/, '')
            console.log('处理后的主图URL(移除域名):', product.mainImage)
          }
          // 确保URL以/api/开头
          if (!product.mainImage.startsWith('/api/')) {
            product.mainImage = '/api/' + product.mainImage.replace(/^\//, '')
            console.log('处理后的主图URL(添加/api/):', product.mainImage)
          }
        } else {
          console.log('主图为空或为(Null)，设置为null')
          product.mainImage = null
        }
        
        // 处理图片数组中的URL
        console.log('处理前的图片数组:', product.images)
        product.images = product.images
          .filter(url => {
            const isValid = url && url.trim() !== '' && url !== '(Null)'
            if (!isValid) {
              console.log('过滤掉无效的图片URL:', url)
            }
            return isValid
          })
          .map(url => {
            let processedUrl = url
            // 移除域名前缀
            if (processedUrl.includes('localhost:8080')) {
              processedUrl = processedUrl.replace(/http:\/\/localhost:8080/, '')
              console.log('处理图片URL(移除域名):', processedUrl)
            }
            // 确保URL以/api/开头
            if (!processedUrl.startsWith('/api/')) {
              processedUrl = '/api/' + processedUrl.replace(/^\//, '')
              console.log('处理图片URL(添加/api/):', processedUrl)
            }
            return processedUrl
          })
        console.log('处理后的图片数组:', product.images)
        
        // 如果有 mainImage，确保它在 images 数组中
        if (product.mainImage && !product.images.includes(product.mainImage)) {
          console.log('将主图添加到图片数组开头')
          product.images.unshift(product.mainImage)
        }
        
        // 如果没有主图但有图片数组，使用第一张图片作为主图
        if (!product.mainImage && product.images.length > 0) {
          console.log('使用第一张图片作为主图')
          product.mainImage = product.images[0]
        }
        
        // 如果既没有主图也没有图片数组，使用默认图片
        if (!product.mainImage && product.images.length === 0) {
          console.log('使用默认图片')
          product.mainImage = defaultImage
          product.images = [defaultImage]
        }
        
        console.log('处理后的商品数据:', JSON.stringify(product))
        console.log('===== 商品数据处理完成 =====')
        return product
      })
      total.value = result.data.totalElements || 0
    } else {
      ElMessage.error(result.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取搜索结果失败:', error)
    ElMessage.error('获取搜索结果失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 跳转到商品详情
const goToProduct = (id) => {
  router.push(`/product/${id}`)
}

// 处理分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchSearchResults()
}

// 处理页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchSearchResults()
}

// 监听路由参数变化
watch(
  () => route.query.q,
  (newVal) => {
    if (newVal) {
      currentPage.value = 1
      fetchSearchResults()
    }
  }
)

// 组件挂载时获取数据
onMounted(() => {
  if (route.query.q) {
    fetchSearchResults()
  }
})
</script>

<style scoped>
.search-results {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.search-header {
  margin-bottom: 20px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.product-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 10px;
}

.product-info h3 {
  margin: 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  margin: 10px 0 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 