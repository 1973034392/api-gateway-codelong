import { useState } from 'react';
import { Plus, ChevronDown, ChevronRight } from 'lucide-react';
import { Button } from './ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from './ui/dialog';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';

interface ServiceInstance {
  id: string;
  ip: string;
  port: string;
  lastHeartbeat: string;
  startTime: string;
  remark: string;
}

interface Service {
  id: string;
  key: string;
  name: string;
  instances: ServiceInstance[];
}

export function CoreServiceManagement() {
  const [services, setServices] = useState<Service[]>([
    {
      id: '1',
      key: 'user-service',
      name: '用户服务',
      instances: [
        {
          id: '1-1',
          ip: '192.168.1.10',
          port: '8080',
          lastHeartbeat: '2025-11-13 10:30:25',
          startTime: '2025-11-13 08:00:00',
          remark: '主节点',
        },
        {
          id: '1-2',
          ip: '192.168.1.11',
          port: '8080',
          lastHeartbeat: '2025-11-13 10:30:23',
          startTime: '2025-11-13 08:05:00',
          remark: '备用节点',
        },
      ],
    },
    {
      id: '2',
      key: 'order-service',
      name: '订单服务',
      instances: [
        {
          id: '2-1',
          ip: '192.168.1.20',
          port: '8081',
          lastHeartbeat: '2025-11-13 10:30:20',
          startTime: '2025-11-13 08:10:00',
          remark: '生产环境',
        },
      ],
    },
  ]);

  const [expandedServices, setExpandedServices] = useState<Set<string>>(new Set(['1']));
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [newService, setNewService] = useState({ key: '', name: '' });

  const toggleService = (serviceId: string) => {
    const newExpanded = new Set(expandedServices);
    if (newExpanded.has(serviceId)) {
      newExpanded.delete(serviceId);
    } else {
      newExpanded.add(serviceId);
    }
    setExpandedServices(newExpanded);
  };

  const handleCreateService = () => {
    if (newService.key && newService.name) {
      const service: Service = {
        id: Date.now().toString(),
        key: newService.key,
        name: newService.name,
        instances: [],
      };
      setServices([...services, service]);
      setNewService({ key: '', name: '' });
      setIsCreateDialogOpen(false);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2>核心服务管理</h2>
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="w-4 h-4 mr-2" />
              创建服务组
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>创建新服务组</DialogTitle>
            </DialogHeader>
            <div className="space-y-4 mt-4">
              <div>
                <Label htmlFor="service-key">服务唯一 Key</Label>
                <Input
                  id="service-key"
                  value={newService.key}
                  onChange={(e) => setNewService({ ...newService, key: e.target.value })}
                  placeholder="例如: user-service"
                />
              </div>
              <div>
                <Label htmlFor="service-name">服务名称</Label>
                <Input
                  id="service-name"
                  value={newService.name}
                  onChange={(e) => setNewService({ ...newService, name: e.target.value })}
                  placeholder="例如: 用户服务"
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
