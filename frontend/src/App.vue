<script setup>
import axios from 'axios'
import { onMounted, ref } from 'vue'

const list = ref([])
const loading = ref(false)
const error = ref('')

// 页面打开时从后端读取城市，刷新按钮也调用同一个方法。
async function loadCities() {
  loading.value = true
  error.value = ''
  list.value = []

  try {
    const result = await axios.get('/api/city/list', { timeout: 10000 })
    if (result.data.code !== 200) {
      throw new Error(result.data.msg || '城市列表获取失败')
    }
    if (!Array.isArray(result.data.data)) {
      throw new Error('城市列表的数据格式不正确')
    }
    list.value = result.data.data
  } catch (err) {
    error.value = axios.isAxiosError(err)
      ? '无法获取城市列表，请检查后端服务是否已启动，再点击刷新。'
      : err.message || '城市列表获取失败，请重试。'
  } finally {
    loading.value = false
  }
}

onMounted(loadCities)
</script>

<template>
  <main class="city-page">
    <header class="page-header">
      <h1>城市列表</h1>
      <el-button :loading="loading" @click="loadCities">刷新</el-button>
    </header>

    <el-alert
      v-if="error"
      :title="error"
      type="error"
      show-icon
      :closable="false"
      class="error-message"
    />

    <el-table
      v-loading="loading"
      :data="list"
      :empty-text="error ? '数据加载失败' : '暂无城市数据'"
      style="width: 100%"
    >
      <el-table-column prop="id" label="编号" width="100" />
      <el-table-column prop="cityName" label="城市名" width="180" />
      <el-table-column prop="description" label="详细描述" min-width="260" />
    </el-table>
  </main>
</template>
