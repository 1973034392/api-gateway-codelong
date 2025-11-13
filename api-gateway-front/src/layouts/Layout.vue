<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="logo-container">
        <div class="logo">
          <div class="logo-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z"></path>
            </svg>
          </div>
          <span v-if="!sidebarCollapsed" class="logo-text">API 网关</span>
        </div>
      </div>
      <nav class="menu">
        <button
          v-for="item in menuItems"
          :key="item.id"
          class="menu-item"
          :class="{ active: activeMenu === item.id }"
          @click="navigateTo(item.id)"
          :title="item.label"
        >
          <span class="menu-icon" v-html="item.icon"></span>
          <span v-if="!sidebarCollapsed" class="menu-label">{{ item.label }}</span>
        </button>
      </nav>
    </aside>

    <!-- 主容器 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="header-left">
          <button class="toggle-btn" @click="toggleSidebar" :title="sidebarCollapsed ? '展开' : '收起'">
            <svg v-if="sidebarCollapsed" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
          </button>
        </div>
        <div class="header-right">
        </div>
      </header>

      <!-- 主内容区 -->
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useAppStore} from '../stores'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = ref('core-service')

const menuItems = [
  {
    id: 'core-service',
    label: '核心服务管理',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="2" width="20" height="20" rx="2.18" ry="2.18"></rect><line x1="7" y1="2" x2="7" y2="22"></line><line x1="17" y1="2" x2="17" y2="22"></line><line x1="2" y1="7" x2="22" y2="7"></line><line x1="2" y1="17" x2="22" y2="17"></line></svg>'
  },
  {
    id: 'gateway-service',
    label: '网关服务管理',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="1"></circle><path d="M12 1v6m0 6v6"></path><path d="M4.22 4.22l4.24 4.24m5.08 5.08l4.24 4.24"></path><path d="M1 12h6m6 0h6"></path><path d="M4.22 19.78l4.24-4.24m5.08-5.08l4.24-4.24"></path></svg>'
  },
  {
    id: 'interface-info',
    label: '接口信息管理',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 18 22 12 16 6"></polyline><polyline points="8 6 2 12 8 18"></polyline></svg>'
  },
  {
    id: 'service-binding',
    label: '服务实例绑定',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path></svg>'
  },
  {
    id: 'rate-limit',
    label: '流量控制',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2v20m10-10H2"></path><circle cx="12" cy="12" r="10"></circle></svg>'
  },
]

// 监听路由变化，更新菜单高亮
watch(() => route.path, (newPath) => {
  const pathMap = {
    '/core-service': 'core-service',
    '/gateway-service': 'gateway-service',
    '/interface-info': 'interface-info',
    '/service-binding': 'service-binding',
    '/rate-limit': 'rate-limit',
  }
  activeMenu.value = pathMap[newPath] || 'core-service'
}, { immediate: true })

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const navigateTo = (menuId) => {
  const pathMap = {
    'core-service': '/core-service',
    'gateway-service': '/gateway-service',
    'interface-info': '/interface-info',
    'service-binding': '/service-binding',
    'rate-limit': '/rate-limit',
  }
  router.push(pathMap[menuId])
}
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  background-color: #f9fafb;
}

.sidebar {
  width: 256px;
  background-color: #ffffff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 80px;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 16px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #030213 0%, #3b82f6 100%);
  border-radius: 8px;
  color: white;
  flex-shrink: 0;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #030213;
  white-space: nowrap;
}

.menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 8px;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #717182;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.menu-item:hover {
  background-color: #f3f3f5;
  color: #030213;
}

.menu-item.active {
  background-color: #e9ebef;
  color: #030213;
}

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.menu-label {
  flex: 1;
  text-align: left;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 64px;
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
}

.toggle-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  padding: 0;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: transparent;
  color: #717182;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0;
}

.toggle-btn:hover {
  background-color: #f3f3f5;
  color: #030213;
}

.toggle-btn svg {
  width: 20px;
  height: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* 滚动条样式 */
.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-track {
  background: transparent;
}

.main-content::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>

