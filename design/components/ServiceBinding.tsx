import { useState } from 'react';
import { Plus, Unlink } from 'lucide-react';
import { Button } from './ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from './ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Label } from './ui/label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Badge } from './ui/badge';

interface Binding {
  id: string;
  coreServiceKey: string;
  coreServiceName: string;
  gatewayServiceKey: string;
  gatewayServiceName: string;
  bindTime: string;
}

export function ServiceBinding() {
  const coreServices = [
    { key: 'user-service', name: '用户服务' },
    { key: 'order-service', name: '订单服务' },
  ];

  const gatewayServices = [
    { key: 'api-gateway-01', name: 'API网关-主' },
    { key: 'api-gateway-02', name: 'API网关-备' },
  ];

  const [bindings, setBindings] = useState<Binding[]>([
    {
      id: '1',
      coreServiceKey: 'user-service',
      coreServiceName: '用户服务',
      gatewayServiceKey: 'api-gateway-01',
      gatewayServiceName: 'API网关-主',
      bindTime: '2025-11-13 08:00:00',
    },
    {
      id: '2',
      coreServiceKey: 'order-service',
      coreServiceName: '订单服务',
      gatewayServiceKey: 'api-gateway-01',
      gatewayServiceName: 'API网关-主',
      bindTime: '2025-11-13 08:05:00',
    },
  ]);

  const [isBindDialogOpen, setIsBindDialogOpen] = useState(false);
  const [newBinding, setNewBinding] = useState({
    coreServiceKey: '',
    gatewayServiceKey: '',
  });

  const handleCreateBinding = () => {
    if (newBinding.coreServiceKey && newBinding.gatewayServiceKey) {
      const coreService = coreServices.find((s) => s.key === newBinding.coreServiceKey);
      const gatewayService = gatewayServices.find((s) => s.key === newBinding.gatewayServiceKey);

      if (coreService && gatewayService) {
        const binding: Binding = {
          id: Date.now().toString(),
          coreServiceKey: coreService.key,
          coreServiceName: coreService.name,
          gatewayServiceKey: gatewayService.key,
          gatewayServiceName: gatewayService.name,
          bindTime: new Date().toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false,
          }).replace(/\//g, '-'),
        };
        setBindings([...bindings, binding]);
        setNewBinding({ coreServiceKey: '', gatewayServiceKey: '' });
        setIsBindDialogOpen(false);
      }
    }
  };

  const handleUnbind = (bindingId: string) => {
    setBindings(bindings.filter((b) => b.id !== bindingId));
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2>服务实例绑定</h2>
        <Dialog open={isBindDialogOpen} onOpenChange={setIsBindDialogOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="w-4 h-4 mr-2" />
              创建绑定
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>创建服务绑定</DialogTitle>
            </DialogHeader>
            <div className="space-y-4 mt-4">
              <div>
                <Label htmlFor="core-service">核心服务</Label>
                <Select
                  value={newBinding.coreServiceKey}
                  onValueChange={(value) =>
                    setNewBinding({ ...newBinding, coreServiceKey: value })
                  }
                >
                  <SelectTrigger id="core-service">
                    <SelectValue placeholder="选择核心服务" />
                  </SelectTrigger>
                  <SelectContent>
                    {coreServices.map((service) => (
                      <SelectItem key={service.key} value={service.key}>
                        {service.name} ({service.key})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="gateway-service">网关服务</Label>
                <Select
                  value={newBinding.gatewayServiceKey}
                  onValueChange={(value) =>
                    setNewBinding({ ...newBinding, gatewayServiceKey: value })
                  }
                >
                  <SelectTrigger id="gateway-service">
                    <SelectValue placeholder="选择网关服务" />
                  </SelectTrigger>
                  <SelectContent>
                    {gatewayServices.map((service) => (
                      <SelectItem key={service.key} value={service.key}>
                        {service.name} ({service.key})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <Button onClick={handleCreateBinding} className="w-full">
                创建绑定
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <div className="bg-white rounded-lg shadow">
        <div className="p-4 border-b">
          <h3 className="text-gray-900">绑定关系列表</h3>
          <p className="text-gray-500 mt-1">
            共 {bindings.length} 个绑定关系
          </p>
        </div>

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>核心服务</TableHead>
              <TableHead>核心服务 Key</TableHead>
              <TableHead>网关服务</TableHead>
              <TableHead>网关服务 Key</TableHead>
              <TableHead>绑定时间</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {bindings.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center text-gray-500 py-8">
                  暂无绑定关系
                </TableCell>
              </TableRow>
            ) : (
              bindings.map((binding) => (
                <TableRow key={binding.id}>
                  <TableCell>{binding.coreServiceName}</TableCell>
                  <TableCell>
                    <Badge variant="outline">{binding.coreServiceKey}</Badge>
                  </TableCell>
                  <TableCell>{binding.gatewayServiceName}</TableCell>
                  <TableCell>
                    <Badge variant="outline">{binding.gatewayServiceKey}</Badge>
                  </TableCell>
                  <TableCell className="text-gray-500">{binding.bindTime}</TableCell>
                  <TableCell>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleUnbind(binding.id)}
                    >
                      <Unlink className="w-4 h-4 mr-1" />
                      解除绑定
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
