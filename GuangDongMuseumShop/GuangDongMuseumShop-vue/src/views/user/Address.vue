<template>
  <div class="address-page">
    <div class="page-header">
      <h2>收货地址</h2>
      <el-button type="primary" @click="handleAdd">
        新增收货地址
      </el-button>
    </div>

    <div class="address-content" v-loading="loading">
      <!-- 地址列表 -->
      <el-empty 
        v-if="addresses.length === 0" 
        description="暂无收货地址"
        :image="'https://via.placeholder.com/160x160?text=NoAddress'"
      >
        <el-button type="primary" @click="handleAdd">
          添加收货地址
        </el-button>
      </el-empty>

      <div v-else class="address-list">
        <el-card 
          v-for="address in addresses" 
          :key="address.id"
          class="address-item"
        >
          <div class="address-info">
            <div class="receiver">
              <span class="name">{{ address.receiver }}</span>
              <span class="phone">{{ address.phone }}</span>
              <el-tag 
                v-if="address.isDefault" 
                type="danger" 
                size="small"
              >
                默认地址
              </el-tag>
            </div>
            <div class="location">
              {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
            </div>
          </div>
          <div class="address-actions">
            <el-button 
              type="primary" 
              link
              @click="handleEdit(address)"
            >
              编辑
            </el-button>
            <el-button 
              type="primary" 
              link
              v-if="!address.isDefault"
              @click="handleSetDefault(address)"
            >
              设为默认
            </el-button>
            <el-button 
              type="danger" 
              link
              @click="handleDelete(address)"
            >
              删除
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 地址编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingAddress ? '编辑地址' : '新增地址'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="addressForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="addressForm.receiver" />
        </el-form-item>

        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="addressForm.phone" />
        </el-form-item>

        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择省/市/区"
          />
        </el-form-item>

        <el-form-item label="详细地址" prop="detailAddress">
          <el-input 
            v-model="addressForm.detailAddress"
            type="textarea"
            rows="2"
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">
            设为默认收货地址
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
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'

// 状态
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editingAddress = ref(null)
const formRef = ref(null)
const addresses = ref([])

// 表单数据
const addressForm = reactive({
  receiver: '',
  phone: '',
  region: [],
  detailAddress: '',
  isDefault: false
})

// 表单验证规则
const rules = {
  receiver: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '���输入正确的手机号码', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 省市区数据（这里使用示例数据，实际项目中应该从后端获取）
const regionOptions = [
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '越秀区', label: '越秀区' },
          { value: '海珠区', label: '海珠区' },
          { value: '荔湾区', label: '荔湾区' }
        ]
      }
    ]
  }
]

// 获取地址列表
const fetchAddresses = async () => {
  loading.value = true
  try {
    const { data } = await axios.get('/api/addresses')
    addresses.value = data
  } catch (error) {
    console.error('获取地址列表失败:', error)
    ElMessage.error('获取地址列表失败')
  } finally {
    loading.value = false
  }
}

// 新增地址
const handleAdd = () => {
  editingAddress.value = null
  addressForm.receiver = ''
  addressForm.phone = ''
  addressForm.region = []
  addressForm.detailAddress = ''
  addressForm.isDefault = false
  dialogVisible.value = true
}

// 编辑地址
const handleEdit = (address) => {
  editingAddress.value = address
  addressForm.receiver = address.receiver
  addressForm.phone = address.phone
  addressForm.region = [address.province, address.city, address.district]
  addressForm.detailAddress = address.detailAddress
  addressForm.isDefault = address.isDefault
  dialogVisible.value = true
}

// 设为默认地址
const handleSetDefault = async (address) => {
  try {
    await axios.put(`/api/addresses/${address.id}/default`)
    ElMessage.success('默认地址设置成功')
    fetchAddresses()
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置默认地址失败')
  }
}

// 删除地址
const handleDelete = async (address) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      type: 'warning'
    })
    
    await axios.delete(`/api/addresses/${address.id}`)
    ElMessage.success('地址删除成功')
    fetchAddresses()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地址失败:', error)
      ElMessage.error('删除地址失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const [province, city, district] = addressForm.region
    const data = {
      receiver: addressForm.receiver,
      phone: addressForm.phone,
      province,
      city,
      district,
      detailAddress: addressForm.detailAddress,
      isDefault: addressForm.isDefault
    }
    
    if (editingAddress.value) {
      await axios.put(`/api/addresses/${editingAddress.value.id}`, data)
      ElMessage.success('地址修改成功')
    } else {
      await axios.post('/api/addresses', data)
      ElMessage.success('地址添加成功')
    }
    
    dialogVisible.value = false
    fetchAddresses()
  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error('保存地址失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchAddresses()
})
</script>

<style scoped lang="scss">
.address-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      font-size: 24px;
      color: #333;
    }
  }

  .address-list {
    display: grid;
    gap: 20px;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));

    .address-item {
      .address-info {
        margin-bottom: 15px;

        .receiver {
          margin-bottom: 10px;

          .name {
            font-weight: bold;
            margin-right: 10px;
          }

          .phone {
            color: #666;
            margin-right: 10px;
          }
        }

        .location {
          color: #666;
          line-height: 1.5;
        }
      }

      .address-actions {
        display: flex;
        justify-content: flex-end;
        gap: 15px;
        padding-top: 15px;
        border-top: 1px solid #eee;
      }
    }
  }
}
</style> 