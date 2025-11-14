import api from './index'

// 网关实例组相关API
export const gatewayGroup = {
  // 创建网关实例组
  create: (data) => api.post('/gateway-group/create', data),

  // 更新网关实例组
  update: (data) => api.put('/gateway-group/update', data),

  // 删除网关实例组
  delete: (id) => api.delete('/gateway-group/delete', { params: { id } }),

  // 查询网关实例组
  get: (id) => api.get('/gateway-group/get', { params: { id } }),

  // 分页查询网关实例组
  page: (params) => api.get('/gateway-group/page', { params }),

  // 获取所有网关实例组列表
  list: () => api.get('/gateway-group/list')
}

// 网关服务相关API
export const gatewayServer = {
  // 创建网关服务
  create: (data) => api.post('/gateway-server/create', data),

  // 更新网关服务
  update: (data) => api.put('/gateway-server/update', data),

  // 删除网关服务
  delete: (id) => api.delete('/gateway-server/delete', { params: { id } }),

  // 更新网关服务状态
  updateStatus: (id) => api.put('/gateway-server/update/status', null, { params: { id } }),

  // 查询网关服务
  get: (id) => api.get('/gateway-server/get', { params: { id } }),

  // 分页查询网关服务
  page: (params) => api.get('/gateway-server/page', { params }),

  // 获取所有网关服务列表
  list: () => api.get('/gateway-server/list')
}

// 限流配置相关API
export const rateLimitConfig = {
  // 创建限流配置
  create: (data) => api.post('/gateway-rate-limit/create', data),

  // 更新限流配置
  update: (data) => api.put('/gateway-rate-limit/update', data),

  // 删除限流配置
  delete: (id) => api.delete(`/gateway-rate-limit/delete/${id}`),

  // 更新限流配置状态
  updateStatus: (id, status) => api.put(`/gateway-rate-limit/status/${id}/${status}`),

  // 查询限流配置详情
  get: (id) => api.get(`/gateway-rate-limit/detail/${id}`),

  // 分页查询限流配置列表
  list: (params) => api.get('/gateway-rate-limit/list', { params }),

  // 刷新所有网关节点的限流配置
  refresh: () => api.post('/gateway-rate-limit/refresh')
}

// 接口管理相关API
export const gatewayInterface = {
  // 创建接口和方法
  create: (data) => api.post('/gateway-interface/create', data),

  // 更新接口
  update: (data) => api.put('/gateway-interface/update', data),

  // 删除接口
  delete: (id) => api.delete(`/gateway-interface/delete/${id}`),

  // 分页查询接口列表
  page: (params) => api.get('/gateway-interface/page', { params }),

  // 获取所有接口列表
  list: () => api.get('/gateway-interface/list'),

  // 获取接口的方法列表
  getMethods: (interfaceId) => api.get(`/gateway-method/list/${interfaceId}`),

  // 创建方法
  createMethod: (data) => api.post('/gateway-method/create', data),

  // 更新方法
  updateMethod: (data) => api.put('/gateway-method/update', data),

  // 删除方法
  deleteMethod: (id) => api.delete(`/gateway-method/delete/${id}`)
}

// 核心服务相关API
export const coreService = {
  // 创建核心服务
  create: (data) => api.post('/gateway-server/create', data),

  // 更新核心服务
  update: (data) => api.put('/gateway-server/update', data),

  // 删除核心服务
  delete: (id) => api.delete('/gateway-server/delete', { params: { id } }),

  // 分页查询核心服务列表
  page: (params) => api.get('/gateway-server/page', { params }),

  // 获取所有核心服务列表
  list: () => api.get('/gateway-server/list'),

  // 获取核心服务的实例列表
  getInstances: (serverId) => api.get(`/gateway-server-detail/list/${serverId}`),

  // 创建核心服务实例
  createInstance: (data) => api.post('/gateway-server-detail/create', data),

  // 更新核心服务实例
  updateInstance: (data) => api.put('/gateway-server-detail/update', data),

  // 删除核心服务实例
  deleteInstance: (id) => api.delete(`/gateway-server-detail/delete/${id}`)
}

// 网关服务相关API
export const gatewayService = {
  // 创建网关服务
  create: (data) => api.post('/gateway-group/create', data),

  // 更新网关服务
  update: (data) => api.put('/gateway-group/update', data),

  // 删除网关服务
  delete: (id) => api.delete('/gateway-group/delete', { params: { id } }),

  // 分页查询网关服务列表
  page: (params) => api.get('/gateway-group/page', { params }),

  // 获取所有网关服务列表
  list: () => api.get('/gateway-group/list'),

  // 获取网关服务的实例列表
  getInstances: (groupId) => api.get(`/gateway-group-detail/list/${groupId}`),

  // 创建网关服务实例
  createInstance: (data) => api.post('/gateway-group-detail/create', data),

  // 更新网关服务实例
  updateInstance: (data) => api.put('/gateway-group-detail/update', data),

  // 删除网关服务实例
  deleteInstance: (id) => api.delete(`/gateway-group-detail/delete/${id}`)
}

// 服务绑定相关API
export const serviceBinding = {
  // 创建服务绑定
  create: (data) => api.post('/gateway-server-group-rel/create', data),

  // 删除服务绑定
  delete: (id) => api.delete('/gateway-server-group-rel/delete', { params: { id } }),

  // 分页查询绑定列表
  page: (params) => api.get('/gateway-server-group-rel/page', { params })
}

