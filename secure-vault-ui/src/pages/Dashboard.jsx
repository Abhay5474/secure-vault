import { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { Shield, FileText, LogOut, Upload, Share2, Eye, Lock, Trash2 } from 'lucide-react';
import UploadModal from './UploadModal';
import SecurePDFViewer from './SecurePDFViewer';
import ShareModal from './ShareModal';

const Dashboard = () => {
  const [files, setFiles] = useState([]);
  const [isUploadOpen, setIsUploadOpen] = useState(false);
  const [shareModalOpen, setShareModalOpen] = useState(false);
  const [activeFile, setActiveFile] = useState({ id: null, name: '' });
  
  // Viewer State
  const [viewFile, setViewFile] = useState(null);
  const [viewFileName, setViewFileName] = useState('');
  
  const navigate = useNavigate();

  // 1. Load Files on Startup
  useEffect(() => {
    fetchFiles();
  }, []);

  const fetchFiles = async () => {
    const token = localStorage.getItem('token');
    
    // Security Check
    if (!token) {
      navigate('/login');
      return;
    }

    try {
      // Fetch only files owned by the user
      const response = await axios.get('http://localhost:8080/api/files', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setFiles(response.data);
    } catch (error) {
      console.error("Error fetching files:", error);
      if (error.response && error.response.status === 403) {
         handleLogout();
      }
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  // --- ACTIONS ---

  // A. VIEW (Decrypt & Stream)
  const handleView = async (fileId, fileName) => {
    const token = localStorage.getItem('token');
    try {
      const response = await axios.get(`http://localhost:8080/api/files/download/${fileId}`, {
        headers: { Authorization: `Bearer ${token}` },
        responseType: 'blob' 
      });

      const fileUrl = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      
      setViewFile(fileUrl);
      setViewFileName(fileName);
      
    } catch (error) {
      console.error(error);
      alert("View Failed! Access Denied or File Corrupted.");
    }
  };

  // B. DOWNLOAD .SNTL (Encrypted Artifact)
  const handleDownloadSntl = async (fileId, fileName) => {
    const token = localStorage.getItem('token');
    try {
      const response = await axios.get(`http://localhost:8080/api/files/download-sntl/${fileId}`, {
        headers: { Authorization: `Bearer ${token}` },
        responseType: 'blob' 
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      
      // Manual stamping of ID in frontend for robustness
      const stampedName = `${fileName}_id_${fileId}.sntl`;
      
      link.setAttribute('download', stampedName);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      alert("Download Failed! Permission denied.");
    }
  };

  // C. OPEN SHARE MODAL
  const openShareModal = (fileId, fileName) => {
    setActiveFile({ id: fileId, name: fileName });
    setShareModalOpen(true);
  };

  // D. DELETE FILE (NEW)
  const handleDelete = async (fileId) => {
    if (!window.confirm("⚠️ ARE YOU SURE?\n\nThis will permanently delete the file and revoke access for EVERYONE.\nThis action cannot be undone.")) {
      return;
    }

    const token = localStorage.getItem('token');
    try {
      await axios.delete(`http://localhost:8080/api/files/delete/${fileId}`, {
         headers: { Authorization: `Bearer ${token}` }
      });

      // Remove from UI immediately
      setFiles(prevFiles => prevFiles.filter(f => f.id !== fileId));
      
    } catch (error) {
      alert("Delete failed: " + (error.response?.data || "Server Error"));
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white p-6 font-sans">
      {/* Navbar */}
      <nav className="flex justify-between items-center mb-10 border-b border-gray-700 pb-4">
        <div className="flex items-center gap-3">
          <div className="bg-blue-600/20 p-2 rounded-lg border border-blue-500/50">
            <Shield className="w-8 h-8 text-blue-500" />
          </div>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">Secure Vault</h1>
            <p className="text-xs text-gray-400 font-mono">DRM PROTECTED SYSTEM</p>
          </div>
        </div>
        <button 
          onClick={handleLogout}
          className="flex items-center gap-2 bg-red-600/20 hover:bg-red-600 text-red-400 hover:text-white border border-red-600/50 px-4 py-2 rounded-lg text-sm transition"
        >
          <LogOut className="w-4 h-4" /> Logout
        </button>
      </nav>

      {/* Main Content */}
      <div className="max-w-6xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <h2 className="text-xl font-semibold text-gray-300 border-l-4 border-blue-500 pl-3">
            My Encrypted Files (Owner)
          </h2>
          
          <div className="flex gap-3">
             <button 
                onClick={() => navigate('/decrypt')}
                className="flex items-center gap-2 bg-green-600 hover:bg-green-500 text-white px-5 py-2.5 rounded-lg shadow-lg shadow-green-900/20 transition transform hover:scale-105"
             >
                <Shield className="w-4 h-4" /> 
                Decrypt External .sntl
             </button>

             <button 
                onClick={() => setIsUploadOpen(true)}
                className="flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white px-5 py-2.5 rounded-lg shadow-lg shadow-blue-900/20 transition transform hover:scale-105"
             >
                <Upload className="w-4 h-4" /> 
                Encrypt New File
             </button>
          </div>
        </div>

        {/* File Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {files.length === 0 ? (
            <div className="col-span-full text-center py-24 text-gray-500 border-2 border-dashed border-gray-800 rounded-xl">
              <Lock className="w-16 h-16 mx-auto mb-4 opacity-20" />
              <p className="text-lg">Vault is empty.</p>
              <p className="text-sm opacity-60">Upload a file to generate a .sntl token</p>
            </div>
          ) : (
            files.map((file) => (
              <div key={file.id} className="bg-gray-800 rounded-xl border border-gray-700 hover:border-blue-500/50 transition duration-300 group overflow-hidden shadow-xl">
                
                {/* File Header */}
                <div className="p-5 pb-0 flex items-start justify-between">
                  <div className="p-3 bg-gray-900 rounded-lg border border-gray-700">
                    <FileText className="w-8 h-8 text-blue-400" />
                  </div>
                  <div className="text-xs font-mono text-gray-500 bg-gray-900 px-2 py-1 rounded border border-gray-700">
                    ID: {file.id.toString().padStart(4, '0')}
                  </div>
                </div>

                {/* File Info */}
                <div className="p-5">
                    <h3 className="font-semibold truncate mb-1 text-lg text-gray-100">{file.fileName}</h3>
                    <p className="text-xs text-gray-400 mb-4 font-mono">
                        UPLOADED: {new Date(file.uploadTime).toLocaleDateString()}
                    </p>

                    {/* ACTION BUTTONS */}
                    <div className="flex gap-2 mt-4 pt-4 border-t border-gray-700">
                        {/* 1. VIEW */}
                        <button 
                            onClick={() => handleView(file.id, file.fileName)}
                            className="flex-1 flex items-center justify-center gap-2 p-2 bg-blue-600/10 hover:bg-blue-600 text-blue-400 hover:text-white rounded-lg transition border border-blue-500/30" 
                            title="Decrypt & View"
                        >
                            <Eye className="w-4 h-4" />
                            <span className="text-xs font-bold">VIEW</span>
                        </button>

                        {/* 2. DOWNLOAD .SNTL */}
                        <button 
                            onClick={() => handleDownloadSntl(file.id, file.fileName)}
                            className="flex-1 flex items-center justify-center gap-2 p-2 bg-green-600/10 hover:bg-green-600 text-green-400 hover:text-white rounded-lg transition border border-green-500/30" 
                            title="Download .sntl Token"
                        >
                            <Shield className="w-4 h-4" />
                            <span className="text-xs font-bold">.SNTL</span>
                        </button>

                        {/* 3. SHARE */}
                        <button 
                            onClick={() => openShareModal(file.id, file.fileName)}
                            className="flex-none p-2 bg-gray-700 hover:bg-gray-600 text-gray-300 hover:text-white rounded-lg transition" 
                            title="Manage Access"
                        >
                            <Share2 className="w-4 h-4" />
                        </button>

                        {/* 4. DELETE (NEW) */}
                        <button 
                            onClick={() => handleDelete(file.id)}
                            className="flex-none p-2 bg-red-900/20 hover:bg-red-600 text-red-500 hover:text-white rounded-lg transition border border-red-900/50" 
                            title="Delete Permanently"
                        >
                            <Trash2 className="w-4 h-4" />
                        </button>
                    </div>
                </div>
                
                {/* Security Footer */}
                <div className="bg-gray-900/50 p-2 text-center border-t border-gray-800">
                    <div className="flex items-center justify-center gap-2 text-[10px] text-gray-500 uppercase font-bold tracking-wider">
                        <span className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></span>
                        AES-256 Protected
                    </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* --- MODALS --- */}
      
      <UploadModal 
        isOpen={isUploadOpen} 
        onClose={() => setIsUploadOpen(false)} 
        onUploadSuccess={() => {
          fetchFiles(); 
        }} 
      />
      
      <SecurePDFViewer 
        isOpen={!!viewFile}
        onClose={() => {
          setViewFile(null);
          setViewFileName('');
        }}
        fileUrl={viewFile}
        fileName={viewFileName}
      />

      <ShareModal 
        isOpen={shareModalOpen}
        onClose={() => setShareModalOpen(false)}
        fileId={activeFile.id}
        fileName={activeFile.name}
      />
    </div>
  );
};

export default Dashboard;