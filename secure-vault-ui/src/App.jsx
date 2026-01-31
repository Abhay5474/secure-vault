import { HashRouter, Routes, Route, Navigate } from 'react-router-dom'; // 1. Import HashRouter
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import DecryptPage from './pages/DecryptPage';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';

function App() {
  return (
    // 2. Use HashRouter instead of Router/BrowserRouter
    <HashRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/decrypt" element={<DecryptPage />} />
        
        {/* Password Reset Routes */}
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        
        <Route path="/" element={<Navigate to="/login" />} />
      </Routes>
    </HashRouter>
  );
}

export default App;