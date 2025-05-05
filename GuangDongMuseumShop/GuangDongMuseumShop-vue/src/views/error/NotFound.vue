<template>
  <div class="error-page">
    <div class="error-content">
      <img src="https://via.placeholder.com/300x300?text=404" alt="404" class="error-image">
      <h1>404</h1>
      <h2>页面不存在</h2>
      <p>抱歉，您访问的页面不存在或已被删除</p>
      <el-button type="primary" @click="$router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  padding: 20px;
}

.error-content {
  text-align: center;

  .error-image {
    width: 300px;
    max-width: 100%;
    margin-bottom: 30px;
  }

  h1 {
    font-size: 72px;
    color: var(--primary-color);
    margin: 0 0 10px;
  }

  h2 {
    font-size: 24px;
    color: #333;
    margin: 0 0 20px;
  }

  p {
    color: #666;
    margin-bottom: 30px;
  }
}
</style> 