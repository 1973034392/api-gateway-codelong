import { useState } from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Badge } from './ui/badge';

interface ApiInfo {
  id: string;
  name: string;
  remark: string;
  type: 'HTTP' | 'DUBBO';
  time: string;
  path?: string;
}

interface ServiceApis {
  serviceKey: string;
  serviceName: string;
  apis: ApiInfo[];
}

export function ApiManagement() {
  const servicesData: ServiceApis[] = [
    {
      serviceKey: 'user-service',
      serviceName: '用户服务',
      apis: [
        {
          id: '1',
          name: 'getUserInfo',
          remark: '获取用户详细信息',
          type: 'HTTP',
          time: '2025-11-13 09:15:00',
          path: '/api/user/info',
        },
        {
          id: '2',
          name: 'updateUserProfile',
          remark: '更新用户资料',
          type: 'HTTP',
          time: '2025-11-13 09:20:00',
          path: '/api/user/profile',
        },
        {
          id: '3',
          name: 'getUserByIdRpc',
          remark: 'RPC方式获取用户信息',
          type: 'DUBBO',
          time: '2025-11-13 09:25:00',
        },
      ],
    },
    {
      serviceKey: 'order-service',
      serviceName: '订单服务',
      apis: [
        {
          id: '4',
          name: 'createOrder',
          remark: '创建新订单',
          type: 'HTTP',
          time: '2025-11-13 09:30:00',
          path: '/api/order/create',
        },
        {
          id: '5',
          name: 'getOrderList',
          remark: '获取订单列表',
          type: 'HTTP',
          time: '2025-11-13 09:35:00',
          path: '/api/order/list',
        },
        {
          id: '6',
          name: 'cancelOrder',
          remark: '取消订单',
          type: 'HTTP',
          time: '2025-11-13 09:40:00',
          path: '/api/order/cancel',
        },
        {
          id: '7',
          name: 'calculateOrderPriceRpc',
          remark: 'RPC方式计算订单价格',
          type: 'DUBBO',
          time: '2025-11-13 09:45:00',
        },
      ],
    },
  ];

  const [selectedService, setSelectedService] = useState<string>(servicesData[0].serviceKey);

  const currentServiceData = servicesData.find((s) => s.serviceKey === selectedService);

  return (
    <div>
      <div className="mb-6">
        <h2>接口信息管理</h2>
      </div>

      <div className="mb-6">
        <label className="block text-gray-700 mb-2">选择核心服务</label>
        <Select value={selectedService} onValueChange={setSelectedService}>
          <SelectTrigger className="w-80">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {servicesData.map((service) => (
              <SelectItem key={service.serviceKey} value={service.serviceKey}>
                {service.serviceName} ({service.serviceKey})
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="bg-white rounded-lg shadow">
        <div className="p-4 border-b">
          <h3 className="text-gray-900">{currentServiceData?.serviceName}</h3>
          <p className="text-gray-500 mt-1">
            共 {currentServiceData?.apis.length} 个接口
          </p>
        </div>

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>接口名称</TableHead>
              <TableHead>接口路径</TableHead>
              <TableHead>接口类型</TableHead>
              <TableHead>备注</TableHead>
              <TableHead>更新时间</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {currentServiceData?.apis.map((api) => (
              <TableRow key={api.id}>
                <TableCell>{api.name}</TableCell>
                <TableCell>
                  {api.path ? (
                    <code className="text-blue-600 bg-blue-50 px-2 py-1 rounded">
                      {api.path}
                    </code>
                  ) : (
                    <span className="text-gray-400">-</span>
                  )}
                </TableCell>
                <TableCell>
                  <Badge variant={api.type === 'HTTP' ? 'default' : 'secondary'}>
                    {api.type}
                  </Badge>
                </TableCell>
                <TableCell>{api.remark}</TableCell>
                <TableCell className="text-gray-500">{api.time}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
