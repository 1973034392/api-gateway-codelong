import { useState } from 'react';
import { Edit, Save, X } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Badge } from './ui/badge';

interface RateLimitConfig {
  id: string;
  serviceKey: string;
  serviceName: string;
  maxRequestsPerSecond: number;
  maxRequestsPerMinute: number;
  maxRequestsPerHour: number;
  enabled: boolean;
}

export function TrafficControl() {
  const [configs, setConfigs] = useState<RateLimitConfig[]>([
    {
      id: '1',
      serviceKey: 'user-service',
      serviceName: '用户服务',
      maxRequestsPerSecond: 100,
      maxRequestsPerMinute: 5000,
      maxRequestsPerHour: 200000,
      enabled: true,
    },
    {
      id: '2',
      serviceKey: 'order-service',
      serviceName: '订单服务',
      maxRequestsPerSecond: 50,
      maxRequestsPerMinute: 2000,
      maxRequestsPerHour: 100000,
      enabled: true,
    },
    {
      id: '3',
      serviceKey: 'api-gateway-01',
      serviceName: 'API网关-主',
      maxRequestsPerSecond: 500,
      maxRequestsPerMinute: 20000,
      maxRequestsPerHour: 1000000,
      enabled: true,
    },
  ]);

  const [editingId, setEditingId] = useState<string | null>(null);
  const [editingConfig, setEditingConfig] = useState<RateLimitConfig | null>(null);

  const handleEdit = (config: RateLimitConfig) => {
    setEditingId(config.id);
    setEditingConfig({ ...config });
  };

  const handleSave = () => {
    if (editingConfig) {
      setConfigs(
        configs.map((c) => (c.id === editingConfig.id ? editingConfig : c))
      );
      setEditingId(null);
      setEditingConfig(null);
    }
  };

  const handleCancel = () => {
    setEditingId(null);
    setEditingConfig(null);
  };

  const handleToggleEnabled = (id: string) => {
    setConfigs(
      configs.map((c) => (c.id === id ? { ...c, enabled: !c.enabled } : c))
    );
  };

  return (
    <div>
      <div className="mb-6">
        <h2>流量控制</h2>
        <p className="text-gray-600 mt-2">
          配置各服务的限流策略，防止服务过载
        </p>
      </div>

      <div className="bg-white rounded-lg shadow">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>服务名称</TableHead>
              <TableHead>服务 Key</TableHead>
              <TableHead>每秒请求数</TableHead>
              <TableHead>每分钟请求数</TableHead>
              <TableHead>每小时请求数</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {configs.map((config) => {
              const isEditing = editingId === config.id;
              const displayConfig = isEditing ? editingConfig! : config;

              return (
                <TableRow key={config.id}>
                  <TableCell>{config.serviceName}</TableCell>
                  <TableCell>
                    <Badge variant="outline">{config.serviceKey}</Badge>
                  </TableCell>
                  <TableCell>
                    {isEditing ? (
                      <Input
                        type="number"
                        value={displayConfig.maxRequestsPerSecond}
                        onChange={(e) =>
                          setEditingConfig({
                            ...editingConfig!,
                            maxRequestsPerSecond: parseInt(e.target.value) || 0,
                          })
                        }
                        className="w-24"
                      />
                    ) : (
                      <span>{config.maxRequestsPerSecond} /s</span>
                    )}
                  </TableCell>
                  <TableCell>
                    {isEditing ? (
                      <Input
                        type="number"
                        value={displayConfig.maxRequestsPerMinute}
                        onChange={(e) =>
                          setEditingConfig({
                            ...editingConfig!,
                            maxRequestsPerMinute: parseInt(e.target.value) || 0,
                          })
                        }
                        className="w-24"
                      />
                    ) : (
                      <span>{config.maxRequestsPerMinute} /min</span>
                    )}
                  </TableCell>
                  <TableCell>
                    {isEditing ? (
                      <Input
                        type="number"
                        value={displayConfig.maxRequestsPerHour}
                        onChange={(e) =>
                          setEditingConfig({
                            ...editingConfig!,
                            maxRequestsPerHour: parseInt(e.target.value) || 0,
                          })
                        }
                        className="w-24"
                      />
                    ) : (
                      <span>{config.maxRequestsPerHour} /h</span>
                    )}
                  </TableCell>
                  <TableCell>
                    <button
                      onClick={() => handleToggleEnabled(config.id)}
                      disabled={isEditing}
                    >
                      <Badge
                        variant={config.enabled ? 'default' : 'secondary'}
                        className="cursor-pointer"
                      >
                        {config.enabled ? '已启用' : '已禁用'}
                      </Badge>
                    </button>
                  </TableCell>
                  <TableCell>
                    {isEditing ? (
                      <div className="flex gap-2">
                        <Button size="sm" onClick={handleSave}>
                          <Save className="w-4 h-4 mr-1" />
                          保存
                        </Button>
                        <Button size="sm" variant="outline" onClick={handleCancel}>
                          <X className="w-4 h-4 mr-1" />
                          取消
                        </Button>
                      </div>
                    ) : (
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => handleEdit(config)}
                      >
                        <Edit className="w-4 h-4 mr-1" />
                        编辑
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </div>

      <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h4 className="text-blue-900 mb-2">提示</h4>
        <ul className="text-blue-700 space-y-1 list-disc list-inside">
          <li>限流配置实时生效，请谨慎修改</li>
          <li>建议根据服务实际承载能力设置合理的限流阈值</li>
          <li>禁用限流可能导致服务过载，请确保服务有足够的处理能力</li>
        </ul>
      </div>
    </div>
  );
}
