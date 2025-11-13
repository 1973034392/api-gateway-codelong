<template>
  <div class="gateway-service-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h1>网关服务管理</h1>
        <p class="subtitle">管理 API 网关服务及其密钥配置</p>
      </div>
      <button class="btn btn-primary" @click="openDialog('create')">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        创建网关服务
      </button>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <div class="search-input-group">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
        <input
          v-model="searchForm.serviceName"
          type="text"
          placeholder="搜索网关服务..."
          @keyup.enter="handleSearch"
        />
      </div>
      <button class="btn btn-secondary" @click="handleSearch">搜索</button>
      <button class="btn btn-outline" @click="handleReset">重置</button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="tableData.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z"></path>
        </svg>
        <p>暂无网关服务数据</p>
      </div>
      <div v-else class="services-list">
        <div v-for="service in tableData" :key="service.id" class="service-card">
          <div class="service-header" @click="toggleService(service.id)">
            <div class="service-info">
              <button class="expand-btn" :class="{ expanded: expandedServices.has(service.id) }">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
              </button>
              <div class="service-details">
                <h3>{{ service.serverName }}</h3>
                <div class="service-meta">
                  <span class="badge">{{ service.safeKey }}</span>
                  <span class="meta-text">实例数: {{ getInstanceCount(service.id) }}</span>
                </div>
                <div class="secret-display">
                  <span class="secret-label">密钥:</span>
                  <code class="secret-value">
                    {{ visibleSecrets.has(service.id) ? service.safeSecret : maskSecret(service.safeSecret) }}
                  </code>
                  <button
                    class="toggle-secret-btn"
                    @click.stop="toggleSecretVisibility(service.id)"
                    :title="visibleSecrets.has(service.id) ? '隐藏' : '显示'"
                  >
                    <svg v-if="visibleSecrets.has(service.id)" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                      <line x1="1" y1="1" x2="23" y2="23"></line>
                    </svg>
                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
            <div class="service-actions">
              <button class="btn btn-sm btn-ghost" @click.stop="viewInstances(service)">查看实例</button>
              <button class="btn btn-sm btn-ghost" @click.stop="openDialog('edit', service)">编辑</button>
              <button class="btn btn-sm btn-danger" @click.stop="handleDelete(service.id)">删除</button>
            </div>
          </div>

          <!-- 实例列表 -->
          <div v-if="expandedServices.has(service.id)" class="service-instances">
            <div v-if="instancesMap[service.id] && instancesMap[service.id].length > 0" class="instances-table">
              <div class="table-header">
                <div class="col col-ip">IP 地址</div>
                <div class="col col-port">端口</div>
                <div class="col col-heartbeat">上次心跳</div>
                <div class="col col-start">启动时间</div>
                <div class="col col-status">状态</div>
              </div>
              <div v-for="instance in instancesMap[service.id]" :key="instance.id" class="table-row">
                <div class="col col-ip">{{ instance.serverAddress }}</div>
                <div class="col col-port">{{ instance.serverAddress?.split(':')[1] || '-' }}</div>
                <div class="col col-heartbeat">{{ instance.lastHeartbeatTime || '未知' }}</div>
                <div class="col col-start">{{ instance.startTime || '未知' }}</div>
                <div class="col col-status">
                  <span class="status-badge" :class="{ online: instance.status === 1 }">
                    {{ instance.status === 1 ? '在线' : '离线' }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="no-instances">
              <p>暂无实例数据</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="tableData.length > 0" class="pagination">
        <button
          class="btn btn-sm btn-outline"
          :disabled="pagination.pageNo === 1"
          @click="pagination.pageNo--; loadData()"
        >
          上一页
        </button>
        <span class="page-info">
          第 {{ pagination.pageNo }} 页 / 共 {{ Math.ceil(pagination.total / pagination.pageSize) }} 页
        </span>
        <button
          class="btn btn-sm btn-outline"
          :disabled="pagination.pageNo * pagination.pageSize >= pagination.total"
          @click="pagination.pageNo++; loadData()"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 创建/编辑服务对话框 -->
    <div v-if="dialogVisible" class="modal-overlay" @click.self="dialogVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h2>{{ dialogTitle }}</h2>
          <button class="close-btn" @click="dialogVisible = false">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>服务名称</label>
            <input
              v-model="formData.serverName"
              type="text"
              placeholder="请输入服务名称"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>唯一 Key</label>
            <input
              v-model="formData.safeKey"
              type="text"
              placeholder="请输入唯一 Key"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>安全密钥</label>
            <input
              v-model="formData.safeSecret"
              type="password"
              placeholder="请输入安全密钥"
              class="form-input"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="dialogVisible = false">取消</button>
          <button class="btn btn-primary" @click="handleSubmit">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {gatewayService} from '../../api/gateway'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const dialogTitle = ref('创建网关服务')
const expandedServices = ref(new Set())
const visibleSecrets = ref(new Set())
const instancesMap = ref({})

const searchForm = reactive({
  serviceName: ''
})

const formData = reactive({
  id: null,
  serverName: '',
  safeKey: '',
  safeSecret: ''
})

const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await gatewayService.page({
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      serverName: searchForm.serviceName
    })
    tableData.value = res.list || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNo = 1
  loadData()
}

