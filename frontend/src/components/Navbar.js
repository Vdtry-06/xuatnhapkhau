import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const adminLinks = [
  { path: '/admin/agents',    label: '🏢 Đại lý' },
  { path: '/admin/suppliers', label: '🚚 Nhà cung cấp' },
];

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate  = useNavigate();
  const location  = useLocation();

  const links =
    user?.role === 'ADMIN' ? adminLinks :
    user?.role === 'SUPPLIER' ? supplierLinks :
    user?.role === 'AGENT' ? agentLinks :
    [];

  const homePath =
    user?.role === 'ADMIN' ? '/admin/agents' :
    user?.role === 'SUPPLIER' ? '/supplier/orders' :
    user?.role === 'AGENT' ? '/agent/products' :
    '/unauthorized';

  return (
    <nav style={s.nav}>
      <div style={s.inner}>
        <div style={s.brand}
          onClick={() => navigate(homePath)}>
          🚢 <span style={s.brandText}>XNK Portal</span>
        </div>

        <div style={s.links}>
          {links.map(link => (
            <button key={link.path}
              style={{ ...s.link, ...(location.pathname === link.path ? s.linkActive : {}) }}
              onClick={() => navigate(link.path)}>
              {link.label}
            </button>
          ))}
        </div>

        <div style={s.right}>
          <div style={s.userInfo}>
            <span style={s.username}>{user?.username}</span>
            <span style={{ ...s.roleBadge,
              background: user?.role === 'ADMIN' ? '#dbeafe' : user?.role === 'SUPPLIER' ? '#fef3c7' : '#dcfce7',
              color:      user?.role === 'ADMIN' ? '#1d4ed8' : user?.role === 'SUPPLIER' ? '#b45309' : '#15803d' }}>
              {user?.role === 'ADMIN' ? 'Admin' : user?.role === 'SUPPLIER' ? 'Supplier' : user?.role === 'AGENT' ? 'Đại lý' : user?.role}
            </span>
          </div>
          <button style={s.logoutBtn} onClick={() => { logout(); navigate('/login'); }}>
            Đăng xuất
          </button>
        </div>
      </div>
    </nav>
  );
}

const s = {
  nav:        { background: '#1e3a5f', borderBottom: '1px solid #2d4f7c',
                position: 'sticky', top: 0, zIndex: 100 },
  inner:      { maxWidth: 1300, margin: '0 auto', padding: '0 24px', height: 56,
                display: 'flex', alignItems: 'center', gap: 24 },
  brand:      { display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', flexShrink: 0 },
  brandText:  { color: '#fff', fontWeight: 700, fontSize: 16, letterSpacing: 0.5 },
  links:      { display: 'flex', gap: 4, flex: 1 },
  link:       { background: 'none', border: 'none', color: '#93c5fd', padding: '6px 14px',
                borderRadius: 7, fontSize: 13, fontWeight: 500, cursor: 'pointer' },
  linkActive: { background: 'rgba(255,255,255,0.15)', color: '#fff' },
  right:      { display: 'flex', alignItems: 'center', gap: 12, flexShrink: 0 },
  userInfo:   { display: 'flex', alignItems: 'center', gap: 8 },
  username:   { color: '#e2e8f0', fontSize: 13, fontWeight: 500 },
  roleBadge:  { padding: '2px 8px', borderRadius: 12, fontSize: 11, fontWeight: 600 },
  logoutBtn:  { background: 'rgba(255,255,255,0.1)', border: '1px solid rgba(255,255,255,0.2)',
                color: '#e2e8f0', padding: '5px 12px', borderRadius: 7, fontSize: 12, cursor: 'pointer' },
};
