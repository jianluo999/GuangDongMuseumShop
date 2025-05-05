<template>
  <div class="review-page">
    <div class="page-header">
      <h2>我的评价</h2>
    </div>

    <div class="review-content" v-loading="loading">
      <!-- 评价列表 -->
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待评价" name="pending">
          <div class="review-list" v-if="pendingReviews.length > 0">
            <el-card 
              v-for="item in pendingReviews" 
              :key="item.orderId"
              class="review-item"
            >
              <div class="product-info">
                <img :src="item.productImage" :alt="item.productName">
                <div class="product-meta">
                  <h4>{{ item.productName }}</h4>
                  <div class="order-info">
                    订单号：{{ item.orderNo }}
                  </div>
                </div>
              </div>
              <div class="review-action">
                <el-button 
                  type="primary"
                  @click="handleReview(item)"
                >
                  立即评价
                </el-button>
              </div>
            </el-card>
          </div>
          <el-empty 
            v-else 
            description="暂无待评价商品"
          />
        </el-tab-pane>

        <el-tab-pane label="已评价" name="completed">
          <div class="review-list" v-if="completedReviews.length > 0">
            <el-card 
              v-for="review in completedReviews" 
              :key="review.id"
              class="review-item"
            >
              <div class="product-info">
                <img :src="review.productImage" :alt="review.productName">
                <div class="product-meta">
                  <h4>{{ review.productName }}</h4>
                  <div class="order-info">
                    订单号：{{ review.orderNo }}
                  </div>
                </div>
              </div>
              <div class="review-content">
                <div class="rating">
                  <el-rate
                    v-model="review.rating"
                    disabled
                    show-score
                    text-color="#ff9900"
                  />
                  <span class="time">{{ formatDate(review.createdAt) }}</span>
                </div>
                <div class="content">{{ review.content }}</div>
                <div class="review-images" v-if="review.images?.length">
                  <el-image
                    v-for="image in review.images"
                    :key="image"
                    :src="image"
                    :preview-src-list="review.images"
                    fit="cover"
                  />
                </div>
              </div>
            </el-card>
          </div>
          <el-empty 
            v-else 
            description="暂无评价记录"
          />
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="发表评价"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="reviewForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="评分" prop="rating">
          <el-rate
            v-model="reviewForm.rating"
            show-score
            text-color="#ff9900"
          />
        </el-form-item>

        <el-form-item label="评价" prop="content">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            rows="4"
            placeholder="请分享您的使用体验"
          />
        </el-form-item>

        <el-form-item label="图片">
          <el-upload
            :action="`${import.meta.env.VITE_API_URL}/api/files/upload/images`"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :on-remove="handleRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="reviewForm.anonymous">
            匿名评价
          </el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting"
          @click="handleSubmit"
        >
          发表评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import axios from '@/utils/axios'

// 状态
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const activeTab = ref('pending')
const formRef = ref(null)
const currentItem = ref(null)

const pendingReviews = ref([])
const completedReviews = ref([])

// 表单数据
const reviewForm = reactive({
  rating: 5,
  content: '',
  images: [],
  anonymous: false
})

// 表单验证规则
const rules = {
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 10, message: '评价内容不能少于10个字', trigger: 'blur' }
  ]
}

// 获取待评价列表
const fetchPendingReviews = async () => {
  try {
    const { data } = await axios.get('/api/reviews/pending')
    pendingReviews.value = data
  } catch (error) {
    console.error('获取待评价列表失败:', error)
    ElMessage.error('获取待评价列表失败')
  }
}

// 获取已评价列表
const fetchCompletedReviews = async () => {
  try {
    const { data } = await axios.get('/api/reviews/completed')
    completedReviews.value = data
  } catch (error) {
    console.error('获取已评价列表失败:', error)
    ElMessage.error('获取已评价列表失败')
  }
}

// 发起评价
const handleReview = (item) => {
  currentItem.value = item
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewForm.images = []
  reviewForm.anonymous = false
  dialogVisible.value = true
}

// 图片上传相关
const handleUploadSuccess = (response) => {
  reviewForm.images.push(response.data)
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
  }

  return isImage && isLt2M
}

const handleRemove = (file) => {
  const index = reviewForm.images.indexOf(file.url)
  if (index !== -1) {
    reviewForm.images.splice(index, 1)
  }
}

// 提交评价
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    await axios.post('/api/reviews', {
      orderId: currentItem.value.orderId,
      productId: currentItem.value.productId,
      rating: reviewForm.rating,
      content: reviewForm.content,
      images: reviewForm.images,
      anonymous: reviewForm.anonymous
    })
    
    ElMessage.success('评价发表成功')
    dialogVisible.value = false
    fetchPendingReviews()
    fetchCompletedReviews()
  } catch (error) {
    console.error('发表评价失败:', error)
    ElMessage.error('发表评价失败')
  } finally {
    submitting.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

onMounted(() => {
  fetchPendingReviews()
  fetchCompletedReviews()
})
</script>

<style scoped lang="scss">
.review-page {
  .page-header {
    margin-bottom: 20px;

    h2 {
      font-size: 24px;
      color: #333;
    }
  }

  .review-list {
    .review-item {
      margin-bottom: 20px;

      .product-info {
        display: flex;
        gap: 15px;
        margin-bottom: 15px;

        img {
          width: 100px;
          height: 100px;
          object-fit: cover;
          border-radius: 4px;
        }

        .product-meta {
          flex: 1;

          h4 {
            margin: 0 0 10px;
            font-size: 16px;
            color: #333;
          }

          .order-info {
            color: #999;
            font-size: 14px;
          }
        }
      }

      .review-content {
        .rating {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;

          .time {
            color: #999;
            font-size: 14px;
          }
        }

        .content {
          color: #666;
          line-height: 1.6;
          margin-bottom: 10px;
        }

        .review-images {
          display: flex;
          gap: 10px;
          margin-top: 10px;

          :deep(.el-image) {
            width: 100px;
            height: 100px;
            border-radius: 4px;
          }
        }
      }

      .review-action {
        display: flex;
        justify-content: flex-end;
        margin-top: 15px;
      }
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .review-item {
    .product-info {
      img {
        width: 80px !important;
        height: 80px !important;
      }
    }

    .review-images {
      :deep(.el-image) {
        width: 80px !important;
        height: 80px !important;
      }
    }
  }
}
</style> 