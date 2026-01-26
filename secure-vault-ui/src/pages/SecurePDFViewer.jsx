import { useState, useEffect } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import { X, ChevronLeft, ChevronRight, Loader2, AlertTriangle } from 'lucide-react';

// 🛑 HARDCODED FIX: 
// We explicitly point to version 4.4.168 which matches your package.json.
// We use the UNPKG CDN which is very reliable for this specific file.
pdfjs.GlobalWorkerOptions.workerSrc = 'https://unpkg.com/pdfjs-dist@4.4.168/build/pdf.worker.min.mjs';

import 'react-pdf/dist/Page/TextLayer.css';
import 'react-pdf/dist/Page/AnnotationLayer.css';

const SecurePDFViewer = ({ isOpen, onClose, fileUrl, fileName }) => {
  const [numPages, setNumPages] = useState(null);
  const [pageNumber, setPageNumber] = useState(1);
  const [loading, setLoading] = useState(true);
  const [renderError, setRenderError] = useState(null);

  // Reset state when file changes
  useEffect(() => {
    setPageNumber(1);
    setLoading(true);
    setRenderError(null);
  }, [fileUrl]);

  if (!isOpen) return null;

  const onDocumentLoadSuccess = ({ numPages }) => {
    setNumPages(numPages);
    setLoading(false);
    setRenderError(null);
  };

  const onDocumentLoadError = (error) => {
    console.error("PDF Load Error:", error);
    setLoading(false);
    setRenderError(error.message);
  };

  return (
    <div className="fixed inset-0 bg-black/95 z-50 flex flex-col items-center justify-center p-4 backdrop-blur-md">
      
      {/* Header */}
      <div className="w-full max-w-4xl flex justify-between items-center text-white mb-4">
        <div className="flex items-center gap-3">
            <div className="w-3 h-3 bg-red-500 rounded-full animate-pulse shadow-[0_0_10px_red]"></div>
            <h2 className="text-xl font-bold font-mono tracking-wide">SECURE_VIEWER :: {fileName}</h2>
        </div>
        <button 
            onClick={onClose} 
            className="p-2 bg-gray-800 hover:bg-red-600 rounded-lg transition border border-gray-700 hover:border-red-500"
        >
          <X className="w-6 h-6" />
        </button>
      </div>

      {/* Secure Viewer Box */}
      <div 
        className="bg-gray-900/50 rounded-xl overflow-hidden shadow-2xl max-h-[85vh] w-full max-w-5xl flex justify-center overflow-y-auto border border-gray-700 relative"
        onContextMenu={(e) => e.preventDefault()} 
      >
        {/* Loading Spinner */}
        {loading && !renderError && (
          <div className="absolute inset-0 flex flex-col items-center justify-center bg-gray-900 z-10 text-blue-400">
            <Loader2 className="w-16 h-16 animate-spin mb-4" />
            <p className="font-mono animate-pulse">DECRYPTING STREAM...</p>
          </div>
        )}

        {/* Error State (Prevents White Screen) */}
        {renderError ? (
          <div className="flex flex-col items-center justify-center h-96 text-red-400 p-8 text-center">
            <AlertTriangle className="w-16 h-16 mb-4" />
            <h3 className="text-xl font-bold mb-2">Decryption Render Failed</h3>
            <p className="max-w-md bg-black/50 p-4 rounded font-mono text-sm border border-red-500/30">
              {renderError}
            </p>
            <button 
              onClick={onClose}
              className="mt-6 px-6 py-2 bg-gray-800 hover:bg-gray-700 rounded-lg text-white transition"
            >
              Close Viewer
            </button>
          </div>
        ) : (
          /* Actual Document */
          <Document
            file={fileUrl}
            onLoadSuccess={onDocumentLoadSuccess}
            onLoadError={onDocumentLoadError}
            className="flex flex-col items-center min-h-[500px]"
            loading={null} 
          >
            <Page 
              pageNumber={pageNumber} 
              renderTextLayer={false}
              renderAnnotationLayer={false}
              className="shadow-2xl my-4"
              scale={1.0} 
              width={window.innerWidth > 800 ? 800 : window.innerWidth - 40}
              onRenderError={() => setRenderError("Page render failed")}
            />
          </Document>
        )}
      </div>

      {/* Pagination (Only show if valid) */}
      {numPages && !renderError && (
        <div className="flex items-center gap-6 mt-6 bg-gray-800/80 px-8 py-3 rounded-full text-white border border-gray-700 backdrop-blur">
            <button 
            disabled={pageNumber <= 1}
            onClick={() => setPageNumber(prev => prev - 1)}
            className="disabled:opacity-30 hover:text-blue-400 transition"
            >
            <ChevronLeft className="w-6 h-6" />
            </button>
            
            <span className="font-mono text-lg">
            {pageNumber} / {numPages}
            </span>

            <button 
            disabled={pageNumber >= numPages}
            onClick={() => setPageNumber(prev => prev + 1)}
            className="disabled:opacity-30 hover:text-blue-400 transition"
            >
            <ChevronRight className="w-6 h-6" />
            </button>
        </div>
      )}
    </div>
  );
};

export default SecurePDFViewer;