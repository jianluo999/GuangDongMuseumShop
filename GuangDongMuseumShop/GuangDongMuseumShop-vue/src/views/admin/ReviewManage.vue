<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="filter-container">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="商品名称" prop="productName">
          <el-input
            v-model="queryParams.productName"
            placeholder="请输入商品名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="queryParams.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-select v-model="queryParams.rating" placeholder="请选择评分" clearable>
            <el-option
              v-for="i in 5"
              :key="i"
              :label="`${i}星`"
              :value="i"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评论列表 -->
    <el-card class="table-container">
      <el-table
        v-loading="loading"
        :data="reviewList"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" width="180" />
        <el-table-column prop="username" label="用户名" width="120">
          <template #default="{ row }">
            {{ row.anonymous ? '匿名用户' : row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <el-rate
              v-model="row.rating"
              disabled
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" />
        <el-table-column prop="createdAt" label="评论时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewList, deleteReview } from '@/api/admin/review'
import { formatDate } from '@/utils/format'

// 查询参数
const queryParams = ref({
  page: 1,
  size: 10,
  productName: '',
  username: '',
  rating: null
})

const loading = ref(false)
const reviewList = ref([])
const total = ref(0)

// 获取评论列表
const getList = async () => {
  try {
    loading.value = true
    const params = {
      page: queryParams.value.page - 1, // 后端分页从0开始
      size: queryParams.value.size,
      productName: queryParams.value.productName || undefined,
      username: queryParams.value.username || undefined,
      rating: queryParams.value.rating || undefined
    }
    
    const res = await getReviewList(params)
    if (res.data.code === 200) {
      reviewList.value = res.data.data.content
      total.value = res.data.data.totalElements
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error(error.message || '获取评论列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.value.page = 1  // 搜索时重置到第一页
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.value = {
    page: 1,
    size: 10,
    productName: '',
    username: '',
    rating: null
  }
  getList()
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.value.size = val
  queryParams.value.page = 1  // 切换每页显示数量时重置到第一页
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.value.page = val
  getList()
}

// 删除评论
const handleDelete = async (id) => {
  if (!id) {
    ElMessage.error('评论ID不能为空')
    return
  }
  
  try {
    console.log('准备删除评论:', id)
    await ElMessageBox.confirm('确认要删除该评论吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    const res = await deleteReview(id)
    if (res && res.code === 200) {
      console.log('评论删除成功:', id)
      ElMessage.success('删除成功')
      getList()
    } else {
      throw new Error(res?.message || '删除失败')
    }
  } catch (error) {
    console.error('删除评论失败:', error)
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '删除评论失败')
    }
  }
}

onMounted(() => {
  console.log('ReviewManage组件已挂载')
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.table-container {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 