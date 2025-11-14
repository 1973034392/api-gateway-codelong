<template>
  <div class="interface-info-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h1>接口信息管理</h1>
        <p class="subtitle">查看和管理所有服务的接口和方法信息</p>
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

    <!-- 接口和方法列表 -->
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

        <!-- 接口列表 -->
        <div class="interfaces-list">
          <div v-for="api in currentServiceApis" :key="api.id" class="interface-item">
            <!-- 接口头部 -->
            <div class="interface-header" @click="toggleInterface(api.id)">
              <div class="interface-info">
                <svg
                  class="expand-icon"
                  :class="{ expanded: expandedInterfaces.has(api.id) }"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
                <span class="interface-name">{{ api.interfaceName }}</span>
                <span class="type-badge" :class="{ http: isHttpInterface(api.interfaceName), dubbo: !isHttpInterface(api.interfaceName) }">
                  {{ isHttpInterface(api.interfaceName) ? 'HTTP' : 'DUBBO' }}
                </span>
                <span class="method-count">{{ interfaceMethods[api.id]?.length || 0 }} 个方法</span>
              </div>
              <div class="interface-time">{{ formatTime(api.createTime) }}</div>
            </div>

            <!-- 方法列表 -->
            <div v-if="expandedInterfaces.has(api.id)" class="methods-container">
              <div v-if="loadingMethods[api.id]" class="methods-loading">加载方法中...</div>
              <div v-else-if="!interfaceMethods[api.id] || interfaceMethods[api.id].length === 0" class="methods-empty">
                暂无方法
              </div>
              <div v-else class="methods-table">
                <div class="methods-header">
                  <div class="col col-method-name">方法名称</div>
                  <div class="col col-url">请求路径</div>
                  <div class="col col-http-type">请求类型</div>
                  <div class="col col-param">参数类型</div>
                  <div class="col col-auth">是否鉴权</div>
                  <div class="col col-time">更新时间</div>
                </div>
                <div v-for="method in interfaceMethods[api.id]" :key="method.id" class="method-row">
                  <div class="col col-method-name">
                    <code class="method-name">{{ method.methodName }}</code>
                  </div>
                  <div class="col col-url">
                    <code v-if="method.url" class="url-path">{{ method.url }}</code>
                    <span v-else class="text-muted">-</span>
                  </div>
                  <div class="col col-http-type">
                    <span v-if="method.isHttp === 1" class="http-type-badge" :class="method.httpType?.toLowerCase()">
                      {{ method.httpType || '-' }}
                    </span>
                    <span v-else class="http-type-badge dubbo">DUBBO</span>
                  </div>
                  <div class="col col-param">
                    <span class="param-type">{{ method.parameterType || '-' }}</span>
                  </div>
                  <div class="col col-auth">
                    <span class="auth-badge" :class="{ required: method.isAuth === 1, optional: method.isAuth === 0 }">
                      {{ method.isAuth === 1 ? '需要' : '不需要' }}
                    </span>
                  </div>
                  <div class="col col-time">{{ formatTime(method.updateTime) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {coreService, gatewayInterface} from '../../api/gateway'

const loading = ref(false)
const selectedService = ref('')
const services = ref([])
const interfaceData = ref({})
const interfaceMethods = reactive({})
const expandedInterfaces = ref(new Set())
const loadingMethods = reactive({})

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
          pageNum: 1,
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

// 切换接口展开/收起
const toggleInterface = async (interfaceId) => {
  if (expandedInterfaces.value.has(interfaceId)) {
    expandedInterfaces.value.delete(interfaceId)
  } else {
    expandedInterfaces.value.add(interfaceId)
    // 如果还没有加载过方法列表,则加载
    if (!interfaceMethods[interfaceId]) {
      await loadMethods(interfaceId)
    }
  }
}

// 加载方法列表
const loadMethods = async (interfaceId) => {
  loadingMethods[interfaceId] = true
  try {
    const methods = await gatewayInterface.getMethods(interfaceId)
    interfaceMethods[interfaceId] = methods || []
  } catch (error) {
    ElMessage.error('加载方法失败: ' + error.message)
    interfaceMethods[interfaceId] = []
  } finally {
    loadingMethods[interfaceId] = false
  }
}

// 判断是否为HTTP接口
const isHttpInterface = (interfaceName) => {
  if (!interfaceName) return false
  // 如果接口名包含 / 则认为是HTTP接口,否则是DUBBO接口
  return interfaceName.includes('/')
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

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

/* 接口列表样式 */
.interfaces-list {
  display: flex;
  flex-direction: column;
}

.interface-item {
  border-bottom: 1px solid #e5e7eb;
}

.interface-item:last-child {
  border-bottom: none;
}

.interface-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.interface-header:hover {
  background-color: #f9fafb;
}

.interface-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.expand-icon {
  transition: transform 0.2s ease;
  color: #717182;
  flex-shrink: 0;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

.interface-name {
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  font-weight: 500;
  color: #030213;
}

.type-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
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

.method-count {
  font-size: 12px;
  color: #717182;
}

.interface-time {
  font-size: 12px;
  color: #717182;
}

/* 方法列表样式 */
.methods-container {
  background: #fafafa;
  border-top: 1px solid #e5e7eb;
}

.methods-loading,
.methods-empty {
  padding: 24px;
  text-align: center;
  color: #717182;
  font-size: 13px;
}

.methods-table {
  padding: 12px 16px 12px 48px;
}

.methods-header {
  display: grid;
  grid-template-columns: 1.5fr 1.5fr 0.8fr 1.2fr 0.8fr 1.2fr;
  gap: 12px;
  padding: 8px 12px;
  background: #f3f3f5;
  border-radius: 6px;
  font-weight: 600;
  font-size: 12px;
  color: #030213;
  margin-bottom: 8px;
}

.method-row {
  display: grid;
  grid-template-columns: 1.5fr 1.5fr 0.8fr 1.2fr 0.8fr 1.2fr;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 12px;
  color: #717182;
  transition: background-color 0.2s ease;
  margin-bottom: 4px;
}

.method-row:hover {
  background-color: white;
}

.col {
  display: flex;
  align-items: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.method-name {
  background: #e3f2fd;
  color: #1976d2;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 11px;
}

.url-path {
  background: #fff3e0;
  color: #e65100;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 11px;
}

.http-type-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  background: #f3f3f5;
  color: #717182;
}

.http-type-badge.get {
  background: #d1fae5;
  color: #065f46;
}

.http-type-badge.post {
  background: #dbeafe;
  color: #1e40af;
}

.http-type-badge.put {
  background: #fef3c7;
  color: #92400e;
}

.http-type-badge.delete {
  background: #fee2e2;
  color: #991b1b;
}

.param-type {
  font-size: 11px;
  color: #717182;
  font-family: 'Monaco', 'Courier New', monospace;
}

.auth-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.auth-badge.required {
  background: #fee2e2;
  color: #991b1b;
}

.auth-badge.optional {
  background: #d1fae5;
  color: #065f46;
}

.text-muted {
  color: #d1d5db;
}
</style>

