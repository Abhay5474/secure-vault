import { useState } from 'react';
import axios from 'axios';
import { X, UploadCloud, Loader2 } from 'lucide-react';

const UploadModal = ({ isOpen, onClose, onUploadSuccess }) => {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) return;

    setUploading(true);
    setError('');

    const formData = new FormData();
    formData.append('file', file);

    const token = localStorage.getItem('token');

    try {
      await axios.post('http://localhost:8080/api/files/upload', formData, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      });
      
      setUploading(false);
      onUploadSuccess(); // Refresh the list
      onClose(); // Close the modal
    } catch (err) {
      console.error(err);
      setError('Upload failed. Please try again.');
      setUploading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 backdrop-blur-sm">
      <div className="bg-gray-800 p-6 rounded-xl border border-gray-700 w-full max-w-md shadow-2xl">
        <div className="flex justify-between items-center mb-6">
          <h3 className="text-xl font-bold text-white">Upload Secure File</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-white">
            <X className="w-6 h-6" />
          </button>
        </div>

        {error && (
          <div className="bg-red-500/10 text-red-500 p-3 rounded-lg mb-4 text-sm border border-red-500/50">
            {error}
          </div>
        )}

        <form onSubmit={handleUpload}>
          <div className="border-2 border-dashed border-gray-600 rounded-lg p-8 flex flex-col items-center justify-center mb-6 hover:border-blue-500 transition-colors cursor-pointer relative">
            <input 
              type="file" 
              accept=".pdf,.mp4,.mkv,.avi,.mp3,.wav" // 🟢 UPDATED: Accepts Videos & PDFs & audio
              className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
              onChange={(e) => setFile(e.target.files[0])}
            />
            <UploadCloud className={`w-12 h-12 mb-3 ${file ? 'text-blue-500' : 'text-gray-500'}`} />
            {file ? (
              <p className="text-blue-400 font-medium text-center break-all">{file.name}</p>
            ) : (
              <p className="text-gray-400 text-sm">Click or Drag file to upload</p>
            )}
          </div>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2.5 bg-gray-700 hover:bg-gray-600 text-white rounded-lg font-medium transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!file || uploading}
              className="flex-1 py-2.5 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-600/50 text-white rounded-lg font-medium transition flex items-center justify-center gap-2"
            >
              {uploading ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Upload Encrypt'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UploadModal;