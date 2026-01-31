import { useState } from 'react';
import api from '../api';
import { Mail, ArrowLeft, Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');

    try {
      await api.post(`/api/auth/forgot-password`, null, {
        params: { email }
      });
      setMessage("Link sent! Check your inbox.");
    } catch (err) {
      setError(err.response?.data || "User not found.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center p-4">
      <div className="bg-gray-800 p-8 rounded-xl shadow-2xl w-full max-w-md border border-gray-700">
        <h2 className="text-2xl font-bold text-white mb-2">Reset Password</h2>
        <p className="text-gray-400 mb-6 text-sm">Enter your email to receive a secure link.</p>

        {message ? (
          <div className="bg-green-500/20 text-green-400 p-4 rounded-lg text-center">
            {message}
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="relative">
              <Mail className="absolute left-3 top-3 w-5 h-5 text-gray-500" />
              <input 
                type="email" 
                placeholder="Enter your email" 
                className="w-full bg-gray-900 border border-gray-600 text-white py-2 pl-10 pr-4 rounded-lg focus:border-blue-500 outline-none"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            {error && <p className="text-red-400 text-sm">{error}</p>}

            <button 
              type="submit" 
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-500 text-white py-2 rounded-lg font-bold transition flex justify-center"
            >
              {loading ? <Loader2 className="animate-spin" /> : "Send Reset Link"}
            </button>
          </form>
        )}

        <button 
          onClick={() => navigate('/login')}
          className="mt-6 flex items-center gap-2 text-gray-400 hover:text-white text-sm mx-auto"
        >
          <ArrowLeft className="w-4 h-4" /> Back to Login
        </button>
      </div>
    </div>
  );
};

export default ForgotPassword;