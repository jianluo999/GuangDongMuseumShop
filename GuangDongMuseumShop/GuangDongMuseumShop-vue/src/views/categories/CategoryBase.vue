<template>
  <div class="category-page">
    <div class="category-header">
      <h1>{{ title }}</h1>
      <div class="category-description">{{ description }}</div>
    </div>

    <div class="filter-section">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="价格区间">
          <el-select v-model="filters.priceRange" placeholder="选择价格区间">
            <el-option label="全部" value="" />
            <el-option label="0-100元" value="0-100" />
            <el-option label="100-500元" value="100-500" />
            <el-option label="500元以上" value="500+" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="排序">
          <el-select v-model="filters.sort" placeholder="选择排序方式">
            <el-option label="默认排序" value="" />
            <el-option label="价格从低到高" value="price-asc" />
            <el-option label="价格从高到低" value="price-desc" />
            <el-option label="销量优先" value="sales" />
            <el-option label="最新上架" value="newest" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <div class="products-grid">
      <el-row :gutter="20" v-loading="loading">
        <template v-if="products.length > 0">
          <el-col 
            v-for="product in products" 
            :key="product.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6"
          >
            <div class="product-card" @click="goToProduct(product.id)">
              <div class="product-image">
                <el-image 
                  :src="product.image" 
                  fit="cover"
                  :preview-src-list="[product.image]"
                />
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-price">¥{{ product.price.toFixed(2) }}</p>
                <p class="product-sales">已售 {{ product.sales }}</p>
              </div>
            </div>
          </el-col>
        </template>
        <el-col :span="24" v-else-if="!loading">
          <div class="empty-state">
            <el-empty description="暂无商品" />
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="pagination-section" v-if="total > 0">
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
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getCategoryProducts } from '@/api/product'
import { ElMessage } from 'element-plus'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  categoryId: {
    type: String,
    required: true
  }
})

const router = useRouter()
const loading = ref(false)
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const filters = reactive({
  priceRange: '',
  sort: ''
})

// 监听筛选条件变化
watch([() => filters.priceRange, () => filters.sort], () => {
  currentPage.value = 1 // 重置页码
  loadProducts()
})

const loadProducts = async () => {
  try {
    loading.value = true
    
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      categoryId: props.categoryId
    }

    const res = await getCategoryProducts(props.categoryId, params)
    
    if (res.code === 200) {
      products.value = res.data.content || []
      total.value = res.data.totalElements || 0
    }
  } catch (error) {
    console.error('Failed to load products:', error)
    ElMessage.error('加载商品失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goToProduct = (productId) => {
  router.push(`/products/${productId}`)
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadProducts()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadProducts()
}

// 添加加载状态
const loadingText = computed(() => {
  return loading.value ? '加载中...' : (products.value.length === 0 ? '暂无商品' : '')
})

onMounted(() => {
  loadProducts()
})
</script>

<style scoped lang="scss">
.category-page {
  padding: 20px;

  .category-header {
    text-align: center;
    margin-bottom: 30px;

    h1 {
      font-size: 28px;
      color: #B24F30;
      margin-bottom: 10px;
    }

    .category-description {
      color: #666;
      font-size: 16px;
    }
  }

  .filter-section {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
  }

  .products-grid {
    margin-bottom: 30px;

    .product-card {
      background: #fff;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      transition: transform 0.3s;
      margin-bottom: 20px;

      &:hover {
        transform: translateY(-5px);
      }

      .product-image {
        aspect-ratio: 1;
        overflow: hidden;

        .el-image {
          width: 100%;
          height: 100%;
        }
      }

      .product-info {
        padding: 15px;

        .product-name {
          font-size: 16px;
          color: #333;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .product-price {
          color: #B24F30;
          font-size: 18px;
          font-weight: bold;
          margin-bottom: 5px;
        }

        .product-sales {
          color: #999;
          font-size: 14px;
        }
      }
    }
  }

  .pagination-section {
    display: flex;
    justify-content: center;
    margin-top: 30px;
  }
}

/* 添加加载状态样式 */
.el-loading-spinner .el-loading-text {
  color: #B24F30;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  width: 100%;
  
  :deep(.el-empty) {
    padding: 40px 0;
  }
}
</style> 