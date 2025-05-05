<template>
  <div class="product-edit">
    <div class="header">
      <h2>{{ isEdit ? '编辑商品' : '新增商品' }}</h2>
    </div>

    <el-form 
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="product-form"
    >
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入商品名称" />
      </el-form-item>

      <el-form-item label="商品分类" prop="categoryId">
        <el-select 
          v-model="form.categoryId" 
          placeholder="请选择商品分类"
          clearable
        >
          <el-option
            v-for="category in flattenCategories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="商品价格" prop="price">
        <el-input-number 
          v-model="form.price"
          :precision="2"
          :step="0.1"
          :min="0"
        />
      </el-form-item>

      <el-form-item label="商品库存" prop="stock">
        <el-input-number 
          v-model="form.stock"
          :min="0"
          :step="1"
        />
      </el-form-item>

      <el-form-item label="商品描述" prop="description">
        <div style="border: 1px solid #ccc; z-index: 100;">
          <Toolbar
            style="border-bottom: 1px solid #ccc"
            :editor="editorRef"
            :defaultConfig="toolbarConfig"
            mode="default"
          />
          <Editor
            style="height: 300px; overflow-y: hidden;"
            v-model="form.description"
            :defaultConfig="editorConfig"
            mode="default"
            @onCreated="handleCreated"
          />
        </div>
      </el-form-item>

      <el-form-item label="文化背景" prop="culturalBackground">
        <el-input
          v-model="form.culturalBackground"
          type="textarea"
          rows="4"
          placeholder="请输入商品的文化背景描述"
        />
      </el-form-item>

      <el-divider content-position="left">商品图片</el-divider>

      <el-form-item label="商品图片" prop="images">
        <el-upload
          list-type="picture-card"
          :http-request="customUpload"
          :on-remove="handleRemove"
          :before-upload="beforeUpload"
          :file-list="fileList"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>

      <el-form-item label="商品状态" prop="enabled">
        <el-switch v-model="form.enabled" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>

    <!-- 批量导入规格对话框 -->
    <el-dialog
      v-model="batchImportVisible"
      title="批量导入规格"
      width="600px"
    >
      <el-form>
        <el-form-item label="规格数据">
          <el-input
            v-model="batchImportText"
            type="textarea"
            :rows="10"
            placeholder="请输入规格数据，格式：
规格名称1: 规格值1, 规格值2, 规格值3
规格名称2: 规格值1, 规格值2
例如：
尺寸: 标准尺寸, 放大尺寸
材质: 青铜, 树脂
包装: 简装, 礼盒装"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchImportVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchImport">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import axios from 'axios'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const categories = ref([])
const fileList = ref([])
const loading = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = ref({
  name: '',
  categoryId: undefined,
  price: 0,
  stock: 0,
  description: '',
  culturalBackground: '',
  images: [],
  enabled: true,
  packageType: '',
  packaging: '',
  length: '',
  width: '',
  height: '',
  material: ''
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }]
}

// 将树形分类数据扁平化
const flattenCategories = computed(() => {
  console.log('计算 flattenCategories, categories.value:', categories.value)
  const flatten = (items) => {
    if (!Array.isArray(items)) return []  // 添加数组检查
    return items.reduce((acc, item) => {
      if (!item) return acc  // 添加空值检查
      
      // 添加当前节点
      acc.push({
        id: item.id,
        name: item.name
      })
      
      // 如果有子节点，递归处理
      if (Array.isArray(item.children) && item.children.length > 0) {
        acc.push(...flatten(item.children))
      }
      return acc
    }, [])
  }
  return flatten(categories.value || [])  // 确保传入数组
})

