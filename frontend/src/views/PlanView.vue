<script setup>
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { copyText, formatRoute } from '../copy'
import request from '../request'

const presetPreferences = ['休闲', '美食', '历史文化', '自然风光', '亲子', '购物', '拍照打卡']

const cities = ref([]) // 目的地下拉建议，也可以直接输入其他城市
const form = reactive({ destination: '', days: 3, preferences: [], otherPreference: '' })

const generating = ref(false)
const result = ref(null) // 生成结果：出行条件 + routeContent + tipsContent
const saving = ref(false)
const saved = ref(false)

onMounted(async () => {
  try {
    cities.value = await request.get('/city/list')
  } catch {
    // 城市加载失败不影响手动输入目的地。
  }
})

// 预设选项和自定义内容合并成一个偏好字符串，例如“休闲、美食、喜欢夜景”。
function buildPreference() {
  return [...form.preferences, form.otherPreference.trim()].filter(Boolean).join('、')
}

async function generate() {
  const preference = buildPreference()
  if (!form.destination.trim()) {
    ElMessage.warning('请选择或输入目的地')
    return
  }
  if (!preference) {
    ElMessage.warning('请选择或填写游玩偏好')
    return
  }

  const trip = { destination: form.destination.trim(), days: form.days, preference }
  generating.value = true
  result.value = null
  saved.value = false
  try {
    // 路线和小贴士同时请求；大模型较慢，超时时间放宽到 90 秒。
    const [routeContent, tipsContent] = await Promise.all([
      request.post('/ai/generateRoute', trip, { timeout: 90000 }),
      request.post('/ai/generateTips', { destination: trip.destination, days: trip.days }, { timeout: 90000 }),
    ])
    result.value = { ...trip, routeContent, tipsContent }
  } catch {
    // 失败提示已由 request.js 显示，用户可以再次点击生成。
  } finally {
    generating.value = false
  }
}

async function save() {
  saving.value = true
  try {
    await request.post('/route/save', result.value)
    saved.value = true
    ElMessage.success('保存成功，可在个人中心查看')
  } catch {
    // 错误提示已由 request.js 统一显示。
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <el-card>
    <template #header>
      <span class="card-title">填写出行条件</span>
    </template>
    <el-form label-width="90px" style="max-width: 640px">
      <el-form-item label="目的地" required>
        <el-select
          v-model="form.destination"
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入目的地"
          style="width: 100%"
        >
          <el-option v-for="c in cities" :key="c.id" :label="c.cityName" :value="c.cityName" />
        </el-select>
      </el-form-item>
      <el-form-item label="出行天数" required>
        <el-input-number v-model="form.days" :min="1" :max="30" :step="1" step-strictly />
      </el-form-item>
      <el-form-item label="游玩偏好" required>
        <el-checkbox-group v-model="form.preferences">
          <el-checkbox v-for="p in presetPreferences" :key="p" :value="p" :label="p" />
        </el-checkbox-group>
        <el-input v-model="form.otherPreference" maxlength="50" placeholder="其他偏好（选填），如：喜欢夜景、不想太累" />
      </el-form-item>
      <el-form-item>
        <!-- 生成期间按钮处于加载状态，避免重复提交 -->
        <el-button type="primary" :loading="generating" @click="generate">
          {{ generating ? 'AI 正在生成，请稍候…' : '生成路线和小贴士' }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-card v-if="result">
    <template #header>
      <div class="card-header">
        <span class="card-title">{{ result.destination }} · {{ result.days }}天 · {{ result.preference }}</span>
        <div>
          <el-button @click="copyText(formatRoute(result))">复制文本</el-button>
          <el-button type="primary" :loading="saving" :disabled="saved" @click="save">
            {{ saved ? '已保存' : '保存路线' }}
          </el-button>
        </div>
      </div>
    </template>
    <h3>路线安排</h3>
    <p class="ai-text">{{ result.routeContent }}</p>
    <h3>出行小贴士</h3>
    <p class="ai-text">{{ result.tipsContent }}</p>
    <el-text type="info" size="small">AI 生成内容仅供出行参考，不保证信息实时准确。</el-text>
  </el-card>
</template>
