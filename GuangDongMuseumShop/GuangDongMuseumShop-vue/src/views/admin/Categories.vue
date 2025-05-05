<template>
  <div class="categories-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索分类名称"
        class="search-input"
        clearable
      />
      <el-button type="primary" class="add-button" @click="handleAdd">
        + 新增分类
      </el-button>
    </div>

    <!-- 分类列表 -->
    <div class="category-list">
      <div class="list-header">
        <span class="column-name">分类名称</span>
        <span class="column-desc">描述</span>
        <span class="column-action">操作</span>
      </div>
      <div v-loading="loading" class="list-content">
        <div v-for="item in categories" :key="item.id" class="list-item">
          <span class="item-name">{{ item.name }}</span>
          <span class="item-desc">{{ item.description }}</span>
          <span class="item-action">
            <el-button link type="primary" @click="handleEdit(item)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(item)">删除</el-button>
          </span>
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const categories = ref([])
const searchQuery = ref('')

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = ref({
  id: null,
  name: '',
  description: '',
  parentId: null,
  level: 0
})

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  loading.value = true
  try {
    const { data } = await request.get('/api/admin/categories/all')
    if (data.code === 200) {
      // 如果有搜索关键词，进行过滤
      if (searchQuery.value) {
        categories.value = data.data.filter(item => 
          item.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
          (item.description && item.description.toLowerCase().includes(searchQuery.value.toLowerCase()))
        )
      } else {
        categories.value = data.data || []
      }
    } else {
      ElMessage.error(data.message || '获取分类列表失败')
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 监听搜索输入
watch(searchQuery, (newVal) => {
  if (newVal === '') {
    fetchCategories()
  } else {
    // 如果已经有数据，直接在现有数据中过滤
    const allCategories = categories.value
    categories.value = allCategories.filter(item =>
      item.name.toLowerCase().includes(newVal.toLowerCase()) ||
      (item.description && item.description.toLowerCase().includes(newVal.toLowerCase()))
    )
  }
})

// 打开新增对话框
const handleAdd = () => {
  dialogTitle.value = '新增分类'
  form.value = {
    id: null,
    name: '',
    description: '',
    parentId: null,
    level: 0
  }
  dialogVisible.value = true
}

// 打开编辑对话框
const handleEdit = (category) => {
  dialogTitle.value = '编辑分类'
  form.value = {
    id: category.id,
    name: category.name,
    description: category.description,
    parentId: category.parentId,
    level: category.level
  }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    const isEdit = form.value.id !== null
    
    const { data } = isEdit
      ? await request.put(`/api/admin/categories/${form.value.id}`, form.value)
      : await request.post('/api/admin/categories', form.value)
    
    if (data.code === 200) {
      ElMessage.success(isEdit ? '更新成功' : '添加成功')
      dialogVisible.value = false
      fetchCategories()
    } else {
      ElMessage.error(data.message || (isEdit ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// 删除分类
const handleDelete = async (category) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const { data } = await request.delete(`/api/admin/categories/${category.id}`)
    if (data.code === 200) {
      ElMessage.success('删除成功')
      fetchCategories()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.categories-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f9fc;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 150, 225, 0.05);
}

.search-input {
  width: 300px;
}

.add-button {
  background-color: #0096e1;
  border-color: #0096e1;
}

.add-button:hover {
  background-color: #007bc1;
  border-color: #007bc1;
}

.category-list {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 150, 225, 0.05);
}

.list-header {
  display: grid;
  grid-template-columns: 2fr 4fr 1fr;
  padding: 16px;
  background: linear-gradient(to right, #e8f4fc, #f5f9fc);
  color: #0096e1;
  font-weight: bold;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  border-bottom: 1px solid #e8f4fc;
}

.list-content {
  padding: 0 16px;
}

.list-item {
  display: grid;
  grid-template-columns: 2fr 4fr 1fr;
  padding: 16px 0;
  border-bottom: 1px solid #e8f4fc;
  align-items: center;
  transition: all 0.3s;
}

.list-item:hover {
  background-color: #f5f9fc;
}

.list-item:last-child {
  border-bottom: none;
}

.item-name {
  color: #2c3e50;
  font-weight: 500;
}

.item-desc {
  color: #5c7a99;
}

.item-action {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

:deep(.el-button--link) {
  padding: 4px 0;
  background: none;
  border: none;
  box-shadow: none;
  font-size: 14px;
}

:deep(.el-button--link.el-button--primary) {
  color: #1890ff;
}

:deep(.el-button--link.el-button--primary:hover) {
  color: #40a9ff;
}

:deep(.el-button--link.el-button--danger) {
  color: #ff4d4f;
}

:deep(.el-button--link.el-button--danger:hover) {
  color: #ff7875;
}

:deep(.el-dialog) {
  border-radius: 8px;
  background: linear-gradient(to bottom, #fff 0%, #f5f9fc 100%);
}

:deep(.el-dialog__header) {
  background: linear-gradient(to right, #e8f4fc, #f5f9fc);
  margin: 0;
  padding: 20px;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  color: #0096e1;
}

:deep(.el-dialog__body) {
  padding: 30px 20px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #e8f4fc;
  padding: 15px 20px;
  background: #f5f9fc;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-input__inner) {
  border-color: #e8f4fc;
}

:deep(.el-input__inner:focus) {
  border-color: #0096e1;
}
</style> 