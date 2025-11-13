<template>
  <div class="service-binding-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h1>服务实例绑定</h1>
        <p class="subtitle">管理核心服务与网关服务的绑定关系</p>
      </div>
      <button class="btn btn-primary" @click="openDialog('create')">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        创建绑定
      </button>
    </div>

    <!-- 绑定列表 -->
    <div class="table-container">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="bindings.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z"></path>
        </svg>
        <p>暂无绑定关系</p>
      </div>
      <div v-else>
        <!-- 统计信息 -->
        <div class="stats-card">
          <div class="stat-item">
            <span class="stat-label">总绑定数</span>
            <span class="stat-value">{{ bindings.length }}</span>
          </div>
        </div>

        <!-- 绑定表格 -->
        <div class="bindings-table">
          <div class="table-header">
            <div class="col col-core">核心服务</div>
            <div class="col col-core-key">核心服务 Key</div>
            <div class="col col-gateway">网关服务</div>
            <div class="col col-gateway-key">网关服务 Key</div>
            <div class="col col-time">绑定时间</div>
            <div class="col col-action">操作</div>
          </div>
          <div v-for="binding in bindings" :key="binding.id" class="table-row">
            <div class="col col-core">{{ binding.coreServiceName }}</div>
            <div class="col col-core-key">
              <span class="key-badge">{{ binding.coreServiceKey }}</span>
            </div>
            <div class="col col-gateway">{{ binding.gatewayServiceName }}</div>
            <div class="col col-gateway-key">
              <span class="key-badge">{{ binding.gatewayServiceKey }}</span>
            </div>
            <div class="col col-time">{{ binding.bindTime }}</div>
            <div class="col col-action">
              <button class="btn btn-sm btn-danger" @click="handleUnbind(binding.id)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M6 18L18 6M6 6l12 12"></path>
                </svg>
                解除
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建绑定对话框 -->
    <div v-if="dialogVisible" class="modal-overlay" @click.self="dialogVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h2>创建服务绑定</h2>
          <button class="close-btn" @click="dialogVisible = false">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>核心服务</label>
            <select v-model="newBinding.coreServiceKey" class="form-select">
              <option value="">-- 请选择核心服务 --</option>
              <option v-for="service in coreServices" :key="service.key" :value="service.key">
                {{ service.name }} ({{ service.key }})
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>网关服务</label>
            <select v-model="newBinding.gatewayServiceKey" class="form-select">
              <option value="">-- 请选择网关服务 --</option>
              <option v-for="service in gatewayServices" :key="service.key" :value="service.key">
                {{ service.name }} ({{ service.key }})
              </option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="dialogVisible = false">取消</button>
          <button class="btn btn-primary" @click="handleCreateBinding">创建绑定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {coreService, gatewayService, serviceBinding} from '../../api/gateway'

const loading = ref(false)
const dialogVisible = ref(false)

const coreServices = ref([])
const gatewayServices = ref([])
const bindings = ref([])

const newBinding = reactive({
  coreServiceKey: '',
  gatewayServiceKey: '',
})

const loadData = async () => {
  loading.value = true
  try {
    // 加载核心服务列表
    const coreRes = await coreService.list()
    coreServices.value = (coreRes || []).map(s => ({
      key: s.safeKey,
      name: s.serverName,
      id: s.id
    }))

    // 加载网关服务列表
    const gatewayRes = await gatewayService.list()
    gatewayServices.value = (gatewayRes || []).map(s => ({
      key: s.safeKey,
      name: s.serverName,
      id: s.id
    }))

    // 加载绑定关系列表
    const bindRes = await serviceBinding.page({
      pageNum: 1,
      pageSize: 100
    })
    bindings.value = bindRes.list || []
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const openDialog = (mode) => {
  newBinding.coreServiceKey = ''
  newBinding.gatewayServiceKey = ''
  dialogVisible.value = true
}

const handleCreateBinding = async () => {
  if (!newBinding.coreServiceKey || !newBinding.gatewayServiceKey) {
    ElMessage.error('请选择核心服务和网关服务')
    return
  }

  try {
    await serviceBinding.create({
      coreServiceKey: newBinding.coreServiceKey,
      gatewayServiceKey: newBinding.gatewayServiceKey
    })
    ElMessage.success('绑定创建成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('创建绑定失败: ' + error.message)
  }
}

const handleUnbind = (bindingId) => {
  ElMessageBox.confirm('确定要解除此绑定吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await serviceBinding.delete(bindingId)
        ElMessage.success('绑定已解除')
        loadData()
      } catch (error) {
        ElMessage.error('解除绑定失败: ' + error.message)
      }
    })
    .catch(() => {})
}

// 初始加载
loadData()
</script>

<style scoped>
.service-binding-list {
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

.stats-card {
  display: flex;
  gap: 24px;
  padding: 16px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #717182;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #030213;
}

.bindings-table {
  display: flex;
  flex-direction: column;
}

.table-header {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr 1fr;
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
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr 1fr;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 13px;
  color: #717182;
  transition: background-color 0.2s ease;
  align-items: center;
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

.key-badge {
  background: #e9ebef;
  color: #030213;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
  font-size: 12px;
}

.col-action {
  justify-content: flex-start;
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

.form-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 14px;
  color: #030213;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
}

.form-select:focus {
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

.btn-outline {
  border: 1px solid #e5e7eb;
  color: #030213;
  background: white;
}

.btn-outline:hover {
  background: #f9fafb;
}

.btn-danger {
  background: transparent;
  color: #d4183d;
  font-size: 13px;
  padding: 6px 12px;
}

.btn-danger:hover {
  background: #fee2e2;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
}
</style>

