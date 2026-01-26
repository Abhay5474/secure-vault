import { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';
import { Shield, UserPlus, Lock, Mail } from 'lucide-react';

const Register = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError("Passwords do not match!");
      return;
    }

    try {
      // Assuming your backend has this endpoint. 
      // If not, use Login page for now.
      await axios.post('http://localhost:8080/api/auth/register', {
        email,
        password
      });
      alert("Registration Successful! Please login.");
      navigate('/login');
    } catch (err) {
      setError(err.response?.data || "Registration failed. Try again.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center p-4">
      <div className="bg-gray-800 p-8 rounded-2xl shadow-2xl w-full max-w-md border border-gray-700">
        <div className="flex justify-center mb-6">
          <div className="bg-blue-600/20 p-3 rounded-full">
            <Shield className="w-10 h-10 text-blue-500" />
          </div>
        </div>
        
        <h2 className="text-3xl font-bold text-center text-white mb-2">Create Account</h2>
        <p className="text-gray-400 text-center mb-8">Join the Secure Vault</p>

        {error && (
          <div className="bg-red-500/10 border border-red-500 text-red-500 p-3 rounded-lg mb-4 text-sm text-center">
            {error}
          </div>
        )}

        <form onSubmit={handleRegister} className="space-y-4">
          <div className="relative">
            <Mail className="absolute left-3 top-3 text-gray-500 w-5 h-5" />
            <input 
              type="email" 
              placeholder="Email Address"
              className="w-full bg-gray-900 border border-gray-700 text-white p-3 pl-10 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="relative">
            <Lock className="absolute left-3 top-3 text-gray-500 w-5 h-5" />
            <input 
              type="password" 
              placeholder="Password"
              className="w-full bg-gray-900 border border-gray-700 text-white p-3 pl-10 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <div className="relative">
            <Lock className="absolute left-3 top-3 text-gray-500 w-5 h-5" />
            <input 
              type="password" 
              placeholder="Confirm Password"
              className="w-full bg-gray-900 border border-gray-700 text-white p-3 pl-10 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>

          <button 
            type="submit" 
            className="w-full bg-blue-600 hover:bg-blue-500 text-white font-bold py-3 rounded-lg transition duration-200 flex items-center justify-center gap-2"
          >
            <UserPlus className="w-5 h-5" /> Sign Up
          </button>
        </form>

        <p className="text-center text-gray-400 mt-6 text-sm">
          Already have an account? <Link to="/login" className="text-blue-400 hover:underline">Login</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;