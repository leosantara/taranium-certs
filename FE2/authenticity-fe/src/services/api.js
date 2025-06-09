// src/services/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_APP_BACKEND_URL, // Ambil URL backend dari .env
  withCredentials: true, // Penting! Untuk mengirim dan menerima cookie sesi (JSESSIONID)
});

export default api;
