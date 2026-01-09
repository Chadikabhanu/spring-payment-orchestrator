import { useState, useEffect } from 'react';
import axios from 'axios';

const API_URL = 'http://localhost:8000/api/v1';
const API_KEY = 'key_test_abc123';
const API_SECRET = 'secret_test_xyz789';

function App() {
  const [orderId, setOrderId] = useState(null);
  const [order, setOrder] = useState(null);
  const [method, setMethod] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [paymentStatus, setPaymentStatus] = useState(null);
  const [paymentId, setPaymentId] = useState(null);

  // Form Inputs
  const [vpa, setVpa] = useState('');
  const [cardNumber, setCardNumber] = useState('');
  const [expiry, setExpiry] = useState('');
  const [cvv, setCvv] = useState('');
  const [holder, setHolder] = useState('');

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const id = params.get('order_id');
    if (id) {
      setOrderId(id);
      fetchOrder(id);
    } else {
      setError("No Order ID provided");
    }
  }, []);

  const fetchOrder = async (id) => {
    try {
      const res = await axios.get(`${API_URL}/orders/${id}`, {
        headers: { 'X-Api-Key': API_KEY, 'X-Api-Secret': API_SECRET }
      });
      setOrder(res.data);
    } catch (err) {
      setError("Invalid Order ID or Network Error");
    }
  };

  const handlePayment = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    const payload = { order_id: orderId, method: method };
    if (method === 'upi') payload.vpa = vpa;
    else {
      const [expMonth, expYear] = expiry.split('/');
      payload.card = { number: cardNumber, expiry_month: expMonth, expiry_year: expYear, cvv, holder_name: holder };
    }

    try {
      const res = await axios.post(`${API_URL}/payments`, payload, {
        headers: { 'X-Api-Key': API_KEY, 'X-Api-Secret': API_SECRET }
      });
      setPaymentId(res.data.id);
      setPaymentStatus('processing');
      pollStatus(res.data.id);
    } catch (err) {
      setLoading(false);
      setError(err.response?.data?.detail?.error?.description || "Payment Failed");
    }
  };

  const pollStatus = async (pid) => {
    const interval = setInterval(async () => {
      try {
        const res = await axios.get(`${API_URL}/payments/${pid}`, {
          headers: { 'X-Api-Key': API_KEY, 'X-Api-Secret': API_SECRET }
        });
        if (res.data.status === 'success' || res.data.status === 'failed') {
          clearInterval(interval);
          setPaymentStatus(res.data.status);
          setLoading(false);
        }
      } catch (err) {
        clearInterval(interval);
        setLoading(false);
      }
    }, 2000);
  };

  // --- RENDER SECTION (Fixed) ---
  if (!orderId) return <div className="card error">{error || "Missing Order ID"}</div>;
  
  // FIX: Check for error BEFORE checking for order, so we see the error message
  if (error) return <div className="card error">Error: {error}</div>;
  
  if (!order) return <div className="card">Loading...</div>;
  
  if (paymentStatus === 'success') return <div className="card success" data-test-id="success-state"><h2>Payment Successful!</h2><p>ID: <span data-test-id="payment-id">{paymentId}</span></p></div>;

  return (
    <div className="card" data-test-id="checkout-container">
      <div data-test-id="order-summary">
        <h2>Pay ₹{(order.amount / 100).toFixed(2)}</h2>
        <p>Order ID: <span data-test-id="order-id">{order.id}</span></p>
      </div>

      {!loading ? (
        <>
          <div className="method-selector">
            <button className={`method-btn ${method === 'upi' ? 'active' : ''}`} onClick={() => setMethod('upi')} data-test-id="method-upi">UPI</button>
            <button className={`method-btn ${method === 'card' ? 'active' : ''}`} onClick={() => setMethod('card')} data-test-id="method-card">Card</button>
          </div>
          
          {method === 'upi' && (
            <form onSubmit={handlePayment} data-test-id="upi-form">
              <input placeholder="user@bank" value={vpa} onChange={e => setVpa(e.target.value)} required data-test-id="vpa-input" />
              <button type="submit" data-test-id="pay-button">Pay Now</button>
            </form>
          )}

          {method === 'card' && (
            <form onSubmit={handlePayment} data-test-id="card-form">
              <input placeholder="Card Number" value={cardNumber} onChange={e => setCardNumber(e.target.value)} required data-test-id="card-number-input" />
              <div style={{display:'flex', gap:'10px'}}>
                <input placeholder="MM/YY" value={expiry} onChange={e => setExpiry(e.target.value)} required data-test-id="expiry-input" />
                <input placeholder="CVV" value={cvv} onChange={e => setCvv(e.target.value)} required data-test-id="cvv-input" />
              </div>
              <input placeholder="Card Holder" value={holder} onChange={e => setHolder(e.target.value)} required data-test-id="cardholder-name-input" />
              <button type="submit" data-test-id="pay-button">Pay Now</button>
            </form>
          )}
        </>
      ) : (
        <div data-test-id="processing-state"><p>Processing payment...</p></div>
      )}
      {error && <div className="error">{error}</div>}
    </div>
  );
}

export default App;