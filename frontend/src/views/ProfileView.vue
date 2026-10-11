<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { auth } from '../auth'
import { copyText, formatRoute } from '../copy'
import request from '../request'

const routes = ref([])
const loading = ref(false)
const loadError = ref(false)
const onlyCollected = ref(false)

// 勾选“只看收藏”时只显示 isCollect 为 1 的路线。
const shownRoutes = computed(() =>
  onlyCollected.value ? routes.value.filter((r) => r.isCollect === 1) : routes.value,
)

async function loadRoutes() {
  loading.value = true
  loadError.value = false
  try {
    routes.value = await request.get('/route/myList')
  } catch {
    routes.value = []
    loadError.value = true
  } finally {
    loading.value = false
  }
}

// 传入目标状态（1 收藏 / 0 取消），而不是让后端取反。
async function toggleCollect(route) {
  const target = route.isCollect === 1 ? 0 : 1
  try {
    await request.put(`/route/collect/${route.id}`, { isCollect: target })
    route.isCollect = target
    ElMessage.success(target === 1 ? '已收藏' : '已取消收藏')
  } catch {
    // 错误提示已由 request.js 统一显示。
  }
}

async function remove(route) {
  try {
    await ElMessageBox.confirm(`确定删除「${route.destination} · ${route.days}天」这条路线吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return // 用户点了取消
  }
  try {
    await request.delete(`/route/delete/${route.id}`)
    routes.value = routes.value.filter((r) => r.id !== route.id)
    ElMessage.success('删除成功')
  } catch {
    // 错误提示已由 request.js 统一显示。
  }
}

onMounted(loadRoutes)
</script>

<template>
  <el-card>
    <template #header>
      <span class="card-title">个人信息</span>
    </template>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="用户名">{{ auth.user?.username }}</el-descriptions-item>
      <el-descriptions-item label="注册时间">{{ auth.user?.createTime }}</el-descriptions-item>
    </el-descriptions>
  </el-card>

  <el-card v-loading="loading">
    <template #header>
      <div class="card-header">
        <span class="card-title">我的路线（{{ routes.length }}）</span>
        <div>
          <el-checkbox v-model="onlyCollected" label="只看收藏" />
          <el-button style="margin-left: 12px" @click="loadRoutes">刷新</el-button>
        </div>
      </div>
    </template>

    <el-empty v-if="!loading && shownRoutes.length === 0" :description="loadError ? '路线加载失败，请点击刷新' : '还没有路线'">
      <el-button v-if="!loadError" type="primary" @click="$router.push('/plan')">去生成路线</el-button>
    </el-empty>

    <el-card v-for="route in shownRoutes" :key="route.id" shadow="never" class="route-card">
      <template #header>
        <div class="card-header">
          <div>
            <span class="card-title">{{ route.destination }} · {{ route.days }}天</span>
            <el-tag size="small" style="margin-left: 8px">{{ route.preference }}</el-tag>
            <el-text type="info" size="small" style="margin-left: 8px">{{ route.createTime }}</el-text>
          </div>
          <div>
            <el-button :type="route.isCollect === 1 ? 'warning' : 'default'" @click="toggleCollect(route)">
              {{ route.isCollect === 1 ? '★ 已收藏' : '☆ 收藏' }}
            </el-button>
            <el-button @click="copyText(formatRoute(route))">复制</el-button>
            <el-button type="danger" plain @click="remove(route)">删除</el-button>
          </div>
        </div>
      </template>
      <p class="ai-text">{{ route.routeContent }}</p>
      <template v-if="route.tipsContent">
        <el-divider content-position="left">出行小贴士</el-divider>
        <p class="ai-text">{{ route.tipsContent }}</p>
      </template>
    </el-card>
  </el-card>
</template>

<style scoped>
.route-card + .route-card {
  margin-top: 16px;
}
</style>
