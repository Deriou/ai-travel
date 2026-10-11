import { ElMessage } from 'element-plus'
import { createRouter, createWebHistory } from 'vue-router'
import { auth } from './auth'
import ExploreView from './views/ExploreView.vue'
import LoginView from './views/LoginView.vue'
import PlanView from './views/PlanView.vue'
import ProfileView from './views/ProfileView.vue'

// requiresAuth 表示需要登录才能进入。
const routes = [
  { path: '/', redirect: '/explore' },
  { path: '/explore', component: ExploreView },
  { path: '/login', component: LoginView },
  { path: '/plan', component: PlanView, meta: { requiresAuth: true } },
  { path: '/profile', component: ProfileView, meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 每次切换页面前检查：需要登录但还没有令牌时，先去登录页。
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !auth.token) {
    ElMessage.info({ message: '请先登录', grouping: true })
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
