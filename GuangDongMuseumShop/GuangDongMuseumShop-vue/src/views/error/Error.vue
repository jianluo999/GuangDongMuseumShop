<template>
  <div class="error-page">
    <div class="error-content">
      <img src="https://via.placeholder.com/300x300?text=500" alt="500" class="error-image">
      <h1>500</h1>
      <h2>服务器错误</h2>
      <p>抱歉，服务器出现了一些问题，请稍后再试</p>
      <el-button type="primary" @click="handleRefresh">
        刷新页面
      </el-button>
    </div>
  </div>
</template>

<script setup>
const handleRefresh = () => {
  window.location.reload()
}
</script>

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