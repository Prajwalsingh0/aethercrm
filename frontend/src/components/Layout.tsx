import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { clearAuth, getStoredUser } from '../api/client';

const nav = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/leads', label: 'Leads' },
  { to: '/accounts', label: 'Accounts' },
  { to: '/contacts', label: 'Contacts' },
  { to: '/pipeline', label: 'Pipeline' },
  { to: '/ai', label: 'AI Copilot' },
];

export default function Layout() {
  const user = getStoredUser();
  const navigate = useNavigate();

  function logout() {
    clearAuth();
    navigate('/login');
  }

  return (
    <div className="min-h-screen flex bg-slate-50">
      <aside className="w-60 bg-slate-900 text-slate-200 flex flex-col shrink-0">
        <div className="px-5 py-5 border-b border-slate-700">
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-indigo-500 flex items-center justify-center font-bold text-white text-sm">A</div>
            <div>
              <div className="font-semibold text-white text-sm">AetherCRM</div>
              <div className="text-xs text-slate-400 truncate max-w-[140px]">{user?.organizationName}</div>
            </div>
          </div>
        </div>
        <nav className="flex-1 py-4 px-3 space-y-0.5">
          {nav.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end}
              className={({ isActive }) =>
                `block rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                  isActive ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                }`}>{item.label}</NavLink>
          ))}
        </nav>
        <div className="p-4 border-t border-slate-700">
          <div className="text-xs text-slate-400 mb-1">{user?.email}</div>
          <div className="text-sm font-medium text-white mb-2">{user?.displayName || user?.firstName}</div>
          <button onClick={logout} className="text-xs text-slate-400 hover:text-white">Sign out</button>
        </div>
      </aside>
      <main className="flex-1 overflow-auto">
        <div className="p-6 max-w-7xl mx-auto"><Outlet /></div>
      </main>
    </div>
  );
}
