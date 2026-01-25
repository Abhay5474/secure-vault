import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard'; // <--- Import this!

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" />} />
      <Route path="/login" element={<Login />} />
      
      {/* Update this line to use the Component */}
      <Route path="/dashboard" element={<Dashboard />} />
    </Routes>
  );
}

export default App;