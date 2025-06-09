<template>
  <header>
    <div class="wrapper">
      <nav>
        <RouterLink to="/">Home (Verifikasi)</RouterLink>
        <RouterLink to="/dashboard" v-if="isLoggedIn">Dashboard (Institusi)</RouterLink>
        <button v-if="!isLoggedIn" @click="handleLogin" class="login-button">Login Institusi</button>
        <button v-else @click="handleLogout" class="logout-button">Logout</button>
      </nav>
    </div>
  </header>

  <RouterView />
</template>

<script setup>
import { RouterLink, RouterView, useRouter } from 'vue-router';
import { ref, onMounted } from 'vue';
import api from '../services/api.js';

const isLoggedIn = ref(false); // Status login pengguna
const router = useRouter();

// Fungsi untuk memeriksa status login dari backend
const checkLoginStatus = async () => {
  try {
    const response = await api.get('/user/role');
    if (response.data.role === 'INSTITUTION') {
      isLoggedIn.value = true;
    } else {
      isLoggedIn.value = false;
    }
  } catch (error) {
    console.error("Gagal memeriksa status login:", error);
    isLoggedIn.value = false;
  }
};

// Handle login (redirect ke Spring Boot OAuth2 endpoint)
const handleLogin = () => {
  window.location.href = `${import.meta.env.VITE_APP_BACKEND_URL}/oauth2/authorization/google`;
};

// Handle logout
const handleLogout = async () => {
  try {
    await api.post('/logout'); // Spring Security memiliki endpoint /logout
    isLoggedIn.value = false;
    router.push('/'); // Redirect ke halaman utama setelah logout
  } catch (error) {
    console.error("Gagal logout:", error);
    alert("Logout gagal.");
  }
};

// Cek status login saat komponen dimuat
onMounted(() => {
  checkLoginStatus();
});
</script>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
  background-color: #f0f0f0;
  padding: 1rem;
  border-bottom: 1px solid #ddd;
}

.wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

nav {
  width: 100%;
  font-size: 1rem;
  text-align: center;
  display: flex;
  gap: 1rem;
  align-items: center;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 0.5rem 1rem;
  border-left: 1px solid var(--color-border);
  text-decoration: none;
  color: #333;
}

nav a:first-of-type {
  border: 0;
}

.login-button, .logout-button {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1rem;
  margin-left: auto; /* Dorong tombol ke kanan */
}

.login-button:hover, .logout-button:hover {
  background-color: #36a076;
}
</style>