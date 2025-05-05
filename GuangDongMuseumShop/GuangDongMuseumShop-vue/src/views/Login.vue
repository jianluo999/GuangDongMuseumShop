const handleLogin = async () => {
  try {
    const response = await request.post('/auth/login', {
      username: form.username,
      password: form.password
    })
    
    // 保存 token
    localStorage.setItem('token', response.data.token)
    
    // 跳转到管理页面
    router.push('/admin')
  } catch (error) {
    console.error('Login failed:', error)
  }
} 