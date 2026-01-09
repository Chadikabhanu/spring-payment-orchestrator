import { useEffect, useState } from 'react';
import axios from 'axios';

const API_URL = 'http://localhost:8000/api/v1';
const API_KEY = 'key_test_abc123';
const API_SECRET = 'secret_test_xyz789';

function Transactions() {
  const [payments, setPayments] = useState([]);

  useEffect(() => {
    const fetchPayments = async () => {
      try {
        const res = await axios.get(`${API_URL}/payments`, {
          headers: { 'X-Api-Key': API_KEY, 'X-Api-Secret': API_SECRET }
        });
        setPayments(res.data);
      } catch (err) {
        console.error(err);
      }
    };
    fetchPayments();
  }, []);

  return (
    <div>
      <h1>Transactions</h1>
      <div className="card">
        <table data-test-id="transactions-table">
          <thead>
            <tr>
              <th>Payment ID</th>
              <th>Amount</th>
              <th>Method</th>
              <th>Status</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            {payments.map(p => (
              <tr key={p.id} data-test-id="transaction-row">
                <td data-test-id="payment-id" style={{fontFamily:'monospace'}}>{p.id}</td>
                <td data-test-id="amount">₹{(p.amount / 100).toFixed(2)}</td>
                <td data-test-id="method">{p.method}</td>
                <td>
                  <span className={`status-${p.status}`} data-test-id="status">
                    {p.status}
                  </span>
                </td>
                <td data-test-id="created-at">{new Date(p.created_at).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default Transactions;