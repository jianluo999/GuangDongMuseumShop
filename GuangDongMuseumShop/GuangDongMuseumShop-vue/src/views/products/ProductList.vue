<template>
  <div class="product-list">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ categoryName || '文创商品' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 分类标题 -->
    <div class="category-header">
      <h1>{{ categoryName || '文创商品' }}</h1>
      <p class="subtitle">精美文创，书写历史</p>
    </div>

    <!-- 筛选器 -->
    <div class="filter-section">
      <div class="price-range">
        <span class="label">价格区间：</span>
        <el-radio-group v-model="priceRange" @change="handlePriceRangeChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="0-100">0-100元</el-radio-button>
          <el-radio-button label="100-500">100-500元</el-radio-button>
          <el-radio-button label="500-1000">500-1000元</el-radio-button>
          <el-radio-button label="1000+">1000元以上</el-radio-button>
        </el-radio-group>
      </div>
      <div class="sort-options">
        <span class="label">排序方式：</span>
        <el-radio-group v-model="sortType" @change="handleSortChange">
          <el-radio-button label="default">默认</el-radio-button>
          <el-radio-button label="priceAsc">价格从低到高</el-radio-button>
          <el-radio-button label="priceDesc">价格从高到低</el-radio-button>
          <el-radio-button label="salesDesc">销量优先</el-radio-button>
          <el-radio-button label="ratingDesc">好评优先</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 商品列表 -->
    <el-row v-loading="loading" :gutter="24">
      <el-col 
        v-for="product in products" 
        :key="product.id" 
        :xs="24" 
        :sm="12" 
        :md="8" 
        :lg="6"
        class="product-item"
      >
        <el-card 
          :body-style="{ padding: '0px' }" 
          shadow="hover"
          @click="handleProductClick(product.id)"
        >
          <!-- 品牌标识 -->
          <div class="brand-logo">
            <el-icon class="museum-logo"><Collection /></el-icon>
          </div>
          
          <img :src="product.mainImage || defaultImage" class="product-image">
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-meta">
              <div class="price-rating">
                <span class="price">¥{{ product.price }}</span>
                <div class="rating">
                  <el-rate
                    v-model="product.rating"
                    disabled
                    text-color="#ff9900"
                    score-template="{value}"
                  />
                  <span class="rating-score">{{ product.rating }}</span>
                </div>
              </div>
              <el-button type="primary" size="small" @click.stop="handleProductClick(product.id)">
                查看详情
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分页器 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Collection } from '@element-plus/icons-vue'
import { getProducts } from '@/api/product'

const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjI0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mH5Yqg6L295LitPC90ZXh0Pjwvc3ZnPg=='

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const priceRange = ref('all')
const sortType = ref('default')

const categoryName = computed(() => {
  return route.query.categoryName || '文创商品'
})

const queryParams = ref({
  categoryId: route.query.categoryId,
  keyword: route.query.keyword,
  page: 1,
  size: 12,
  sort: 'createdAt,desc',
  minPrice: null,
  maxPrice: null
})

const handleProductClick = (productId) => {
  router.push(`/products/${productId}`)
}

const handlePriceRangeChange = (range) => {
  switch(range) {
    case '0-100':
      queryParams.value.minPrice = 0
      queryParams.value.maxPrice = 100
      break
    case '100-500':
      queryParams.value.minPrice = 100
      queryParams.value.maxPrice = 500
      break
    case '500-1000':
      queryParams.value.minPrice = 500
      queryParams.value.maxPrice = 1000
      break
    case '1000+':
      queryParams.value.minPrice = 1000
      queryParams.value.maxPrice = null
      break
    default:
      queryParams.value.minPrice = null
      queryParams.value.maxPrice = null
  }
  loadProducts()
}

const handleSortChange = (sort) => {
  switch(sort) {
    case 'priceAsc':
      queryParams.value.sort = 'price,asc'
      break
    case 'priceDesc':
      queryParams.value.sort = 'price,desc'
      break
    case 'salesDesc':
      queryParams.value.sort = 'sales,desc'
      break
    case 'ratingDesc':
      queryParams.value.sort = 'rating,desc'
      break
    default:
      queryParams.value.sort = 'createdAt,desc'
  }
  loadProducts()
}

const loadProducts = async () => {
  try {
    loading.value = true
    const params = {
      keyword: queryParams.value.keyword,
      categoryId: queryParams.value.categoryId,
      page: queryParams.value.page - 1,
      size: queryParams.value.size,
      sort: queryParams.value.sort,
      minPrice: queryParams.value.minPrice,
      maxPrice: queryParams.value.maxPrice
    }
    
    const response = await getProducts(params)
    
    if (response?.data?.content) {
      products.value = response.data.content
      total.value = response.data.totalElements
    } else {
      products.value = []
      total.value = 0
      console.warn('No products data in response:', response)
    }
  } catch (error) {
    console.error('Failed to load products:', error)
    ElMessage.error('加载商品列表失败')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  queryParams.value.page = page
  loadProducts()
}

const handleSizeChange = (size) => {
  queryParams.value.size = size
  queryParams.value.page = 1
  loadProducts()
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped lang="scss">
.product-list {
  padding: 24px;
  background-color: #fff;

  .el-breadcrumb {
    margin-bottom: 20px;
  }

  .category-header {
    text-align: center;
    margin-bottom: 32px;
    
    h1 {
      font-size: 32px;
      color: #333;
      margin: 0 0 8px;
      font-weight: 500;
    }

    .subtitle {
      font-size: 16px;
      color: #666;
      margin: 0;
    }
  }

  .filter-section {
    background: #f8f9fa;
    padding: 16px;
    border-radius: 4px;
    margin-bottom: 24px;

    .price-range, .sort-options {
      margin: 8px 0;

      .label {
        color: #666;
        margin-right: 12px;
      }

      .el-radio-button__inner {
        padding: 8px 16px;
      }
    }
  }

  .product-item {
    margin-bottom: 24px;
    
    .el-card {
      position: relative;
      transition: all 0.3s ease;
      border: none;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 6px 16px rgba(0,0,0,0.1);
      }
    }

    .brand-logo {
      position: absolute;
      top: 12px;
      left: 12px;
      z-index: 1;
      background: rgba(255, 255, 255, 0.9);
      border-radius: 50%;
      padding: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      
      .museum-logo {
        width: 24px;
        height: 24px;
        color: #409EFF;
      }
    }

    .product-image {
      width: 100%;
      height: 300px;
      object-fit: cover;
      transition: all 0.3s ease;
    }

    .product-info {
      padding: 16px;
      background: #fff;

      .product-name {
        font-size: 16px;
        color: #333;
        margin: 0 0 8px;
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .product-desc {
        font-size: 14px;
        color: #666;
        margin: 0 0 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        display: box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        box-orient: vertical;
        height: 40px;
      }

      .product-meta {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;

        .price-rating {
          .price {
            font-size: 20px;
            color: #f56c6c;
            font-weight: bold;
            margin-bottom: 4px;
            display: block;
          }

          .rating {
            display: flex;
            align-items: center;
            
            .el-rate {
              margin-right: 8px;
            }

            .rating-score {
              font-size: 14px;
              color: #ff9900;
            }
          }
        }
      }
    }
  }

  .pagination {
    margin-top: 32px;
    display: flex;
    justify-content: center;
  }
}
</style>