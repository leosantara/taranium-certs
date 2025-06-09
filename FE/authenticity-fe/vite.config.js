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
  // Tambahkan bagian server di sini
  server: {
    headers: {
      // Mengizinkan kebijakan lintas origin yang lebih longgar untuk development.
      // Ini dapat membantu mengatasi masalah CORS/COOP/COEP dengan pop-up pihak ketiga seperti Google login.
      // Catatan: 'unsafe-none' umumnya TIDAK direkomendasikan untuk produksi karena alasan keamanan.
      // Gunakan ini HANYA untuk development lokal.
      'Cross-Origin-Embedder-Policy': 'unsafe-none',
      'Cross-Origin-Opener-Policy': 'unsafe-none' // Menambahkan ini juga untuk lebih spesifik ke COOP
    }
  }
})