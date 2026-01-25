import { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { Shield, FileText, LogOut, Upload, Share2, Download } from 'lucide-react';

const Dashboard = () => {
  const [files, setFiles] = useState([]);
  const navigate = useNavigate();

  // 1. Load Files on Startup
  useEffect(() => {
    fetchFiles();
  }, []);

  const fetchFiles = async () => {
    const token = localStorage.getItem('token');
    
    // Security Check: If no passport, kick them out
    if (!token) {
      navigate('/login');
      return;
    }

    try {
      // 2. Ask Backend for "My Files"
      const response = await axios.get('http://localhost:8080/api/files', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setFiles(response.data);
    } catch (error) {
      console.error("Error fetching files:", error);
      // If token expired, logout
    //   if (error.response && error.response.status === 403) {
    //     handleLogout();
    //   }
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const handleDownload = async (fileId, fileName) => {
    const token = localStorage.getItem('token');
    try {
      const response = await axios.get(`http://localhost:8080/api/files/download/${fileId}`, {
        headers: { Authorization: `Bearer ${token}` },
        responseType: 'blob' // Important: Tell axios this is a file, not text
      });

      // Create a fake link to trigger download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      alert("Download Failed! You might not have permission.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white p-6">
      {/* Navbar */}
      <nav className="flex justify-between items-center mb-10 border-b border-gray-700 pb-4">
        <div className="flex items-center gap-3">
          <Shield className="w-8 h-8 text-blue-500" />
          <h1 className="text-2xl font-bold">Secure Vault</h1>
        </div>
        <button 
          onClick={handleLogout}
          className="flex items-center gap-2 bg-red-600 hover:bg-red-700 px-4 py-2 rounded-lg text-sm transition"
        >
          <LogOut className="w-4 h-4" /> Logout
        </button>
      </nav>

      {/* Main Content */}
      <div className="max-w-6xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-semibold text-gray-300">My Secure Files</h2>
          <button className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg transition">
            <Upload className="w-4 h-4" /> Upload New
          </button>
        </div>

        {/* File Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {files.length === 0 ? (
            <div className="col-span-full text-center py-20 text-gray-500">
              <Shield className="w-16 h-16 mx-auto mb-4 opacity-20" />
              <p>No files found. Upload one to get started.</p>
            </div>
          ) : (
            files.map((file) => (
              <div key={file.id} className="bg-gray-800 p-5 rounded-xl border border-gray-700 hover:border-blue-500/50 transition duration-300 group">
                <div className="flex items-start justify-between mb-4">
                  <div className="p-3 bg-gray-700 rounded-lg">
                    <FileText className="w-8 h-8 text-blue-400" />
                  </div>
                  {/* Action Buttons */}
                  <div className="flex gap-2">
                    <button 
                        onClick={() => handleDownload(file.id, file.fileName)}
                        className="p-2 hover:bg-gray-700 rounded-lg text-gray-400 hover:text-white transition" 
                        title="Download"
                    >
                      <Download className="w-5 h-5" />
                    </button>
                    <button className="p-2 hover:bg-gray-700 rounded-lg text-gray-400 hover:text-white transition" title="Share">
                      <Share2 className="w-5 h-5" />
                    </button>
                  </div>
                </div>
                
                <h3 className="font-semibold truncate mb-1">{file.fileName}</h3>
                <p className="text-sm text-gray-500">{new Date(file.uploadTime).toLocaleDateString()}</p>
                <div className="mt-4 flex items-center gap-2 text-xs text-gray-400 bg-gray-900/50 p-2 rounded">
                  <span className="w-2 h-2 rounded-full bg-green-500"></span>
                  AES-256 Encrypted
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;