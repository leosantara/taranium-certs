// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import DashboardView from '../views/DashboardView.vue';
import UploadCertificate from '../components/UploadCertificate.vue'; // Ini akan jadi komponen yang bisa diakses di dashboard
import api from '../services/api'; // Import instance Axios

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true }, // Route ini memerlukan otentikasi
    },
    {
      path: '/upload', // Anda bisa memisahkan halaman upload atau menjadikannya modal di Dashboard
      name: 'upload',
      component: UploadCertificate,
      meta: { requiresAuth: true },
    },
    // Rute callback OAuth2, Spring Security akan menangani redirect utamanya.
    // Frontend mungkin perlu ini jika ada logika khusus setelah login,
    // tapi seringkali hanya perlu cek status login.
    {
      path: '/login/oauth2/code/google',
      redirect: '/dashboard', // Setelah login via Spring Boot, langsung redirect ke dashboard
    },
    // Atau bisa juga membuat komponen AuthCallback jika ada logika penanganan token dsb.
    // {
    //   path: '/oauth2/callback', // Ini jika ada callback khusus ke frontend
    //   name: 'auth-callback',
    //   component: AuthCallback,
    // },
  ],
});

// Navigation Guard: Melindungi rute yang memerlukan otentikasi
router.beforeEach(async (to, from, next) => {
  if (to.meta.requiresAuth) {
    try {
      const response = await api.get('/user/role'); // Cek status login dan role dari backend
      if (response.data.role === 'INSTITUTION') {
        next(); // User terotentikasi dan memiliki role INSTITUTION
      } else {
        // Jika tidak terotentikasi atau bukan INSTITUTION, redirect ke halaman login Google
        window.location.href = `${import.meta.env.VITE_APP_BACKEND_URL}/oauth2/authorization/google`;
      }
    } catch (error) {
      console.error("Error checking authentication:", error);
      // Jika terjadi error (misal, sesi habis), redirect ke halaman login Google
      window.location.href = `${import.meta.env.VITE_APP_BACKEND_URL}/oauth2/authorization/google`;
    }
  } else {
    next(); // Lanjutkan ke rute tanpa perlu otentikasi
  }
});

export default router;