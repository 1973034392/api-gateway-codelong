import { useState } from 'react';
import { Sidebar } from './components/Sidebar';
import { CoreServiceManagement } from './components/CoreServiceManagement';
import { GatewayServiceManagement } from './components/GatewayServiceManagement';
import { ApiManagement } from './components/ApiManagement';
import { ServiceBinding } from './components/ServiceBinding';
import { TrafficControl } from './components/TrafficControl';

type Page = 'core-service' | 'gateway-service' | 'api-management' | 'service-binding' | 'traffic-control';

export default function App() {
  const [currentPage, setCurrentPage] = useState<Page>('core-service');

  const renderPage = () => {
    switch (currentPage) {
      case 'core-service':
        return <CoreServiceManagement />;
      case 'gateway-service':
        return <GatewayServiceManagement />;
      case 'api-management':
        return <ApiManagement />;
      case 'service-binding':
        return <ServiceBinding />;
      case 'traffic-control':
        return <TrafficControl />;
      default:
        return <CoreServiceManagement />;
    }
  };

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar currentPage={currentPage} onPageChange={setCurrentPage} />
      <main className="flex-1 overflow-auto">
        <div className="p-8">
          {renderPage()}
        </div>
      </main>
    </div>
  );
}
