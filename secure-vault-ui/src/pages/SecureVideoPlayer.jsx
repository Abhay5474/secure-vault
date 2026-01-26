import { X, Film } from 'lucide-react';

const SecureVideoPlayer = ({ isOpen, onClose, fileUrl, fileName }) => {
  if (!isOpen || !fileUrl) return null;

  return (
    <div className="fixed inset-0 bg-black/95 z-50 flex flex-col items-center justify-center p-4 backdrop-blur-md">
      
      {/* Header */}
      <div className="w-full max-w-4xl flex justify-between items-center text-white mb-4">
        <div className="flex items-center gap-3">
            <div className="w-3 h-3 bg-red-500 rounded-full animate-pulse shadow-[0_0_10px_red]"></div>
            <h2 className="text-xl font-bold font-mono tracking-wide flex items-center gap-2">
              <Film className="w-5 h-5" />
              SECURE_PLAYER :: {fileName}
            </h2>
        </div>
        <button 
            onClick={onClose} 
            className="p-2 bg-gray-800 hover:bg-red-600 rounded-lg transition border border-gray-700 hover:border-red-500"
        >
          <X className="w-6 h-6" />
        </button>
      </div>

      {/* Video Player Box */}
      <div className="bg-black rounded-xl overflow-hidden shadow-2xl w-full max-w-5xl border border-gray-800 relative group">
        
        {/* HTML5 Video Player - Plays the Decrypted Blob Stream */}
        <video 
          controls 
          autoPlay 
          className="w-full h-auto max-h-[80vh]"
          controlsList="nodownload" // Harder for user to save via right-click
          onContextMenu={(e) => e.preventDefault()} // Disable Right Click
        >
          <source src={fileUrl} type="video/mp4" />
          Your browser does not support the video tag.
        </video>

      </div>
    </div>
  );
};

export default SecureVideoPlayer;