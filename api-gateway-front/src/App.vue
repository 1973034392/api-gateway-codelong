<script setup>
import {onMounted} from 'vue'
import {useRouter} from 'vue-router'

const router = useRouter()

onMounted(() => {
  // 应用初始化
})
</script>

<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body,
#app {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

body {
  background-color: #f9fafb;
}
</style>
