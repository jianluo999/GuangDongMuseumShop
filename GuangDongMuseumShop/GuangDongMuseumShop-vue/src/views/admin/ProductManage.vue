<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="filter-container">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="queryParams.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" placeholder="请选择分类" clearable style="width: 200px">
            <el-option label="全部" value="" />
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="已上架" value="ON_SALE" />
            <el-option label="已下架" value="OFF_SALE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-container">
      <template #header>
        <div class="card-header">
          <span class="card-title">商品列表</span>
          <div class="right-buttons">
            <el-button type="primary" icon="Plus" @click="handleAdd">新增商品</el-button>
            <el-button 
              type="danger" 
              icon="Delete"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
            >
              批量删除
            </el-button>
          </div>
        </div>
      </template>

      <!-- 商品表格 -->
      <el-table
        v-loading="loading"
        :data="productList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image 
              :src="getImageUrl(row)"
              :preview-src-list="[getImageUrl(row)]"
              fit="cover"
              style="width: 50px; height: 50px"
              :initial-index="0"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="商品名称" prop="name" min-width="200" show-overflow-tooltip />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="库存" prop="stock" width="100" />
        <el-table-column label="销量" prop="sales" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ON_SALE' ? 'success' : 'info'">
              {{ row.status === 'ON_SALE' ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button 
              link 
              :type="row.status === 'ON_SALE' ? 'warning' : 'success'"
              @click="handleStatusChange(row)"
            >
              <el-icon><SwitchButton /></el-icon>
              {{ row.status === 'ON_SALE' ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
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
          :layout="'共 ' + total + ' 条, 每页 sizes 条, prev, pager, next, 前往 jumper 页'"
          :prev-text="'上一页'"
          :next-text="'下一页'"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
          <template #sizes-prefix>
            显示
          </template>
          <template #sizes-suffix>
            条
          </template>
          <template #jumper-prefix>
            前往
          </template>
          <template #jumper-suffix>
            页
          </template>
        </el-pagination>
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="700px"
      append-to-body
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number 
            v-model="productForm.price" 
            :precision="2" 
            :step="0.1" 
            :min="0"
          />
        </el-form-item>
        <el-form-item label="商品库存" prop="stock">
          <el-input-number 
            v-model="productForm.stock" 
            :min="0" 
            :precision="0"
          />
        </el-form-item>
        <el-form-item label="商品主图" prop="mainImage">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleMainImageSuccess"
            :before-upload="beforeImageUpload"
          >
            <img v-if="productForm.mainImage" :src="productForm.mainImage" class="avatar">
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
          <el-upload
            :action="uploadUrl"
            list-type="picture-card"
            :file-list="productForm.images"
            :on-success="handleImageSuccess"
            :on-remove="handleImageRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, SwitchButton, Delete, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 数据定义
const loading = ref(false)
const dialogVisible = ref(false)
const productList = ref([])
const categories = ref([])
const total = ref(0)
const selectedIds = ref([])
const productFormRef = ref()
const uploadUrl = import.meta.env.VITE_API_URL + '/api/admin/upload'
const defaultImage = 'https://via.placeholder.com/50x50.png?text=No+Image'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  categoryId: undefined,
  status: ''
})

// 表单数据
const productForm = reactive({
  id: undefined,
  name: '',
  categoryId: undefined,
  price: 0,
  stock: 0,
  mainImage: '',
  images: [],
  description: '',
  status: 'ON_SALE'
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }],
  mainImage: [{ required: true, message: '请上传商品主图', trigger: 'change' }]
}

// 计算属性
const dialogTitle = computed(() => productForm.id ? '编辑商品' : '新增商品')

// 获取商品列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await request.get('/api/admin/products', {
      params: {
        page: queryParams.pageNum - 1,
        size: queryParams.pageSize,
        name: queryParams.name || undefined,
        categoryId: queryParams.categoryId || undefined,
        status: queryParams.status || undefined
      }
    })
    
    if (data.code === 200) {
      // 处理图片URL
      productList.value = data.data.content.map(item => ({
        ...item,
        mainImage: item.mainImage || item.image,
        categoryName: getCategoryName(item.categoryId)
      }))
      total.value = data.data.totalElements
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const getCategories = async () => {
  try {
    const { data } = await request.get('/api/admin/categories/tree')
    if (data.code === 200) {
      // 将树形结构展平为一维数组
      const flattenCategories = (categories, level = 0) => {
        let result = []
        categories.forEach(category => {
          result.push({
            id: category.id,
            name: '  '.repeat(level) + category.name  // 添加缩进表示层级
          })
          if (category.children && category.children.length > 0) {
            result = result.concat(flattenCategories(category.children, level + 1))
          }
        })
        return result
      }
      categories.value = flattenCategories(data.data)
    } else {
      ElMessage.error('获取分类列表失败')
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 新增商品
const handleAdd = () => {
  router.push('/admin/products/add')
}

// 编辑商品
const handleEdit = (row) => {
  router.push(`/admin/products/edit/${row.id}`)
}

// 提交表单
const handleSubmit = async () => {
  if (!productFormRef.value) return
  
  try {
    await productFormRef.value.validate()
    
    if (productForm.id) {
      await request.put(`/api/admin/products/${productForm.id}`, productForm)
      ElMessage.success('更新成功')
    } else {
      await request.post('/api/admin/products', productForm)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('保存商品失败:', error)
    ElMessage.error('保存失败')
  }
}

// 删除商品
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/api/admin/products/${id}`)
    ElMessage.success('删除成功')
    await getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的商品')
    return
  }
  
  ElMessageBox.confirm(`确认要删除这 ${selectedIds.value.length} 个商品吗？`, '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await Promise.all(selectedIds.value.map(id => request.delete(`/api/admin/products/${id}`)))
      ElMessage.success('批量删除成功')
      getList()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

// 更改商品状态
const handleStatusChange = async (row) => {
  try {
    const newStatus = row.status === 'ON_SALE' ? 'OFF_SALE' : 'ON_SALE'
    await request.put(`/api/admin/products/${row.id}/status`, {
      status: newStatus
    })
    row.status = newStatus // 直接更新本地状态
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('更新商品状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

// 图片上传相关
const handleMainImageSuccess = (response) => {
  productForm.mainImage = response.data
}

const handleImageSuccess = (response) => {
  productForm.images.push({
    url: response.data
  })
}

const handleImageRemove = (file) => {
  const index = productForm.images.indexOf(file)
  if (index !== -1) {
    productForm.images.splice(index, 1)
  }
}

const beforeImageUpload = (file) => {
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

// 其他处理函数
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
  queryParams.name = ''
  queryParams.categoryId = undefined
  queryParams.status = ''
  handleQuery()
}

// 添加获取分类名称的方法
const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : '-'
}

// 在 script setup 部分添加 getImageUrl 方法
const getImageUrl = (row) => {
  if (!row) return defaultImage;
  const baseUrl = import.meta.env.VITE_API_URL || '';
  const imageUrl = row.mainImage || row.image;
  if (!imageUrl) return defaultImage;
  return imageUrl.startsWith('http') ? imageUrl : `${baseUrl}${imageUrl}`;
}

onMounted(() => {
  getList()
  getCategories()
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

  .avatar-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);

      &:hover {
        border-color: var(--el-color-primary);
      }
    }
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    text-align: center;
    line-height: 178px;
  }

  .avatar {
    width: 178px;
    height: 178px;
    display: block;
  }

  .category-filter {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    max-width: 800px;
    
    .category-item {
      margin-bottom: 8px;
    }
  }

  :deep(.el-table) {
    .category-column {
      color: #606266;
    }
  }

  .image-error {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 50px;
    height: 50px;
    background-color: #f0f0f0;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
  }
}
</style> 