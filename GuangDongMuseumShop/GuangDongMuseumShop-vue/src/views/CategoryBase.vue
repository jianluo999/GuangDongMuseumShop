// 商品列表数据
const products = ref([
  {
    id: 1,
    name: '苏绣丝绸围巾',
    description: '苏州手工刺绣，100%桑蚕丝材质',
    price: 599,
    image: '/images/products/silk-scarf1.jpg',
    sales: 156,
    rating: 4.8
  },
  {
    id: 2,
    name: '广绣真丝披肩',
    description: '广东特色刺绣工艺，真丝面料',
    price: 799,
    image: '/images/products/silk-shawl1.jpg',
    sales: 89,
    rating: 4.9
  },
  {
    id: 3,
    name: '云锦桌旗',
    description: '南京云锦织造，传统纹样设计',
    price: 1299,
    image: '/images/products/silk-runner1.jpg',
    sales: 45,
    rating: 5.0
  },
  {
    id: 4,
    name: '丝绸手帕套装',
    description: '真丝手工卷边，古典花卉图案',
    price: 299,
    image: '/images/products/silk-handkerchief1.jpg',
    sales: 267,
    rating: 4.7
  },
  {
    id: 5,
    name: '蜀锦书签',
    description: '成都蜀锦工艺，双面织造',
    price: 159,
    image: '/images/products/silk-bookmark1.jpg',
    sales: 312,
    rating: 4.6
  },
  {
    id: 6,
    name: '真丝领带',
    description: '100%桑蚕丝，博物馆典藏纹样',
    price: 459,
    image: '/images/products/silk-tie1.jpg',
    sales: 178,
    rating: 4.8
  },
  {
    id: 7,
    name: '丝绸团扇',
    description: '双面刺绣，竹骨丝绸面',
    price: 399,
    image: '/images/products/silk-fan1.jpg',
    sales: 145,
    rating: 4.9
  },
  {
    id: 8,
    name: '真丝香包',
    description: '传统香包造型，丝绸面料制作',
    price: 199,
    image: '/images/products/silk-sachets1.jpg',
    sales: 234,
    rating: 4.7
  }
])

const total = ref(products.value.length)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const currentSort = ref('default')

const loadProducts = async (retryCount = 3) => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      categoryId: props.categoryId,
      sort: sortField.value + ',' + sortOrder.value
    }
    
    const response = await request.get('/api/products', { params })
    products.value = response.data.content
    total.value = response.data.totalElements
    
  } catch (error) {
    console.error('加载商品失败:', error)
    if (error.response?.status === 401 && retryCount > 0) {
      // 如果是未授权错误且还有重试次数,则等待一秒后重试
      await new Promise(resolve => setTimeout(resolve, 1000))
      return loadProducts(retryCount - 1)
    }
    ElMessage.error('加载商品失败,请刷新页面重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProducts()
}) 