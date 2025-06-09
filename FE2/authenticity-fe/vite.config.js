import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    // Memastikan development server berjalan di port 5173 (default Vite)
    // atau jika Anda ingin memaksa ke 3000, ubah port di sini.
    // Misal: port: 3000,
    headers: {
      // Ini membantu mengatasi masalah Cross-Origin-Opener-Policy dengan pop-up MetaMask
      // CATATAN PENTING: Pengaturan 'unsafe-none' ini HANYA UNTUK DEVELOPMENT LOKAL.
      // Di produksi, Anda harus menghapus ini atau menggunakan kebijakan yang lebih aman.
      'Cross-Origin-Opener-Policy': 'unsafe-none',
      'Cross-Origin-Embedder-Policy': 'unsafe-none'
    }
  }
})
