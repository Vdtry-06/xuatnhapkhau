import { useState, useEffect } from 'react';
import api from '../../services/api';
import Navbar from '../../components/Navbar';
import { useAuth } from '../../context/AuthContext';

const statusColor = {
  PENDING: '#f59e0b',
  SUPPLIER_REVIEWING: '#8b5cf6',
  PARTIALLY_PLANNED: '#0f766e',
  READY_TO_SHIP: '#2563eb',
  IN_PRODUCTION: '#d97706',
  CONFIRMED: '#3b82f6',
  SHIPPED: '#06b6d4',
  DELIVERED: '#16a34a',
  UNFULFILLABLE: '#991b1b',
  CANCELED: '#dc2626',
};

const PAYMENT_METHODS = [
  { value: 'BANK_TRANSFER', label: 'Chuyen khoan ngan hang' },
  { value: 'CREDIT_DEBT', label: 'Ghi no tin dung' },
  { value: 'LC', label: 'L/C' },
];

export default function AgentOrdersPage() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [msg, setMsg] = useState({ text: '', ok: true });
  const [selectingResponseId, setSelectingResponseId] = useState(null);
  const [delivering, setDelivering] = useState(false);
  const [paymentInfo, setPaymentInfo] = useState(null);
  const [showPayForm, setShowPayForm] = useState(false);
  const [payMethod, setPayMethod] = useState('BANK_TRANSFER');
  const [paying, setPaying] = useState(false);

  const flash = (text, ok = true) => {
    setMsg({ text, ok });
    setTimeout(() => setMsg({ text: '' }), 5000);
  };

  const fetchOrders = () => {
    if (!user?.agentId) return;
    setLoading(true);
    api.get(`/api/orders/agent/${user.agentId}`)
      .then((res) => setOrders(res.data))
      .catch(() => flash('Loi tai don hang', false))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchOrders(); }, [user]);

  const handleSelectOrder = async (order) => {
    setSelected(order);
    setShowPayForm(false);
    setPaymentInfo(null);
    try {
      const res = await api.get(`/api/payments/order/${order.id}`);
      setPaymentInfo(res.data);
    } catch {
      setPaymentInfo(null);
    }
  };

  const handleSelectSupplier = async (lineId, responseId) => {
    if (!selected) return;
    setSelectingResponseId(responseId);
    try {
      const res = await api.put(`/api/orders/${selected.id}/lines/${lineId}/responses/${responseId}/select`);
      setSelected(res.data);
      flash('Da chon nha cung cap cho dong hang');
      fetchOrders();
    } catch (err) {
      flash(err.response?.data?.message || 'Khong the chon nha cung cap', false);
    } finally {
      setSelectingResponseId(null);
    }
  };

  const handlePay = async () => {
    if (!selected) return;
    setPaying(true);
    try {
      const res = await api.post('/api/payments', {
        orderId: selected.id,
        amount: selected.totalAmount,
        paymentMethod: payMethod,
      });
      setPaymentInfo(res.data);
      setShowPayForm(false);
      flash(res.data.status === 'SUCCESS' ? 'Thanh toan thanh cong' : 'Tao yeu cau thanh toan thanh cong');
    } catch (err) {
      flash(err.response?.data?.message || 'Thanh toan that bai', false);
    } finally {
      setPaying(false);
    }
  };

  const handleDelivered = async () => {
    if (!selected || !user?.agentId) return;
    setDelivering(true);
    try {
      const res = await api.put(`/api/orders/${selected.id}/agent/${user.agentId}/deliver`);
      setSelected(res.data);
      flash('Da xac nhan DELIVERED');
      fetchOrders();
    } catch (err) {
      flash(err.response?.data?.message || 'Khong the xac nhan DELIVERED', false);
    } finally {
      setDelivering(false);
    }
  };

  const canPay = selected
    && !paymentInfo
    && !['CANCELED', 'UNFULFILLABLE'].includes(selected.status)
    && selected.orderlines?.every((line) => !!line.supplierId);

  return (
    <div style={s.page}>
      <Navbar />
      <div style={s.container}>
        <h1 style={s.h1}>Don hang cua toi</h1>

        {msg.text && (
          <div style={{ ...s.toast, background: msg.ok ? '#ecfdf5' : '#fef2f2',
            color: msg.ok ? '#065f46' : '#dc2626',
            border: `1px solid ${msg.ok ? '#6ee7b7' : '#fecaca'}` }}>
            {msg.text}
          </div>
        )}

        {loading ? <div style={s.center}>Dang tai...</div>
        : orders.length === 0 ? (
          <div style={s.empty}>Ban chua co don hang nao</div>
        ) : (
          <div style={s.layout}>
            <div style={s.list}>
              {orders.map((o) => (
                <div key={o.id}
                  style={{ ...s.card, borderLeft: `4px solid ${statusColor[o.status] || '#94a3b8'}`,
                    background: selected?.id === o.id ? '#eff6ff' : '#fff' }}
                  onClick={() => handleSelectOrder(o)}>
                  <div style={s.cardTop}>
                    <span style={s.orderId}>Don #{o.id}</span>
                    <span style={{ ...s.badge, background: (statusColor[o.status] || '#94a3b8') + '22',
                      color: statusColor[o.status] || '#64748b' }}>
                      {o.status}
                    </span>
                  </div>
                  <div style={s.cardMid}>
                    <span>{new Date(o.orderDate).toLocaleString('vi-VN')}</span>
                    <b>{Number(o.totalAmount).toLocaleString('vi-VN')} VND</b>
                  </div>
                  <div style={s.cardBot}>{o.orderlines?.length} san pham</div>
                </div>
              ))}
            </div>

            {selected && (
              <div style={s.detail}>
                <div style={s.detailHeader}>
                  <h3 style={s.detailTitle}>Don #{selected.id}</h3>
                  <button style={s.closeBtn} onClick={() => { setSelected(null); setShowPayForm(false); }}>X</button>
                </div>
                <div style={s.detailBody}>
                  <Info label="Ngay dat" value={new Date(selected.orderDate).toLocaleString('vi-VN')} />
                  <Info label="Dia chi" value={selected.shippingAddress} />
                  <Info label="Trang thai" value={selected.status} />

                  <h4 style={s.sub}>San pham</h4>
                  {selected.orderlines?.map((l) => (
                    <div key={l.id} style={s.lineBlock}>
                      <div style={s.line}>
                        <div style={{ flex: 1 }}>
                          <div style={s.lineName}>{l.productName}</div>
                          <div style={s.lineUnit}>{Number(l.unitPrice).toLocaleString('vi-VN')} VND x {l.quantity}</div>
                          {l.supplierName && <div style={s.lineSelected}>Da chon: {l.supplierName}</div>}
                        </div>
                        <b style={s.lineSub}>{Number(l.subTotal).toLocaleString('vi-VN')} VND</b>
                      </div>
                      {l.supplierOptions?.length > 0 && (
                        <div style={s.optionWrap}>
                          <div style={s.optionTitle}>Danh sach supplier nhan dong hang</div>
                          {l.supplierOptions.map((option) => (
                            <div key={option.id} style={s.optionCard}>
                              <div style={s.optionTop}>
                                <b>{option.supplierName}</b>
                                <span style={s.optionBadge}>
                                  {option.canceledByAgent ? 'CANCELED_BY_AGENT' : option.status}
                                </span>
                              </div>
                              <div style={s.optionMeta}>
                                San co: {option.availableQuantity} | San xuat them: {option.productionQuantity}
                              </div>
                              <div style={s.optionMeta}>
                                Du kien san sang: {option.expectedReadyDate || 'Chua cap nhat'}
                              </div>
                              <div style={s.optionNote}>{option.note || 'Khong co ghi chu'}</div>
                              {!option.selected && !option.canceledByAgent && (
                                <button
                                  style={s.optionBtn}
                                  onClick={() => handleSelectSupplier(l.id, option.id)}
                                  disabled={selectingResponseId === option.id}
                                >
                                  {selectingResponseId === option.id ? 'Dang chon...' : 'Chon supplier nay'}
                                </button>
                              )}
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  ))}

                  <div style={s.totalRow}>
                    <span>Tong cong</span>
                    <b style={s.totalAmt}>{Number(selected.totalAmount).toLocaleString('vi-VN')} VND</b>
                  </div>

                  {selected.status === 'SHIPPED' && (
                    <button style={s.deliveredBtn} onClick={handleDelivered} disabled={delivering}>
                      {delivering ? 'Dang xac nhan...' : 'Xac nhan DELIVERED'}
                    </button>
                  )}

                  <div style={s.paySection}>
                    {paymentInfo && (
                      <div style={s.payStatus}>
                        <Info label="Payment status" value={paymentInfo.status} />
                        <Info label="Phuong thuc" value={paymentInfo.paymentMethod} />
                        <Info label="Ma giao dich" value={paymentInfo.transactionRef || 'N/A'} />
                      </div>
                    )}

                    {canPay && !showPayForm && (
                      <button style={s.payBtn} onClick={() => setShowPayForm(true)}>Thanh toan</button>
                    )}

                    {canPay && showPayForm && (
                      <div style={s.payForm}>
                        <div style={s.payAmount}>So tien: <b>{Number(selected.totalAmount).toLocaleString('vi-VN')} VND</b></div>
                        <select style={s.input} value={payMethod} onChange={(e) => setPayMethod(e.target.value)}>
                          {PAYMENT_METHODS.map((m) => <option key={m.value} value={m.value}>{m.label}</option>)}
                        </select>
                        <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
                          <button style={s.payConfirmBtn} onClick={handlePay} disabled={paying}>
                            {paying ? 'Dang xu ly...' : 'Xac nhan'}
                          </button>
                          <button style={s.payCancelBtn} onClick={() => setShowPayForm(false)}>Huy</button>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

function Info({ label, value }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid #f1f5f9', fontSize: 12 }}>
      <span style={{ color: '#64748b' }}>{label}</span>
      <span style={{ color: '#1e293b', fontWeight: 500 }}>{value}</span>
    </div>
  );
}

const s = {
  page: { minHeight: '100vh', background: '#f8fafc' },
  container: { maxWidth: 1100, margin: '0 auto', padding: 24 },
  h1: { fontSize: 22, color: '#1e3a5f', marginBottom: 20 },
  toast: { padding: '10px 16px', borderRadius: 8, marginBottom: 16, fontSize: 13 },
  center: { textAlign: 'center', padding: 60, color: '#94a3b8' },
  empty: { textAlign: 'center', padding: 80, color: '#94a3b8' },
  layout: { display: 'flex', gap: 20, alignItems: 'flex-start' },
  list: { flex: 1, display: 'flex', flexDirection: 'column', gap: 12 },
  card: { background: '#fff', borderRadius: 10, border: '1px solid #e2e8f0', padding: '14px 18px', cursor: 'pointer' },
  cardTop: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 },
  orderId: { fontWeight: 700, color: '#1e3a5f', fontSize: 14 },
  badge: { padding: '3px 10px', borderRadius: 20, fontSize: 11, fontWeight: 600 },
  cardMid: { display: 'flex', justifyContent: 'space-between', marginBottom: 4, fontSize: 12 },
  cardBot: { fontSize: 12, color: '#94a3b8' },
  detail: { width: 360, background: '#fff', borderRadius: 12, border: '1px solid #e2e8f0', position: 'sticky', top: 72, maxHeight: 'calc(100vh - 100px)', overflowY: 'auto' },
  detailHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 18px', borderBottom: '1px solid #f1f5f9', position: 'sticky', top: 0, background: '#fff' },
  detailTitle: { margin: 0, fontSize: 15, color: '#1e3a5f' },
  closeBtn: { background: 'none', border: 'none', cursor: 'pointer', color: '#94a3b8' },
  detailBody: { padding: '14px 18px' },
  sub: { margin: '12px 0 8px', fontSize: 13, color: '#475569', fontWeight: 600 },
  lineBlock: { border: '1px solid #e2e8f0', borderRadius: 10, padding: 10, marginBottom: 10, background: '#f8fafc' },
  line: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  lineName: { fontSize: 13, fontWeight: 500 },
  lineUnit: { fontSize: 12, color: '#94a3b8' },
  lineSelected: { fontSize: 11, color: '#0f766e', marginTop: 4, fontWeight: 600 },
  lineSub: { fontSize: 13, color: '#1e3a5f' },
  optionWrap: { marginTop: 8 },
  optionTitle: { fontSize: 11, fontWeight: 700, color: '#475569', marginBottom: 6 },
  optionCard: { background: '#fff', border: '1px solid #e2e8f0', borderRadius: 8, padding: 8, marginBottom: 6 },
  optionTop: { display: 'flex', justifyContent: 'space-between', marginBottom: 4, fontSize: 12 },
  optionBadge: { background: '#eff6ff', color: '#2563eb', padding: '2px 6px', borderRadius: 999, fontSize: 10, fontWeight: 700 },
  optionMeta: { fontSize: 11, color: '#64748b' },
  optionNote: { fontSize: 11, color: '#334155', marginTop: 4, marginBottom: 6 },
  optionBtn: { background: '#1d4ed8', color: '#fff', border: 'none', borderRadius: 6, padding: '6px 8px', fontSize: 11, cursor: 'pointer' },
  totalRow: { display: 'flex', justifyContent: 'space-between', paddingTop: 10, marginTop: 4, borderTop: '2px solid #e2e8f0', fontSize: 14 },
  totalAmt: { color: '#1e3a5f' },
  deliveredBtn: { width: '100%', marginTop: 12, padding: '10px 12px', background: '#15803d', color: '#fff', border: 'none', borderRadius: 8, fontWeight: 700, cursor: 'pointer' },
  paySection: { marginTop: 14, paddingTop: 12, borderTop: '1px dashed #cbd5e1' },
  payStatus: { background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: 8, padding: 10, marginBottom: 8 },
  payBtn: { width: '100%', padding: 10, background: '#1e3a5f', color: '#fff', border: 'none', borderRadius: 8, fontWeight: 600, cursor: 'pointer' },
  payForm: { background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: 8, padding: 10 },
  payAmount: { fontSize: 12, color: '#475569', marginBottom: 6 },
  input: { width: '100%', padding: '8px 10px', border: '1px solid #d1d5db', borderRadius: 7, fontSize: 12, boxSizing: 'border-box' },
  payConfirmBtn: { flex: 1, padding: 8, background: '#1d4ed8', color: '#fff', border: 'none', borderRadius: 7, cursor: 'pointer', fontSize: 12 },
  payCancelBtn: { padding: '8px 12px', background: '#f1f5f9', color: '#334155', border: '1px solid #cbd5e1', borderRadius: 7, cursor: 'pointer', fontSize: 12 },
};
