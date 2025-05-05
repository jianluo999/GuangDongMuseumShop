<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="filter-container">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="用户ID" prop="userId">
          <el-input
            v-model="queryParams.userId"
            placeholder="请输入用户ID"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="创建时间" prop="dateRange">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="购物车状态" clearable style="width: 200px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="已失效" value="INVALID" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-container">
      <template #header>
        <div class="card-header">
          <span class="card-title">购物车列表</span>
          <div class="right-buttons">
            <el-button
              type="danger"
              icon="Delete"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
            >批量删除</el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="cartList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="用户ID" prop="userId" width="120" align="center" />
        <el-table-column label="商品数量" prop="itemCount" width="100" align="center" />
        <el-table-column label="总金额" align="center" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'NORMAL' ? 'success' : 'info'">
              {{ row.status === 'NORMAL' ? '正常' : '已失效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
        <el-table-column label="更新时间" prop="updateTime" width="180" align="center" />
        <el-table-column label="操作" align="center" width="200">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="handleView(row)">查看</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="购物车详情"
      width="800px"
      append-to-body
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="购物车ID">{{ currentCart?.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentCart?.userId }}</el-descriptions-item>
        <el-descriptions-item label="商品数量">{{ currentCart?.itemCount }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentCart?.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentCart?.status === 'NORMAL' ? 'success' : 'info'">
            {{ currentCart?.status === 'NORMAL' ? '正常' : '已失效' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentCart?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentCart?.updateTime }}</el-descriptions-item>
      </el-descriptions>

      <div class="cart-items" v-if="currentCart?.items?.length">
        <h3>购物车商品</h3>
        <el-table :data="currentCart.items" border>
          <el-table-column label="商品图片" width="100">
            <template #default="{ row }">
              <el-image 
                :src="row.productImage" 
                :preview-src-list="[row.productImage]"
                fit="cover"
                style="width: 50px; height: 50px"
              />
            </template>
          </el-table-column>
          <el-table-column label="商品名称" prop="productName" />
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              ¥{{ row.price?.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="100" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥{{ (row.price * row.quantity)?.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="是否选中" width="100">
            <template #default="{ row }">
              <el-tag :type="row.selected ? 'success' : 'info'">
                {{ row.selected ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const dialogVisible = ref(false)
const cartList = ref([])
const total = ref(0)
const selectedIds = ref([])
const currentCart = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userId: '',
  status: ''
})

// 获取购物车列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await request.get('/api/admin/carts', {
      params: {
        page: queryParams.pageNum - 1,
        size: queryParams.pageSize,
        userId: queryParams.userId || undefined,
        status: queryParams.status || undefined
      }
    })
    cartList.value = data.content
    total.value = data.totalElements
  } catch (error) {
    console.error('获取购物车列表失败:', error)
    ElMessage.error('获取购物车列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleView = async (row) => {
  try {
    const { data } = await request.get(`/api/admin/carts/${row.id}`)
    currentCart.value = data
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取购物车详情失败')
  }
}

// 删除购物车
const handleDelete = (row) => {
  ElMessageBox.confirm('确认要删除该购物车吗？', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/api/admin/carts/${row.id}`)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的购物车')
    return
  }
  
  ElMessageBox.confirm(`确认要删除这 ${selectedIds.value.length} 个购物车吗？`, '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await Promise.all(selectedIds.value.map(id => request.delete(`/api/admin/carts/${id}`)))
      ElMessage.success('批量删除成功')
      getList()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.userId = ''
  queryParams.status = ''
  handleQuery()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;

  .filter-container,
  .table-container {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 16px;
      font-weight: bold;
    }

    .right-buttons {
      display: flex;
      gap: 10px;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }

  .cart-items {
    margin-top: 20px;

    h3 {
      margin-bottom: 15px;
      font-size: 16px;
      font-weight: bold;
    }
  }
}
</style> 