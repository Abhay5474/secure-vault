import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    {/* 🛑 REMOVED BrowserRouter HERE because App.jsx already has one */}
    <App />
  </React.StrictMode>,
)