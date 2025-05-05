<template>
  <div class="intangible-page">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <h1 class="title">非遗文创</h1>
      <p class="subtitle">传承非物质文化遗产，展现岭南文化魅力</p>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="价格区间">
          <el-select v-model="filterForm.priceRange" placeholder="请选择价格区间" clearable>
            <el-option label="0-100元" value="0-100" />
            <el-option label="100-500元" value="100-500" />
            <el-option label="500-1000元" value="500-1000" />
            <el-option label="1000元以上" value="1000+" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="filterForm.sort" placeholder="请选择排序方式" clearable>
            <el-option label="价格从低到高" value="price-asc" />
            <el-option label="价格从高到低" value="price-desc" />
            <el-option label="销量优先" value="sales" />
            <el-option label="最新上架" value="newest" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <!-- 商品展示区域 -->
    <div class="products-grid">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in products" :key="product.id">
          <el-card class="product-card" :body-style="{ padding: '0px' }" shadow="hover">
            <div class="product-image">
              <el-image :src="product.image" fit="cover">
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-price-row">
                <span class="product-price">¥{{ product.price }}</span>
                <el-button type="primary" size="small" @click="addToCart(product)">加入购物车</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 筛选表单
const filterForm = reactive({
  priceRange: '',
  sort: ''
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 商品列表
const products = ref([])
const loading = ref(false)

// 获取商品列表
const getProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      priceRange: filterForm.priceRange,
      sort: filterForm.sort
    }
    
    const response = await request.get('/api/products/intangible', { params })
    if (response.data.code === 200) {
      products.value = response.data.data.content
      total.value = response.data.data.totalElements
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 加入购物车
const addToCart = async (product) => {
  try {
    const response = await request.post('/api/cart/add', {
      productId: product.id,
      quantity: 1
    })
    if (response.data.code === 200) {
      ElMessage.success('添加到购物车成功')
    }
  } catch (error) {
    console.error('添加到购物车失败:', error)
    ElMessage.error('添加到购物车失败')
  }
}

// 处理分页变化
const handleSizeChange = (val) => {
  pageSize.value = val
  getProducts()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  getProducts()
}

// 监听筛选条件变化
watch([() => filterForm.priceRange, () => filterForm.sort], () => {
  currentPage.value = 1
  getProducts()
})

onMounted(() => {
  getProducts()
})
</script>

<style lang="scss" scoped>
.intangible-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  .page-header {
    text-align: center;
    margin-bottom: 40px;
    
    .title {
      font-size: 32px;
      color: #333;
      margin-bottom: 10px;
    }
    
    .subtitle {
      font-size: 16px;
      color: #666;
    }
  }

  .filter-section {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  }

  .products-grid {
    margin-bottom: 40px;

    .product-card {
      margin-bottom: 20px;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-5px);
      }

      .product-image {
        height: 200px;
        overflow: hidden;
        
        .el-image {
          width: 100%;
          height: 100%;
        }
      }

      .product-info {
        padding: 14px;

        .product-name {
          font-size: 16px;
          margin: 0 0 8px;
          color: #333;
        }

        .product-desc {
          font-size: 14px;
          color: #666;
          margin: 0 0 10px;
          height: 40px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .product-price-row {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .product-price {
            color: #ff6b6b;
            font-size: 18px;
            font-weight: bold;
          }
        }
      }
    }
  }

  .pagination-section {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }

  .image-placeholder {
    width: 100%;
    height: 200px;
    display: flex;
    justify-content: center;
    align-items: center;
    background: #f5f7fa;
    color: #909399;
    font-size: 30px;
  }
}
</style> 