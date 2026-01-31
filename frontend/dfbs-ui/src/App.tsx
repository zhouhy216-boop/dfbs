import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { AuthGuard } from '@/layouts/BasicLayout';
import BasicLayout from '@/layouts/BasicLayout';
import Login from '@/pages/Login';
import Dashboard from '@/pages/Dashboard';
import Customer from '@/pages/Customer';
import Quote from '@/pages/Quote';
import Shipment from '@/pages/Shipment';
import Finance from '@/pages/Finance';
import AfterSales from '@/pages/AfterSales';
import AfterSalesDetail from '@/pages/AfterSales/Detail';
import MasterDataContract from '@/pages/MasterData/Contract';
import MasterDataMachineModel from '@/pages/MasterData/MachineModel';
import MasterDataMachineModelDetail from '@/pages/MasterData/MachineModel/Detail';
import MasterDataSparePart from '@/pages/MasterData/SparePart';
import MasterDataMachine from '@/pages/MasterData/Machine';
import MasterDataMachineDetail from '@/pages/MasterData/Machine/Detail';
import MasterDataSimCard from '@/pages/MasterData/SimCard';
import MasterDataSimCardDetail from '@/pages/MasterData/SimCard/Detail';
import MasterDataModelPartList from '@/pages/MasterData/ModelPartList';

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <AuthGuard>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<BasicLayout />}>
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="customers" element={<Customer />} />
              <Route path="quotes" element={<Quote />} />
              <Route path="shipments" element={<Shipment />} />
              <Route path="after-sales" element={<AfterSales />} />
              <Route path="after-sales/:id" element={<AfterSalesDetail />} />
              <Route path="finance" element={<Finance />} />
              <Route path="master-data" element={<Navigate to="/master-data/contracts" replace />} />
              <Route path="master-data/contracts" element={<MasterDataContract />} />
              <Route path="master-data/machine-models" element={<MasterDataMachineModel />} />
              <Route path="master-data/machine-models/:id" element={<MasterDataMachineModelDetail />} />
              <Route path="master-data/spare-parts" element={<MasterDataSparePart />} />
              <Route path="master-data/machines" element={<MasterDataMachine />} />
              <Route path="master-data/machines/:id" element={<MasterDataMachineDetail />} />
              <Route path="master-data/sim-cards" element={<MasterDataSimCard />} />
              <Route path="master-data/sim-cards/:id" element={<MasterDataSimCardDetail />} />
              <Route path="master-data/model-part-lists" element={<MasterDataModelPartList />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthGuard>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
