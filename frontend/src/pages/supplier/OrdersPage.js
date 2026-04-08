import { useEffect, useMemo, useState } from 'react';
import Navbar from '../../components/Navbar';
import api from '../../services/api';
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

export default function SupplierOrdersPage() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [msg, setMsg] = useState({ text: '', ok: true });
  const [shippingOrderId, setShippingOrderId] = useState(null);
  const [responseForms, setResponseForms] = useState({});
  const [respondingLineId, setRespondingLineId] = useState(null);
  const [paymentInfo, setPaymentInfo] = useState(null);
  const [updatingPayment, setUpdatingPayment] = useState(false);

  const flash = (text, ok = true) => {
    setMsg({ text, ok });
    setTimeout(() => setMsg({ text: '' }), 4000);
  };

  const fetchOrders = async () => {
    if (!user?.supplierId) return;
    setLoading(true);
    try {
      const res = await api.get(`/api/orders/supplier/${user.supplierId}`);
      setOrders(res.data);
      if (selected) {
        const refreshed = res.data.find((item) => item.id === selected.id);
        setSelected(refreshed || null);
      }
    } catch {
      flash('Loi tai danh sach don hang cua supplier', false);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchOrders(); }, [user]);

  useEffect(() => {
    if (!selected?.id) {
      setPaymentInfo(null);
      return;
    }
    api.get(`/api/payments/order/${selected.id}`)
      .then((res) => setPaymentInfo(res.data))
      .catch(() => setPaymentInfo(null));
  }, [selected]);

  const reviewingCount = useMemo(
    () => orders.filter((item) => ['PENDING', 'SUPPLIER_REVIEWING', 'PARTIALLY_PLANNED', 'IN_PRODUCTION', 'READY_TO_SHIP', 'CONFIRMED'].includes(item.status)).length,
    [orders]
  );

  const getLineForm = (lineId) => responseForms[lineId] || {
    availableQuantity: 0,
    productionQuantity: 0,
    expectedReadyDate: '',
    status: 'CAN_FULFILL',
    note: '',
  };

  const updateLineForm = (lineId, patch) => {
    setResponseForms((prev) => ({
      ...prev,
      [lineId]: { ...getLineForm(lineId), ...patch },
    }));
  };

  const handleRespondLine = async (line) => {
    if (!selected || !user?.supplierId) return;
    const form = getLineForm(line.id);
    setRespondingLineId(line.id);
    try {
      const res = await api.put(`/api/orders/${selected.id}/lines/${line.id}/supplier-response`, {
        supplierId: user.supplierId,
        supplierName: user.username || `Supplier #${user.supplierId}`,
        availableQuantity: Number(form.availableQuantity),
        productionQuantity: Number(form.productionQuantity),
        expectedReadyDate: form.expectedReadyDate || null,
        status: form.status,
        note: form.note,
      });
      setSelected(res.data);
      flash(`Da cap nhat nhan don cho dong ${line.productName}`);
      await fetchOrders();
    } catch (err) {
      flash(err.response?.data?.message || 'Khong the cap nhat dong hang nay', false);
    } finally {
      setRespondingLineId(null);
    }
  };

  const handleShip = async (orderId) => {
    if (!user?.supplierId) return;
    setShippingOrderId(orderId);
    try {
      await api.put(`/api/orders/${orderId}/supplier/${user.supplierId}/ship`);
      flash(`Don #${orderId} da cap nhat SHIPPED`);
      fetchOrders();
    } catch (err) {
      flash(err.response?.data?.message || 'Khong the cap nhat SHIPPED', false);
    } finally {
      setShippingOrderId(null);
    }
  };

  const handlePaymentStatus = async (status) => {
    if (!paymentInfo?.id || !user?.supplierId) return;
    setUpdatingPayment(true);
    try {
      const res = await api.put(`/api/payments/${paymentInfo.id}/supplier/${user.supplierId}/status?status=${status}`);
      setPaymentInfo(res.data);
      flash(`Da cap nhat payment ${status}`);
    } catch (err) {
      flash(err.response?.data?.message || 'Khong the cap nhat payment', false);
    } finally {
      setUpdatingPayment(false);
    }
  };

  return (
    <div style={s.page}>
      <Navbar />
      <div style={s.container}>
        <div style={s.header}>
          <div>
            <h1 style={s.h1}>Don dat truoc can xu ly</h1>
            <p style={s.sub}>Supplier nhan tung dong hang; dong nao bi agent chon supplier khac se bi huy voi supplier hien tai.</p>
          </div>
          <div style={s.summaryWrap}>
            <div style={s.summaryCard}>
              <div style={s.summaryLabel}>Tong don</div>
              <div style={s.summaryValue}>{orders.length}</div>
            </div>
            <div style={s.summaryCardWarn}>
              <div style={s.summaryLabel}>Dang xu ly</div>
              <div style={s.summaryValue}>{reviewingCount}</div>
            </div>
          </div>
        </div>

        {msg.text && (
          <div style={{
            ...s.toast,
            background: msg.ok ? '#ecfdf5' : '#fef2f2',
            color: msg.ok ? '#065f46' : '#dc2626',
            border: `1px solid ${msg.ok ? '#6ee7b7' : '#fecaca'}`,
          }}>
            {msg.text}
          </div>
        )}

        <div style={s.layout}>
          <div style={s.list}>
            {loading ? <div style={s.center}>Dang tai...</div> : orders.length === 0 ? (
              <div style={s.empty}>Chua co don hang phu hop de supplier xu ly.</div>
            ) : (
              orders.map((item) => (
                <div
                  key={item.id}
                  style={{
                    ...s.card,
                    borderLeft: `4px solid ${statusColor[item.status] || '#94a3b8'}`,
                    background: selected?.id === item.id ? '#eff6ff' : '#fff',
                  }}
                  onClick={() => setSelected(item)}
                >
                  <div style={s.cardTop}>
                    <span style={s.orderId}>Don #{item.id}</span>
                    <span style={{
                      ...s.badge,
                      background: `${statusColor[item.status] || '#94a3b8'}22`,
                      color: statusColor[item.status] || '#64748b',
                    }}>
                      {item.status}
                    </span>
                  </div>
                  <div style={s.cardMid}>
                    <span>Dai ly: #{item.agentId}</span>
                    <b>{Number(item.totalAmount).toLocaleString('vi-VN')} VND</b>
                  </div>
                  <div style={s.cardBottom}>{item.orderlines?.length || 0} san pham</div>
                </div>
              ))
            )}
          </div>

          {selected && (
            <div style={s.detail}>
              <div style={s.detailHeader}>
                <h3 style={s.detailTitle}>Chi tiet don #{selected.id}</h3>
                <button style={s.closeBtn} onClick={() => setSelected(null)}>X</button>
              </div>
              <div style={s.detailBody}>
                <Info label="Dai ly" value={`#${selected.agentId}`} />
                <Info label="Ngay dat" value={new Date(selected.orderDate).toLocaleString('vi-VN')} />
                <Info label="Dia chi giao" value={selected.shippingAddress} />
                <Info label="Trang thai" value={selected.status} />

                <h4 style={s.subTitle}>Payment</h4>
                {paymentInfo ? (
                  <div style={s.payBox}>
                    <Info label="Payment status" value={paymentInfo.status} />
                    <Info label="Phuong thuc" value={paymentInfo.paymentMethod} />
                    {paymentInfo.status === 'PENDING' && (
                      <div style={s.payActions}>
                        <button style={s.okBtn} onClick={() => handlePaymentStatus('SUCCESS')} disabled={updatingPayment}>
                          {updatingPayment ? 'Dang cap nhat...' : 'SUCCESS'}
                        </button>
                        <button style={s.failBtn} onClick={() => handlePaymentStatus('FAILED')} disabled={updatingPayment}>
                          FAILED
                        </button>
                      </div>
                    )}
                  </div>
                ) : (
                  <div style={s.payEmpty}>Don nay chua tao payment.</div>
                )}

                <h4 style={s.subTitle}>Nhan don theo tung dong hang</h4>
                {selected.orderlines?.map((line) => {
                  const selectedByOtherSupplier = line.supplierId && line.supplierId !== user.supplierId;
                  const selectedByMe = line.supplierId && line.supplierId === user.supplierId;
                  return (
                    <div key={line.id} style={s.lineBlock}>
                      <div style={s.line}>
                        <div>
                          <div style={s.lineName}>{line.productName}</div>
                          <div style={s.lineMeta}>SL yeu cau: {line.quantity}</div>
                          {selectedByMe && <div style={s.lineMetaOk}>Dang duoc chon: {line.supplierName}</div>}
                          {selectedByOtherSupplier && <div style={s.lineMetaFail}>Bi agent huy: da chon {line.supplierName}</div>}
                        </div>
                        <b>{Number(line.subTotal).toLocaleString('vi-VN')} VND</b>
                      </div>

                      {!selectedByOtherSupplier && !selectedByMe && (
                        <div style={s.responseForm}>
                          <div style={s.responseGrid}>
                            <input
                              style={s.input}
                              type="number"
                              min="0"
                              placeholder="Hang san co"
                              value={getLineForm(line.id).availableQuantity}
                              onChange={(e) => updateLineForm(line.id, { availableQuantity: e.target.value })}
                            />
                            <input
                              style={s.input}
                              type="number"
                              min="0"
                              placeholder="Can san xuat them"
                              value={getLineForm(line.id).productionQuantity}
                              onChange={(e) => updateLineForm(line.id, { productionQuantity: e.target.value })}
                            />
                            <input
                              style={s.input}
                              type="date"
                              value={getLineForm(line.id).expectedReadyDate}
                              onChange={(e) => updateLineForm(line.id, { expectedReadyDate: e.target.value })}
                            />
                            <select
                              style={s.input}
                              value={getLineForm(line.id).status}
                              onChange={(e) => updateLineForm(line.id, { status: e.target.value })}
                            >
                              <option value="CAN_FULFILL">Du hang</option>
                              <option value="PARTIAL_FULFILL">Co san mot phan</option>
                              <option value="IN_PRODUCTION">Co the san xuat them</option>
                              <option value="CANNOT_FULFILL">Khong lam duoc</option>
                            </select>
                          </div>
                          <textarea
                            style={s.textarea}
                            rows={2}
                            placeholder="Ghi chu cho agent"
                            value={getLineForm(line.id).note}
                            onChange={(e) => updateLineForm(line.id, { note: e.target.value })}
                          />
                          <button
                            style={s.respondBtn}
                            onClick={() => handleRespondLine(line)}
                            disabled={respondingLineId === line.id}
                          >
                            {respondingLineId === line.id ? 'Dang gui...' : 'Nhan don dong nay'}
                          </button>
                        </div>
                      )}
                    </div>
                  );
                })}

                {['PENDING', 'SUPPLIER_REVIEWING', 'PARTIALLY_PLANNED', 'READY_TO_SHIP', 'IN_PRODUCTION', 'CONFIRMED'].includes(selected.status) && (
                  <button
                    style={{ ...s.shipBtn, width: '100%', marginTop: 14 }}
                    onClick={() => handleShip(selected.id)}
                    disabled={shippingOrderId === selected.id}
                  >
                    {shippingOrderId === selected.id ? 'Dang cap nhat...' : 'Cap nhat SHIPPED'}
                  </button>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function Info({ label, value }) {
  return (
    <div style={s.infoRow}>
      <span style={s.infoLabel}>{label}</span>
      <span style={s.infoValue}>{value}</span>
    </div>
  );
}

const s = {
  page: { minHeight: '100vh', background: '#f8fafc' },
  container: { maxWidth: 1240, margin: '0 auto', padding: 24 },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 16, marginBottom: 20, flexWrap: 'wrap' },
  h1: { margin: 0, fontSize: 22, color: '#1e3a5f' },
  sub: { margin: '6px 0 0', fontSize: 13, color: '#64748b' },
  summaryWrap: { display: 'flex', gap: 10 },
  summaryCard: { background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, padding: '12px 16px', minWidth: 110 },
  summaryCardWarn: { background: '#fff7ed', border: '1px solid #fdba74', borderRadius: 12, padding: '12px 16px', minWidth: 110 },
  summaryLabel: { fontSize: 12, color: '#64748b' },
  summaryValue: { marginTop: 4, fontSize: 22, fontWeight: 700, color: '#1e3a5f' },
  toast: { padding: '10px 16px', borderRadius: 8, marginBottom: 16, fontSize: 13 },
  layout: { display: 'flex', gap: 20, alignItems: 'flex-start' },
  list: { flex: 1, display: 'flex', flexDirection: 'column', gap: 12 },
  center: { textAlign: 'center', padding: 48, color: '#94a3b8' },
  empty: { background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, padding: 48, textAlign: 'center', color: '#94a3b8' },
  card: { background: '#fff', borderRadius: 12, border: '1px solid #e2e8f0', padding: 16, cursor: 'pointer' },
  cardTop: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  orderId: { fontSize: 15, fontWeight: 700, color: '#1e3a5f' },
  badge: { padding: '3px 10px', borderRadius: 20, fontSize: 11, fontWeight: 600 },
  cardMid: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 13, color: '#475569', marginBottom: 6 },
  cardBottom: { fontSize: 12, color: '#94a3b8' },
  shipBtn: { marginTop: 12, background: '#0f766e', color: '#fff', border: 'none', borderRadius: 8, padding: '10px 14px', fontWeight: 600, cursor: 'pointer', fontSize: 13 },
  detail: { width: 380, background: '#fff', borderRadius: 12, border: '1px solid #e2e8f0', position: 'sticky', top: 72, maxHeight: 'calc(100vh - 100px)', overflowY: 'auto' },
  detailHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 18px', borderBottom: '1px solid #f1f5f9' },
  detailTitle: { margin: 0, fontSize: 15, color: '#1e3a5f' },
  closeBtn: { background: 'none', border: 'none', fontSize: 16, cursor: 'pointer', color: '#94a3b8' },
  detailBody: { padding: 18 },
  infoRow: { display: 'flex', justifyContent: 'space-between', gap: 12, padding: '8px 0', borderBottom: '1px solid #f8fafc', fontSize: 13 },
  infoLabel: { color: '#64748b' },
  infoValue: { color: '#1e293b', fontWeight: 500, textAlign: 'right' },
  subTitle: { margin: '16px 0 8px', fontSize: 13, color: '#475569' },
  payBox: { background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: 10, padding: 12 },
  payEmpty: { background: '#f8fafc', border: '1px dashed #cbd5e1', color: '#64748b', borderRadius: 10, padding: 12, fontSize: 13 },
  payActions: { display: 'flex', gap: 8, marginTop: 10 },
  okBtn: { flex: 1, background: '#15803d', color: '#fff', border: 'none', borderRadius: 7, padding: '8px 10px', cursor: 'pointer', fontSize: 12 },
  failBtn: { flex: 1, background: '#fff', color: '#b91c1c', border: '1px solid #fecaca', borderRadius: 7, padding: '8px 10px', cursor: 'pointer', fontSize: 12 },
  line: { display: 'flex', justifyContent: 'space-between', gap: 12, padding: '8px 0', borderBottom: '1px solid #f8fafc', fontSize: 13 },
  lineBlock: { border: '1px solid #e2e8f0', borderRadius: 10, padding: 12, marginBottom: 12, background: '#f8fafc' },
  lineName: { fontWeight: 500, color: '#1e293b' },
  lineMeta: { fontSize: 11, color: '#94a3b8', marginTop: 2 },
  lineMetaOk: { fontSize: 11, color: '#15803d', marginTop: 2, fontWeight: 600 },
  lineMetaFail: { fontSize: 11, color: '#b91c1c', marginTop: 2, fontWeight: 600 },
  responseForm: { marginTop: 10 },
  responseGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 },
  input: { width: '100%', padding: '9px 10px', border: '1px solid #d1d5db', borderRadius: 7, fontSize: 12, boxSizing: 'border-box' },
  textarea: { width: '100%', marginTop: 8, padding: '9px 10px', border: '1px solid #d1d5db', borderRadius: 7, fontSize: 12, boxSizing: 'border-box', resize: 'vertical' },
  respondBtn: { marginTop: 8, background: '#1d4ed8', color: '#fff', border: 'none', borderRadius: 8, padding: '10px 12px', fontWeight: 600, cursor: 'pointer', fontSize: 12 },
};
