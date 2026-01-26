import { useState } from 'react';
import axios from 'axios';
import { Shield, Lock, FileKey, ArrowLeft, Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import SecurePDFViewer from './SecurePDFViewer';
import SecureVideoPlayer from './SecureVideoPlayer'; // 🟢 Import the new player

const DecryptPage = () => {
  // PDF State
  const [viewFile, setViewFile] = useState(null);
  const [viewFileName, setViewFileName] = useState('');
  
  // 🟢 Video State
  const [videoFile, setVideoFile] = useState(null);
  const [videoName, setVideoName] = useState('');

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false); 
  const navigate = useNavigate();

  // 1. Handle File Selection
  const handleFileChange = async (event) => {
    setError('');
    const file = event.target.files[0];
    if (!file) return;

    if (!file.name.endsWith('.sntl')) {
      setError("Invalid file! You must upload a .sntl token file.");
      return;
    }

    // Robust ID Extraction (Handles "(1)" duplicates)
    const match = file.name.match(/_id_(\d+)/);
    
    if (!match) {
      setError("Invalid Token: This file is missing the system stamp ('_id_NUMBER'). Please ask the owner to download a fresh copy.");
      return;
    }

    const fileId = match[1]; 
    const originalName = file.name.split('_id_')[0];

    await attemptDecryption(fileId, originalName);
  };

  // 2. The Smart Decryption Logic
  const attemptDecryption = async (fileId, fileName) => {
    const token = localStorage.getItem('token');
    setLoading(true); 
    
    try {
      const response = await axios.get(`http://localhost:8080/api/files/download/${fileId}`, {
        headers: { Authorization: `Bearer ${token}` },
        responseType: 'blob' 
      });

      // 🟢 INTELLIGENT ROUTING
      const mimeType = response.data.type; // e.g. "video/mp4" or "application/pdf"
      
      // Create the Decrypted Blob URL
      const fileUrl = window.URL.createObjectURL(new Blob([response.data], { type: mimeType }));

      if (mimeType.startsWith('video/')) {
        // IT IS A VIDEO -> Open Video Player
        setVideoName(fileName);
        setVideoFile(fileUrl);
      } else {
        // IT IS A PDF (or default) -> Open PDF Viewer
        setViewFileName(fileName);
        setViewFile(fileUrl);
      }

    } catch (err) {
      console.error(err);
      if (err.response && err.response.status === 403) {
        setError("ACCESS DENIED: The owner has not granted you permission for this file.");
      } else {
        setError("Decryption Failed: File not found or server error.");
      }
    } finally {
      setLoading(false); 
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white p-6 font-sans flex flex-col">
      
      {/* Navbar */}
      <nav className="flex justify-between items-center mb-10 border-b border-gray-700 pb-4">
        <div className="flex items-center gap-3">
          <Shield className="w-8 h-8 text-green-500" />
          <h1 className="text-2xl font-bold tracking-tight">Secure Decryptor</h1>
        </div>
        <button 
          onClick={() => navigate('/dashboard')}
          className="flex items-center gap-2 bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg text-sm transition"
        >
          <ArrowLeft className="w-4 h-4" /> Back to Vault
        </button>
      </nav>

      {/* Main Content */}
      <div className="flex-1 flex flex-col items-center justify-center">
        
        <div className="w-full max-w-xl bg-gray-800 p-10 rounded-2xl border-2 border-dashed border-gray-600 hover:border-green-500 transition-colors text-center relative group">
          
          <input 
            type="file" 
            accept=".sntl"
            onChange={handleFileChange}
            className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
          />

          <div className="pointer-events-none">
            <div className="bg-gray-700 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6 group-hover:bg-green-500/20 transition">
              {loading ? (
                 <Loader2 className="w-10 h-10 text-green-500 animate-spin" />
              ) : (
                 <FileKey className="w-10 h-10 text-green-500" />
              )}
            </div>
            
            <h2 className="text-2xl font-bold mb-2">Upload .sntl Token</h2>
            <p className="text-gray-400 mb-6">
              Click or Drag your encrypted .sntl file here to decrypt and view it.
            </p>

            <div className="inline-block bg-gray-700 px-4 py-2 rounded text-sm font-mono text-green-400">
              Only .sntl files accepted
            </div>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mt-8 bg-red-500/10 border border-red-500 text-red-400 px-6 py-4 rounded-xl flex items-center gap-3 animate-bounce-short">
            <Lock className="w-5 h-5" />
            <span className="font-semibold">{error}</span>
          </div>
        )}

      </div>

      {/* 🟢 Render Video Player if Video */}
      <SecureVideoPlayer 
        isOpen={!!videoFile}
        onClose={() => {
          setVideoFile(null);
          setVideoName('');
        }}
        fileUrl={videoFile}
        fileName={videoName}
      />

      {/* 🟢 Render PDF Viewer if PDF */}
      <SecurePDFViewer 
        isOpen={!!viewFile}
        onClose={() => {
          setViewFile(null);
          setViewFileName('');
        }}
        fileUrl={viewFile}
        fileName={viewFileName}
      />
    </div>
  );
};

export default DecryptPage;