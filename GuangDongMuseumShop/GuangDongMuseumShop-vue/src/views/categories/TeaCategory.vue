<template>
  <div class="category-page">
    <div class="category-header">
      <div class="header-content">
        <h1>茶道精品</h1>
        <p>品味茶道文化，感受岭南韵味</p>
        <div class="decorative-line"></div>
      </div>
    </div>
    
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="24">
          <div class="filter-options">
            <el-radio-group v-model="sortBy" size="large">
              <el-radio-button label="">默认</el-radio-button>
              <el-radio-button label="price,asc">价格从低到高</el-radio-button>
              <el-radio-button label="price,desc">价格从高到低</el-radio-button>
              <el-radio-button label="createdAt,desc">最新上架</el-radio-button>
            </el-radio-group>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="products-grid" v-loading="loading">
      <el-row :gutter="30">
        <el-col :span="6" v-for="product in products" :key="product.id">
          <el-card class="product-card" :body-style="{ padding: '0px' }" shadow="hover">
            <div class="product-image">
              <el-image 
                :src="product.mainImage || product.image" 
                fit="cover"
                :preview-src-list="[product.mainImage || product.image]"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="hover-overlay">
                <el-button class="view-btn" type="primary" @click="viewProduct(product.id)">
                  查看详情
                </el-button>
              </div>
            </div>
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="description" v-html="product.culturalBackground || '暂无文化背景介绍'"></p>
              <div class="price-section">
                <p class="price">¥{{ product.price }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const sortBy = ref('')  // 默认为空字符串
const loading = ref(false)

// 获取商品数据
const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    // 只有当选择了排序方式时才添加排序参数
    if (sortBy.value) {
      params.sort = sortBy.value
    }
    
    const response = await request.get('/api/products/category/4', { params })
    if (response.data.code === 200) {
      products.value = response.data.data.content
      total.value = response.data.data.totalElements
    } else {
      ElMessage.error(response.data.message || '获取商品列表失败')
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

const viewProduct = (id) => {
  router.push(`/products/${id}`)
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadProducts()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadProducts()
}

// 监听排序方式变化
watch(sortBy, () => {
  currentPage.value = 1
  loadProducts()
})

onMounted(() => {
  loadProducts()
})
</script>

<style scoped lang="scss">
.category-page {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(to bottom, #f9f6f2 0%, #fff 100%);
  min-height: 100vh;

  .category-header {
    text-align: center;
    margin-bottom: 40px;
    padding: 40px 0;
    background: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMCIgaGVpZ2h0PSIyMCI+PGNpcmNsZSBjeD0iMTAiIGN5PSIxMCIgcj0iMiIgZmlsbD0icmdiYSgyMDAsIDE2NiwgMTE3LCAwLjEpIi8+PC9zdmc+');
    position: relative;

    .header-content {
      position: relative;
      display: inline-block;
    }

    h1 {
      font-size: 36px;
      color: #8b6b43;
      margin-bottom: 15px;
      font-weight: 600;
      letter-spacing: 4px;
    }

    p {
      color: #a68b6d;
      font-size: 18px;
      margin-bottom: 20px;
      letter-spacing: 2px;
    }

    .decorative-line {
      width: 100px;
      height: 3px;
      background: linear-gradient(90deg, transparent, #c8a675, transparent);
      margin: 0 auto;
    }
  }

  .filter-section {
    margin-bottom: 40px;
    padding: 25px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);

    .filter-options {
      display: flex;
      justify-content: center;

      :deep(.el-radio-button__inner) {
        background: transparent;
        border-color: #e0d5c7;
        color: #8b6b43;
        padding: 12px 25px;
        transition: all 0.3s ease;

        &:hover {
          color: #c8a675;
          border-color: #c8a675;
        }
      }

      :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
        background: linear-gradient(135deg, #c8a675 0%, #b89563 100%);
        border-color: #c8a675;
        box-shadow: -1px 0 0 0 #c8a675;
      }
    }
  }

  .products-grid {
    margin-bottom: 50px;
    min-height: 400px;

    .product-card {
      margin-bottom: 30px;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      border: none;
      border-radius: 12px;
      overflow: hidden;
      background: #fff;
      
      &:hover {
        transform: translateY(-8px);
        box-shadow: 0 15px 30px rgba(200, 166, 117, 0.2);

        .product-image {
          .el-image {
            transform: scale(1.08);
          }

          .hover-overlay {
            opacity: 1;
            pointer-events: auto;
          }
        }
      }

      .product-image {
        width: 100%;
        height: 280px;
        overflow: hidden;
        position: relative;

        .el-image {
          width: 100%;
          height: 100%;
          transition: transform 0.6s ease;
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .hover-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.3);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: all 0.3s ease;
          pointer-events: none;
        }
      }

      .product-info {
        padding: 20px;
        background: #fff;

        h3 {
          font-size: 18px;
          color: #2c3e50;
          margin-bottom: 12px;
          font-weight: 500;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .description {
          color: #666;
          font-size: 14px;
          line-height: 1.6;
          margin-bottom: 15px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          height: 44px;
        }

        .price-section {
          padding-top: 15px;
          border-top: 1px solid rgba(200, 166, 117, 0.2);

          .price {
            background: linear-gradient(120deg, #b8860b 0%, #ffdb58 50%, #b8860b 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            font-size: 20px;
            font-weight: bold;
            font-family: 'DIN Alternate', sans-serif;
            margin: 0;
            display: inline-block;
            
            &::before {
              content: '¥';
              font-size: 16px;
              margin-right: 2px;
              background: inherit;
              -webkit-background-clip: text;
              -webkit-text-fill-color: transparent;
            }
          }
        }
      }

      .view-btn {
        background: linear-gradient(135deg, #c8a675 0%, #b89563 100%);
        border: none;
        padding: 12px 30px;
        font-size: 16px;
        border-radius: 25px;
        color: #fff;
        transition: all 0.3s ease;
        transform: translateY(20px);

        &:hover {
          transform: translateY(15px);
          box-shadow: 0 5px 15px rgba(200, 166, 117, 0.4);
        }
      }
    }
  }

  .pagination-section {
    text-align: center;
    margin-top: 50px;
    padding-top: 30px;
    border-top: 1px solid rgba(200, 166, 117, 0.2);

    :deep(.el-pagination) {
      .el-pagination__total,
      .el-pagination__sizes {
        font-size: 14px;
      }

      .el-pagination__sizes .el-input .el-input__inner {
        border-color: #e0d5c7;
      }

      button {
        background: transparent;
        color: #8b6b43;

        &:hover {
          color: #c8a675;
        }
      }

      .el-pager li {
        background: transparent;
        color: #8b6b43;
        border: 1px solid #e0d5c7;
        margin: 0 4px;
        min-width: 32px;
        
        &.active {
          background: linear-gradient(135deg, #c8a675 0%, #b89563 100%);
          color: #fff;
          border-color: #c8a675;
        }
        
        &:hover {
          color: #c8a675;
          border-color: #c8a675;
        }
      }
    }
  }
}
</style> 