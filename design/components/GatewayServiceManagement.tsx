import { useState } from 'react';
import { Plus, ChevronDown, ChevronRight, Eye, EyeOff } from 'lucide-react';
import { Button } from './ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from './ui/dialog';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';

interface GatewayInstance {
  id: string;
  ip: string;
  port: string;
  lastHeartbeat: string;
  startTime: string;
  remark: string;
}

interface GatewayService {
  id: string;
  key: string;
  name: string;
  secret: string;
  instances: GatewayInstance[];
}

export function GatewayServiceManagement() {
  const [services, setServices] = useState<GatewayService[]>([
    {
      id: '1',
      key: 'api-gateway-01',
      name: 'API网关-主',
      secret: 'sk_live_a1b2c3d4e5f6g7h8',
      instances: [
        {
          id: '1-1',
          ip: '192.168.2.10',
          port: '80',
          lastHeartbeat: '2025-11-13 10:30:28',
          startTime: '2025-11-13 08:00:00',
          remark: '主网关',
        },
      ],
    },
    {
      id: '2',
      key: 'api-gateway-02',
      name: 'API网关-备',
      secret: 'sk_live_x9y8z7w6v5u4t3s2',
      instances: [
        {
          id: '2-1',
          ip: '192.168.2.20',
          port: '80',
          lastHeartbeat: '2025-11-13 10:30:26',
          startTime: '2025-11-13 08:02:00',
          remark: '备用网关',
        },
      ],
    },
  ]);

  const [expandedServices, setExpandedServices] = useState<Set<string>>(new Set(['1']));
  const [visibleSecrets, setVisibleSecrets] = useState<Set<string>>(new Set());
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [newService, setNewService] = useState({ key: '', name: '', secret: '' });

  const toggleService = (serviceId: string) => {
    const newExpanded = new Set(expandedServices);
    if (newExpanded.has(serviceId)) {
      newExpanded.delete(serviceId);
    } else {
      newExpanded.add(serviceId);
    }
    setExpandedServices(newExpanded);
  };

  const toggleSecretVisibility = (serviceId: string) => {
    const newVisible = new Set(visibleSecrets);
    if (newVisible.has(serviceId)) {
      newVisible.delete(serviceId);
    } else {
      newVisible.add(serviceId);
    }
    setVisibleSecrets(newVisible);
  };

  const handleCreateService = () => {
    if (newService.key && newService.name && newService.secret) {
      const service: GatewayService = {
        id: Date.now().toString(),
        key: newService.key,
        name: newService.name,
        secret: newService.secret,
        instances: [],
      };
      setServices([...services, service]);
      setNewService({ key: '', name: '', secret: '' });
      setIsCreateDialogOpen(false);
    }
  };

  const maskSecret = (secret: string) => {
    return '•'.repeat(secret.length);
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2>网关服务管理</h2>
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="w-4 h-4 mr-2" />
              创建网关服务组
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>创建新网关服务组</DialogTitle>
            </DialogHeader>
            <div className="space-y-4 mt-4">
              <div>
                <Label htmlFor="gateway-key">服务唯一 Key</Label>
                <Input
                  id="gateway-key"
                  value={newService.key}
                  onChange={(e) => setNewService({ ...newService, key: e.target.value })}
                  placeholder="例如: api-gateway-03"
                />
              </div>
              <div>
                <Label htmlFor="gateway-name">服务名称</Label>
                <Input
                  id="gateway-name"
                  value={newService.name}
                  onChange={(e) => setNewService({ ...newService, name: e.target.value })}
                  placeholder="例如: API网关-测试"
                />
              </div>
              <div>
                <Label htmlFor="gateway-secret">密钥</Label>
                <Input
                  id="gateway-secret"
                  type="password"
                  value={newService.secret}
                  onChange={(e) => setNewService({ ...newService, secret: e.target.value })}
                  placeholder="请输入密钥"
                />
              </div>
              <Button onClick={handleCreateService} className="w-full">
                创建
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <div className="bg-white rounded-lg shadow">
        {services.map((service) => (
          <div key={service.id} className="border-b last:border-b-0">
            <div
              className="flex items-center gap-3 p-4 cursor-pointer hover:bg-gray-50"
              onClick={() => toggleService(service.id)}
            >
              {expandedServices.has(service.id) ? (
                <ChevronDown className="w-5 h-5 text-gray-500" />
              ) : (
                <ChevronRight className="w-5 h-5 text-gray-500" />
              )}
              <div className="flex-1">
                <div className="flex items-center gap-3">
                  <span className="text-gray-900">{service.name}</span>
                  <span className="text-gray-500 px-2 py-1 bg-gray-100 rounded">
                    {service.key}
                  </span>
                </div>
                <div className="flex items-center gap-2 mt-2">
                  <span className="text-gray-500">密钥:</span>
                  <code className="text-gray-700 bg-gray-100 px-2 py-1 rounded">
                    {visibleSecrets.has(service.id) ? service.secret : maskSecret(service.secret)}
                  </code>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      toggleSecretVisibility(service.id);
                    }}
                    className="text-gray-500 hover:text-gray-700"
                  >
                    {visibleSecrets.has(service.id) ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
                <div className="text-gray-500 mt-1">
                  实例数: {service.instances.length}
                </div>
              </div>
            </div>

            {expandedServices.has(service.id) && service.instances.length > 0 && (
              <div className="px-4 pb-4">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>IP 地址</TableHead>
                      <TableHead>端口</TableHead>
                      <TableHead>上次心跳时间</TableHead>
                      <TableHead>服务启动时间</TableHead>
                      <TableHead>备注</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {service.instances.map((instance) => (
                      <TableRow key={instance.id}>
                        <TableCell>{instance.ip}</TableCell>
                        <TableCell>{instance.port}</TableCell>
                        <TableCell>{instance.lastHeartbeat}</TableCell>
                        <TableCell>{instance.startTime}</TableCell>
                        <TableCell>{instance.remark}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
