import axios from 'axios';

// Automatically picks the right URL based on where it is running
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
    baseURL: API_URL
});

export default api;