<script setup>
import { ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { saveLogin } from '../auth'
import request from '../request'

const route = useRoute()
const router = useRouter()

const mode = ref('login') // login 登录，register 注册
const form = reactive({ username: '', password: '', confirm: '' })
const loading = ref(false)

async function submit() {
  if (!form.username.trim() || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (mode.value === 'register' && form.password !== form.confirm) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    const body = { username: form.username.trim(), password: form.password }
    if (mode.value === 'register') {
      await request.post('/user/register', body)
      ElMessage.success('注册成功，请登录')
      mode.value = 'login'
      form.password = ''
      form.confirm = ''
    } else {
      const data = await request.post('/user/login', body)
      saveLogin(data.token, data.user)
      ElMessage.success('登录成功')
      // 从哪个页面被拦截过来，登录后就回到哪里。
      router.push(route.query.redirect || '/plan')
    }
  } catch {
    // 错误提示已由 request.js 统一显示。
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-card class="login-card">
    <el-tabs v-model="mode" stretch>
      <el-tab-pane label="登录" name="login" />
      <el-tab-pane label="注册" name="register" />
    </el-tabs>

    <el-form label-width="80px" @submit.prevent="submit">
      <el-form-item label="用户名">
        <el-input v-model="form.username" maxlength="50" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" show-password maxlength="20" placeholder="6～20 个字符" />
      </el-form-item>
      <el-form-item v-if="mode === 'register'" label="确认密码">
        <el-input v-model="form.confirm" type="password" show-password maxlength="20" placeholder="再次输入密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">
          {{ mode === 'login' ? '登录' : '注册' }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.login-card {
  max-width: 420px;
  margin: 60px auto;
}
</style>
