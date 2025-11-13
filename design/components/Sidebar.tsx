import { Server, Globe, Code, Link2, Gauge } from 'lucide-react';

type Page = 'core-service' | 'gateway-service' | 'api-management' | 'service-binding' | 'traffic-control';

interface SidebarProps {
  currentPage: Page;
  onPageChange: (page: Page) => void;
}

export function Sidebar({ currentPage, onPageChange }: SidebarProps) {
  const menuItems = [
    { id: 'core-service' as Page, label: '核心服务管理', icon: Server },
    { id: 'gateway-service' as Page, label: '网关服务管理', icon: Globe },
    { id: 'api-management' as Page, label: '接口信息管理', icon: Code },
    { id: 'service-binding' as Page, label: '服务实例绑定', icon: Link2 },
    { id: 'traffic-control' as Page, label: '流量控制', icon: Gauge },
  ];

  return (
    <aside className="w-64 bg-white border-r border-gray-200">
      <div className="p-6">
        <h1 className="text-blue-600">服务管理系统</h1>
      </div>
      <nav className="px-3">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentPage === item.id;
          
          return (
            <button
              key={item.id}
              onClick={() => onPageChange(item.id)}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg mb-1 transition-colors ${
                isActive
                  ? 'bg-blue-50 text-blue-600'
                  : 'text-gray-700 hover:bg-gray-50'
              }`}
            >
              <Icon className="w-5 h-5" />
              <span>{item.label}</span>
            </button>
          );
        })}
      </nav>
    </aside>
  );
}
