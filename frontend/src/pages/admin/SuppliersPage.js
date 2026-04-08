import { useEffect, useState } from 'react';
import api from '../../services/api';
import Navbar from '../../components/Navbar';

export default function AdminSuppliersPage() {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [msg, setMsg] = useState({ text: '', ok: true });
  const [form, setForm] = useState({
    name: '', email: '', phone: '', address: '', taxCode: '',
    username: '', password: ''
  });

  const flash = (text, ok = true) => {
    setMsg({ text, ok });
    setTimeout(() => setMsg({ text: '' }), 4000);
  };

  const fetchSuppliers = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/suppliers');
      setSuppliers(res.data);
    } catch {
      flash('Lỗi tải danh sách nhà cung cấp', false);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchSuppliers(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      const supplierRes = await api.post('/api/suppliers', {
        name: form.name,
        email: form.email,
        phone: form.phone || null,
        address: form.address || null,
        taxCode: form.taxCode,
      });
      await api.post('/api/auth/register-supplier', {
        username: form.username,
        password: form.password,
        supplierId: supplierRes.data.id,
      });
      flash('Tạo nhà cung cấp và tài khoản đăng nhập thành công');
      setShowForm(false);
      setForm({ name: '', email: '', phone: '', address: '', taxCode: '', username: '', password: '' });
      fetchSuppliers();
    } catch (err) {
      flash(err.response?.data?.message || 'Lỗi tạo nhà cung cấp', false);
    }
  };

  return (
    <div style={s.page}>
      <Navbar />
      <div style={s.container}>
        <div style={s.header}>
          <h1 style={s.h1}>Quản lý nhà cung cấp</h1>
          <button style={s.btnPrimary} onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Đóng' : '+ Thêm nhà cung cấp'}
          </button>
        </div>

        {msg.text && (
          <div style={{ ...s.toast, background: msg.ok ? '#ecfdf5' : '#fef2f2',
            color: msg.ok ? '#065f46' : '#dc2626',
            border: `1px solid ${msg.ok ? '#6ee7b7' : '#fecaca'}` }}>
            {msg.text}
          </div>
        )}

        {showForm && (
          <form style={s.form} onSubmit={handleCreate}>
            <h3 style={s.formTitle}>Tạo nhà cung cấp + tài khoản đăng nhập</h3>
            <div style={s.grid}>
              <input style={s.input} placeholder="Tên nhà cung cấp *" value={form.name}
                onChange={e => setForm({ ...form, name: e.target.value })} required />
              <input style={s.input} placeholder="Email *" type="email" value={form.email}
                onChange={e => setForm({ ...form, email: e.target.value })} required />
              <input style={s.input} placeholder="Số điện thoại" value={form.phone}
                onChange={e => setForm({ ...form, phone: e.target.value })} />
              <input style={s.input} placeholder="Địa chỉ" value={form.address}
                onChange={e => setForm({ ...form, address: e.target.value })} />
              <input style={s.input} placeholder="Mã số thuế *" value={form.taxCode}
                onChange={e => setForm({ ...form, taxCode: e.target.value })} required />
              <input style={s.input} placeholder="Username đăng nhập *" value={form.username}
                onChange={e => setForm({ ...form, username: e.target.value })} required />
              <input style={s.input} placeholder="Mật khẩu *" type="password" value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })} required />
            </div>
            <button style={s.btnPrimary}>Lưu nhà cung cấp</button>
          </form>
        )}

        {loading ? <div style={s.center}>Đang tải...</div> : (
          <div style={s.tableWrap}>
            <table style={s.table}>
              <thead>
                <tr style={s.thead}>
                  {['ID', 'Tên nhà cung cấp', 'Email', 'Điện thoại', 'Mã số thuế', 'Trạng thái'].map(h =>
                    <th key={h} style={s.th}>{h}</th>)}
                </tr>
              </thead>
              <tbody>
                {suppliers.map(a => (
                  <tr key={a.id} style={s.tr}>
                    <td style={s.td}>{a.id}</td>
                    <td style={s.td}><b>{a.name}</b></td>
                    <td style={s.td}>{a.email}</td>
                    <td style={s.td}>{a.phone || '—'}</td>
                    <td style={s.td}>{a.taxCode}</td>
                    <td style={s.td}>{a.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

const s = {
  page: { minHeight: '100vh', background: '#f8fafc' },
  container: { maxWidth: 1200, margin: '0 auto', padding: 24 },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 },
  h1: { margin: 0, fontSize: 22, color: '#1e3a5f' },
  btnPrimary: { background: '#1e3a5f', color: '#fff', border: 'none', borderRadius: 8,
    padding: '10px 20px', fontWeight: 600, cursor: 'pointer', fontSize: 14 },
  toast: { padding: '10px 16px', marginBottom: 16, borderRadius: 8, fontSize: 13 },
  form: { background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, padding: 24, marginBottom: 20 },
  formTitle: { margin: '0 0 16px', fontSize: 16, color: '#1e3a5f' },
  grid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 14 },
  input: { width: '100%', padding: '9px 12px', border: '1px solid #d1d5db', borderRadius: 7, fontSize: 13, boxSizing: 'border-box' },
  center: { textAlign: 'center', padding: 40, color: '#94a3b8' },
  tableWrap: { overflowX: 'auto', background: '#fff', borderRadius: 12, border: '1px solid #e2e8f0' },
  table: { width: '100%', borderCollapse: 'collapse' },
  thead: { background: '#f1f5f9' },
  th: { padding: '12px 16px', textAlign: 'left', fontSize: 12, fontWeight: 600, color: '#475569' },
  tr: { borderTop: '1px solid #f1f5f9' },
  td: { padding: '12px 16px', fontSize: 13, color: '#374151' },
};
