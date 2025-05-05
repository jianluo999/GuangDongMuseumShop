<template>
  <div class="specs-container">
    <div v-for="spec in specs" :key="spec.name" class="spec-group">
      <div class="spec-name">{{ spec.name }}</div>
      <div class="spec-options">
        <el-radio-group v-model="selectedSpecs[spec.name]" @change="handleSpecChange">
          <el-radio-button 
            v-for="option in spec.options" 
            :key="option.value"
            :label="option.value"
            :disabled="!option.enabled"
          >
            {{ option.label }}
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { getProductSpecs } from '@/api/spec'

const props = defineProps({
  productId: {
    type: Number,
    required: true
  },
  initialSpecs: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['spec-change'])

const specs = ref([])
const selectedSpecs = ref({})

// 加载规格数据
const loadSpecs = async () => {
  try {
    const response = await getProductSpecs(props.productId)
    if (response.data.code === 200) {
      // 对规格进行去重处理
      const uniqueSpecs = response.data.data.reduce((acc, curr) => {
        const existingSpec = acc.find(spec => spec.name === curr.name)
        if (!existingSpec) {
          acc.push(curr)
        }
        return acc
      }, [])
      specs.value = uniqueSpecs
      // 初始化选中状态
      specs.value.forEach(spec => {
        selectedSpecs.value[spec.name] = ''
      })
    }
  } catch (error) {
    console.error('加载商品规格失败:', error)
  }
}

// 处理规格选择变化
const handleSpecChange = () => {
  emit('spec-change', selectedSpecs.value)
}

// 监听初始规格变化
watch(() => props.initialSpecs, (newSpecs) => {
  if (newSpecs && newSpecs.length > 0) {
    // 对初始规格进行去重处理
    const uniqueSpecs = newSpecs.reduce((acc, curr) => {
      const existingSpec = acc.find(spec => spec.name === curr.name)
      if (!existingSpec) {
        acc.push(curr)
      }
      return acc
    }, [])
    specs.value = uniqueSpecs
    // 初始化选中状态
    specs.value.forEach(spec => {
      selectedSpecs.value[spec.name] = ''
    })
  }
}, { immediate: true })

onMounted(() => {
  if (!props.initialSpecs || props.initialSpecs.length === 0) {
    loadSpecs()
  }
})
</script>

<style lang="scss" scoped>
.specs-container {
  margin: 20px 0;
  
  .spec-group {
    margin-bottom: 20px;
    
    .spec-name {
      font-size: 16px;
      font-weight: bold;
      margin-bottom: 10px;
      color: #333;
    }
    
    .spec-options {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      
      :deep(.el-radio-button__inner) {
        padding: 8px 15px;
        border-radius: 4px;
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
      
      :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
        background-color: var(--el-color-primary);
        border-color: var(--el-color-primary);
        box-shadow: -1px 0 0 0 var(--el-color-primary);
      }
    }
  }
}
</style> 