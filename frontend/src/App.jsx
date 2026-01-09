import { BrowserRouter, Routes, Route, Navigate, Link, useLocation } from 'react-router-dom';
import { useState } from 'react';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Transactions from './pages/Transactions';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Helper for active links
  const NavLink = ({ to, icon, children }) => {
    const location = useLocation();
    const isActive = location.pathname === to;
    return (
      <Link to={to} className={isActive ? 'active' : ''}>
        <span>{icon}</span>
        {children}
      </Link>
    );
  };

  const ProtectedRoute = ({ children }) => {
    if (!isAuthenticated) return <Navigate to="/login" />;
    
    return (
      <div className="layout">
        {/* Light Sidebar */}
        <aside className="sidebar">
          <div className="sidebar-header">
            <h2>Gateway</h2>
          </div>
          <nav className="sidebar-nav">
            <NavLink to="/dashboard" icon="📊">Overview</NavLink>
            <NavLink to="/transactions" icon="💳">Transactions</NavLink>
          </nav>
          <div className="sidebar-footer">
            <button onClick={() => setIsAuthenticated(false)}>Sign Out</button>
          </div>
        </aside>

        {/* Main Content */}
        <main className="main-content">
          <div className="page-content">
            {children}
          </div>
        </main>
      </div>
    );
  };

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login setAuth={setIsAuthenticated} />} />
        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/transactions" element={<ProtectedRoute><Transactions /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/dashboard" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;