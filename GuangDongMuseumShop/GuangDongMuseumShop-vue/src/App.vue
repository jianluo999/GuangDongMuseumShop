<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<style>
#app {
  font-family: Arial, sans-serif;
}
</style>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  userStore.checkLoginState()
})
</script>
