// src/main.js
import './assets/base.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import vue3GoogleLogin from 'vue3-google-login'

const app = createApp(App)

app.use(router)

app.use(vue3GoogleLogin, {
  clientId: import.meta.env.VITE_APP_GOOGLE_CLIENT_ID,
  // Masukkan opsi ke Google Identity Services SDK di sini
  clientConfig: {
    auto_select: true, // aktifkan auto select akun sebelumnya
    prompt: ''         // kosongkan prompt jika tidak perlu
  }
})

app.mount('#app')
