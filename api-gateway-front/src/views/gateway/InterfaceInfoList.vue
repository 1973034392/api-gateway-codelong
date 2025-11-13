<template>
  <div class="interface-info-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h1>接口信息管理</h1>
        <p class="subtitle">查看和管理所有服务的接口信息</p>
      </div>
    </div>

    <!-- 服务选择 -->
    <div class="service-selector">
      <label>选择核心服务</label>
      <select v-model="selectedService" class="service-select">
        <option value="">-- 请选择服务 --</option>
        <option v-for="service in services" :key="service.id" :value="service.id">
          {{ service.serverName }} ({{ service.safeKey }})
        </option>
      </select>
    </div>

    <!-- 接口列表 -->
    <div class="table-container">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="!selectedService" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z"></path>
        </svg>
        <p>请选择一个服务查看接口信息</p>
      </div>
      <div v-else-if="currentServiceApis.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z"></path>
        </svg>
        <p>暂无接口数据</p>
      </div>
      <div v-else>
        <!-- 服务信息卡片 -->
        <div class="service-info-card">
          <h3>{{ currentService?.serverName }}</h3>
          <p>共 {{ currentServiceApis.length }} 个接口</p>
        </div>

        <!-- 接口表格 -->
        <div class="interfaces-table">
          <div class="table-header">
            <div class="col col-name">接口名称</div>
            <div class="col col-path">接口路径</div>
            <div class="col col-type">接口类型</div>
            <div class="col col-remark">备注</div>
            <div class="col col-time">更新时间</div>
          </div>
          <div v-for="api in currentServiceApis" :key="api.id" class="table-row">
            <div class="col col-name">
              <span class="api-name">{{ api.name }}</span>
            </div>
            <div class="col col-path">
              <code v-if="api.path" class="api-path">{{ api.path }}</code>
              <span v-else class="text-muted">-</span>
            </div>
            <div class="col col-type">
              <span class="type-badge" :class="{ http: api.type === 'HTTP', dubbo: api.type === 'DUBBO' }">
                {{ api.type }}
              </span>
            </div>
            <div class="col col-remark">{{ api.remark }}</div>
            <div class="col col-time">{{ api.createTime || '-' }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {coreService, gatewayInterface} from '../../api/gateway'

const loading = ref(false)
const selectedService = ref('')
const services = ref([])
const interfaceData = ref({})

const loadData = async () => {
  loading.value = true
  try {
    // 加载核心服务列表
    const res = await coreService.list()
    services.value = res || []

    // 为每个服务加载接口列表
    for (const service of services.value) {
      try {
        const apiRes = await gatewayInterface.page({
          pageNo: 1,
          pageSize: 100,
          serverId: service.id
        })
        interfaceData.value[service.id] = apiRes.list || []
      } catch (error) {
        interfaceData.value[service.id] = []
      }
    }
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const currentService = computed(() => {
  return services.value.find(s => s.id === selectedService.value)
})

const currentServiceApis = computed(() => {
  return interfaceData.value[selectedService.value] || []
})

// 初始加载
loadData()
</script>

<style scoped>
.interface-info-list {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #030213;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #717182;
  margin: 0;
}

.service-selector {
  background: white;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  margin-bottom: 24px;
}

.service-selector label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #030213;
  margin-bottom: 8px;
}

.service-select {
  width: 100%;
  max-width: 400px;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 14px;
  color: #030213;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
}

.service-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.table-container {
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.loading,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #717182;
}

.empty-state svg {
  margin-bottom: 16px;
  opacity: 0.5;
}

.service-info-card {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.service-info-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #030213;
  margin: 0 0 4px 0;
}

.service-info-card p {
  font-size: 13px;
  color: #717182;
  margin: 0;
}

.interfaces-table {
  display: flex;
  flex-direction: column;
}

.table-header {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr;
  gap: 12px;
  padding: 12px 16px;
  background: #f3f3f5;
  font-weight: 600;
  font-size: 13px;
  color: #030213;
  border-bottom: 1px solid #e5e7eb;
}

.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 13px;
  color: #717182;
  transition: background-color 0.2s ease;
}

.table-row:hover {
  background-color: #f9fafb;
}

.table-row:last-child {
  border-bottom: none;
}

.col {
  display: flex;
  align-items: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-name {
  font-weight: 500;
  color: #030213;
}

.api-name {
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 12px;
}

.api-path {
  background: #e3f2fd;
  color: #1976d2;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 12px;
}

.type-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: #f3f3f5;
  color: #717182;
}

.type-badge.http {
  background: #dbeafe;
  color: #1e40af;
}

.type-badge.dubbo {
  background: #fce7f3;
  color: #be185d;
}

.text-muted {
  color: #ececf0;
}
</style>

