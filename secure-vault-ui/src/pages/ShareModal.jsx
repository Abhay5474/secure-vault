import { useState, useEffect } from 'react';
import axios from 'axios';
import { X, UserPlus, Users, Trash2, AlertCircle } from 'lucide-react';

const ShareModal = ({ isOpen, onClose, fileId, fileName }) => {
  const [email, setEmail] = useState('');
  const [sharedUsers, setSharedUsers] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Fetch list of users this file is shared with
  useEffect(() => {
    if (isOpen && fileId) {
      fetchShares();
    }
  }, [isOpen, fileId]);

  const fetchShares = async () => {
    const token = localStorage.getItem('token');
    try {
      const response = await axios.get(`http://localhost:8080/api/files/shares/${fileId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSharedUsers(response.data);
    } catch (err) {
      console.error("Failed to load shares");
    }
  };
  const handleRevoke = async (targetEmail) => {
    if(!window.confirm(`Are you sure you want to revoke access for ${targetEmail}?`)) return;

    const token = localStorage.getItem('token');
    try {
      await axios.delete(`http://localhost:8080/api/files/revoke`, {
        headers: { Authorization: `Bearer ${token}` },
        params: { fileId, email: targetEmail }
      });
      // Refresh list
      fetchShares();
    } catch (err) {
      alert("Failed to revoke access.");
    }
  };

  const handleShare = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const token = localStorage.getItem('token');
    try {
      await axios.post(`http://localhost:8080/api/files/share`, null, {
        params: { fileId, email: email },
        headers: { Authorization: `Bearer ${token}` }
      });

      setSuccess(`Successfully shared with ${email}`);
      setEmail('');
      fetchShares(); // Refresh the list immediately
    } catch (err) {
        // Show the exact error from Backend (e.g. "User not registered")
        setError(err.response?.data?.message || err.response?.data || "Share failed");
    }
  };

  if (!isOpen) return null;

 return (
    <div className="fixed inset-0 bg-black/80 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
      <div className="bg-gray-800 rounded-xl w-full max-w-md border border-gray-700 shadow-2xl overflow-hidden">
        
        {/* Header */}
        <div className="bg-gray-900 p-4 flex justify-between items-center border-b border-gray-700">
          <h3 className="text-white font-bold flex items-center gap-2">
            <Users className="w-5 h-5 text-blue-400" />
            Manage Access
          </h3>
          <button onClick={onClose} className="text-gray-400 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6">
          <p className="text-gray-400 text-sm mb-4">
            File: <span className="text-white font-mono">{fileName}</span>
          </p>

          {/* Share Form */}
          <form onSubmit={handleShare} className="flex gap-2 mb-6">
            <input 
              type="email" 
              placeholder="Enter user email..." 
              className="flex-1 bg-gray-900 border border-gray-600 text-white rounded-lg px-3 py-2 text-sm focus:border-blue-500 outline-none"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <button 
              type="submit" 
              className="bg-blue-600 hover:bg-blue-500 text-white px-4 py-2 rounded-lg text-sm font-semibold transition"
            >
              <UserPlus className="w-4 h-4" />
            </button>
          </form>

          {/* Messages */}
          {error && (
            <div className="bg-red-500/20 text-red-400 p-3 rounded-lg text-xs mb-4 flex items-center gap-2">
              <AlertCircle className="w-4 h-4" /> {error}
            </div>
          )}
          {success && (
            <div className="bg-green-500/20 text-green-400 p-3 rounded-lg text-xs mb-4">
              {success}
            </div>
          )}

          {/* Access List */}
          <div className="border-t border-gray-700 pt-4">
            <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-3">People with Access</h4>
            
            <div className="space-y-2 max-h-40 overflow-y-auto pr-2">
              {sharedUsers.length === 0 ? (
                <p className="text-gray-600 text-xs italic text-center py-2">Private file. Only you have access.</p>
              ) : (
                sharedUsers.map((userEmail, index) => (
                  <div key={index} className="flex justify-between items-center bg-gray-700/50 p-2 rounded-lg group">
                    <span className="text-sm text-gray-300">{userEmail}</span>
                    
                    <div className="flex items-center gap-2">
                        {/* Status Badge */}
                        <span className="text-[10px] bg-green-500/20 text-green-400 px-2 py-0.5 rounded border border-green-500/30">
                            Active
                        </span>
                        
                        {/* 🛑 REVOKE BUTTON (Trash Icon) */}
                        <button 
                            onClick={() => handleRevoke(userEmail)}
                            className="text-gray-500 hover:text-red-500 transition p-1 hover:bg-red-500/10 rounded"
                            title="Revoke Access"
                        >
                            <Trash2 className="w-4 h-4" />
                        </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};

export default ShareModal;