<template>
  <div class="rate-limit-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h1>流量控制</h1>
        <p class="subtitle">配置各服务的限流策略，防止服务过载</p>
      </div>
      <button class="btn btn-primary" @click="openCreateDialog">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        创建限流配置
      </button>
    </div>

    <!-- 提示信息 -->
    <div class="alert alert-info">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="16" x2="12" y2="12"></line>
        <line x1="12" y1="8" x2="12.01" y2="8"></line>
      </svg>
      <div>
        <h4>提示</h4>
        <ul>
          <li>限流配置实时生效，请谨慎修改</li>
          <li>建议根据服务实际承载能力设置合理的限流阈值</li>
          <li>禁用限流可能导致服务过载，请确保服务有足够的处理能力</li>
        </ul>
      </div>
    </div>

    <!-- 限流配置表格 -->
    <div class="table-container">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="configs.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z"></path>
        </svg>
        <p>暂无限流配置</p>
      </div>
      <div v-else class="configs-table">
        <div class="table-header">
          <div class="col col-name">规则名称</div>
          <div class="col col-type">限流类型</div>
          <div class="col col-target">限流目标</div>
          <div class="col col-count">阈值(请求/秒)</div>
          <div class="col col-strategy">策略</div>
          <div class="col col-status">状态</div>
          <div class="col col-action">操作</div>
        </div>
        <div v-for="config in configs" :key="config.id" class="table-row">
          <div class="col col-name">{{ config.ruleName }}</div>
          <div class="col col-type">
            <span class="type-badge" :class="config.limitType.toLowerCase()">
              {{ getLimitTypeLabel(config.limitType) }}
            </span>
          </div>
          <div class="col col-target">{{ config.limitTarget }}</div>
          <div class="col col-count">
            <div v-if="editingId === config.id" class="edit-input-group">
              <input
                v-model.number="editingConfig.limitCount"
                type="number"
                class="edit-input"
              />
            </div>
            <span v-else>{{ config.limitCount }}</span>
          </div>
          <div class="col col-strategy">
            <span class="strategy-badge">{{ getStrategyLabel(config.strategy) }}</span>
          </div>
          <div class="col col-status">
            <button
              v-if="editingId !== config.id"
              class="status-toggle"
              :class="{ enabled: config.status === 1 }"
              @click="handleToggleEnabled(config.id, config.status)"
            >
              <span class="status-dot"></span>
              {{ config.status === 1 ? '已启用' : '已禁用' }}
            </button>
            <span v-else class="status-badge" :class="{ enabled: editingConfig.status === 1 }">
              {{ editingConfig.status === 1 ? '已启用' : '已禁用' }}
            </span>
          </div>
          <div class="col col-action">
            <div v-if="editingId === config.id" class="action-buttons">
              <button class="btn btn-sm btn-success" @click="handleSave">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                保存
              </button>
              <button class="btn btn-sm btn-outline" @click="handleCancel">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
                取消
              </button>
            </div>
            <div v-else class="action-buttons">
              <button class="btn btn-sm btn-ghost" @click="handleEdit(config)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
                编辑
              </button>
              <button class="btn btn-sm btn-danger" @click="handleDelete(config.id)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
                删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="configs.length > 0" class="pagination">
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

    <!-- 创建/编辑限流配置对话框 -->
    <div v-if="createDialogVisible" class="modal-overlay" @click.self="createDialogVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h2>创建限流配置</h2>
          <button class="close-btn" @click="createDialogVisible = false">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>规则名称</label>
            <input
              v-model="newConfig.ruleName"
              type="text"
              placeholder="请输入规则名称"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>限流类型</label>
            <select v-model="newConfig.limitType" class="form-select">
              <option value="">-- 请选择 --</option>
              <option value="GLOBAL">全局限流</option>
              <option value="SERVICE">服务级限流</option>
              <option value="INTERFACE">接口级限流</option>
              <option value="IP">IP级限流</option>
            </select>
          </div>
          <div class="form-group">
            <label>限流目标</label>
            <input
              v-model="newConfig.limitTarget"
              type="text"
              placeholder="请输入限流目标（服务名/接口URL/IP地址等）"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>阈值（请求/秒）</label>
            <input
              v-model.number="newConfig.limitCount"
              type="number"
              placeholder="请输入阈值"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>时间窗口（秒）</label>
            <input
              v-model.number="newConfig.timeWindow"
              type="number"
              placeholder="请输入时间窗口"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>限流策略</label>
            <select v-model="newConfig.strategy" class="form-select">
              <option value="TOKEN_BUCKET">令牌桶</option>
              <option value="SLIDING_WINDOW">滑动窗口</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="createDialogVisible = false">取消</button>
          <button class="btn btn-primary" @click="handleCreate">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {rateLimitConfig} from '../../api/gateway'

const loading = ref(false)
const editingId = ref(null)
const editingConfig = ref(null)
const createDialogVisible = ref(false)

const configs = ref([])
const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

