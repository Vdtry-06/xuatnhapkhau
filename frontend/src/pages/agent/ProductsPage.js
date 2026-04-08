import { useState, useEffect } from 'react';
import api from '../../services/api';
import Navbar from '../../components/Navbar';
import { useAuth } from '../../context/AuthContext';

export default function AgentProductsPage() {
  const { user } = useAuth();
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [shippingAddress, setShippingAddress] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [msg, setMsg] = useState({ text:'', type:'success' });
  const [showCart, setShowCart] = useState(false);

  useEffect(() => {
    api.get('/api/products')
      .then(res => setProducts(res.data || []))
      .catch(() => flash('Lỗi tải sản phẩm', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const flash = (text, type='success') => { setMsg({ text, type }); setTimeout(() => setMsg({ text:'' }), 4000); };

  const addToCart = (product) => {
    setCart(prev => {
      const exists = prev.find(i => i.productId === product.id);
      if (exists) return prev.map(i => i.productId === product.id ? { ...i, quantity: i.quantity + 1 } : i);
      return [...prev, { productId: product.id, productName: product.name,
        unitPrice: product.exportPrice, quantity: 1 }];
    });
  };

  const updateQty = (productId, qty) => {
    if (qty < 1) return removeFromCart(productId);
    setCart(prev => prev.map(i => i.productId === productId ? { ...i, quantity: qty } : i));
  };

  const removeFromCart = (productId) => setCart(prev => prev.filter(i => i.productId !== productId));

  const totalAmount = cart.reduce((sum, i) => sum + i.unitPrice * i.quantity, 0);

  const handleOrder = async () => {
    if (!shippingAddress.trim()) { flash('Vui lòng nhập địa chỉ giao hàng', 'error'); return; }
    if (cart.length === 0) { flash('Giỏ hàng trống', 'error'); return; }
    setSubmitting(true);
    try {
      await api.post('/api/orders', {
        agentId: user.agentId,
        shippingAddress,
        orderlines: cart.map(i => ({ productId: i.productId, quantity: i.quantity }))
      });
      flash('Đặt hàng thành công!');
      setCart([]); setShippingAddress(''); setShowCart(false);
    } catch (err) { flash(err.response?.data?.message || 'Lỗi đặt hàng', 'error'); }
    finally { setSubmitting(false); }
  };

  const cartCount = cart.reduce((s, i) => s + i.quantity, 0);

  return (
    <div style={s.page}>
      <Navbar />
      <div style={s.container}>
        <div style={s.header}>
          <h1 style={s.h1}>Catalogue sản phẩm</h1>
          <button style={s.cartBtn} onClick={() => setShowCart(!showCart)}>
            🛒 Giỏ hàng {cartCount > 0 && <span style={s.cartBadge}>{cartCount}</span>}
          </button>
        </div>

        {msg.text && (
          <div style={{ ...s.toast, background: msg.type==='error'?'#fef2f2':'#ecfdf5',
            color: msg.type==='error'?'#dc2626':'#065f46',
            border: `1px solid ${msg.type==='error'?'#fecaca':'#6ee7b7'}` }}>
            {msg.text}
          </div>
        )}

        <div style={s.layout}>
          <div style={s.products}>
            {loading ? <div style={s.center}>Đang tải sản phẩm...</div> : (
              <div style={s.grid}>
                {products.map(p => {
                  const inCart = cart.find(i => i.productId === p.id);
                  return (
                    <div key={p.id} style={s.card}>
                      <div style={s.cardCat}>{p.categoryName}</div>
                      <h3 style={s.cardName}>{p.name}</h3>
                      <code style={s.sku}>{p.sku}</code>
                      <p style={s.cardDesc}>{p.description || 'Không có mô tả'}</p>
                      <div style={s.cardFooter}>
                        <div>
                          <div style={s.price}>{Number(p.exportPrice).toLocaleString('vi-VN')}₫</div>
                          <div style={{ fontSize:11, color: p.stockQuantity<10?'#dc2626':'#64748b' }}>
                            Còn: {p.stockQuantity} {p.stockQuantity<10?'⚠️':''}
                          </div>
                        </div>
                        {inCart ? (
                          <div style={s.qtyControl}>
                            <button style={s.qtyBtn} onClick={() => updateQty(p.id, inCart.quantity - 1)}>−</button>
                            <span style={s.qty}>{inCart.quantity}</span>
                            <button style={s.qtyBtn} onClick={() => updateQty(p.id, inCart.quantity + 1)}>+</button>
                          </div>
                        ) : (
                          <button style={s.addBtn} onClick={() => addToCart(p)}
                            disabled={p.stockQuantity === 0}>
                            {p.stockQuantity === 0 ? 'Hết hàng' : 'Thêm vào giỏ'}
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {showCart && (
            <div style={s.sidebar}>
              <h3 style={s.sideTitle}>Giỏ hàng ({cartCount} sản phẩm)</h3>
              {cart.length === 0 ? <p style={s.emptyCart}>Chưa có sản phẩm nào</p> : (
                <>
                  {cart.map(i => (
                    <div key={i.productId} style={s.cartItem}>
                      <div style={{ flex:1 }}>
                        <div style={s.cartName}>{i.productName}</div>
                        <div style={s.cartPrice}>{Number(i.unitPrice).toLocaleString('vi-VN')}₫/sp</div>
                      </div>
                      <div style={s.qtyControl}>
                        <button style={s.qtyBtn} onClick={() => updateQty(i.productId, i.quantity-1)}>−</button>
                        <span style={s.qty}>{i.quantity}</span>
                        <button style={s.qtyBtn} onClick={() => updateQty(i.productId, i.quantity+1)}>+</button>
                      </div>
                    </div>
                  ))}
                  <div style={s.total}>
                    Tổng: <b>{Number(totalAmount).toLocaleString('vi-VN')}₫</b>
                  </div>
                  <label style={s.label}>Địa chỉ giao hàng *</label>
                  <textarea style={s.textarea} rows={3} value={shippingAddress}
                    onChange={e => setShippingAddress(e.target.value)}
                    placeholder="Nhập địa chỉ giao hàng..." />
                  <button style={s.orderBtn} onClick={handleOrder} disabled={submitting}>
                    {submitting ? 'Đang đặt hàng...' : '✓ Xác nhận đặt hàng'}
                  </button>
                </>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

const s = {
  page: { minHeight:'100vh', background:'#f8fafc' },
  container: { maxWidth:1300, margin:'0 auto', padding:24 },
  header: { display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:20 },
  h1: { margin:0, fontSize:22, color:'#1e3a5f' },
  cartBtn: { background:'#1e3a5f', color:'#fff', border:'none', borderRadius:8,
    padding:'10px 20px', fontWeight:600, cursor:'pointer', fontSize:14, position:'relative' },
  cartBadge: { background:'#ef4444', color:'#fff', borderRadius:10, fontSize:11,
    padding:'1px 6px', marginLeft:6 },
  toast: { padding:'10px 16px', marginBottom:16, borderRadius:8, fontSize:13 },
  layout: { display:'flex', gap:20, alignItems:'flex-start' },
  products: { flex:1 },
  grid: { display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(220px, 1fr))', gap:16 },
  card: { background:'#fff', borderRadius:12, border:'1px solid #e2e8f0', padding:16, display:'flex', flexDirection:'column' },
  cardCat: { fontSize:11, color:'#3b82f6', fontWeight:600, marginBottom:6 },
  cardName: { margin:'0 0 4px', fontSize:14, color:'#1e293b', fontWeight:600 },
  sku: { background:'#f1f5f9', color:'#64748b', padding:'2px 6px', borderRadius:4, fontSize:11, marginBottom:8, display:'inline-block' },
  cardDesc: { fontSize:12, color:'#94a3b8', flex:1, margin:'0 0 12px' },
  cardFooter: { display:'flex', justifyContent:'space-between', alignItems:'flex-end' },
  price: { fontSize:16, fontWeight:700, color:'#1e3a5f' },
  addBtn: { background:'#1e3a5f', color:'#fff', border:'none', borderRadius:7,
    padding:'7px 12px', fontSize:12, cursor:'pointer', fontWeight:500 },
  qtyControl: { display:'flex', alignItems:'center', gap:6 },
  qtyBtn: { width:28, height:28, border:'1px solid #d1d5db', borderRadius:6, background:'#fff',
    cursor:'pointer', fontSize:16, display:'flex', alignItems:'center', justifyContent:'center' },
  qty: { fontSize:14, fontWeight:600, minWidth:20, textAlign:'center' },
  sidebar: { width:320, background:'#fff', borderRadius:12, border:'1px solid #e2e8f0', padding:18 },
  sideTitle: { margin:'0 0 16px', fontSize:15, color:'#1e3a5f' },
  emptyCart: { color:'#94a3b8', fontSize:13, textAlign:'center', padding:20 },
  cartItem: { display:'flex', alignItems:'center', gap:8, padding:'10px 0', borderBottom:'1px solid #f1f5f9' },
  cartName: { fontSize:13, fontWeight:500 },
  cartPrice: { fontSize:12, color:'#64748b' },
  total: { fontSize:14, textAlign:'right', padding:'12px 0', borderTop:'2px solid #e2e8f0', marginTop:8 },
  label: { display:'block', fontSize:13, fontWeight:500, marginBottom:6, marginTop:12 },
  textarea: { width:'100%', padding:'9px 12px', border:'1px solid #d1d5db', borderRadius:7,
    fontSize:13, boxSizing:'border-box', resize:'vertical' },
  orderBtn: { width:'100%', marginTop:12, padding:12, background:'#16a34a', color:'#fff',
    border:'none', borderRadius:8, fontWeight:600, cursor:'pointer', fontSize:14 },
  center: { textAlign:'center', padding:40, color:'#94a3b8' },
};
