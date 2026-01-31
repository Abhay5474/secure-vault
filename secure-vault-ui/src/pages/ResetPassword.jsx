import { useState } from 'react';
import api from '../api';
import { Lock, Loader2, CheckCircle } from 'lucide-react';
import { useSearchParams, useNavigate } from 'react-router-dom';

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();

  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password !== confirm) {
      setError("Passwords do not match");
      return;
    }

    setLoading(true);
    setError('');

    try {
      await api.post(`/api/auth/reset-password`, null, {
        params: { token, newPassword: password }
      });
      setSuccess(true);
      setTimeout(() => navigate('/login'), 3000);
    } catch (err) {
      setError(err.response?.data || "Invalid or Expired Token");
    } finally {
      setLoading(false);
    }
  };

  if (!token) return <div className="text-white text-center mt-20">Invalid Link</div>;

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center p-4">
      <div className="bg-gray-800 p-8 rounded-xl shadow-2xl w-full max-w-md border border-gray-700">
        
        {success ? (
          <div className="text-center">
            <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-white mb-2">Password Changed!</h2>
            <p className="text-gray-400">Redirecting to login...</p>
          </div>
        ) : (
          <>
            <h2 className="text-2xl font-bold text-white mb-6">Set New Password</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="relative">
                <Lock className="absolute left-3 top-3 w-5 h-5 text-gray-500" />
                <input 
                  type="password" 
                  placeholder="New Password" 
                  className="w-full bg-gray-900 border border-gray-600 text-white py-2 pl-10 pr-4 rounded-lg focus:border-blue-500 outline-none"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
              
              <div className="relative">
                <Lock className="absolute left-3 top-3 w-5 h-5 text-gray-500" />
                <input 
                  type="password" 
                  placeholder="Confirm Password" 
                  className="w-full bg-gray-900 border border-gray-600 text-white py-2 pl-10 pr-4 rounded-lg focus:border-blue-500 outline-none"
                  value={confirm}
                  onChange={(e) => setConfirm(e.target.value)}
                  required
                />
              </div>

              {error && <p className="text-red-400 text-sm">{error}</p>}

              <button 
                type="submit" 
                disabled={loading}
                className="w-full bg-blue-600 hover:bg-blue-500 text-white py-2 rounded-lg font-bold transition flex justify-center"
              >
                {loading ? <Loader2 className="animate-spin" /> : "Update Password"}
              </button>
            </form>
          </>
        )}
      </div>
    </div>
  );
};

export default ResetPassword;