const newConfig = reactive({
  ruleName: '',
  limitType: '',
  limitTarget: '',
  limitCount: 100,
  timeWindow: 1,
  strategy: 'TOKEN_BUCKET',
  status: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await rateLimitConfig.list({
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize
    })
    configs.value = res.list || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  newConfig.ruleName = ''
  newConfig.limitType = ''
  newConfig.limitTarget = ''
  newConfig.limitCount = 100
  newConfig.timeWindow = 1
  newConfig.strategy = 'TOKEN_BUCKET'
  newConfig.status = 1
  createDialogVisible.value = true
}

const handleCreate = async () => {
  if (!newConfig.ruleName || !newConfig.limitType || !newConfig.limitTarget) {
    ElMessage.error('请填写所有必填项')
    return
  }

  try {
    await rateLimitConfig.create({
      ruleName: newConfig.ruleName,
      limitType: newConfig.limitType,
      limitTarget: newConfig.limitTarget,
      limitCount: newConfig.limitCount,
      timeWindow: newConfig.timeWindow,
      strategy: newConfig.strategy,
      status: newConfig.status
    })
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('创建失败: ' + error.message)
  }
}

const handleEdit = (config) => {
  editingId.value = config.id
  editingConfig.value = { ...config }
}

const handleSave = async () => {
  if (editingConfig.value) {
    try {
      await rateLimitConfig.update({
        id: editingConfig.value.id,
        ruleName: editingConfig.value.ruleName,
        limitType: editingConfig.value.limitType,
        limitTarget: editingConfig.value.limitTarget,
        limitCount: editingConfig.value.limitCount,
        timeWindow: editingConfig.value.timeWindow,
        strategy: editingConfig.value.strategy,
        status: editingConfig.value.status
      })
      ElMessage.success('保存成功')
      editingId.value = null
      editingConfig.value = null
      loadData()
    } catch (error) {
      ElMessage.error('保存失败: ' + error.message)
    }
  }
}

const handleCancel = () => {
  editingId.value = null
  editingConfig.value = null
}

const handleToggleEnabled = async (id, currentStatus) => {
  const newStatus = currentStatus === 1 ? 0 : 1
  try {
    await rateLimitConfig.updateStatus(id, newStatus)
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    loadData()
  } catch (error) {
    ElMessage.error('更新状态失败: ' + error.message)
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除此限流配置吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await rateLimitConfig.delete(id)
        ElMessage.success('删除成功')
        loadData()
      } catch (error) {
        ElMessage.error('删除失败: ' + error.message)
      }
    })
    .catch(() => {})
}

const getLimitTypeLabel = (type) => {
  const typeMap = {
    'GLOBAL': '全局',
    'SERVICE': '服务级',
    'INTERFACE': '接口级',
    'IP': 'IP级'
  }
  return typeMap[type] || type
}

const getStrategyLabel = (strategy) => {
  const strategyMap = {
    'TOKEN_BUCKET': '令牌桶',
    'SLIDING_WINDOW': '滑动窗口'
  }
  return strategyMap[strategy] || strategy
}

// 初始加载
loadData()
</script>

<style scoped>
.rate-limit-list {
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

.alert {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
  border: 1px solid;
}

.alert-info {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #1e40af;
}

.alert svg {
  flex-shrink: 0;
  margin-top: 2px;
}

.alert h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: inherit;
}

.alert ul {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
}

.alert li {
  margin-bottom: 4px;
}

.alert li:last-child {
  margin-bottom: 0;
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

.configs-table {
  display: flex;
  flex-direction: column;
  overflow-x: auto;
}

.table-header {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr 1fr 1fr;
  gap: 12px;
  padding: 12px 16px;
  background: #f3f3f5;
  font-weight: 600;
  font-size: 13px;
  color: #030213;
  border-bottom: 1px solid #e5e7eb;
  min-width: 1400px;
}

.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr 1fr 1fr;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 13px;
  color: #717182;
  transition: background-color 0.2s ease;
  align-items: center;
  min-width: 1400px;
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

.type-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: #f3f3f5;
  color: #717182;
}

.type-badge.global {
  background: #dbeafe;
  color: #1e40af;
}

.type-badge.service {
  background: #fce7f3;
  color: #be185d;
}

.type-badge.interface {
  background: #dcfce7;
  color: #166534;
}

.type-badge.ip {
  background: #fed7aa;
  color: #92400e;
}

.strategy-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: #e9ebef;
  color: #030213;
}

.edit-input-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.edit-input {
  width: 80px;
  padding: 4px 8px;
  border: 1px solid #3b82f6;
  border-radius: 4px;
  font-size: 12px;
  color: #030213;
  background: white;
}

.edit-input:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.status-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  background: white;
  color: #717182;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.status-toggle:hover {
  background: #f9fafb;
}

.status-toggle.enabled {
  border-color: #d1fae5;
  background: #d1fae5;
  color: #065f46;
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
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

.status-badge.enabled {
  background: #d1fae5;
  color: #065f46;
}

.action-buttons {
  display: flex;
  gap: 6px;
}

.col-action {
  justify-content: flex-start;
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
  max-height: 90vh;
  overflow-y: auto;
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

.form-input,
.form-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 14px;
  color: #030213;
  background: white;
  transition: all 0.2s ease;
}

.form-input:focus,
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

.btn-outline:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover {
  background: #059669;
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

