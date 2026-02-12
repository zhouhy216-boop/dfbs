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
import ImportCenter from '@/pages/ImportCenter';
import WarehouseInventory from '@/pages/Warehouse/Inventory';
import WarehouseReplenish from '@/pages/Warehouse/Replenish';
import WorkOrderPublic from '@/pages/WorkOrder/Public';
import WorkOrderInternal from '@/pages/WorkOrder/Internal';
import WorkOrderInternalDetail from '@/pages/WorkOrder/Internal/Detail';
import ConfirmationCenter from '@/pages/Admin/ConfirmationCenter';
import OrgLevelConfig from '@/pages/Admin/OrgLevelConfig';
import OrgTree from '@/pages/Admin/OrgTree';
import OrgChangeLog from '@/pages/Admin/OrgChangeLog';
import DictionaryTypes from '@/pages/Admin/DictionaryTypes';
import DictionaryItems from '@/pages/Admin/DictionaryItems';
import { SuperAdminGuard } from '@/shared/components/SuperAdminGuard';
import PlatformConfig from '@/pages/System/PlatformConfig';
import PlatformApplication from '@/pages/Platform/Application';
import PlatformOrg from '@/pages/Platform/Org';
import PlatformSimApplication from '@/pages/Platform/SimApplication';
import ApplicationsHistory from '@/pages/Platform/applications/History';
import PlatformApply from '@/pages/Platform/applications/Apply';
import ReusePlaceholder from '@/pages/Platform/applications/Reuse';
import VerificationPlaceholder from '@/pages/Platform/applications/Verification';
import SimActivationPlaceholder from '@/pages/Platform/applications/SimActivation';

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Routes>
          {/* Public routes – no auth */}
          <Route path="/login" element={<Login />} />
          <Route path="/public/repair" element={<WorkOrderPublic />} />

          {/* Protected routes – AuthGuard + BasicLayout */}
          <Route path="/" element={<AuthGuard><BasicLayout /></AuthGuard>}>
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="customers" element={<Customer />} />
            <Route path="quotes" element={<Quote />} />
            <Route path="logistics" element={<Navigate to="/shipments" replace />} />
            <Route path="shipments" element={<Shipment />} />
            <Route path="after-sales" element={<AfterSales />} />
            <Route path="after-sales/:id" element={<AfterSalesDetail />} />
            <Route path="after-sales-service" element={<Navigate to="/work-orders" replace />} />
            <Route path="work-orders" element={<WorkOrderInternal />} />
            <Route path="work-orders/:id" element={<WorkOrderInternalDetail />} />
            <Route path="finance" element={<Finance />} />
            <Route path="warehouse/inventory" element={<WarehouseInventory />} />
            <Route path="warehouse/replenish" element={<WarehouseReplenish />} />
            <Route path="import-center" element={<ImportCenter />} />
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
            <Route path="platform" element={<Navigate to="/platform/applications" replace />} />
            <Route path="platform/orgs" element={<PlatformOrg />} />
            <Route path="platform/applications" element={<PlatformApplication />} />
            <Route path="platform/applications/history" element={<ApplicationsHistory />} />
            <Route path="platform/apply" element={<PlatformApply />} />
            <Route path="platform/applications/reuse" element={<ReusePlaceholder />} />
            <Route path="platform/applications/verification" element={<VerificationPlaceholder />} />
            <Route path="platform/applications/sim-activation" element={<SimActivationPlaceholder />} />
            <Route path="platform/applications/enterprise-direct" element={<Navigate to="/platform/apply?source=enterprise" replace />} />
            <Route path="platform/sim-applications" element={<PlatformSimApplication />} />
            <Route path="admin" element={<Navigate to="/admin/confirmation-center" replace />} />
            <Route path="admin/confirmation-center" element={<ConfirmationCenter />} />
            <Route path="admin/org-levels" element={<SuperAdminGuard><OrgLevelConfig /></SuperAdminGuard>} />
            <Route path="admin/org-tree" element={<SuperAdminGuard><OrgTree /></SuperAdminGuard>} />
            <Route path="admin/org-change-logs" element={<SuperAdminGuard><OrgChangeLog /></SuperAdminGuard>} />
            <Route path="admin/dictionary-types" element={<SuperAdminGuard><DictionaryTypes /></SuperAdminGuard>} />
            <Route path="admin/dictionary-types/:typeId/items" element={<SuperAdminGuard><DictionaryItems /></SuperAdminGuard>} />
            <Route path="system/platform-config" element={<PlatformConfig />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
