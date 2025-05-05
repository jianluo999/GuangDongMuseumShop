<template>
  <div class="category-page">
    <div class="category-header">
      <h1 class="category-title">{{ title }}</h1>
      <p class="category-description">{{ description }}</p>
    </div>

    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="sortBy" placeholder="排序方式" class="filter-select">
            <el-option label="默认排序" value="default" />
            <el-option label="价格从低到高" value="price-asc" />
            <el-option label="价格从高到低" value="price-desc" />
            <el-option label="最新上架" value="newest" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="priceRange" placeholder="价格区间" class="filter-select">
            <el-option label="全部" value="" />
            <el-option label="0-100元" value="0-100" />
            <el-option label="100-500元" value="100-500" />
            <el-option label="500-1000元" value="500-1000" />
            <el-option label="1000元以上" value="1000+" />
          </el-select>
        </el-col>
      </el-row>
    </div>

    <el-loading v-model:loading="loading">
      <div class="products-grid">
      <el-row :gutter="24">
          <el-col v-for="product in products" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
            <router-link :to="'/products/' + product.id" class="product-card">
              <el-card :body-style="{ padding: '0px' }" shadow="hover">
              <img :src="product.image" class="product-image" />
                <div class="product-info">
                  <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-price">¥{{ product.price }}</p>
                </div>
              </el-card>
            </router-link>
          </el-col>
        </el-row>
      </div>
    </el-loading>

    <div class="pagination-section">
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
import { ref } from 'vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  },
  category: {
    type: String,
    required: true
  }
})

const sortBy = ref('default')
const priceRange = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const products = ref([])

// 模拟数据
products.value = Array(12).fill(null).map((_, index) => ({
  id: index + 1,
  name: '文创产品 ' + (index + 1),
  price: Math.floor(Math.random() * 1000) + 100,
  image: 'https://via.placeholder.com/300x300'
}))
total.value = 100

const handleSizeChange = (val) => {
  pageSize.value = val
  // TODO: 加载新数据
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  // TODO: 加载新数据
}
</script>

<style scoped>
.category-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.category-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 20px 0;
  background-color: #FDF6EC;
  border-radius: 8px;
}

.category-title {
  color: #862D2D;
  font-size: 28px;
  margin-bottom: 10px;
}

.category-description {
  color: #666;
  font-size: 16px;
}

.filter-section {
  margin-bottom: 30px;
}

.filter-select {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  background-color: #FDF6EC;
}

.products-grid {
  margin-bottom: 40px;
}

.product-card {
  display: block;
  text-decoration: none;
  margin-bottom: 24px;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.product-info {
  padding: 14px;
}

.product-name {
  color: #333;
  font-size: 16px;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  color: #862D2D;
  font-size: 18px;
  font-weight: bold;
  margin: 8px 0 0;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #862D2D;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #862D2D;
}
</style>
