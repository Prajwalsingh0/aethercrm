import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { getAccessToken } from './api/client';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import LeadsPage from './pages/LeadsPage';
import AccountsPage from './pages/AccountsPage';
import ContactsPage from './pages/ContactsPage';
import PipelinePage from './pages/PipelinePage';
import ActivitiesPage from './pages/ActivitiesPage';
import SupportPage from './pages/SupportPage';
import AiPage from './pages/AiPage';

function PrivateRoute({ children }: { children: React.ReactNode }) {
  if (!getAccessToken()) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <Layout />
            </PrivateRoute>
          }
        >
          <Route index element={<DashboardPage />} />
          <Route path="leads" element={<LeadsPage />} />
          <Route path="accounts" element={<AccountsPage />} />
          <Route path="contacts" element={<ContactsPage />} />
          <Route path="pipeline" element={<PipelinePage />} />
          <Route path="activities" element={<ActivitiesPage />} />
          <Route path="support" element={<SupportPage />} />
          <Route path="ai" element={<AiPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
