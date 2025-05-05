<template>
  <div class="home">
    <!-- 顶部导航图标 -->
    <div class="nav-icons">
      <div class="icon-item" v-for="icon in navIcons" :key="icon.id" @click="goToCategory(icon.id)">
        <div class="icon-circle">
          <img :src="icon.image" :alt="icon.name">
        </div>
        <span>{{ icon.name }}</span>
      </div>
    </div>

    <!-- 博物馆建筑展示图 -->
    <div class="palace-banner">
      <el-image 
        :src="museumBuilding1" 
        alt="博物馆建筑"
        fit="cover"
        class="banner-image"
      >
        <template #error>
          <div class="image-placeholder">
            <el-icon><Picture /></el-icon>
          </div>
        </template>
      </el-image>
      <div class="banner-content">
        <h1>博物馆文创</h1>
        <p>传承文化 创新设计</p>
      </div>
    </div>

    <!-- 精选商品 -->
    <div class="section featured-products">
      <div class="section-header">
        <h2>精选商品</h2>
        <div class="decorative-line"></div>
      </div>
      <el-row :gutter="20" v-loading="loading">
        <el-col
          :span="8"
          v-for="product in featuredProducts"
          :key="product.id"
        >
          <div class="product-card" @click="goToProduct(product.id)">
            <div class="product-image">
              <el-image 
                :src="product.mainImage || (product.images && product.images.length > 0 ? product.images[0] : defaultImage)" 
                :alt="product.name"
                fit="cover"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="price-tag">¥{{ product.price }}</div>
            </div>
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="description" v-html="product.culturalBackground || '暂无文化背景介绍'"></p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 茶具系列 -->
    <div class="section tea-series">
      <div class="section-header">
        <h2>茶具系列</h2>
        <div class="decorative-line"></div>
      </div>
      <div class="series-content">
        <div class="series-image">
          <el-image 
            :src="teaSetImage" 
            alt="茶具系列"
            fit="cover"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        <div class="series-info">
          <h3>匠心制器</h3>
          <p>传统工艺 现代设计</p>
          <el-button type="primary" @click="goToCategory('tea')">查看系列</el-button>
        </div>
      </div>
    </div>

    <!-- 新品上架 -->
    <div class="section new-arrivals">
      <div class="section-header">
        <h2>新品上架</h2>
        <div class="decorative-line"></div>
      </div>
      <el-row :gutter="20" v-loading="loading">
        <el-col
          :span="6"
          v-for="product in newProducts"
          :key="product.id"
        >
          <div class="product-card" @click="goToProduct(product.id)">
            <div class="product-image">
              <el-image 
                :src="product.mainImage || (product.images && product.images.length > 0 ? product.images[0] : defaultImage)" 
                :alt="product.name"
                fit="cover"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="price-tag">¥{{ product.price }}</div>
            </div>
            <div class="product-info">
              <h3>{{ product.name }}</h3>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getFeaturedProducts, getNewProducts } from '@/api/product'
import { ElMessage } from 'element-plus'
import museumBuilding1 from '../assets/images/sections/museum-building1.webp'
import teaSetImage from '../assets/images/sections/tea-set.jpg'

const router = useRouter()

// 导航图标
const navIcons = ref([
  {
    id: 'art',
    name: '艺术制品',
    image: 'https://api.iconify.design/fluent-emoji-flat:artist-palette.svg',
    route: '/categories/art'
  },
  {
    id: 'gifts',
    name: '私人定制',
    image: 'https://api.iconify.design/fluent-emoji-flat:wrapped-gift.svg',
    route: '/categories/gifts'
  },
  {
    id: 'museum',
    name: '广馆文创',
    image: 'https://api.iconify.design/fluent-emoji-flat:scroll.svg',
    route: '/categories/museum'
  },
  {
    id: 'tea',
    name: '茶道精品',
    image: 'https://api.iconify.design/fluent-emoji-flat:teacup-without-handle.svg',
    route: '/categories/tea'
  },
  {
    id: 'jewelry',
    name: '国风首饰',
    image: 'https://api.iconify.design/fluent-emoji-flat:gem-stone.svg',
    route: '/categories/jewelry'
  }
])

// 精选商品数据
const featuredProducts = ref([])
const newProducts = ref([])
const loading = ref(false)

// 默认图片
const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjI0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mH5Yqg6L295LitPC90ZXh0Pjwvc3ZnPg=='

