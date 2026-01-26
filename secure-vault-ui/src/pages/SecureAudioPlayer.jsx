import { X, Music } from 'lucide-react';

const SecureAudioPlayer = ({ isOpen, onClose, fileUrl, fileName }) => {
  if (!isOpen || !fileUrl) return null;

  return (
    <div className="fixed inset-0 bg-black/95 z-50 flex flex-col items-center justify-center p-4 backdrop-blur-md">
      
      {/* Header */}
      <div className="w-full max-w-md flex justify-between items-center text-white mb-8">
        <div className="flex items-center gap-3">
            <div className="w-3 h-3 bg-purple-500 rounded-full animate-pulse shadow-[0_0_10px_purple]"></div>
            <h2 className="text-xl font-bold font-mono tracking-wide flex items-center gap-2">
              <Music className="w-5 h-5 text-purple-400" />
              SECURE_AUDIO
            </h2>
        </div>
        <button 
            onClick={onClose} 
            className="p-2 bg-gray-800 hover:bg-red-600 rounded-lg transition border border-gray-700 hover:border-red-500"
        >
          <X className="w-6 h-6" />
        </button>
      </div>

      {/* Audio Player Box */}
      <div className="bg-gray-900 p-8 rounded-2xl border border-gray-700 shadow-2xl w-full max-w-md flex flex-col items-center">
        
        {/* Visual Icon */}
        <div className="w-32 h-32 bg-gray-800 rounded-full flex items-center justify-center mb-6 border-4 border-gray-700 shadow-inner">
             <Music className="w-16 h-16 text-purple-500 animate-bounce-slow" />
        </div>

        <h3 className="text-lg font-medium text-white mb-6 text-center break-all font-mono">{fileName}</h3>

        {/* HTML5 Audio Player */}
        <audio 
          controls 
          autoPlay 
          className="w-full"
          controlsList="nodownload" 
          onContextMenu={(e) => e.preventDefault()} 
        >
          <source src={fileUrl} type="audio/mpeg" />
          Your browser does not support the audio element.
        </audio>

      </div>
    </div>
  );
};

export default SecureAudioPlayer;