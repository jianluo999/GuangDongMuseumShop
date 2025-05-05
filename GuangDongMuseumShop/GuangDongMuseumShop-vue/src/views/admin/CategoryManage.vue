<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">分类管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增分类</el-button>
        </div>
      </template>

      <!-- 分类树形表格 -->
      <el-table
        v-loading="loading"
        :data="categoryList"
        row-key="id"
        border
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column label="分类名称" prop="name" min-width="200">
          <template #default="{ row }">
            <el-icon v-if="row.icon" :class="row.icon" />
            <span style="margin-left: 8px">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="'ENABLED'"
              :inactive-value="'DISABLED'"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAdd(row)">添加子分类</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              link 
              type="danger" 
              @click="handleDelete(row)"
              :disabled="row.children?.length > 0"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分类表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="上级分���">
          <el-tree-select
            v-model="categoryForm.parentId"
            :data="categoryOptions"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择上级分类"
            clearable
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="categoryForm.icon" placeholder="请输入图标类名">
            <template #append>
              <el-button @click="openIconSelect">
                <el-icon><Select /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="categoryForm.status">
            <el-radio label="ENABLED">启用</el-radio>
            <el-radio label="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 图标选择器对话框 -->
    <el-dialog
      title="选择图标"
      v-model="iconDialogVisible"
      width="800px"
      append-to-body
    >
      <div class="icon-list">
        <div
          v-for="icon in iconList"
          :key="icon"
          class="icon-item"
          @click="handleSelectIcon(icon)"
        >
          <el-icon :class="icon" />
          <span class="icon-name">{{ icon }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 数据定义
const loading = ref(false)
const dialogVisible = ref(false)
const iconDialogVisible = ref(false)
const dialogType = ref('add')
const categoryList = ref([])
const categoryFormRef = ref()

// 分类表单
const categoryForm = reactive({
  id: undefined,
  parentId: undefined,
  name: '',
  icon: '',
  sort: 0,
  status: 'ENABLED'
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入分类��称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return dialogType.value === 'add' ? '新增分类' : '编辑分类'
})

// 分类树形选项
const categoryOptions = computed(() => {
  const options = JSON.parse(JSON.stringify(categoryList.value))
  // 如果是编辑状态，需要过滤掉当前节点及其子节点
  if (dialogType.value === 'edit' && categoryForm.id) {
    const filterNode = (data) => {
      return data.filter(item => {
        if (item.children) {
          item.children = filterNode(item.children)
        }
        return item.id !== categoryForm.id
      })
    }
    return filterNode(options)
  }
  return options
})

// 获取分类列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await request.get('/api/admin/categories/tree')
    categoryList.value = data
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 新增分类
const handleAdd = (row) => {
  dialogType.value = 'add'
  categoryForm.id = undefined
  categoryForm.parentId = row?.id
  categoryForm.name = ''
  categoryForm.icon = ''
  categoryForm.sort = 0
  categoryForm.status = 'ENABLED'
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  dialogType.value = 'edit'
  categoryForm.id = row.id
  categoryForm.parentId = row.parentId
  categoryForm.name = row.name
  categoryForm.icon = row.icon
  categoryForm.sort = row.sort
  categoryForm.status = row.status
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!categoryFormRef.value) return
  
  try {
    await categoryFormRef.value.validate()
    
    if (dialogType.value === 'add') {
      await request.post('/api/admin/categories', categoryForm)
      ElMessage.success('新增成功')
    } else {
      await request.put(`/api/admin/categories/${categoryForm.id}`, categoryForm)
      ElMessage.success('修改成功')
    }
    
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('保存分类失败:', error)
    ElMessage.error('保存失败')
  }
}

// 删除分类
const handleDelete = (row) => {
  if (row.children?.length > 0) {
    ElMessage.warning('该分类下有子分类，不能删除')
    return
  }
  
  ElMessageBox.confirm('确认要删除该分类吗？', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/api/admin/categories/${row.id}`)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 更改分类状态
const handleStatusChange = async (row) => {
  try {
    await request.put(`/api/admin/categories/${row.id}/status`, {
      status: row.status
    })
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  }
}

// 图标选择相关
const iconList = [
  'Shop', 'ShoppingCart', 'ShoppingBag', 'Present', 'Collection', 
  'Goods', 'Box', 'Stamp', 'Ticket', 'Picture', 'Brush', 'Medal'
]

const openIconSelect = () => {
  iconDialogVisible.value = true
}

const handleSelectIcon = (icon) => {
  categoryForm.icon = icon
  iconDialogVisible.value = false
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .card-title {
      font-size: 16px;
      font-weight: bold;
    }
  }
  
  .icon-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 10px;
    padding: 10px;
    
    .icon-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 10px;
      border: 1px solid #eee;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        background: #f5f7fa;
        border-color: var(--el-color-primary);
      }
      
      .el-icon {
        font-size: 24px;
        margin-bottom: 5px;
      }
      
      .icon-name {
        font-size: 12px;
        color: #666;
      }
    }
  }
}
</style> 