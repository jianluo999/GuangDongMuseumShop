<template>
  <div class="product-category">
    <div class="container">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ category.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 分类信息 -->
      <div class="category-header">
        <h1>{{ category.name }}</h1>
        <p>{{ category.description }}</p>
      </div>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-card>
          <div class="filter-group">
            <span class="filter-label">价格区间：</span>
            <el-radio-group v-model="filters.priceRange">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="0-100">0-100元</el-radio-button>
              <el-radio-button label="100-500">100-500元</el-radio-button>
              <el-radio-button label="500-1000">500-1000元</el-radio-button>
              <el-radio-button label="1000-">1000元以上</el-radio-button>
            </el-radio-group>
          </div>
          
          <div class="filter-group">
            <span class="filter-label">排序方式：</span>
            <el-radio-group v-model="filters.sortBy">
              <el-radio-button label="default">默认</el-radio-button>
              <el-radio-button label="priceAsc">价格从低到高</el-radio-button>
              <el-radio-button label="priceDesc">价格从高到低</el-radio-button>
              <el-radio-button label="salesDesc">销量优先</el-radio-button>
              <el-radio-button label="ratingDesc">好评优先</el-radio-button>
            </el-radio-group>
          </div>
        </el-card>
      </div>

      <!-- 商品列表 -->
      <div class="product-list">
        <el-row :gutter="20">
          <el-col 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6" 
            v-for="product in products" 
            :key="product.id"
          >
            <el-card 
              class="product-card" 
              shadow="hover"
              @click="goToProduct(product.id)"
            >
              <img :src="product.image" class="product-image">
              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="price">¥{{ product.price }}</p>
                <div class="product-meta">
                  <el-rate v-model="product.rating" disabled show-score />
                  <span class="sales">销量: {{ product.sales }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[12, 24, 36, 48]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 分类信息
const category = ref({})
// 商品列表
const products = ref([])
// 分页参数
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
// 筛选条件
const filters = reactive({
  priceRange: '',
  sortBy: 'default'
})

// 获取分类信息
const getCategoryInfo = async () => {
  try {
    // 模拟数据
    category.value = {
      id: route.params.id,
      name: '文具用品',
      description: '精美文具，书写历史'
    }
  } catch (error) {
    console.error('获取分类信息失败:', error)
  }
}

// 获取商品列表
const getProducts = async () => {
  try {
    // 模拟数据
    const mockProducts = Array(pageSize.value).fill(null).map((_, i) => ({
      id: i + 1,
      name: `商品${i + 1}`,
      price: Math.floor(Math.random() * 1000) + 100,
      rating: 4 + Math.random(),
      sales: Math.floor(Math.random() * 1000),
      image: `https://via.placeholder.com/300x300?text=Product${i + 1}`
    }))
    products.value = mockProducts
    total.value = 100 // 总数
  } catch (error) {
    console.error('获取商品列表失败:', error)
  }
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  getProducts()
}

// 每页数量改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  getProducts()
}

// 跳转到商品详情
const goToProduct = (id) => {
  router.push(`/products/${id}`)
}

// 监听筛选条件变化
watch(filters, () => {
  currentPage.value = 1
  getProducts()
}, { deep: true })

onMounted(() => {
  getCategoryInfo()
  getProducts()
})
</script>

<style scoped lang="scss">
.product-category {
  padding: 20px 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .category-header {
    margin: 20px 0 30px;
    text-align: center;
    
    h1 {
      font-size: 32px;
      margin-bottom: 10px;
      color: #333;
    }
    
    p {
      color: #666;
      font-size: 16px;
    }
  }

  .filter-section {
    margin-bottom: 30px;
    
    .filter-group {
      margin-bottom: 15px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .filter-label {
        display: inline-block;
        width: 80px;
        color: #666;
      }
    }
  }

  .product-card {
    margin-bottom: 20px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-5px);
    }
    
    .product-image {
      width: 100%;
      height: 200px;
      object-fit: cover;
    }
    
    .product-info {
      padding: 15px;
      
      h3 {
        margin: 0 0 10px;
        font-size: 16px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .price {
        color: #ff6b81;
        font-size: 18px;
        font-weight: bold;
        margin: 10px 0;
      }
      
      .product-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .sales {
          color: #666;
          font-size: 14px;
        }
      }
    }
  }

  .pagination {
    margin-top: 30px;
    display: flex;
    justify-content: center;
  }
}
</style> 