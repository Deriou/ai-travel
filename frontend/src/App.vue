<script setup>
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { auth, clearLogin } from './auth'

const route = useRoute()
const router = useRouter()

function logout() {
  clearLogin()
  ElMessage.success('已退出登录')
  router.push('/explore')
}
</script>

<template>
  <header class="nav">
    <span class="logo">AI 智游</span>
    <!-- router 模式：点击菜单项跳转到 index 指定的路径 -->
    <el-menu mode="horizontal" router :default-active="route.path" :ellipsis="false" class="nav-menu">
      <el-menu-item index="/explore">城市与景点</el-menu-item>
      <el-menu-item index="/plan">AI 路线规划</el-menu-item>
      <el-menu-item index="/profile">个人中心</el-menu-item>
    </el-menu>
    <div class="nav-user">
      <template v-if="auth.token">
        <span>你好，{{ auth.user?.username }}</span>
        <el-button link type="primary" @click="logout">退出登录</el-button>
      </template>
      <el-button v-else type="primary" @click="router.push('/login')">登录 / 注册</el-button>
    </div>
  </header>

  <main class="page">
    <router-view />
  </main>
</template>
