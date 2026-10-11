<script setup>
import { onMounted, ref } from 'vue'
import request from '../request'

// ---------- 城市列表 ----------
const cities = ref([])
const cityLoading = ref(false)
const cityError = ref(false)

async function loadCities() {
  cityLoading.value = true
  cityError.value = false
  try {
    cities.value = await request.get('/city/list')
  } catch {
    cities.value = []
    cityError.value = true
  } finally {
    cityLoading.value = false
  }
}

function cityName(cityId) {
  return cities.value.find((c) => c.id === cityId)?.cityName || '-'
}

// ---------- 景点分页 ----------
const scenics = ref([])
const total = ref(0)
const cityId = ref(null) // 为空表示全部城市
const pageNum = ref(1)
const pageSize = ref(5)
const scenicLoading = ref(false)
const scenicError = ref(false)

async function loadScenics() {
  scenicLoading.value = true
  scenicError.value = false
  try {
    const data = await request.get('/scenic/list', {
      params: { cityId: cityId.value || undefined, pageNum: pageNum.value, pageSize: pageSize.value },
    })
    scenics.value = data.list
    total.value = data.total
  } catch {
    scenics.value = []
    total.value = 0
    scenicError.value = true
  } finally {
    scenicLoading.value = false
  }
}

// 切换城市后回到第一页。
function changeCity() {
  pageNum.value = 1
  loadScenics()
}

// 点击城市表格中的一行，直接筛选该城市的景点。
function selectCity(row) {
  cityId.value = row.id
  changeCity()
}

onMounted(() => {
  loadCities()
  loadScenics()
})
</script>

<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span class="card-title">城市列表</span>
        <el-button :loading="cityLoading" @click="loadCities">刷新</el-button>
      </div>
    </template>
    <el-table
      v-loading="cityLoading"
      :data="cities"
      :empty-text="cityError ? '数据加载失败，请点击刷新' : '暂无城市数据'"
      highlight-current-row
      @row-click="selectCity"
    >
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="cityName" label="城市" width="140" />
      <el-table-column prop="description" label="简介" min-width="260" />
    </el-table>
  </el-card>

  <el-card>
    <template #header>
      <div class="card-header">
        <span class="card-title">景点列表</span>
        <el-select v-model="cityId" placeholder="全部城市" clearable style="width: 180px" @change="changeCity">
          <el-option v-for="c in cities" :key="c.id" :label="c.cityName" :value="c.id" />
        </el-select>
      </div>
    </template>
    <el-table
      v-loading="scenicLoading"
      :data="scenics"
      :empty-text="scenicError ? '数据加载失败' : '暂无景点数据'"
    >
      <el-table-column prop="scenicName" label="景点" width="180" />
      <el-table-column label="所属城市" width="120">
        <template #default="{ row }">{{ cityName(row.cityId) }}</template>
      </el-table-column>
      <el-table-column prop="scenicDesc" label="简介" min-width="300" />
    </el-table>
    <el-pagination
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      class="pagination"
      @current-change="loadScenics"
    />
  </el-card>
</template>
