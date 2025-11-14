import {createRouter, createWebHistory} from 'vue-router'
import Layout from '../layouts/Layout.vue'
import RateLimitList from '../views/gateway/RateLimitList.vue'
import CoreServiceList from '../views/gateway/CoreServiceList.vue'
import GatewayServiceList from '../views/gateway/GatewayServiceList.vue'
import InterfaceInfoList from '../views/gateway/InterfaceInfoList.vue'
import ServiceBindingList from '../views/gateway/ServiceBindingList.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/core-service',
    children: [
      {
        path: 'core-service',
        component: CoreServiceList,
        meta: { title: '下游服务管理' }
      },
      {
        path: 'gateway-service',
        component: GatewayServiceList,
        meta: { title: '网关实例管理' }
      },
      {
        path: 'interface-info',
        component: InterfaceInfoList,
        meta: { title: '接口信息管理' }
      },
      {
        path: 'service-binding',
        component: ServiceBindingList,
        meta: { title: '服务实例绑定' }
      },
      {
        path: 'rate-limit',
        component: RateLimitList,
        meta: { title: '流量控制' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