// 获取商品数据
const loadProducts = async () => {
  loading.value = true
  try {
    // 获取精选商品
    const featuredResponse = await getFeaturedProducts({
      page: 0,
      size: 3,
      sort: 'sales,desc'
    })
    if (featuredResponse.data.code === 200) {
      featuredProducts.value = featuredResponse.data.data.content.map(product => {
        // 确保每个商品都有图片数组
        if (!product.images) {
          product.images = []
        }
        // 如果有 mainImage，添加到 images 数组开头
        if (product.mainImage && !product.images.includes(product.mainImage)) {
          product.images.unshift(product.mainImage)
        }
        // 如果仍然没有图片，使用默认图片
        if (product.images.length === 0) {
          product.images = [defaultImage]
        }
        return product
      })
    } else {
      ElMessage.error('获取精选商品失败')
    }

    // 获取新品数据
    const newResponse = await getNewProducts({
      page: 0,
      size: 4
    })
    if (newResponse.data.code === 200) {
      newProducts.value = newResponse.data.data.content.map(product => {
        // 确保每个商品都有图片数组
        if (!product.images) {
          product.images = []
        }
        // 如果有 mainImage，添加到 images 数组开头
        if (product.mainImage && !product.images.includes(product.mainImage)) {
          product.images.unshift(product.mainImage)
        }
        // 如果仍然没有图片，使用默认图片
        if (product.images.length === 0) {
          product.images = [defaultImage]
        }
        return product
      })
    } else {
      ElMessage.error('获取新品数据失败')
    }
  } catch (error) {
    console.error('获取商品数据失败:', error)
    ElMessage.error('获取商品数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 页面跳转方法
const goToProduct = (id) => {
  router.push(`/products/${id}`)
}

const goToCategory = (id) => {
  const icon = navIcons.value.find(icon => icon.id === id)
  if (icon) {
    router.push(icon.route)
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped lang="scss">
.home {
  background-color: #F5F0E6;
  min-height: 100vh;
  padding: 20px;

  .nav-icons {
    display: flex;
    justify-content: space-around;
    margin-bottom: 30px;
    padding: 20px 0;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    .icon-item {
      text-align: center;
      cursor: pointer;

      .icon-circle {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        background-color: #F5F0E6;
        margin: 0 auto 10px;
        display: flex;
        align-items: center;
        justify-content: center;

        img {
          width: 36px;
          height: 36px;
        }
      }

      span {
        font-size: 14px;
        color: #6A5D4D;
      }
    }
  }

  .palace-banner {
    position: relative;
    height: 400px;
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 40px;

    .banner-image {
      width: 100%;
      height: 100%;
      
      :deep(img) {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s ease;
        
        &:hover {
          transform: scale(1.05);
        }
      }
    }

    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      justify-content: center;
      align-items: center;
      background: #f5f7fa;
      color: #909399;
      font-size: 30px;
    }

    .banner-content {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      text-align: center;
      color: #fff;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
      z-index: 1;

      h1 {
        font-size: 48px;
        margin-bottom: 20px;
        font-weight: 500;
      }

      p {
        font-size: 24px;
      }
    }
  }

  .section {
    margin-bottom: 60px;

    .section-header {
      text-align: center;
      margin-bottom: 30px;
      position: relative;

      h2 {
        font-size: 28px;
        color: #B24F30;
        font-weight: 500;
        margin-bottom: 15px;
      }

      .decorative-line {
        width: 60px;
        height: 2px;
        background-color: #B24F30;
        margin: 0 auto;
        position: relative;

        &::before,
        &::after {
          content: '';
          position: absolute;
          width: 6px;
          height: 6px;
          background-color: #B24F30;
          border-radius: 50%;
          top: -2px;
        }

        &::before {
          left: -10px;
        }

        &::after {
          right: -10px;
        }
      }
    }
  }

  .product-card {
    background-color: #fff;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.3s;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    &:hover {
      transform: translateY(-5px);
    }

    .product-image {
      position: relative;
      width: 100%;
      height: 250px;
      overflow: hidden;
      border-radius: 8px;

      .el-image {
        width: 100%;
        height: 100%;
        transition: transform 0.3s ease;

        &:hover {
          transform: scale(1.05);
        }
      }

      .image-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        background: #f5f7fa;
        color: #909399;
        font-size: 30px;
      }

      .price-tag {
        position: absolute;
        bottom: 10px;
        right: 10px;
        background: rgba(134, 45, 45, 0.8);
        color: #fff;
        padding: 5px 10px;
        border-radius: 4px;
        font-weight: bold;
      }
    }

    .product-info {
      padding: 15px;

      h3 {
        font-size: 16px;
        color: #6A5D4D;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .description {
        font-size: 14px;
        color: #999;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  .tea-series {
    .series-content {
      display: flex;
      background-color: #fff;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

      .series-image {
        flex: 1;
        height: 400px;

        .el-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .series-info {
        flex: 1;
        padding: 40px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        text-align: center;
        background: linear-gradient(135deg, #F5F0E6, #fff);

        h3 {
          font-size: 36px;
          color: #B24F30;
          margin-bottom: 20px;
        }

        p {
          font-size: 18px;
          color: #6A5D4D;
          margin-bottom: 30px;
        }

        .el-button {
          background-color: #B24F30;
          border-color: #B24F30;

          &:hover {
            background-color: darken(#B24F30, 10%);
            border-color: darken(#B24F30, 10%);
          }
        }
      }
    }
  }

  .footer {
    text-align: center;
    padding: 20px 0;
    background-color: #fff;
    color: #6A5D4D;
    font-size: 14px;
    margin-top: 40px;
    border-top: 1px solid #e0d5c7;
    
    p {
      margin: 0;
      line-height: 1.5;
    }
  }
}
</style>
