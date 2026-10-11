import axios from 'axios'
import { ElMessage } from 'element-plus'
import { auth, clearLogin } from './auth'
import router from './router'

// 所有请求以 /api 开头，Vite 代理去掉 /api 后转发到 http://localhost:8080。
const request = axios.create({ baseURL: '/api', timeout: 15000 })

function showError(message) {
  // grouping：同时出现多条相同提示时合并显示。
  ElMessage.error({ message, grouping: true })
}

// 发送前：已登录就在请求头携带令牌。
request.interceptors.request.use((config) => {
  if (auth.token) {
    config.headers.Authorization = 'Bearer ' + auth.token
  }
  return config
})

// 收到响应后：code 为 200 时直接返回 data；其他情况统一提示 msg，并让调用方进入 catch。
request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code === 200) {
      return result.data
    }
    showError(result.msg || '请求失败')
    return Promise.reject(new Error(result.msg))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      // 令牌缺失、过期或无效：清除登录状态，跳转登录页，登录后回到当前页面。
      clearLogin()
      ElMessage.warning({ message: '登录已失效，请重新登录', grouping: true })
      const current = router.currentRoute.value
      if (current.path !== '/login') {
        router.push({ path: '/login', query: { redirect: current.fullPath } })
      }
    } else if (error.code === 'ECONNABORTED') {
      showError('请求超时，请稍后重试')
    } else {
      showError(error.response?.data?.msg || '无法连接服务器，请检查后端是否已启动')
    }
    return Promise.reject(error)
  },
)

export default request
