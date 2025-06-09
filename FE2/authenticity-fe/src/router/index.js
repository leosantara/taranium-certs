// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import DashboardView from '../views/DashboardView.vue';
import UploadCertificate from '../components/UploadCertificate.vue';

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
      meta: { requiresAuth: true }, // Route ini memerlukan otentikasi (MetaMask connection)
      props: true // Agar userAddress bisa dilewatkan sebagai prop
    },
    {
      path: '/upload', // Komponen upload akan digunakan di dashboard, rute ini opsional
      name: 'upload',
      component: UploadCertificate,
      meta: { requiresAuth: true },
      props: true
    },
  ],
});

// Navigation Guard: Melindungi rute yang memerlukan otentikasi MetaMask
router.beforeEach(async (to, from, next) => {
  if (to.meta.requiresAuth) {
    const { getCurrentWalletAddress } = await import('../utils/web3'); // Impor dinamis untuk menghindari circular dependency
    const address = await getCurrentWalletAddress();

    if (address) {
      next(); // User terotentikasi (MetaMask terhubung)
    } else {
      alert("Anda harus terhubung ke MetaMask untuk mengakses halaman ini.");
      next({ name: 'home' }); // Redirect ke halaman utama jika tidak terhubung
    }
  } else {
    next(); // Lanjutkan ke rute tanpa perlu otentikasi
  }
});

export default router;
