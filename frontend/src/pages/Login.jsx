import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Login({ setAuth }) {
  const [email, setEmail] = useState('test@example.com');
  const [password, setPassword] = useState('password');
  const navigate = useNavigate();

  const handleLogin = (e) => {
    e.preventDefault();
    if (email === 'test@example.com') {
      setAuth(true);
      navigate('/dashboard');
    } else {
      alert('Invalid credentials. Use test@example.com');
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h2>Merchant Login</h2>
        {/* ADDED data-test-id attributes below */}
        <form onSubmit={handleLogin} data-test-id="login-form">
          <input 
            type="email" 
            value={email} 
            onChange={(e) => setEmail(e.target.value)} 
            placeholder="Email"
            data-test-id="email-input"
          />
          <input 
            type="password" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            placeholder="Password"
            data-test-id="password-input"
          />
          <button data-test-id="login-button">Login</button>
        </form>
        <p style={{fontSize:'0.8rem', color:'#666', textAlign:'center', marginTop:'10px'}}>
          Use: test@example.com / any password
        </p>
      </div>
    </div>
  );
}

export default Login;