const handleReset = () => {
  searchForm.serviceName = ''
  pagination.pageNo = 1
  loadData()
}

const openDialog = (mode, row = null) => {
  dialogMode.value = mode
  if (mode === 'create') {
    dialogTitle.value = '创建网关服务'
    formData.id = null
    formData.serverName = ''
    formData.safeKey = ''
    formData.safeSecret = ''
  } else {
    dialogTitle.value = '编辑网关服务'
    formData.id = row.id
    formData.serverName = row.serverName
    formData.safeKey = row.safeKey
    formData.safeSecret = row.safeSecret
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formData.serverName || !formData.safeKey || !formData.safeSecret) {
    ElMessage.error('请填写所有必填项')
    return
  }

  try {
    if (dialogMode.value === 'create') {
      await gatewayService.create({
        serverName: formData.serverName,
        safeKey: formData.safeKey,
        safeSecret: formData.safeSecret
      })
      ElMessage.success('创建成功')
    } else {
      await gatewayService.update({
        id: formData.id,
        serverName: formData.serverName,
        safeKey: formData.safeKey,
        safeSecret: formData.safeSecret
      })
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该网关服务吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await gatewayService.delete(id)
        ElMessage.success('删除成功')
        loadData()
      } catch (error) {
        ElMessage.error('删除失败: ' + error.message)
      }
    })
    .catch(() => {})
}

const toggleService = (serviceId) => {
  if (expandedServices.value.has(serviceId)) {
    expandedServices.value.delete(serviceId)
  } else {
    expandedServices.value.add(serviceId)
    loadInstances(serviceId)
  }
}

const viewInstances = async (service) => {
  if (!expandedServices.value.has(service.id)) {
    expandedServices.value.add(service.id)
    await loadInstances(service.id)
  }
}

const loadInstances = async (serverId) => {
  try {
    const res = await gatewayService.getInstances(serverId)
    instancesMap.value[serverId] = res || []
  } catch (error) {
    ElMessage.error('加载实例失败: ' + error.message)
  }
}

const getInstanceCount = (serviceId) => {
  return instancesMap.value[serviceId]?.length || 0
}

const toggleSecretVisibility = (serviceId) => {
  if (visibleSecrets.value.has(serviceId)) {
    visibleSecrets.value.delete(serviceId)
  } else {
    visibleSecrets.value.add(serviceId)
  }
}

const maskSecret = (secret) => {
  return '•'.repeat(Math.min(secret?.length || 0, 20))
}

// 初始加载
loadData()
</script>

<style scoped>
.gateway-service-list {
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

.search-form {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  background: white;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.search-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  background: #f3f3f5;
  border-radius: 6px;
  padding: 0 12px;
  color: #717182;
}

.search-input-group input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
  padding: 8px 0;
}

.search-input-group input::placeholder {
  color: #ececf0;
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

.services-list {
  display: flex;
  flex-direction: column;
}

.service-card {
  border-bottom: 1px solid #e5e7eb;
}

.service-card:last-child {
  border-bottom: none;
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.service-header:hover {
  background-color: #f9fafb;
}

.service-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.expand-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #717182;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 6px;
}

.expand-btn:hover {
  background-color: #e9ebef;
  color: #030213;
}

.expand-btn.expanded {
  transform: rotate(180deg);
}

.service-details h3 {
  font-size: 16px;
  font-weight: 600;
  color: #030213;
  margin: 0 0 4px 0;
}

.service-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  margin-bottom: 8px;
}

.badge {
  background: #e9ebef;
  color: #030213;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.meta-text {
  color: #717182;
}

.secret-display {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.secret-label {
  color: #717182;
}

.secret-value {
  background: #f3f3f5;
  color: #030213;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 12px;
}

.toggle-secret-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: none;
  background: transparent;
  color: #717182;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toggle-secret-btn:hover {
  color: #030213;
}

.service-actions {
  display: flex;
  gap: 8px;
}

.service-instances {
  background: #f9fafb;
  padding: 16px;
  border-top: 1px solid #e5e7eb;
}

.instances-table {
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
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
}

.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid #e5e7eb;
  font-size: 13px;
  color: #717182;
}

.table-row:first-child {
  border-top: none;
}

.col {
  display: flex;
  align-items: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: #f3f3f5;
  color: #717182;
}

.status-badge.online {
  background: #d1fae5;
  color: #065f46;
}

.no-instances {
  padding: 24px;
  text-align: center;
  color: #717182;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #e5e7eb;
}

.page-info {
  font-size: 14px;
  color: #717182;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 90%;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #030213;
  margin: 0;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #717182;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #e9ebef;
  color: #030213;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #030213;
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 14px;
  color: #030213;
  background: white;
  transition: all 0.2s ease;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #e5e7eb;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-secondary {
  background: #e9ebef;
  color: #030213;
}

.btn-secondary:hover {
  background: #d1d5db;
}

.btn-outline {
  border: 1px solid #e5e7eb;
  color: #030213;
  background: white;
}

.btn-outline:hover {
  background: #f9fafb;
}

.btn-outline:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-ghost {
  background: transparent;
  color: #3b82f6;
}

.btn-ghost:hover {
  background: #e9ebef;
}

.btn-danger {
  background: transparent;
  color: #d4183d;
}

.btn-danger:hover {
  background: #fee2e2;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
}
</style>

