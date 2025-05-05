const fetchProducts = async () => {
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      categoryId: props.categoryId,
      sort: sortField.value + ',' + sortOrder.value
    }
    console.log('Fetching products with params:', params)
    const response = await getProducts(params)
    console.log('Products response:', response)
    if (response.code === 200) {
      products.value = response.data.content
      total.value = response.data.totalElements
    } else {
      throw new Error(response.message || '获取商品列表失败')
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败，请重试')
  }
} 