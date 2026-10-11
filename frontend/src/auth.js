import { reactive } from 'vue'

// 登录状态保存在 localStorage，刷新页面后仍保持登录。
// reactive 让导航栏等页面在登录、退出后自动更新。
export const auth = reactive({
  token: localStorage.getItem('token') || '',
  user: JSON.parse(localStorage.getItem('user') || 'null'),
})

export function saveLogin(token, user) {
  auth.token = token
  auth.user = user
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(user))
}

// 退出登录只清除浏览器中的令牌，后端不需要退出接口。
export function clearLogin() {
  auth.token = ''
  auth.user = null
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}
