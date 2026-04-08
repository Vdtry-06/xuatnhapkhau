import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      const user = await login(form.username, form.password);
      const nextPath =
        user.role === 'ADMIN' ? '/admin/agents' :
        user.role === 'SUPPLIER' ? '/supplier/orders' :
        user.role === 'AGENT' ? '/agent/products' :
        '/unauthorized';
      navigate(nextPath);
    } catch {
      setError('Tên đăng nhập hoặc mật khẩu không đúng');
    } finally { setLoading(false); }
  };

  return (
    <div style={s.page}>
      <div style={s.card}>
        <div style={s.logo}>🚢</div>
        <h2 style={s.title}>XNK Portal</h2>
        <p style={s.sub}>Hệ thống quản lý xuất nhập khẩu</p>
        {error && <div style={s.err}>{error}</div>}
        <form onSubmit={handleSubmit}>
          <label style={s.label}>Tên đăng nhập</label>
          <input style={s.input} value={form.username} placeholder="admin / agent01..."
            onChange={e => setForm({...form, username: e.target.value})} required />
          <label style={s.label}>Mật khẩu</label>
          <input style={s.input} type="password" value={form.password} placeholder="••••••••"
            onChange={e => setForm({...form, password: e.target.value})} required />
          <button style={s.btn} disabled={loading}>
            {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>
        </form>
        <div style={s.hint}>
          <b>Admin:</b> admin / admin123
          <br />
          <b>Đại lý:</b> agent01 / agent123
          <br />
          <b>Supplier:</b> đăng nhập để xử lý đơn đặt trước và cập nhật giao hàng
        </div>
      </div>
    </div>
  );
}

const s = {
  page: { minHeight:'100vh', background:'#f0f4f8', display:'flex', alignItems:'center', justifyContent:'center' },
  card: { background:'#fff', borderRadius:16, padding:'40px 36px', width:360, boxShadow:'0 4px 24px rgba(0,0,0,0.1)' },
  logo: { fontSize:48, textAlign:'center', marginBottom:8 },
  title: { textAlign:'center', margin:'0 0 4px', color:'#1e3a5f', fontSize:22 },
  sub: { textAlign:'center', color:'#64748b', fontSize:13, marginBottom:24 },
  err: { background:'#fef2f2', color:'#dc2626', border:'1px solid #fecaca', borderRadius:8,
    padding:'10px 14px', fontSize:13, marginBottom:16 },
  label: { display:'block', fontSize:13, fontWeight:500, color:'#374151', marginBottom:6 },
  input: { width:'100%', padding:'10px 14px', border:'1px solid #d1d5db', borderRadius:8,
    fontSize:14, marginBottom:16, boxSizing:'border-box', outline:'none' },
  btn: { width:'100%', padding:'12px', background:'#1e3a5f', color:'#fff', border:'none',
    borderRadius:8, fontSize:15, fontWeight:600, cursor:'pointer' },
  hint: { marginTop:20, fontSize:12, color:'#94a3b8', textAlign:'center', background:'#f8fafc',
    padding:'8px 12px', borderRadius:8 },
};
