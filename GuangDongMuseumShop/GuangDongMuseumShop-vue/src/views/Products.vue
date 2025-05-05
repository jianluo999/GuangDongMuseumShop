<template>
  <div class="products">
    <h1>商品列表</h1>
    <el-row :gutter="20">
      <el-col :span="6" v-for="product in products" :key="product.id">
        <el-card class="product-card">
          <img :src="`https://placehold.co/300x200?text=${product.name}`" class="product-image">
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p class="price">¥{{ product.price }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const products = ref([
  { id: 1, name: '商品1', price: 99 },
  { id: 2, name: '商品2', price: 199 },
  { id: 3, name: '商品3', price: 299 },
  { id: 4, name: '商品4', price: 399 }
])
</script>

<style scoped>
.products {
  padding: 20px;
}

.product-card {
  margin-bottom: 20px;
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 14px;
}

.price {
  color: #ff6b81;
  font-size: 18px;
  font-weight: bold;
}
</style> 