// 加载分类数据
const loadCategories = async () => {
  try {
    console.log('开始加载分类数据')
    const { data: res } = await request.get('/api/admin/categories/tree')
    console.log('API 返回数据:', res)
    
    if (res.code === 200) {
      categories.value = res.data
      console.log('设置分类数据:', categories.value)
    } else {
      console.error('API 返回错误:', res.message)
      ElMessage.error(res.message || '获取分类数据失败')
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  }
}

// 监听分类数据变化
watch(categories, (newVal) => {
  console.log('categories 发生变化:', newVal)
}, { deep: true })

// 监听表单分类 ID 变化
watch(() => form.value.categoryId, (newVal) => {
  console.log('form.categoryId 变化:', newVal)
})

// 获取商品信息
const getProduct = async (id) => {
  try {
    loading.value = true
    console.log('开始获取商品信息, id:', id)
    const res = await request.get(`/api/admin/products/${id}`)
    console.log('API 返回数据:', res)

    if (res?.data?.code === 200 && res.data.data) {
      console.log('API 返回的商品数据:', res.data.data)
      
      // 直接使用解构赋值并保留现有结构
      const {
        name,
        description,
        price,
        stock,
        categoryId,
        enabled,
        culturalBackground,
        images = [],
        specs = {},
        packageType = '',
        packaging = '',
        length = '',
        width = '',
        height = '',
        material = ''
      } = res.data.data
      
      console.log('解构后的数据:', {
        name,
        description,
        price,
        stock,
        categoryId,
        enabled,
        culturalBackground,
        images,
        specs,
        packageType,
        packaging,
        length,
        width,
        height,
        material
      })

      // 更新表单数据
      form.value = {
        name,
        description,
        price,
        stock,
        categoryId,
        enabled,
        culturalBackground: culturalBackground || '',
        images: [...images],
        specs: specs || {},
        packageType,
        packaging,
        length,
        width,
        height,
        material
      }
      console.log('更新后的表单数据:', form.value)

      // 设置文件列表
      if (images && images.length > 0) {
        console.log('处理图片列表:', images)
        fileList.value = images.map((url, index) => ({
          name: url.split('/').pop(),
          url,
          uid: -(index + 1)
        }))
        console.log('设置后的文件列表:', fileList.value)
      }
    } else {
      console.warn('API 返回数据异常:', res)
      ElMessage.warning('获取商品数据失败')
    }
  } catch (error) {
    console.error('获取商品信息失败:', error)
    ElMessage.error(error.message || '获取商品信息失败')
  } finally {
    loading.value = false
  }
}

// 处理图片上传成功
const handleUploadSuccess = (url) => {
  if (!form.value.images) {
    form.value.images = []
  }
  if (typeof url === 'string') {
    form.value.images.push(url)
  }
}

// 处理图片删除
const handleRemove = (uploadFile) => {
  console.log('删除图片:', uploadFile)
  const index = form.value.images.indexOf(uploadFile.url)
  if (index !== -1) {
    form.value.images.splice(index, 1)
    fileList.value.splice(index, 1)
  }
}

// 上传前校验
const beforeUpload = (file) => {
  console.log('上传前校验:', file)
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

const uploadImage = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const token = localStorage.getItem('token')
    console.log('Token:', token)
    
    const response = await request.post('/api/admin/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    console.log('Upload response:', response)
    return response.data
  } catch (error) {
    console.error('Upload error details:', error.response?.data)
    console.error('上传失败:', error)
    throw error
  }
}

// 添加自定义上传方法
const customUpload = async ({ file }) => {
  try {
    const result = await uploadImage(file)
    if (result.code === 200) {
      handleUploadSuccess(result.data)
      return result.data
    } else {
      ElMessage.error(result.message || '上传失败')
      return false
    }
  } catch (error) {
    console.error('上传错误:', error)
    ElMessage.error('上传失败')
    return false
  }
}

// 添加规格相关的响应式变量
const batchImportVisible = ref(false)
const batchImportText = ref('')
const specList = ref([])

// 处理批量导入
const handleBatchImport = () => {
  try {
    const lines = batchImportText.value.split('\n').filter(line => line.trim())
    const specs = lines.map(line => {
      const [name, valuesStr] = line.split(':')
      const values = valuesStr.split(',').map(v => v.trim()).filter(v => v)
      return {
        name: name.trim(),
        values: values.join(', ')
      }
    })
    specList.value = specs
    form.value.specs = specs.reduce((acc, spec) => {
      acc[spec.name] = spec.values.split(', ')
      return acc
    }, {})
    batchImportVisible.value = false
    ElMessage.success('规格导入成功')
  } catch (error) {
    ElMessage.error('规格数据格式不正确，请检查后重试')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await request.put(`/api/admin/products/${route.params.id}`, form.value)
    } else {
      await request.post('/api/admin/products', form.value)
    }
    ElMessage.success(`${isEdit.value ? '更新' : '新增'}成功`)
    router.push('/admin/products')
  } catch (error) {
    ElMessage.error(`${isEdit.value ? '更新' : '新增'}失败`)
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

// 编辑器实例，必须用 shallowRef
const editorRef = ref(null)
const editorConfig = { 
  placeholder: '请输入商品描述...',
  MENU_CONF: {
    uploadImage: {
      server: '/api/admin/upload',
      fieldName: 'file',
      headers: {
        Authorization: localStorage.getItem('token')
      },
      maxFileSize: 2 * 1024 * 1024,
      maxNumberOfFiles: 10,
      allowedFileTypes: ['image/*'],
      customInsert(res, insertFn) {
        if (res.code === 200 && res.data) {
          insertFn(res.data)
        } else {
          ElMessage.error('图片上传失败')
        }
      },
      onError(file, err) {
        console.error('图片上传出错:', err)
        ElMessage.error('图片上传失败: ' + (err.message || '未知错误'))
      }
    }
  }
}

const toolbarConfig = {
  excludeKeys: []
}

// 处理编辑器创建完成
const handleCreated = (editor) => {
  editorRef.value = editor
}

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

onMounted(async () => {
  try {
    console.log('组件挂载, 路由参数:', route.params)
    await loadCategories()
    if (route.params.id) {
      console.log('开始加载商品数据, id:', route.params.id)
      await getProduct(route.params.id)
    }
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.error('初始化失败')
  }
})

// 监听表单数据变化
watch(() => form.value, (newVal) => {
  console.log('表单数据变化:', newVal)
}, { deep: true })
</script>

<style scoped>
.product-edit {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.product-form {
  max-width: 800px;
}

:deep(.w-e-text-container) {
  min-height: 300px !important;
}
</style> 