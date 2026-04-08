import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';

import LoginPage            from './pages/LoginPage';
import AdminAgentsPage      from './pages/admin/AgentsPage';
import AdminSuppliersPage   from './pages/admin/SuppliersPage';
import AgentProductsPage    from './pages/agent/ProductsPage';
import AgentOrdersPage      from './pages/agent/OrdersPage';
import SupplierOrdersPage   from './pages/supplier/OrdersPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          {/* Admin routes */}
          <Route path="/admin/agents" element={
            <PrivateRoute allowedRoles={['ADMIN']}><AdminAgentsPage /></PrivateRoute>} />
          <Route path="/admin/suppliers" element={
            <PrivateRoute allowedRoles={['ADMIN']}><AdminSuppliersPage /></PrivateRoute>} />

          {/* Agent routes */}
          <Route path="/agent/products" element={
            <PrivateRoute allowedRoles={['AGENT']}><AgentProductsPage /></PrivateRoute>} />
          <Route path="/agent/orders" element={
            <PrivateRoute allowedRoles={['AGENT']}><AgentOrdersPage /></PrivateRoute>} />

          {/* Supplier routes */}
          <Route path="/supplier/orders" element={
            <PrivateRoute allowedRoles={['SUPPLIER']}><SupplierOrdersPage /></PrivateRoute>} />

          <Route path="/unauthorized" element={
            <div style={{ textAlign: 'center', padding: 80 }}>
              <h2>Không có quyền truy cập</h2>
              <p>Tài khoản hiện tại không có màn hình phù hợp trong phiên bản này.</p>
              <a href="/login">Đăng nhập lại</a>
            </div>} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
