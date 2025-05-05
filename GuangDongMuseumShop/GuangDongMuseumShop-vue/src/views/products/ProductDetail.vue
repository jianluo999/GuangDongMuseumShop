<template>
  <div class="product-detail">
    <div class="container">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/products' }">商品列表</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 商品信息 -->
      <div class="product-info">
        <div class="product-gallery">
          <el-carousel ref="carousel" trigger="click" height="400px" :autoplay="false" @change="handleCarouselChange">
            <el-carousel-item v-for="(image, index) in product.images" :key="index">
              <el-image 
                :src="image" 
                :alt="product.name"
                fit="cover"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </el-carousel-item>
          </el-carousel>
          <div class="thumbnail-list">
            <el-image 
              v-for="(image, index) in product.images" 
              :key="index"
              :src="image"
              :class="{ active: currentImage === index }"
              @click="setCurrentImage(index)"
              fit="cover"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
        </div>

        <div class="product-summary">
          <h1 class="product-name">{{ product.name }}</h1>
          <div class="product-meta" style="display: none;">
            <el-rate v-model="product.rating" disabled show-score />
            <span class="sales">销量: {{ product.sales }}</span>
          </div>
          
          <!-- 文化背景介绍 -->
          <div class="cultural-background">
            <h3>文化背景</h3>
            <p>{{ product.culturalBackground }}</p>
          </div>

          <div class="product-price">
            <span class="price">¥{{ product.price }}</span>
            <span class="original-price" v-if="product.originalPrice">
              原价: <del>¥{{ product.originalPrice }}</del>
            </span>
          </div>

          <!-- 商品规格 -->
          <div class="specs-section" v-if="specs.length > 0">
            <h3>商品规格</h3>
            <ProductSpecs 
              :product-id="Number(route.params.id)"
              :initial-specs="specs"
              @spec-change="handleSpecChange"
            />
          </div>

          <!-- 商品详细信息 -->
          <div class="product-details">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="商品编号" v-if="false">{{ product.id }}</el-descriptions-item>
              <el-descriptions-item label="商品分类" v-if="false">{{ product.categoryName }}</el-descriptions-item>
              <el-descriptions-item label="商品材质" v-if="false">{{ product.material }}</el-descriptions-item>
              <el-descriptions-item label="商品尺寸" v-if="false">{{ product.dimensions }}</el-descriptions-item>
              <el-descriptions-item label="包装方式" v-if="false">{{ product.packaging }}</el-descriptions-item>
              <el-descriptions-item label="保养说明" v-if="product.careInstructions">{{ product.careInstructions }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 购买数量 -->
          <div class="quantity-selector">
            <span class="label">数量</span>
            <el-input-number 
              v-model="quantity" 
              :min="1" 
              :max="product.stock"
              size="large"
            />
            <span class="stock">库存: {{ product.stock }}件</span>
          </div>

          <!-- 购买按钮 -->
          <div class="action-buttons">
            <el-button 
              type="primary" 
              size="large" 
              @click="addToCart"
              :loading="loading"
            >
              加入购物车
            </el-button>
            <el-button 
              type="danger" 
              size="large" 
              @click="buyNow"
              :loading="loading"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品详情 -->
      <div class="product-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="detail-content">
              <!-- 商品描述 -->
              <div class="product-description">
                <h3>商品描述</h3>
                <div v-html="product.description"></div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="规格参数" name="params" v-if="false">
            <el-descriptions :column="1" border>
              <el-descriptions-item 
                v-for="param in product.params" 
                :key="param.key" 
                :label="param.key"
              >
                {{ param.value }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="商品评价" name="reviews" ref="reviewsTab">
            <div class="reviews-section">
              <!-- 评价统计信息 -->
              <div class="review-header">
                <div class="rating-summary" v-if="reviewStats">
                  <div class="average-rating">
                    <span class="rating">{{ reviewStats.averageRating?.toFixed(1) || '0.0' }}</span>
                    <el-rate v-model="reviewStats.averageRating" disabled />
                  </div>
                  <div class="rating-count">共{{ reviewStats?.totalReviews || 0 }}条评价</div>
                </div>
              </div>

              <!-- 评价表单 -->
              <div v-if="!hasOrdered" class="review-tip">
                购买后才能评价哦~
              </div>
              
              <div v-else-if="canReview" class="review-form">
                <el-form ref="reviewFormRef" :model="reviewForm" :rules="rules">
                  <el-form-item label="评分" prop="rating" class="rating-item">
                    <el-rate 
                      v-model="reviewForm.rating"
                      :colors="['#FFD700', '#FFD700', '#FFD700']"
                      void-color="#C6D1DE"
                      :texts="['1分', '2分', '3分', '4分', '5分']"
                      show-text
                      :max="5"
                      @change="handleRatingChange"
                    />
                  </el-form-item>
                  <el-form-item label="评价内容" prop="content">
                    <el-input
                      v-model="reviewForm.content"
                      type="textarea"
                      :rows="4"
                      placeholder="请输入您的评价内容（5-500字）"
                      :maxlength="500"
                      show-word-limit
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-upload
                      class="review-uploader"
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :on-success="handleUploadSuccess"
                      :on-error="handleUploadError"
                      :before-upload="beforeUpload"
                      multiple
                      :limit="5"
                      list-type="picture-card"
                    >
                      <el-icon><Plus /></el-icon>
                    </el-upload>
                  </el-form-item>
                  <el-form-item>
                    <el-checkbox v-model="reviewForm.anonymous">匿名评价</el-checkbox>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="submitReview">提交评价</el-button>
                  </el-form-item>
                </el-form>
              </div>

              <!-- 评价列表 -->
              <div class="review-list" v-loading="reviewsLoading">
                <template v-if="reviews.length > 0">
                  <div v-for="review in reviews" :key="review.id" class="review-item">
                    <div class="review-user">
                      <el-avatar :src="review.userAvatar || defaultAvatar" />
                      <span>{{ review.anonymous ? '匿名用户' : review.username }}</span>
                      <el-rate v-model="review.rating" disabled />
                    </div>
                    <div class="review-content">{{ review.content }}</div>
                    <div class="review-images" v-if="review.images && review.images.length">
                      <el-image 
                        v-for="(img, index) in review.images"
                        :key="index"
                        :src="img"
                        :preview-src-list="review.images"
                      />
                    </div>
                    <div class="review-time">{{ formatDate(review.createdAt) }}</div>
                  </div>
                </template>
                <template v-else>
                  <div class="no-reviews">
                    <el-empty 
                      description="暂无评价" 
                      :image-size="200"
                    >
                      <template #description>
                        <p>暂时还没有用户评价，购买后可以发表评价哦~</p>
                      </template>
                    </el-empty>
                  </div>
                </template>
              </div>

              <!-- 分页器 -->
              <div class="pagination-container" v-if="total > 0">
                <el-pagination
                  v-model:current-page="currentPage"
                  v-model:page-size="pageSize"
                  :total="total"
                  :page-sizes="[10, 20, 30]"
                  layout="total, sizes, prev, pager, next"
                  @size-change="handleSizeChange"
                  @current-change="handlePageChange"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { formatDate } from '@/utils/format'
import request from '@/utils/request'
import { checkReviewStatus } from '@/api/review'
import ProductSpecs from '@/components/ProductSpecs.vue'
import { getProductSpecs } from '@/api/spec'
import { Plus, Picture } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const product = ref({})
const reviews = ref([])
const quantity = ref(1)
const loading = ref(false)
const activeTab = ref('detail')
const currentImage = ref(0)
const specs = ref([])
const selectedSpecs = ref({})
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const reviewStats = ref(null)
const hasOrdered = ref(false)
const canReview = ref(false)
const showReviewForm = ref(false)
const orderId = ref(null)
const reviewsTab = ref(null)
const reviewsLoading = ref(false)
const carousel = ref(null)

// 评分颜色配置
const rateColors = ['#FFD700', '#FFD700', '#FFD700']
const voidColor = '#C6D1DE'

// 评分变化处理
const handleRatingChange = (value) => {
  reviewForm.rating = value
}

// 修改 reviewForm 的初始化
const reviewForm = reactive({
  rating: 0,
  content: '',
  images: [],
  anonymous: false
})

// 修改验证规则
const rules = reactive({
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' },
    { type: 'number', min: 1, max: 5, message: '评分必须在1-5之间', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 5, max: 500, message: '评价内容长度必须在5-500字之间', trigger: 'blur' }
  ]
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const uploadUrl = computed(() => `${import.meta.env.VITE_API_URL}/api/files/upload`)

// 默认图片
const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjI0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mH5Yqg6L295LitPC90ZXh0Pjwvc3ZnPg=='
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 获取商品详情
const getProductDetail = async () => {
  loading.value = true
  try {
    const response = await request.get(`/api/products/${route.params.id}`)
    if (response.data.code === 200) {
      product.value = response.data.data
      // 确保图片列表存在
      if (!product.value.images || product.value.images.length === 0) {
        product.value.images = [product.value.mainImage || product.value.image || defaultImage]
      }
    } else if (response.data.code === 404) {
      ElMessage.error('该商品不存在或已下架')
      router.push('/404')
    } else {
      ElMessage.error(response.data.message || '获取商品详情失败')
      router.push('/404')
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    if (error.response?.status === 404) {
      ElMessage.error('该商品不存在或已下架')
    } else if (error.response?.status === 500 && error.response?.data?.message?.includes('Redis')) {
      ElMessage.warning('系统繁忙，正在重试...')
      // Redis错误会自动重试，由request.js中的拦截器处理
    } else {
      ElMessage.error('获取商品详情失败，请稍后重试')
    }
    router.push('/404')
  } finally {
    loading.value = false
  }
}

// 获取商品评价
const getProductReviews = async () => {
  try {
    const response = await request.get(`/api/products/${route.params.id}/reviews`, {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    if (response.data.code === 200) {
      reviews.value = response.data.data.content
      total.value = response.data.data.totalElements
    } else {
      ElMessage.error('获取评价失败')
    }
  } catch (error) {
    console.error('获取评价失败:', error)
    ElMessage.error('获取评价失败')
  }
}

// 加载商品规格
const loadProductSpecs = async () => {
  try {
    const response = await getProductSpecs(route.params.id)
    if (response.data.code === 200) {
      specs.value = response.data.data
    }
  } catch (error) {
    console.error('加载商品规格失败:', error)
  }
}

// 处理规格选择变化
const handleSpecChange = (selected) => {
  selectedSpecs.value = selected
}

// 检查是否已选择所有规格
const checkSpecSelected = () => {
  if (specs.value.length === 0) return true
  return specs.value.every(spec => selectedSpecs.value[spec.name])
}

// 添加到购物车
const addToCart = async () => {
  if (!checkSpecSelected()) {
    ElMessage.warning('请选择商品规格')
    return
  }
  
  loading.value = true
  try {
    await cartStore.addToCart(product.value.id, quantity.value)
    ElMessage.success('添加成功')
  } catch (error) {
    ElMessage.error('添加失败')
  } finally {
    loading.value = false
  }
}

// 立即购买
const buyNow = async () => {
  if (!checkSpecSelected()) {
    ElMessage.warning('请选择商品规格')
    return
  }
  
  loading.value = true
  try {
    const orderItem = {
      productId: product.value.id,
      productName: product.value.name,
      productImage: product.value.mainImage,
      productPrice: product.value.price,
      specs: Object.entries(selectedSpecs.value).map(([key, value]) => `${key}: ${value}`).join(', '),
      quantity: quantity.value
    }

    // 直接跳转到确认订单页面
    await router.push({
      path: '/checkout',
      query: { 
        orderItem: encodeURIComponent(JSON.stringify(orderItem)),
        buyNow: 'true'
      }
    })
  } catch (error) {
    console.error('处理立即购买失败:', error)
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

// 图片预览
const previewImage = (url) => {
  // 实现图片预览逻辑
}

// 获取评价列表
const getReviews = async () => {
  reviewsLoading.value = true
  try {
    const res = await request.get(`/api/products/${route.params.id}/reviews`, {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    if (res.data.code === 200) {
      reviews.value = res.data.data.content
      total.value = res.data.data.totalElements
    } else {
      ElMessage.warning(res.data.message || '获取评价失败')
    }
  } catch (error) {
    console.error('获取评价失败:', error)
    ElMessage.error('获取评价列表失败，请稍后重试')
  } finally {
    reviewsLoading.value = false
  }
}

// 检查是否可以评价
const checkCanReview = async () => {
  try {
    const productId = route.params.id
    const { data } = await checkReviewStatus(productId)
    canReview.value = data
  } catch (error) {
    console.error('检查评价状态失败:', error)
  }
}

// 获取评价统计
const getReviewStats = async () => {
  try {
    const res = await request.get(`/api/products/${route.params.id}/review-stats`)
    if (res.data.code === 200) {
      reviewStats.value = res.data.data
    } else {
      reviewStats.value = {
        averageRating: 0,
        totalReviews: 0
      }
    }
  } catch (error) {
    console.error('获取评价统计失败:', error)
    reviewStats.value = {
      averageRating: 0,
      totalReviews: 0
    }
  }
}

// 检查评价权限
const checkReviewPermission = async () => {
  console.log('开始检查评价权限:', {
    orderId: route.query.orderId,
    showReview: route.query.showReview,
    productId: route.params.id
  })

  // 如果从订单页面跳转过来，直接允许评价
  if (route.query.orderId && route.query.showReview === 'true') {
    console.log('从订单页面跳转过来，允许评价')
    canReview.value = true
    showReviewForm.value = true
    activeTab.value = 'reviews'
    orderId.value = route.query.orderId
    hasOrdered.value = true
    // 滚动到评价区域
    nextTick(() => {
      console.log('滚动到评价区域')
      const reviewsSection = document.querySelector('.reviews-section')
      if (reviewsSection) {
        reviewsSection.scrollIntoView({ behavior: 'smooth' })
      }
    })
  } else {
    console.log('不是从订单页面跳转，不允许评价')
  }
}

// 提交评价
const submitReview = async () => {
  console.log('开始提交评价:', {
    orderId: route.query.orderId,
    rating: reviewForm.rating,
    content: reviewForm.content,
    images: reviewForm.images,
    anonymous: reviewForm.anonymous
  })

  if (!reviewForm.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  if (!reviewForm.content) {
    ElMessage.warning('请输入评价内容')
    return
  }
  
  try {
    const response = await request.post(`/api/products/${route.params.id}/reviews`, {
      orderId: route.query.orderId,
      rating: reviewForm.rating,
      content: reviewForm.content,
      images: reviewForm.images,
      anonymous: reviewForm.anonymous
    })
    
    console.log('评价提交响应:', response.data)
    
    if (response.data.code === 200) {
      ElMessage.success('评价成功')
      showReviewForm.value = false
      canReview.value = false
      // 刷新评价列表
      getProductReviews()
      // 评价成功后返回订单页面
      router.push('/user/orders')
    } else {
      ElMessage.error(response.data.message || '评价失败')
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error('提交评价失败')
  }
}

// 监听路由参数变化
watch(() => route.query, (newQuery) => {
  console.log('路由参数变化:', newQuery)
  if (newQuery.orderId && newQuery.showReview === 'true') {
    checkReviewPermission()
  }
}, { immediate: true })

// 上传图片相关方法
const handleUploadSuccess = (res) => {
  reviewForm.images.push(res.data)
}

const handleUploadError = (error) => {
  console.error('上传图片失败:', error)
  ElMessage.error('上传图片失败')
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handlePageChange = (page) => {
  currentPage.value = page
  getReviews()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  getReviews()
}

// 处理轮播图变化
const handleCarouselChange = (index) => {
  currentImage.value = index
}

// 设置当前图片
const setCurrentImage = (index) => {
  currentImage.value = index
  carousel.value?.setActiveItem(index)
}

onMounted(() => {
  getProductDetail()
  getProductReviews()
  getReviews()
  getReviewStats()
  checkCanReview()
  loadProductSpecs()
  if (route.query.showReview === 'true' && route.query.orderId) {
    checkReviewPermission()
  }
})
</script>

<style scoped lang="scss">
.product-detail {
  background: linear-gradient(135deg, #f8f4ef 0%, #fff 100%);
  min-height: 100vh;
  padding: 40px 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .el-breadcrumb {
    margin-bottom: 30px;
    font-size: 14px;
    :deep(.el-breadcrumb__item) {
      .el-breadcrumb__inner {
        color: #8b6b43;
        &:hover {
          color: #c8a675;
        }
      }
    }
  }

  .product-info {
    display: flex;
    gap: 20px;
    background: #fff;
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
    transition: transform 0.3s ease;
    align-items: flex-start;
    min-height: 500px;
    
    &:hover {
      transform: translateY(-5px);
    }
  }

  .product-gallery {
    width: 400px;
    flex-shrink: 0;
    margin-bottom: 20px;
    
    :deep(.el-carousel__item) {
      overflow: hidden;
      border-radius: 12px;
      height: 400px;
      
      .el-image {
        width: 100%;
        height: 100%;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: contain;
        }
      }
    }
    
    .thumbnail-list {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      margin-top: 15px;
      padding: 4px;
      max-width: 400px;
      justify-content: flex-start;
      
      .el-image {
        width: 80px;
        height: 80px;
        flex: 0 0 80px;
        border-radius: 8px;
        border: 2px solid transparent;
        cursor: pointer;
        transition: all 0.3s ease;
        overflow: hidden;
        
        &:hover {
          border-color: #c8a675;
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(200, 166, 117, 0.2);
        }
        
        &.active {
          border-color: #c8a675;
          box-shadow: 0 4px 12px rgba(200, 166, 117, 0.3);
        }

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
    }
  }

  .product-summary {
    flex: 1;
    min-width: 0;
    
    .product-name {
      font-size: 24px;
      margin-bottom: 15px;
    }
    
    .product-meta {
      display: flex;
      align-items: center;
      gap: 25px;
      margin-bottom: 30px;
      padding-bottom: 25px;
      border-bottom: 1px dashed rgba(200, 166, 117, 0.3);
      
      .sales {
        color: #666;
        font-size: 15px;
        display: flex;
        align-items: center;
        gap: 8px;
        
        &::before {
          content: '';
          display: inline-block;
          width: 4px;
          height: 4px;
          background: #c8a675;
          border-radius: 50%;
        }
      }
    }

    .cultural-background {
      background: linear-gradient(135deg, #f8f4ef 0%, #fff 100%);
      padding: 15px;
      border-radius: 12px;
      margin: 20px 0;
      border: 1px solid rgba(200, 166, 117, 0.2);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);

      h3 {
        color: #8b6b43;
        font-size: 20px;
        margin-bottom: 15px;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 10px;
        
        &::before {
          content: '';
          display: inline-block;
          width: 4px;
          height: 20px;
          background: #c8a675;
          border-radius: 2px;
        }
      }

      p {
        color: #666;
        line-height: 1.8;
        font-size: 15px;
        text-align: justify;
      }
    }
    
    .product-price {
      margin: 20px 0;
      display: flex;
      align-items: baseline;
      gap: 15px;
      
      .price {
        font-size: 36px;
        color: #c8a675;
        font-weight: bold;
        font-family: 'DIN Alternate', sans-serif;
      }
      
      .original-price {
        color: #999;
        font-size: 16px;
        text-decoration: line-through;
      }
    }

    .specs-section {
      margin: 25px 0;
      padding: 25px;
      border-radius: 12px;
      background: linear-gradient(135deg, #f8f4ef 0%, #fff 100%);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
      border: 1px solid rgba(200, 166, 117, 0.2);

      .spec-title {
        font-size: 16px;
        color: #2c3e50;
        margin-bottom: 15px;
        font-weight: 500;
      }

      .spec-group {
        margin-bottom: 20px;

        &:last-child {
          margin-bottom: 0;
        }

        .spec-options {
          display: flex;
          flex-wrap: wrap;
          gap: 12px;

          .spec-option {
            padding: 8px 20px;
            border-radius: 8px;
            border: 1px solid #e0d5c7;
            color: #666;
            cursor: pointer;
            transition: all 0.3s ease;
            background: #fff;
            font-size: 14px;
            position: relative;
            overflow: hidden;

            &::before {
              content: '';
              position: absolute;
              top: 0;
              left: 0;
              width: 100%;
              height: 100%;
              background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.3), transparent);
              transform: translateX(-100%);
              transition: transform 0.6s ease;
            }

            &:hover {
              border-color: #c8a675;
              color: #8b6b43;
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(200, 166, 117, 0.15);

              &::before {
                transform: translateX(100%);
              }
            }

            &.active {
              background: linear-gradient(135deg, #c8a675 0%, #b89563 100%);
              border-color: transparent;
              color: #fff;
              box-shadow: 0 4px 12px rgba(200, 166, 117, 0.3);
            }

            &.disabled {
              background: #f5f5f5;
              border-color: #ddd;
              color: #999;
              cursor: not-allowed;
              pointer-events: none;
            }
          }
        }
      }
    }
    
    .product-details {
      margin-top: 15px;
      margin-bottom: 15px;
      
      :deep(.el-descriptions__label) {
        width: 120px;
        color: #666;
      }
    }
    
    .quantity-selector {
      display: flex;
      align-items: center;
      gap: 25px;
      margin: 20px 0;
      
      .label {
        color: #2c3e50;
        font-size: 16px;
        font-weight: 500;
      }
      
      .stock {
        color: #666;
        font-size: 14px;
        display: flex;
        align-items: center;
        gap: 8px;
        
        &::before {
          content: '';
          display: inline-block;
          width: 6px;
          height: 6px;
          background: #c8a675;
          border-radius: 50%;
          opacity: 0.6;
        }
      }

      :deep(.el-input-number) {
        .el-input-number__decrease,
        .el-input-number__increase {
          background: #f8f4ef;
          border-color: #e0d5c7;
          color: #8b6b43;
          transition: all 0.3s ease;
          
          &:hover {
            background: #c8a675;
            color: #fff;
          }
        }

        .el-input__inner {
          border-color: #e0d5c7;
          color: #8b6b43;
          font-weight: 500;
          
          &:focus {
            border-color: #c8a675;
            box-shadow: 0 0 0 2px rgba(200, 166, 117, 0.2);
          }
        }
      }
    }
    
    .action-buttons {
      margin-top: 20px;
      position: sticky;
      bottom: 0;
      background: #fff;
      padding: 15px 0;
      z-index: 1;
      display: flex;
      gap: 25px;

      .el-button {
        flex: 1;
        height: 54px;
        font-size: 17px;
        border-radius: 10px;
        font-weight: 500;
        letter-spacing: 1px;
        transition: all 0.3s ease;
        
        &.el-button--primary {
          background: linear-gradient(135deg, #c8a675 0%, #b89563 100%);
          border: none;
          box-shadow: 0 4px 15px rgba(200, 166, 117, 0.3);
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(200, 166, 117, 0.4);
          }
        }
        
        &.el-button--danger {
          background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
          border: none;
          box-shadow: 0 4px 15px rgba(231, 76, 60, 0.3);
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(231, 76, 60, 0.4);
          }
        }
      }
    }
  }

  .product-tabs {
    margin-top: 20px;
    padding: 30px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);

    :deep(.el-tabs__header) {
      margin-bottom: 25px;
      
      .el-tabs__nav-wrap::after {
        height: 1px;
        background-color: rgba(200, 166, 117, 0.2);
      }
      
      .el-tabs__item {
        font-size: 16px;
        color: #666;
        padding: 0 25px;
        height: 50px;
        line-height: 50px;
        transition: all 0.3s ease;
        
        &.is-active {
          color: #c8a675;
          font-weight: 500;
        }
        
        &:hover {
          color: #c8a675;
        }
      }
      
      .el-tabs__active-bar {
        background-color: #c8a675;
        height: 3px;
        border-radius: 1.5px;
      }
    }

    .product-description {
      padding: 15px 0;
      line-height: 1.6;
      color: #2c3e50;
      font-size: 15px;
      text-align: justify;

      h3 {
        margin-bottom: 12px;
        font-size: 18px;
        color: #333;
      }
      
      p {
        margin-bottom: 10px;
        line-height: 1.6;
        color: #666;
      }
    }
  }

  .reviews-section {
    background: #fff;
    border-radius: 16px;
    margin-top: 40px;
    padding: 35px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
    
    .review-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 35px;
      padding-bottom: 25px;
      border-bottom: 1px solid rgba(200, 166, 117, 0.2);
      
      .rating-summary {
        display: flex;
        align-items: center;
        gap: 35px;
        
        .average-rating {
          display: flex;
          align-items: center;
          gap: 18px;
          
          .rating {
            font-size: 32px;
            color: #c8a675;
            font-weight: bold;
            font-family: 'DIN Alternate', sans-serif;
          }
        }
        
        .rating-count {
          color: #666;
          font-size: 15px;
        }
      }
    }
    
    .review-tip {
      text-align: center;
      color: #999;
      font-size: 15px;
      padding: 50px 0;
      background: #f8f4ef;
      border-radius: 12px;
      margin: 20px 0;
    }
    
    .review-list {
      min-height: 200px;
      
      .review-item {
        padding: 30px 0;
        border-bottom: 1px solid rgba(200, 166, 117, 0.2);
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(248, 244, 239, 0.5);
        }
        
        &:last-child {
          border-bottom: none;
        }
        
        .review-user {
          display: flex;
          align-items: center;
          gap: 18px;
          margin-bottom: 18px;

          .el-avatar {
            border: 2px solid #f0f0f0;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
          }

          span {
            color: #2c3e50;
            font-weight: 500;
            font-size: 15px;
          }
        }
        
        .review-content {
          color: #2c3e50;
          line-height: 1.8;
          font-size: 15px;
          margin: 18px 0;
          text-align: justify;
        }
        
        .review-images {
          display: flex;
          gap: 15px;
          margin: 20px 0;
          flex-wrap: wrap;
          
          .el-image {
            width: 120px;
            height: 120px;
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
            transition: all 0.3s ease;
            
            &:hover {
              transform: translateY(-3px);
              box-shadow: 0 6px 15px rgba(0, 0, 0, 0.12);
            }
          }
        }
        
        .review-time {
          color: #999;
          font-size: 13px;
          margin-top: 12px;
        }
      }

      .no-reviews {
        padding: 50px 0;
        text-align: center;

        :deep(.el-empty__description) {
          p {
            color: #666;
            font-size: 15px;
            margin-top: 10px;
            line-height: 1.6;
          }
        }
      }
    }

    .pagination-container {
      margin-top: 35px;
      display: flex;
      justify-content: center;
      
      :deep(.el-pagination) {
        .el-pagination__total,
        .el-pagination__sizes {
          font-size: 14px;
        }
        
        .el-pagination__sizes .el-input .el-input__inner {
          font-size: 14px;
        }
        
        button {
          background: transparent;
          
          &:hover {
            color: #c8a675;
          }
          
          &:disabled {
            background: transparent;
          }
        }
        
        .el-pager li {
          background: transparent;
          font-size: 14px;
          
          &.active {
            color: #c8a675;
            font-weight: bold;
          }
          
          &:hover {
            color: #c8a675;
          }
        }
      }
    }
  }

  .review-form {
    background: linear-gradient(135deg, #f8f4ef 0%, #fff 100%);
    padding: 30px;
    border-radius: 12px;
    margin-bottom: 30px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    border: 1px solid rgba(200, 166, 117, 0.2);
    
    :deep(.el-form-item) {
      margin-bottom: 25px;
    }

    :deep(.el-form-item__label) {
      font-weight: 500;
      color: #2c3e50;
    }

    :deep(.el-input__inner),
    :deep(.el-textarea__inner) {
      background-color: #fff !important;
      color: #2c3e50 !important;
      border-color: #e0d5c7;
      border-radius: 8px;
      
      &:focus {
        border-color: #c8a675;
        box-shadow: 0 0 0 2px rgba(200, 166, 117, 0.2);
      }
      
      &::placeholder {
        color: #999;
      }
    }

    :deep(.el-rate) {
      display: inline-block;
      
      .el-rate__icon {
        font-size: 28px;
        margin-right: 6px;
        transition: all 0.3s ease;
        
        &.el-rate__icon--on {
          color: #FFD700;
          transform: scale(1.1);
        }
        
        &.el-rate__icon--off {
          color: #C6D1DE;
        }
      }
    }

    :deep(.el-rate__text) {
      font-size: 15px;
      margin-left: 12px;
      color: #666;
    }

    :deep(.el-upload) {
      border-radius: 8px;
      border-color: #e0d5c7;
      transition: all 0.3s ease;
      
      &:hover {
        border-color: #c8a675;
      }
    }

    :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
      background-color: #c8a675;
      border-color: #c8a675;
    }

    :deep(.el-checkbox__label) {
      color: #666;
    }

    .el-button--primary {
      background: linear-gradient(135deg, #c8a675 0%, #b89563 100%);
      border: none;
      padding: 12px 30px;
      font-size: 16px;
      border-radius: 8px;
      font-weight: 500;
      letter-spacing: 1px;
      box-shadow: 0 4px 15px rgba(200, 166, 117, 0.3);
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(200, 166, 117, 0.4);
      }
    }
  }

  .rating-item {
    :deep(.el-form-item__content) {
      display: flex;
      align-items: center;
    }
  }
}
</style> 