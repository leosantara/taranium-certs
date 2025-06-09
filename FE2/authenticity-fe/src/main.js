// src/main.js
import './assets/base.css' // Atau sesuaikan dengan CSS default Anda

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(router)

app.mount('#app')
