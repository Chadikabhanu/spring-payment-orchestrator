import { useEffect, useState } from 'react';
import axios from 'axios';

const API_URL = 'http://localhost:8000/api/v1';
const API_KEY = 'key_test_abc123';
const API_SECRET = 'secret_test_xyz789';

function Dashboard() {
  const [stats, setStats] = useState({ count: 0, amount: 0, successRate: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await axios.get(`${API_URL}/payments`, {
          headers: { 'X-Api-Key': API_KEY, 'X-Api-Secret': API_SECRET }
        });
        
        const payments = res.data;
        const totalCount = payments.length;
        const successCount = payments.filter(p => p.status === 'success').length;
        const totalAmount = payments.filter(p => p.status === 'success').reduce((sum, p) => sum + p.amount, 0);

        setStats({
          count: totalCount,
          amount: totalAmount,
          successRate: totalCount > 0 ? Math.round((successCount / totalCount) * 100) : 0
        });
        setLoading(false);
      } catch (err) {
        console.error("Failed to fetch stats", err);
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) return <div>Loading Stats...</div>;

  return (
    <div data-test-id="dashboard">
      <h1>Overview</h1>
      
      {/* API Credentials Card */}
      <div className="card" data-test-id="api-credentials">
        <h3 style={{margin:'0 0 15px 0'}}>API Credentials</h3>
        <div style={{display:'grid', gridTemplateColumns:'100px 1fr', gap:'10px', alignItems:'center'}}>
          <strong>API Key:</strong> 
          <code style={{background:'#f1f5f9', padding:'5px'}} data-test-id="api-key">{API_KEY}</code>
          <strong>API Secret:</strong> 
          <code style={{background:'#f1f5f9', padding:'5px'}} data-test-id="api-secret">{API_SECRET}</code>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="stats-grid" data-test-id="stats-container">
        <div className="stat-card">
          <div className="stat-label">Total Transactions</div>
          <div className="stat-value" data-test-id="total-transactions">{stats.count}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Total Volume</div>
          <div className="stat-value" data-test-id="total-amount">₹{(stats.amount / 100).toFixed(2)}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Success Rate</div>
          <div className="stat-value" data-test-id="success-rate">{stats.successRate}%</div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;